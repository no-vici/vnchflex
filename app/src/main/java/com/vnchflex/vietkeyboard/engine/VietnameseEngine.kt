package com.vnchflex.vietkeyboard.engine

import android.view.inputmethod.InputConnection

/**
 * Engine Tiếng Việt Telex Bỏ dấu tự do theo phong cách vnchKey:
 * - Thuần Telex (loại bỏ VNI và loại bỏ SenFlex dấu trước âm phức tạp).
 * - Bỏ dấu tự do chuẩn vnchKey: người dùng có thể gõ dấu thanh (`s, f, r, x, j, z`) hoặc dấu mũ/móc/đ (`aa, ee, oo, aw, ow, uw, dd`)
 *   ngay sau nguyên âm hoặc ở cuối chữ sau phụ âm cuối (ví dụ: `vieetj` hoặc `vietej` -> `việt`, `nhana` -> `nhân`,
 *   `truwongr` hoặc `truongwr` -> `trưởng`, `dduowcj` hoặc `duocwdj` -> `được`).
 * - Hỗ trợ đầy đủ từ phương ngữ Nam Bộ & phụ âm mở rộng (`dz-`, `z-`, `f-`, `j-`, `w-`) giống vnchKey:
 *   + `dziaf` -> `dzìa`, `dzachs` -> `dzách`, `dzoo` -> `dzô`, `dzoj` -> `dzọ`, `dzui` -> `dzui`, `dzaayj` -> `dzậy`, `dzotj` -> `dzọt`
 *   + `zaayj` -> `zậy`, `zui` -> `zui`, `fairs` -> `fải`, `was` -> `wá`, `waayj` -> `wậy`, `jups` -> `júp`
 *   + Giữ nguyên các từ tiếng Anh có phụ âm mở rộng (`zero`, `zone`, `zip`, `zest`, `just`, `free`, `fee`, `water`, `web`)
 * - Đặt dấu đúng vị trí nguyên âm theo kiểu mới (`oà, oè, uý`) hoặc kiểu cũ/cổ điển (`òa, òe, úy`).
 * - Lặp phím dấu / phím mũ-móc để hoàn nguyên (escape) khi gõ tiếng Anh (`canhf + f -> canhf`, `aaa -> aa`, `ww -> w`, `ddd -> dd`).
 * - Nút `Bỏ dấu` (`removeDiacritics`) hoặc phím `z` để xóa dấu tự do.
 */
class VietnameseEngine {
    enum class Tone(val index: Int) {
        NONE(0),
        ACUTE(1), // Sắc (s)
        GRAVE(2), // Huyền (f)
        HOOK(3),  // Hỏi (r)
        TILDE(4), // Ngã (x)
        DOT(5)    // Nặng (j)
    }

    var freeTonePlacement: Boolean = true
    var modernOrthography: Boolean = true
    var quickTelex: Boolean = false
    var relaxedCoda: Boolean = false
    /** Cho phép gõ từ phương ngữ Nam Bộ (`dzìa`, `dzách`, `dzô`...) và phụ âm mở rộng (`f, j, w, z`) chuẩn vnchKey */
    var southernDialectAndGenZ: Boolean = true
    /** Tự động khôi phục từ tiếng Anh khi gõ trong chế độ Tiếng Việt (vd: text -> text thay vì tẽt) */
    var autoRestoreEnglish: Boolean = true
    /** Danh sách từ ngoại lệ do người dùng thêm để giữ nguyên raw text */
    var userWhitelist: Set<String> = emptySet()
    /** Dùng composing span (gạch chân). Tắt cho app có vấn đề lag/underline như Telegram. */
    var useComposingSpan: Boolean = true

    private val rawChars = mutableListOf<Char>()
    private var renderedOutput: String = ""

    val composingText: String get() = renderedOutput
    val isComposing: Boolean get() = rawChars.isNotEmpty()

    fun onChar(input: Char, ic: InputConnection): Boolean {
        if (input.isLetter()) {
            val prevLen = renderedOutput.length
            rawChars.add(input)
            renderedOutput = transformTelex(rawChars)
            replaceComposingDirect(ic, prevLen, renderedOutput)
            return true
        }
        commit(ic)
        return false
    }

    fun onBackspace(ic: InputConnection): Boolean {
        if (rawChars.isEmpty()) return false
        val prevLen = renderedOutput.length
        if (renderedOutput.isNotEmpty()) {
            val updated = renderedOutput.dropLast(1)
            if (updated.isEmpty()) {
                rawChars.clear()
                renderedOutput = ""
                replaceComposingDirect(ic, prevLen, "")
            } else {
                renderedOutput = updated
                rawChars.clear()
                rawChars.addAll(rawKeysFor(updated).toList())
                replaceComposingDirect(ic, prevLen, renderedOutput)
            }
            return true
        }
        rawChars.removeAt(rawChars.lastIndex)
        renderedOutput = if (rawChars.isEmpty()) "" else transformTelex(rawChars)
        replaceComposingDirect(ic, prevLen, renderedOutput)
        return true
    }

    fun commit(ic: InputConnection) {
        if (renderedOutput.isEmpty() && rawChars.isEmpty()) return
        if (useComposingSpan) ic.finishComposingText()
        rawChars.clear()
        renderedOutput = ""
    }

    fun reset(ic: InputConnection? = null) {
        if (ic != null && useComposingSpan && (renderedOutput.isNotEmpty() || rawChars.isNotEmpty())) {
            // Discard active composing span so stale composing text doesn't linger.
            ic.commitText("", 1)
        }
        rawChars.clear()
        renderedOutput = ""
    }

    /**
     * Post-commit editing (chuẩn vnchKey): nạp lại từ đã gõ trước con trỏ vào bộ đệm
     * để người dùng có thể sửa dấu hoặc xóa lùi tiếp trên từ đó.
     * Biến từ đó thành composing span để lần gõ kế tiếp thay thế đúng, không bị dính/nhân đôi.
     */
    fun rehydrateWord(word: String, ic: InputConnection? = null) {
        if (word.isEmpty()) return
        rawChars.clear()
        rawChars.addAll(rawKeysFor(word).toList())
        renderedOutput = word
        if (ic != null && useComposingSpan) {
            ic.beginBatchEdit()
            try {
                ic.deleteSurroundingText(word.length, 0)
                ic.setComposingText(word, 1)
            } finally {
                ic.endBatchEdit()
            }
        }
    }

    /** Bỏ dấu tự do (kiểu vnchKey): xóa dấu trên từ đang gõ, vùng đang chọn, hoặc từ ngay trước con trỏ. */
    fun removeDiacritics(ic: InputConnection) {
        if (rawChars.isNotEmpty()) {
            val prevLen = renderedOutput.length
            val stripped = DiacriticUtils.strip(renderedOutput)
            rawChars.clear()
            rawChars.addAll(stripped.toList())
            renderedOutput = stripped
            replaceComposingDirect(ic, prevLen, renderedOutput)
            return
        }
        val selected = ic.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            ic.commitText(DiacriticUtils.strip(selected), 1)
            return
        }
        val before = ic.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        val word = before.takeLastWhile { !it.isWhitespace() }
        if (word.isEmpty()) return
        ic.beginBatchEdit()
        try {
            ic.deleteSurroundingText(word.length, 0)
            ic.commitText(DiacriticUtils.strip(word), 1)
        } finally {
            ic.endBatchEdit()
        }
    }

    private fun replaceComposingDirect(ic: InputConnection, prevLen: Int, newText: String) {
        ic.beginBatchEdit()
        try {
            if (newText.isEmpty()) {
                if (prevLen > 0) {
                    if (useComposingSpan) ic.commitText("", 1) else ic.deleteSurroundingText(prevLen, 0)
                }
            } else if (useComposingSpan) {
                ic.setComposingText(newText, 1)
            } else {
                if (prevLen > 0) ic.deleteSurroundingText(prevLen, 0)
                ic.commitText(newText, 1)
            }
        } finally {
            ic.endBatchEdit()
        }
    }

    // ==================== TELEX BỎ DẤU TỰ DO & PHƯƠNG NGỮ NAM BỘ (PHONG CÁCH VNCHKEY) ====================
    fun transformTelex(chars: List<Char>): String {
        if (chars.isEmpty()) return ""
        val lowerRaw = chars.map { it.lowercaseChar() }.joinToString("")

        // Giữ nguyên các từ trong whitelist của người dùng
        if (lowerRaw in userWhitelist) {
            return chars.joinToString("")
        }

        // Tự động khôi phục từ tiếng Anh chuẩn vnchKey (vd: text -> text, next -> next, flex -> flex, more -> more...)
        if (autoRestoreEnglish && lowerRaw in ENGLISH_AUTO_RESTORE_WORDS) {
            return chars.joinToString("")
        }

        // Kiểm tra phụ âm đầu không chuẩn tiếng Việt (dz-, f-, j-, z-, w-) theo quy tắc vnchKey
        val firstLower = lowerRaw.first()
        val isDzOnset = lowerRaw.startsWith("dz")
        if (!southernDialectAndGenZ && (isDzOnset || firstLower == 'f' || firstLower == 'j' || firstLower == 'z')) {
            return chars.joinToString("")
        }
        if (southernDialectAndGenZ && !isDzOnset && (firstLower == 'f' || firstLower == 'j' || firstLower == 'z')) {
            // Bảo vệ các từ tiếng Anh thông dụng bắt đầu bằng f/j/z (free, fee, zero, zone, zip, zest, just...)
            if (lowerRaw in ENGLISH_FJZ_PROTECTED_WORDS ||
                (firstLower == 'f' && lowerRaw.startsWith("fr")) ||
                ((firstLower == 'z' || firstLower == 'j') && lowerRaw.endsWith("st"))
            ) {
                return chars.joinToString("")
            }
        }

        // Xử lý từ bắt đầu bằng 'w' trong chế độ Nam Bộ / Gen-Z (vd: was -> wá, waayj -> wậy, wans -> wán)
        if (southernDialectAndGenZ && firstLower == 'w' && chars.size >= 3) {
            val c1 = chars[1].lowercaseChar()
            if ((c1 == 'a' || c1 == 'e' || c1 == 'y') && lowerRaw !in ENGLISH_W_PROTECTED_WORDS) {
                val zChars = chars.toMutableList().apply { this[0] = if (chars[0].isUpperCase()) 'Z' else 'z' }
                val zTransformed = transformTelexCore(zChars, isDzOnset = false)
                if (zTransformed.lowercase() != zChars.joinToString("").lowercase()) {
                    return chars[0] + zTransformed.drop(1)
                }
            }
        }

        return transformTelexCore(chars, isDzOnset = isDzOnset)
    }

    private fun transformTelexCore(chars: List<Char>, isDzOnset: Boolean): String {
        val result = mutableListOf<Char>()
        var tone = Tone.NONE
        var toneEscapeActive = false
        var hornCancelled = false
        var circumflexCancelledKey: Char? = null
        var strokeDCancelled = false

        var i = 0
        // Nếu từ bắt đầu bằng phụ âm kép Nam Bộ "dz" (vd: dzìa, dzách, dzô, dzậy, dzọt, dzui),
        // giữ nguyên cụm phụ âm đầu "dz" không biến đổi thành "đ" và nhận diện nguyên âm + dấu thanh bình thường.
        if (isDzOnset && chars.size >= 2) {
            result.add(chars[0])
            result.add(chars[1])
            i = 2
        }

        while (i < chars.size) {
            val ch = chars[i]
            val lower = ch.lowercaseChar()

            // 1. Check dd -> đ và ddd -> dd (escape) khi không phải onset Nam Bộ "dz"
            if (!isDzOnset && lower == 'd' && !result.any { isVietnameseVowel(it) }) {
                if (i + 2 < chars.size &&
                    chars[i + 1].lowercaseChar() == 'd' &&
                    chars[i + 2].lowercaseChar() == 'd'
                ) {
                    result.add(ch)
                    result.add(chars[i + 1])
                    strokeDCancelled = true
                    toneEscapeActive = true
                    i += 3
                    continue
                }
                if (i + 1 < chars.size && chars[i + 1].lowercaseChar() == 'd') {
                    result.add(if (ch.isUpperCase()) 'Đ' else 'đ')
                    i += 2
                    continue
                }
            }

            // 2. Quick Telex phụ âm đầu (cc -> ch, gg -> gi, qq -> qu)
            if (quickTelex && result.isEmpty() && i + 1 < chars.size) {
                val nextLower = chars[i + 1].lowercaseChar()
                if (lower == 'c' && nextLower == 'c') {
                    result.addAll((if (ch.isUpperCase()) "Ch" else "ch").toList())
                    i += 2
                    continue
                }
                if (lower == 'g' && nextLower == 'g') {
                    result.addAll((if (ch.isUpperCase()) "Gi" else "gi").toList())
                    i += 2
                    continue
                }
                if (lower == 'q' && nextLower == 'q') {
                    result.addAll((if (ch.isUpperCase()) "Qu" else "qu").toList())
                    i += 2
                    continue
                }
            }

            // 3. Lặp 3 nguyên âm để hoàn nguyên (aaa -> aa, eee -> ee, ooo -> oo)
            if (!toneEscapeActive && (lower == 'a' || lower == 'e' || lower == 'o') &&
                i + 2 < chars.size &&
                chars[i + 1].lowercaseChar() == lower &&
                chars[i + 2].lowercaseChar() == lower
            ) {
                result.add(ch)
                result.add(chars[i + 1])
                tone = Tone.NONE
                circumflexCancelledKey = lower
                i += 3
                continue
            }

            // 4. Hai nguyên âm liền nhau tạo dấu mũ (aa -> â, ee -> ê, oo -> ô)
            if (!toneEscapeActive && (lower == 'a' || lower == 'e' || lower == 'o') &&
                i + 1 < chars.size &&
                chars[i + 1].lowercaseChar() == lower &&
                circumflexCancelledKey != lower
            ) {
                val hasPriorCircumflex = result.any { "âêôă".contains(it.lowercaseChar()) }
                if (!hasPriorCircumflex) {
                    val circumflex = when (lower) {
                        'a' -> if (ch.isUpperCase()) 'Â' else 'â'
                        'e' -> if (ch.isUpperCase()) 'Ê' else 'ê'
                        else -> if (ch.isUpperCase()) 'Ô' else 'ô'
                    }
                    result.add(circumflex)
                    i += 2
                    continue
                }
            }

            // 5. Bỏ dấu mũ & gạch ngang chữ đ tự do ở cuối từ (nhana -> nhân, vietej -> việt, trongo -> trông, dodongf -> đồng)
            // Lưu ý: Không biến đổi 'd' của phụ âm đầu Nam Bộ "dz" thành "đz"
            if (freeTonePlacement && !toneEscapeActive && result.isNotEmpty()) {
                if (!isDzOnset && lower == 'd' && result.any { isVietnameseVowel(it) }) {
                    val firstLower = result.first().lowercaseChar()
                    if (firstLower == 'd' && !strokeDCancelled) {
                        result[0] = if (result[0].isUpperCase()) 'Đ' else 'đ'
                        i += 1
                        continue
                    } else if (firstLower == 'đ') {
                        result[0] = if (result[0].isUpperCase()) 'D' else 'd'
                        strokeDCancelled = true
                        result.add(ch)
                        i += 1
                        continue
                    }
                }

                if ((lower == 'a' || lower == 'e' || lower == 'o') && circumflexCancelledKey != lower) {
                    val targetIdx = result.indexOfLast {
                        val c = it.lowercaseChar()
                        c == lower || (lower == 'o' && c == 'ơ') || (lower == 'a' && c == 'ă')
                    }
                    if (targetIdx >= 0 && (targetIdx < result.lastIndex || result[targetIdx].lowercaseChar() != lower)) {
                        val isUpper = result[targetIdx].isUpperCase()
                        if (lower == 'o' && targetIdx >= 1 && result[targetIdx - 1].lowercaseChar() == 'ư') {
                            result[targetIdx - 1] = if (result[targetIdx - 1].isUpperCase()) 'U' else 'u'
                        }
                        result[targetIdx] = when (lower) {
                            'a' -> if (isUpper) 'Â' else 'â'
                            'e' -> if (isUpper) 'Ê' else 'ê'
                            else -> if (isUpper) 'Ô' else 'ô'
                        }
                        i += 1
                        continue
                    }
                }
            }

            // 6. Phím w (aw -> ă, ow -> ơ, uw -> ư, uow -> ươ, ww -> w)
            if (lower == 'w') {
                if (hornCancelled) {
                    result.add(ch)
                    i += 1
                    continue
                }
                if (i + 1 < chars.size && chars[i + 1].lowercaseChar() == 'w') {
                    result.add(ch)
                    hornCancelled = true
                    i += 2
                    continue
                }
                var transformed = false
                val searchIndices = if (freeTonePlacement) result.indices.reversed() else listOfNotNull(result.indices.lastOrNull())
                for (idx in searchIndices) {
                    val vLower = result[idx].lowercaseChar()
                    val vUpper = result[idx].isUpperCase()
                    if (vLower == 'ă' || vLower == 'ư' || vLower == 'ơ') {
                        break
                    } else if (vLower == 'a' || vLower == 'â') {
                        if (idx >= 1 && result[idx - 1].lowercaseChar() == 'u') {
                            val isAfterQ = idx >= 2 && result[idx - 2].lowercaseChar() == 'q'
                            if (!isAfterQ) {
                                result[idx - 1] = if (result[idx - 1].isUpperCase()) 'Ư' else 'ư'
                                transformed = true
                                break
                            }
                        }
                        result[idx] = if (vUpper) 'Ă' else 'ă'
                        transformed = true
                        break
                    } else if (vLower == 'o' || vLower == 'ô') {
                        if (idx >= 1) {
                            val prevLower = result[idx - 1].lowercaseChar()
                            val isAfterQ = idx >= 2 && result[idx - 2].lowercaseChar() == 'q'
                            if (!isAfterQ && (prevLower == 'u' || prevLower == 'ư')) {
                                result[idx - 1] = if (result[idx - 1].isUpperCase()) 'Ư' else 'ư'
                            }
                        }
                        result[idx] = if (vUpper) 'Ơ' else 'ơ'
                        transformed = true
                        break
                    } else if (vLower == 'u') {
                        if (idx >= 1 && (result[idx - 1].lowercaseChar() == 'o' || result[idx - 1].lowercaseChar() == 'ơ')) {
                            continue
                        }
                        val isSingleUAfterQ = idx >= 1 && result[idx - 1].lowercaseChar() == 'q'
                        if (isSingleUAfterQ) continue
                        if (idx >= 1 && result[idx - 1].lowercaseChar() == 'u') {
                            result[idx - 1] = if (result[idx - 1].isUpperCase()) 'Ư' else 'ư'
                            transformed = true
                            break
                        }
                        result[idx] = if (vUpper) 'Ư' else 'ư'
                        transformed = true
                        break
                    }
                }
                if (transformed) {
                    i += 1
                    continue
                } else {
                    result.add(ch)
                    i += 1
                    continue
                }
            }

            // 7. Phím dấu thanh Telex (s, f, r, x, j) và phím xóa dấu (z)
            val hasVowels = !result.any { it.lowercaseChar() == 'w' } && result.any { isVietnameseVowel(it) }
            if (hasVowels && lower in "sfrxjz") {
                if (lower == 'z' && tone != Tone.NONE) {
                    tone = Tone.NONE
                    i += 1
                    continue
                }
                val newTone = when (lower) {
                    's' -> Tone.ACUTE
                    'f' -> Tone.GRAVE
                    'r' -> Tone.HOOK
                    'x' -> Tone.TILDE
                    'j' -> Tone.DOT
                    else -> null
                }
                if (newTone != null) {
                    if (toneEscapeActive) {
                        result.add(ch)
                        i += 1
                        continue
                    }
                    if (tone == newTone) {
                        // Nhấn lặp lại cùng phím dấu để hủy dấu và xuất chữ cái thường (vd: canhf + f -> canhf)
                        tone = Tone.NONE
                        toneEscapeActive = true
                        result.add(ch)
                    } else {
                        tone = newTone
                    }
                    i += 1
                    continue
                }
            }

            // 8. Tốc gõ phụ âm cuối (-g -> -ng, -h -> -nh, -k -> -c)
            if (relaxedCoda && !toneEscapeActive && hasVowels &&
                result.lastOrNull()?.let { isVietnameseVowel(it) } == true &&
                ((i + 1) until chars.size).all { chars[it].lowercaseChar() in "sfrxjzwaeod" }
            ) {
                when (lower) {
                    'g' -> {
                        result.addAll((if (ch.isUpperCase()) "NG" else "ng").toList())
                        i += 1
                        continue
                    }
                    'h' -> {
                        result.addAll((if (ch.isUpperCase()) "NH" else "nh").toList())
                        i += 1
                        continue
                    }
                    'k' -> {
                        result.add(if (ch.isUpperCase()) 'C' else 'c')
                        i += 1
                        continue
                    }
                }
            }

            result.add(ch)
            i += 1
        }

        // Đồng bộ cụm nguyên âm đôi 'ưo' -> 'ươ' khi có phụ âm/nguyên âm theo sau (vd: truwongr -> trưởng, dduwocj -> được)
        if (!hornCancelled && result.size >= 3) {
            for (idx in 0 until result.size - 2) {
                if (result[idx].lowercaseChar() == 'ư' && result[idx + 1].lowercaseChar() == 'o') {
                    result[idx + 1] = if (result[idx + 1].isUpperCase()) 'Ơ' else 'ơ'
                }
            }
        }

        if (tone != Tone.NONE) {
            applyTone(result, tone, modernOrthography)
        }
        return result.joinToString("")
    }

    // ==================== QUY TẮC ĐẶT DẤU CHUẨN TIẾNG VIỆT ====================
    private fun applyTone(chars: MutableList<Char>, tone: Tone, modern: Boolean) {
        if (tone == Tone.NONE) return
        val vowelIndices = chars.indices.filter { isVietnameseVowel(chars[it]) }
        if (vowelIndices.isEmpty()) return

        // Bổ sung phụ âm cuối -k (teen-code cho -c) vào tập coda để đặt dấu đúng trên âm tiết đóng
        val validCodaChars = setOf('c', 'm', 'n', 'p', 't', 'g', 'h', 'k')
        val targetIndex: Int = when (vowelIndices.size) {
            1 -> vowelIndices[0]
            2 -> {
                val firstV = chars[vowelIndices[0]].lowercaseChar()
                val secondV = chars[vowelIndices[1]].lowercaseChar()
                val isFallingOffglide = secondV == 'u' ||
                    (secondV == 'o' && firstV != 'u') ||
                    (secondV == 'i' && firstV != 'u') ||
                    (secondV == 'y' && firstV != 'u')
                val trailingIsCoda = vowelIndices[1] < chars.lastIndex &&
                    chars[vowelIndices[1] + 1].lowercaseChar() in validCodaChars
                val hasCoda = trailingIsCoda && !isFallingOffglide

                if (vowelIndices[0] > 0 && chars[vowelIndices[0] - 1].lowercaseChar() == 'q' && firstV == 'u') {
                    vowelIndices[1]
                } else if (vowelIndices[0] > 0 && chars[vowelIndices[0] - 1].lowercaseChar() == 'g' && firstV == 'i') {
                    vowelIndices[1]
                } else if (hasCoda) {
                    vowelIndices[1]
                } else {
                    val pair = "$firstV$secondV"
                    when {
                        // Từ Nam Bộ / chuẩn tiếng Việt có vần "ia" (dzìa, tía, mía), "ua", "ưa" đặt dấu ở nguyên âm đầu;
                        // riêng "oa", "oe", "uy", "uê" đặt dấu theo chuẩn mới/cũ
                        pair == "oa" || pair == "oe" || pair == "uy" || pair == "uê" ->
                            if (modern) vowelIndices[1] else vowelIndices[0]
                        pair == "iê" || pair == "yê" || pair == "uô" || pair == "ươ" || pair == "ưo" || pair == "uơ" ->
                            vowelIndices[1]
                        else -> vowelIndices[0]
                    }
                }
            }
            else -> {
                val lastVIdx = vowelIndices.last()
                val hasCoda = lastVIdx < chars.lastIndex && chars[lastVIdx + 1].lowercaseChar() in validCodaChars
                if (hasCoda) {
                    lastVIdx
                } else {
                    val lastLower = chars[lastVIdx].lowercaseChar()
                    if (lastLower == 'i' || lastLower == 'y' || lastLower == 'u') {
                        vowelIndices[vowelIndices.size - 2]
                    } else {
                        val modIdx = vowelIndices.firstOrNull { chars[it].lowercaseChar() in "âêôơưă" }
                        modIdx ?: vowelIndices[vowelIndices.size - 2]
                    }
                }
            }
        }
        chars[targetIndex] = addToneToVowel(chars[targetIndex], tone)
    }

    private fun rawKeysFor(text: String): String {
        val sb = StringBuilder()
        var toneKey: Char? = null
        for (ch in text) {
            val isUpper = ch.isUpperCase()
            val lower = ch.lowercaseChar()
            if (lower == 'đ') {
                sb.append(if (isUpper) "DD" else "dd")
                continue
            }
            val base = REVERSE_BASE_VOWEL_MAP[lower]
            val toneIdx = TONE_INDEX_MAP[lower] ?: 0
            if (base != null) {
                val rawBase = when (base) {
                    'ă' -> "aw"
                    'â' -> "aa"
                    'ê' -> "ee"
                    'ô' -> "oo"
                    'ơ' -> "ow"
                    'ư' -> "uw"
                    else -> base.toString()
                }
                val cased = if (isUpper) rawBase.replaceFirstChar { it.uppercaseChar() } else rawBase
                sb.append(cased)
                if (toneIdx > 0) {
                    toneKey = when (toneIdx) {
                        1 -> 's'
                        2 -> 'f'
                        3 -> 'r'
                        4 -> 'x'
                        5 -> 'j'
                        else -> null
                    }
                }
            } else {
                sb.append(ch)
            }
        }
        if (toneKey != null) sb.append(toneKey)
        return sb.toString()
    }

    companion object {
        private val ENGLISH_AUTO_RESTORE_WORDS = setOf(
            "text", "next", "flex", "fix", "box", "mix", "six", "tax", "wax", "fox", "ox", "ex",
            "more", "does", "life", "care", "user", "users", "test", "tests", "rest", "best", "fast",
            "last", "past", "list", "most", "post", "host", "cost", "lost", "just", "must", "rust",
            "dust", "trust", "first", "worst", "worth", "work", "works", "word", "words", "world",
            "wind", "will", "with", "what", "when", "where", "which", "while", "white", "who", "why",
            "way", "ways", "want", "wants", "wait", "walk", "wall", "warm", "wash", "watch", "wave",
            "wear", "web", "week", "weeks", "well", "went", "were", "west", "wide", "wife", "wild",
            "win", "wine", "wing", "winter", "wire", "wise", "wish", "woman", "women", "wonder",
            "wood", "write", "writer", "wrong", "zero", "zeros", "zone", "zones", "zoom", "zip",
            "zest", "zebra", "free", "fee", "fees", "feel", "feed", "feet", "file", "files", "find",
            "fine", "fire", "firm", "five", "flat", "flow", "focus", "folder", "follow", "font",
            "food", "foot", "force", "form", "format", "found", "four", "frame", "fresh", "friend",
            "from", "front", "full", "func", "function", "fund", "future", "java", "json", "join",
            "job", "jobs", "jump", "june", "july", "judge", "server", "service", "system", "status",
            "string", "stream", "start", "stop", "step", "store", "style", "switch", "search",
            "select", "send", "set", "show", "size", "sort", "source", "space", "split", "state",
            "static", "sync", "tab", "tag", "target", "task", "team", "term", "theme", "then",
            "there", "these", "they", "this", "those", "three", "time", "title", "tool", "tools",
            "true", "type", "update", "upload", "used", "using", "value", "view", "void", "your",
            "android", "gboard", "vnchflex", "macro", "keyboard", "shift", "enter", "delete",
            "english", "vietnamese", "release", "debug", "build", "gradle", "commit", "push", "pull"
        )

        private val ENGLISH_FJZ_PROTECTED_WORDS = setOf(
            "fee", "fees", "feel", "feed", "feet", "fast", "first", "file", "files", "find", "fine", "fire", "firm",
            "five", "flat", "flow", "focus", "folder", "follow", "font", "food", "foot", "force", "form", "format",
            "found", "four", "frame", "free", "fresh", "friend", "from", "front", "full", "func", "function", "fund",
            "future", "zero", "zeros", "zone", "zones", "zoom", "zip", "zest", "zebra", "just", "join", "json", "java",
            "job", "jobs", "jump", "june", "july", "judge"
        )

        private val ENGLISH_W_PROTECTED_WORDS = setOf(
            "water", "want", "wants", "wait", "walk", "wall", "warm", "warn", "wash", "watch", "wave", "way", "ways",
            "wear", "weather", "web", "week", "weeks", "weight", "welcome", "well", "went", "were", "west", "what",
            "when", "where", "which", "while", "white", "who", "whole", "whom", "whose", "why", "wide", "wife", "wild",
            "will", "win", "wind", "window", "windows", "wine", "wing", "winter", "wire", "wise", "wish", "with",
            "within", "without", "woman", "women", "wonder", "wood", "word", "words", "work", "works", "world",
            "worry", "worse", "worst", "worth", "would", "write", "writer", "wrong"
        )

        private val TONE_VOWEL_TABLE: Map<Char, List<Char>> = mapOf(
            // [none, acute, grave, hook, tilde, dot]
            'a' to listOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
            'ă' to listOf('ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ'),
            'â' to listOf('â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
            'e' to listOf('e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'),
            'ê' to listOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'),
            'i' to listOf('i', 'í', 'ì', 'ỉ', 'ĩ', 'ị'),
            'o' to listOf('o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'),
            'ô' to listOf('ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'),
            'ơ' to listOf('ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'),
            'u' to listOf('u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'),
            'ư' to listOf('ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'),
            'y' to listOf('y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ'),
        )

        private val REVERSE_BASE_VOWEL_MAP: Map<Char, Char> = buildMap {
            for ((base, tones) in TONE_VOWEL_TABLE) {
                for (t in tones) put(t, base)
            }
        }

        private val TONE_INDEX_MAP: Map<Char, Int> = buildMap {
            for ((_, tones) in TONE_VOWEL_TABLE) {
                tones.forEachIndexed { idx, t -> put(t, idx) }
            }
        }

        fun isVietnameseVowel(ch: Char): Boolean = REVERSE_BASE_VOWEL_MAP.containsKey(ch.lowercaseChar())

        fun addToneToVowel(ch: Char, tone: Tone): Char {
            val isUpper = ch.isUpperCase()
            val lower = ch.lowercaseChar()
            val base = REVERSE_BASE_VOWEL_MAP[lower] ?: return ch
            val row = TONE_VOWEL_TABLE[base] ?: return ch
            val accented = row.getOrElse(tone.index) { ch }
            return if (isUpper) accented.uppercaseChar() else accented
        }
    }
}
