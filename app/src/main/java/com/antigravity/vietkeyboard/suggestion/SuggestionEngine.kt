package com.antigravity.vietkeyboard.suggestion

/**
 * Basic dictionary suggestion engine.
 * MVP: prefix match against a small bundled word list + user words.
 * Replace with n-gram/language model for GBoard-like quality.
 */
class SuggestionEngine {
    private val dictionary = linkedSetOf(
        "xin", "chào", "việt", "nam", "bàn", "phím", "không", "được", "cảm", "ơn",
        "tôi", "bạn", "đang", "làm", "gì", "hôm", "nay", "ngày", "mai", "đi",
        "học", "việc", "nhà", "công", "nghệ", "android", "macro", "bỏ", "dấu",
    )
    private val userWords = linkedSetOf<String>()

    fun suggest(prefix: String, limit: Int = 3): List<String> {
        val q = prefix.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val candidates = (userWords + dictionary)
            .filter { it.startsWith(q) && it != q }
            .distinct()
            .take(limit)
        return candidates
    }

    fun learn(word: String) {
        val w = word.trim().lowercase()
        if (w.length >= 2) userWords.add(w)
    }
}
