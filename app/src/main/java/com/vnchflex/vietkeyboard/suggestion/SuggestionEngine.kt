package com.vnchflex.vietkeyboard.suggestion

import android.content.Context
import com.vnchflex.vietkeyboard.engine.DiacriticUtils

/**
 * Modern Vietnamese Word Suggestion & Trigram Next-Word Suggestion Engine
 * (Architecture aligned with Modern-Vietnamese-IME-and-Macro-Engine & Gboard):
 *
 * 1. **Word Suggestions (Prefix + Unaccented-to-Accented + Context Re-ranking)**:
 *    - Matches exact accented prefix AND unaccented Telex/ASCII prefix (`"viet"` -> `"việt"`, `"dzia"` -> `"dzìa"`, `"dzach"` -> `"dzách"`).
 *    - Re-ranks prefix candidates using active Trigram `(prev2, prev1)` and Bigram `(prev1)` language model weights
 *      combined with unigram frequency and user-learned frequency.
 *    - Integrates Personal Dictionary shortcuts, Macro expansion previews, and Emoji suggestions.
 *
 * 2. **Trigram Next-Word Suggestions (`P(w_i | w_{i-2}, w_{i-1})` with Katz Backoff)**:
 *    - Primary: User-learned Trigrams + Bundled Vietnamese Trigram table `(prev2, prev1) -> [candidates]`.
 *    - Backoff 1: User-learned Bigrams + Bundled Bigram table `(prev1) -> [candidates]`.
 *    - Backoff 2: High-frequency Vietnamese sentence starters / unigrams.
 *
 * 3. **Online N-gram Learning**:
 *    - Automatically learns Unigrams, Bigrams `(prev1 -> word)`, and Trigrams `(prev2 + " " + prev1 -> word)`
 *      as the user commits words, persisting them locally via `PersonalDictionaryStore`.
 */
class SuggestionEngine(context: Context? = null) {
    private val dictStore = context?.let { PersonalDictionaryStore(it) }

    // Unigram dictionary with base frequency weights (including Southern Vietnamese dialect words: dzìa, dzách, dzô, dzậy, dzọt, dzui...)
    private val coreUnigramWeights: Map<String, Int> = linkedMapOf(
        "xin" to 95, "chào" to 96, "việt" to 98, "nam" to 97, "bàn" to 88, "phím" to 89,
        "không" to 100, "được" to 99, "cảm" to 94, "ơn" to 94, "tôi" to 96, "bạn" to 97,
        "đang" to 92, "làm" to 93, "gì" to 91, "hôm" to 90, "nay" to 92, "ngày" to 91,
        "mai" to 89, "đi" to 93, "học" to 88, "việc" to 90, "nhà" to 91, "công" to 92,
        "nghệ" to 87, "android" to 85, "macro" to 82, "bỏ" to 84, "dấu" to 85, "tự" to 88,
        "do" to 86, "thương" to 87, "trường" to 88, "người" to 95, "những" to 93, "trong" to 94,
        "ngoài" to 86, "thời" to 90, "gian" to 90, "thành" to 91, "phố" to 91, "hà" to 92,
        "nội" to 92, "sài" to 93, "gòn" to 93, "đà" to 89, "nẵng" to 89, "chúng" to 90,
        "ta" to 89, "mình" to 95, "nhưng" to 91, "với" to 93, "cho" to 94, "biết" to 92,
        "thế" to 90, "nào" to 91, "bây" to 89, "giờ" to 92, "tất" to 88, "cả" to 90,
        "rất" to 91, "vui" to 90, "nhiều" to 92, "lắm" to 89, "quá" to 90, "luôn" to 89,
        "nha" to 91, "nhé" to 91, "ạ" to 90, "ơi" to 89, "rồi" to 94, "chưa" to 92,
        "có" to 98, "là" to 98, "của" to 96, "và" to 97, "này" to 93, "đó" to 92,
        "anh" to 94, "em" to 94, "chị" to 90, "ăn" to 89, "cơm" to 87, "uống" to 86,
        "nước" to 90, "cà" to 86, "phê" to 86, "điện" to 89, "thoại" to 89, "máy" to 88,
        "tính" to 88, "chương" to 85, "trình" to 86, "ứng" to 87, "dụng" to 87, "hệ" to 86,
        "thống" to 86, "tiếng" to 93, "anh" to 92, "nhanh" to 88, "chóng" to 84, "tốt" to 90,
        "đẹp" to 88, "mới" to 91, "cũ" to 83, "muốn" to 92, "cần" to 91, "phải" to 92,
        // Từ phương ngữ Nam Bộ & Gen-Z (vnchKey)
        "dzìa" to 88, "dzách" to 87, "dzô" to 89, "dzậy" to 88, "dzọt" to 85, "dzui" to 87,
        "dzữ" to 85, "dza" to 82, "zậy" to 86, "zui" to 86, "wá" to 85, "fải" to 84,
        "bá" to 85, "cháy" to 84, "thiệt" to 88, "hông" to 88, "hen" to 85, "nghen" to 85,
    )

    // Bundled Trigram Language Model: "prev2 prev1" -> ordered list of next words
    private val bundledTrigrams: Map<String, List<String>> = mapOf(
        "xin chào" to listOf("bạn", "mọi người", "anh", "chị"),
        "cảm ơn" to listOf("bạn", "anh", "chị", "nhiều"),
        "rất vui" to listOf("được", "khi", "vì"),
        "vui được" to listOf("gặp", "làm", "biết"),
        "được gặp" to listOf("bạn", "anh", "mọi người"),
        "hôm nay" to listOf("tôi", "bạn", "trời", "mình"),
        "ngày mai" to listOf("mình", "tôi", "bạn", "đi"),
        "bây giờ" to listOf("mình", "tôi", "bạn", "đang"),
        "thành phố" to listOf("hồ chí minh", "hà nội", "đà nẵng"),
        "hồ chí" to listOf("minh", "minh và"),
        "việt nam" to listOf("ta", "mình", "vô địch", "ơi"),
        "tiếng việt" to listOf("rất", "chuẩn", "tự do"),
        "bỏ dấu" to listOf("tự do", "tiếng việt", "nhanh"),
        "dấu tự" to listOf("do", "động"),
        "bàn phím" to listOf("tiếng việt", "gboard", "android"),
        "số điện" to listOf("thoại", "thoại của"),
        "điện thoại" to listOf("của", "di động", "mới"),
        "không biết" to listOf("có", "sao", "là", "nữa"),
        "tôi đang" to listOf("làm", "ở", "đi", "học"),
        "mình đang" to listOf("ở", "làm", "đi", "ăn"),
        "bạn đang" to listOf("làm gì", "ở đâu", "đi đâu"),
        "đang làm" to listOf("gì", "việc", "bài"),
        "làm gì" to listOf("đó", "vậy", "thế", "dzậy"),
        "đi dzìa" to listOf("nhà", "quê", "sớm", "thôi"),
        "mới dzìa" to listOf("tới", "nhà", "luôn"),
        "bá cháy" to listOf("bọ chét", "luôn", "thiệt"),
        "số dzách" to listOf("luôn", "thiệt", "nha"),
        "hết sảy" to listOf("con bà bảy", "luôn", "nha"),
        "chúc bạn" to listOf("một ngày", "vui vẻ", "thành công"),
        "một ngày" to listOf("tốt lành", "vui vẻ", "mới"),
    )

    // Bundled Bigram Language Model: "prev1" -> ordered list of next words
    private val bundledBigrams: Map<String, List<String>> = mapOf(
        "xin" to listOf("chào", "lỗi", "cảm ơn", "hỏi"),
        "cảm" to listOf("ơn", "thấy", "xúc", "giác"),
        "việt" to listOf("nam", "ngữ", "kiều"),
        "hôm" to listOf("nay", "qua", "trước", "đó"),
        "ngày" to listOf("mai", "nay", "xưa", "mới"),
        "thành" to listOf("phố", "công", "viên", "thật"),
        "bàn" to listOf("phím", "làm", "bạc", "ăn"),
        "không" to listOf("biết", "có", "được", "thể"),
        "tôi" to listOf("đang", "muốn", "là", "có"),
        "mình" to listOf("đang", "sẽ", "cũng", "đi"),
        "bạn" to listOf("có", "đang", "ơi", "nhé"),
        "đang" to listOf("làm", "ở", "đi", "học"),
        "làm" to listOf("việc", "gì", "sao", "ơn"),
        "đi" to listOf("dzìa", "học", "làm", "chơi"),
        "dzìa" to listOf("nhà", "quê", "trễ", "sớm"),
        "dzô" to listOf("đây", "nhà", "một", "luôn"),
        "số" to listOf("dzách", "điện thoại", "lượng", "một"),
        "dzách" to listOf("luôn", "thiệt", "nha"),
        "bá" to listOf("cháy", "đạo", "chủ"),
        "bỏ" to listOf("dấu", "qua", "đi"),
        "dấu" to listOf("tự do", "câu", "thanh"),
        "thời" to listOf("gian", "tiết", "điểm"),
        "công" to listOf("việc", "ty", "nghệ", "nhận"),
        "sài" to listOf("gòn", "thành"),
        "hà" to listOf("nội", "đông"),
        "đà" to listOf("nẵng", "lạt"),
        "rất" to listOf("vui", "nhiều", "tốt", "hay"),
    )

    private val emojiMap = mapOf(
        "vui" to "😊", "dzui" to "😄", "cười" to "😂", "yêu" to "❤️", "tim" to "❤️",
        "ok" to "👍", "được" to "👍", "dzách" to "🔥", "dzô" to "🍻", "buồn" to "😢",
        "khóc" to "😭", "chào" to "👋", "cafe" to "☕", "lửa" to "🔥",
    )

    private val userUnigramCounts = mutableMapOf<String, Int>()
    private val userBigrams = mutableMapOf<String, MutableMap<String, Int>>()
    private val userTrigrams = mutableMapOf<String, MutableMap<String, Int>>()

    init {
        dictStore?.loadLearnedWords()?.forEach { w ->
            userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 5
        }
        dictStore?.loadLearnedBigrams()?.forEach { (ctx, map) ->
            userBigrams[ctx] = map.toMutableMap()
        }
        dictStore?.loadLearnedTrigrams()?.forEach { (ctx, map) ->
            userTrigrams[ctx] = map.toMutableMap()
        }
    }

    /**
     * Gợi ý từ đang gõ (Word Suggestions):
     * - Khớp tiền tố có dấu và tiền tố không dấu (`"viet"` -> `"việt"`, `"dzia"` -> `"dzìa"`, `"dzach"` -> `"dzách"`).
     * - Ưu tiên các từ phù hợp với ngữ cảnh Trigram `(prevWord2, prevWord1)` và Bigram `(prevWord1)`.
     */
    fun suggest(
        prefix: String,
        prevWord1: String = "",
        prevWord2: String = "",
        limit: Int = 3,
        includeEmoji: Boolean = true,
        macroPreview: String? = null,
    ): List<String> {
        val q = prefix.trim()
        if (q.isEmpty()) return emptyList()
        val lower = q.lowercase()
        val strippedQuery = DiacriticUtils.strip(lower)
        val isUpperFirst = q.first().isUpperCase()

        val results = mutableListOf<String>()
        // Slot 0 (Gboard left slot): exact composing text
        results.add(q)

        // Slot 1 priority: Macro preview or Personal Dictionary shortcut
        if (!macroPreview.isNullOrEmpty() && !results.contains(macroPreview)) {
            results.add(macroPreview)
        }

        val personalWords = dictStore?.loadPersonalWords().orEmpty()
        personalWords.forEach { pw ->
            val pwLower = pw.word.lowercase()
            val pwStripped = DiacriticUtils.strip(pwLower)
            if (pw.shortcut.equals(lower, ignoreCase = true) ||
                pwLower.startsWith(lower) ||
                pwStripped.startsWith(strippedQuery)
            ) {
                if (!results.contains(pw.word)) results.add(pw.word)
            }
        }

        if (includeEmoji) {
            (emojiMap[lower] ?: emojiMap[strippedQuery])?.let { emoji ->
                if (!results.contains(emoji)) results.add(emoji)
            }
        }

        // Contextual Trigram & Bigram candidate boosts for prefix completion
        val contextualFollowers = getContextualWeights(prevWord2, prevWord1)

        val allWords = LinkedHashSet<String>().apply {
            addAll(userUnigramCounts.keys)
            addAll(coreUnigramWeights.keys)
        }

        val scoredCandidates = allWords
            .filter { candidate ->
                if (candidate.equals(lower, ignoreCase = true)) return@filter false
                val candStripped = DiacriticUtils.strip(candidate)
                candidate.startsWith(lower) || candStripped.startsWith(strippedQuery)
            }
            .map { candidate ->
                val exactPrefixBoost = if (candidate.startsWith(lower)) 250 else 100
                val contextBoost = (contextualFollowers[candidate] ?: 0) * 120
                val userBoost = (userUnigramCounts[candidate] ?: 0) * 15
                val baseWeight = coreUnigramWeights[candidate] ?: 50
                val lengthPenalty = (candidate.length - lower.length).coerceAtLeast(0) * 4
                val totalScore = exactPrefixBoost + contextBoost + userBoost + baseWeight - lengthPenalty
                candidate to totalScore
            }
            .sortedByDescending { it.second }

        scoredCandidates.forEach { (candidate, _) ->
            val cased = if (isUpperFirst) candidate.replaceFirstChar { it.uppercaseChar() } else candidate
            if (!results.contains(cased)) results.add(cased)
        }

        return results.take(limit)
    }

    /**
     * Gợi ý từ tiếp theo theo mô hình Trigram `(prevWord2, prevWord1) -> nextWord`
     * với Katz Backoff về Bigram `(prevWord1) -> nextWord` và Unigram.
     */
    fun suggestNextWord(
        prevWord1: String,
        prevWord2: String = "",
        limit: Int = 3,
    ): List<String> {
        val p1 = prevWord1.trim().lowercase()
        val p2 = prevWord2.trim().lowercase()
        if (p1.isEmpty()) return listOf("Tôi", "Xin", "Mình").take(limit)

        val candidates = LinkedHashSet<String>()

        // 1. Trigram lookup: (p2, p1) -> nextWord
        if (p2.isNotEmpty()) {
            val trigramKey = "$p2 $p1"
            userTrigrams[trigramKey]?.entries
                ?.sortedByDescending { it.value }
                ?.forEach { candidates.add(it.key) }
            bundledTrigrams[trigramKey]?.forEach { candidates.add(it) }
        }

        // 2. Katz Backoff -> Bigram lookup: (p1) -> nextWord
        if (candidates.size < limit) {
            userBigrams[p1]?.entries
                ?.sortedByDescending { it.value }
                ?.forEach { candidates.add(it.key) }
            bundledBigrams[p1]?.forEach { candidates.add(it) }
        }

        // 3. Backoff -> High-frequency Vietnamese connectors / unigrams
        if (candidates.size < limit) {
            listOf("là", "có", "và", "được", "không", "nha").forEach {
                if (!it.equals(p1, ignoreCase = true)) candidates.add(it)
            }
        }

        return candidates.take(limit)
    }

    /**
     * Học từ đơn (Unigram) + cặp từ (Bigram) + bộ 3 từ (Trigram) khi người dùng nhập xong từ.
     */
    fun learn(word: String, prevWord1: String = "", prevWord2: String = "") {
        val w = word.trim().lowercase()
        if (w.length < 2) return

        // 1. Learn Unigram
        userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 1
        dictStore?.addLearnedWord(w)

        // 2. Learn Bigram (prevWord1 -> w)
        val p1 = prevWord1.trim().lowercase()
        if (p1.isNotEmpty()) {
            val bMap = userBigrams.getOrPut(p1) { mutableMapOf() }
            bMap[w] = (bMap[w] ?: 0) + 1
            dictStore?.saveLearnedBigrams(userBigrams)

            // 3. Learn Trigram (prevWord2 + " " + prevWord1 -> w)
            val p2 = prevWord2.trim().lowercase()
            if (p2.isNotEmpty()) {
                val tKey = "$p2 $p1"
                val tMap = userTrigrams.getOrPut(tKey) { mutableMapOf() }
                tMap[w] = (tMap[w] ?: 0) + 1
                dictStore?.saveLearnedTrigrams(userTrigrams)
            }
        }
    }

    fun clearLearnedWords() {
        userUnigramCounts.clear()
        userBigrams.clear()
        userTrigrams.clear()
        dictStore?.clearLearnedWords()
    }

    fun getGlideVocabulary(): Set<String> = LinkedHashSet<String>().apply {
        addAll(userUnigramCounts.keys)
        addAll(coreUnigramWeights.keys)
    }

    fun getContextBoosts(prevWord2: String, prevWord1: String): Map<String, Int> =
        getContextualWeights(prevWord2, prevWord1)

    fun flushPendingSaves() {
        dictStore?.flushNgrams()
    }

    private fun getContextualWeights(prevWord2: String, prevWord1: String): Map<String, Int> {
        val weights = mutableMapOf<String, Int>()
        val p1 = prevWord1.trim().lowercase()
        val p2 = prevWord2.trim().lowercase()
        if (p1.isEmpty()) return weights

        if (p2.isNotEmpty()) {
            val tKey = "$p2 $p1"
            bundledTrigrams[tKey]?.forEachIndexed { idx, w ->
                weights[w] = (weights[w] ?: 0) + (10 - idx).coerceAtLeast(3)
            }
            userTrigrams[tKey]?.forEach { (w, count) ->
                weights[w] = (weights[w] ?: 0) + count * 6
            }
        }

        bundledBigrams[p1]?.forEachIndexed { idx, w ->
            weights[w] = (weights[w] ?: 0) + (5 - idx).coerceAtLeast(1)
        }
        userBigrams[p1]?.forEach { (w, count) ->
            weights[w] = (weights[w] ?: 0) + count * 3
        }
        return weights
    }
}
