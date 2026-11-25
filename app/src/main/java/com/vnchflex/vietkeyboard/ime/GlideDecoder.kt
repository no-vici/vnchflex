package com.vnchflex.vietkeyboard.ime

import com.vnchflex.vietkeyboard.engine.DiacriticUtils
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

/**
 * Lightweight Vietnamese + English Glide Typing Decoder:
 * Matches a swiped key sequence (e.g. "xinchao" / "xnhao" -> "xin", "chào", "việt", "dzìa")
 * against the SuggestionEngine vocabulary using start/end anchor matching + subsequence edit distance,
 * then re-ranks candidates using the active Trigram `(prevWord2, prevWord1)` language model.
 */
object GlideDecoder {
    fun decode(
        swipedKeys: String,
        suggestionEngine: SuggestionEngine,
        prevWord1: String,
        prevWord2: String,
        limit: Int = 3
    ): List<String> {
        val raw = swipedKeys.lowercase()
        if (raw.length < 2) return emptyList()

        val firstChar = raw.first()
        val lastChar = raw.last()
        val vocab = suggestionEngine.getGlideVocabulary()
        val contextWeights = suggestionEngine.getContextBoosts(prevWord2, prevWord1)

        val scored = mutableListOf<Pair<String, Int>>()
        for (word in vocab) {
            val stripped = DiacriticUtils.strip(word).lowercase()
            if (stripped.length < 2) continue
            // Start & end key must match (or be very close) for fast, accurate pruning
            if (stripped.first() != firstChar || stripped.last() != lastChar) continue

            val subseqMatched = isSubsequence(stripped, raw)
            val dist = levenshtein(stripped, raw)
            if (!subseqMatched && dist > (raw.length / 2).coerceAtLeast(2)) continue

            var score = 0
            if (subseqMatched) score += 200
            score -= dist * 25
            score -= kotlin.math.abs(stripped.length - raw.length) * 8
            score += (contextWeights[word] ?: 0) * 60
            scored.add(word to score)
        }

        return scored
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
            .take(limit)
    }

    private fun isSubsequence(target: String, source: String): Boolean {
        var ti = 0
        var si = 0
        while (ti < target.length && si < source.length) {
            if (target[ti] == source[si]) ti++
            si++
        }
        return ti == target.length
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = IntArray(n + 1) { it }
        for (i in 1..m) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..n) {
                val temp = dp[j]
                dp[j] = if (a[i - 1] == b[j - 1]) prev
                else 1 + minOf(prev, dp[j], dp[j - 1])
                prev = temp
            }
        }
        return dp[n]
    }
}
