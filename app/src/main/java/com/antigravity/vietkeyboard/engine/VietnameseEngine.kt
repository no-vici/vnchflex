package com.antigravity.vietkeyboard.engine

import android.view.inputmethod.InputConnection

/**
 * Lightweight Vietnamese composing engine.
 * MVP supports Telex-style marks and "bỏ dấu tự do" (free diacritic removal).
 * For production, replace with a full syllable parser or reuse Funput/UniKey core.
 */
class VietnameseEngine {
    private val buffer = StringBuilder()

    val composingText: String get() = buffer.toString()
    val isComposing: Boolean get() = buffer.isNotEmpty()

    fun onChar(input: Char, ic: InputConnection): Boolean {
        val lower = input.lowercaseChar()
        if (buffer.isNotEmpty() && isTelexToneKey(lower)) {
            val replaced = applyTone(buffer.toString(), lower)
            if (replaced != null) {
                buffer.clear()
                buffer.append(matchCase(replaced, bufferCaseHint = input))
                ic.setComposingText(buffer.toString(), 1)
                return true
            }
        }
        if (buffer.isNotEmpty() && isTelexShapeKey(lower)) {
            val replaced = applyShape(buffer.toString(), lower)
            if (replaced != null) {
                buffer.clear()
                buffer.append(replaced)
                ic.setComposingText(buffer.toString(), 1)
                return true
            }
        }

        // Default: append to composing buffer for alphabetic chars, commit otherwise.
        if (input.isLetter()) {
            buffer.append(input)
            ic.setComposingText(buffer.toString(), 1)
            return true
        }
        commit(ic)
        return false
    }

    fun onBackspace(ic: InputConnection): Boolean {
        if (buffer.isEmpty()) return false
        buffer.deleteCharAt(buffer.length - 1)
        if (buffer.isEmpty()) ic.finishComposingText() else ic.setComposingText(buffer.toString(), 1)
        return true
    }

    fun commit(ic: InputConnection) {
        if (buffer.isEmpty()) return
        ic.commitText(buffer.toString(), 1)
        buffer.clear()
    }

    fun reset(ic: InputConnection? = null) {
        buffer.clear()
        ic?.finishComposingText()
    }

    /** Bỏ dấu tự do: strip diacritics from composing buffer or last word in editor. */
    fun removeDiacritics(ic: InputConnection) {
        if (buffer.isNotEmpty()) {
            val stripped = DiacriticUtils.strip(buffer.toString())
            buffer.clear()
            buffer.append(stripped)
            ic.setComposingText(buffer.toString(), 1)
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

    private fun isTelexToneKey(c: Char): Boolean = c in "sfrxjz"
    private fun isTelexShapeKey(c: Char): Boolean = c in "aeowd"

    private fun applyTone(text: String, key: Char): String? {
        if (key == 'z') return DiacriticUtils.strip(text)
        val tone = when (key) {
            's' -> '\u0301' // acute
            'f' -> '\u0300' // grave
            'r' -> '\u0309' // hook
            'x' -> '\u0303' // tilde
            'j' -> '\u0323' // dot below
            else -> return null
        }
        // MVP: place tone on last vowel group (simplified "free" placement).
        val chars = text.toMutableList()
        val vowelIdx = chars.indexOfLast { it.lowercaseChar() in VOWELS }
        if (vowelIdx < 0) return null
        val base = chars[vowelIdx].toString()
        val strippedVowel = DiacriticUtils.strip(base)
        val existing = NormalizerSafe.marks(base)
        val newMarks = existing.filterNot { it in TONE_MARKS } + tone
        chars[vowelIdx] = NormalizerSafe.compose(strippedVowel, newMarks).first()
        return chars.joinToString("")
    }

    private fun applyShape(text: String, key: Char): String? {
        // Simplified: handle đ, â/ă/ê/ô/ơ/ư on last matching char.
        val chars = text.toMutableList()
        if (key == 'd') {
            val idx = chars.indexOfLast { it == 'd' || it == 'D' || it == 'đ' || it == 'Đ' }
            if (idx < 0) return null
            chars[idx] = when (chars[idx]) {
                'd' -> 'đ'; 'D' -> 'Đ'; 'đ' -> 'd'; 'Đ' -> 'D'; else -> chars[idx]
            }
            return chars.joinToString("")
        }
        val target = when (key) {
            'a' -> charArrayOf('a', 'â', 'ă')
            'e' -> charArrayOf('e', 'ê')
            'o' -> charArrayOf('o', 'ô', 'ơ')
            'w' -> charArrayOf('a', 'ă', 'o', 'ơ', 'u', 'ư')
            else -> return null
        }
        val idx = chars.indexOfLast { target.contains(it.lowercaseChar()) }
        if (idx < 0) return null
        chars[idx] = cycleShape(chars[idx], key)
        return chars.joinToString("")
    }

    private fun cycleShape(c: Char, key: Char): Char {
        val isUpper = c.isUpperCase()
        val lower = c.lowercaseChar()
        val out = when (key) {
            'a' -> when (lower) { 'a' -> 'â'; 'â' -> 'a'; 'ă' -> 'â'; else -> lower }
            'e' -> if (lower == 'e') 'ê' else if (lower == 'ê') 'e' else lower
            'o' -> if (lower == 'o') 'ô' else if (lower == 'ô') 'o' else if (lower == 'ơ') 'ô' else lower
            'w' -> when (lower) { 'a' -> 'ă'; 'ă' -> 'a'; 'o' -> 'ơ'; 'ơ' -> 'o'; 'u' -> 'ư'; 'ư' -> 'u'; else -> lower }
            else -> lower
        }
        return if (isUpper) out.uppercaseChar() else out
    }

    private fun matchCase(text: String, bufferCaseHint: Char): String = text // simplified

    private companion object {
        val VOWELS = "aeiouyăâêôơưáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ".toSet()
        val TONE_MARKS = setOf('\u0301', '\u0300', '\u0309', '\u0303', '\u0323')
    }
}

private object NormalizerSafe {
    fun marks(text: String): List<Char> =
        java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .drop(1)
            .filter { it.category == CharCategory.NON_SPACING_MARK || it.category == CharCategory.COMBINING_SPACING_MARK }
            .toList()

    fun compose(base: String, marks: List<Char>): String =
        java.text.Normalizer.normalize(base + marks.joinToString(""), java.text.Normalizer.Form.NFC)
}
