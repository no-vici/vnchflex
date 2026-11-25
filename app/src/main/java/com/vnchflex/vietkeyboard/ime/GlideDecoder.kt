package com.vnchflex.vietkeyboard.ime

import com.vnchflex.vietkeyboard.engine.DiacriticUtils
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

/**
 * Lightweight Vietnamese + English Glide Typing Decoder:
 * - Normalizes consecutive duplicate letters (`collapseRepeats`: `"letter"` -> `"leter"`, `"book"` -> `"bok"`)
 *   so words with double letters get the full subsequence bonus and zero edit-distance penalty when swiped.
 * - Tolerates start/end key keyboard-neighbor misses (`areKeysAdjacent`) with a slight penalty instead of hard-dropping.
 * - Combines geometric path similarity, unigram frequency weight, and active Trigram `(prevWord2, prevWord1)` context boosts.
 */
object GlideDecoder {
    private val KEY_NEIGHBORS: Map<Char, String> = mapOf(
        'q' to "wa", 'w' to "qeas", 'e' to "wrsd", 'r' to "etdf", 't' to "ryfg",
        'y' to "tugh", 'u' to "yihj", 'i' to "uojk", 'o' to "ipkl", 'p' to "ol",
        'a' to "qwsz", 's' to "weadzx", 'd' to "ersfxc", 'f' to "rtgvcd", 'g' to "tyhbvf",
        'h' to "yujnbg", 'j' to "uikmnh", 'k' to "AEiojl,m".lowercase(), 'l' to "opk",
        'z' to "asx", 'x' to "zsdc", 'c' to "xdfv", 'v' to "cfgb", 'b' to "vghn",
        'n' to "bhjm", 'm' to "njk"
    )

    fun decode(
        swipedKeys: String,
        suggestionEngine: SuggestionEngine,
        prevWord1: String,
        prevWord2: String,
        limit: Int = 3,
        isVietnameseMode: Boolean = true
    ): List<String> {
        val raw = collapseRepeats(swipedKeys.lowercase())
        if (raw.length < 2) return emptyList()

        val firstChar = raw.first()
        val lastChar = raw.last()
        val vocab = suggestionEngine.getGlideVocabulary(isVietnameseMode)
        val contextWeights = suggestionEngine.getContextBoosts(prevWord2, prevWord1)

        val scored = mutableListOf<Pair<String, Int>>()
        for ((word, unigramWeight) in vocab) {
            val stripped = DiacriticUtils.strip(word).lowercase()
            if (stripped.length < 2) continue

            val collapsedTarget = collapseRepeats(stripped)
            val exactFirst = collapsedTarget.first() == firstChar
            val exactLast = collapsedTarget.last() == lastChar
            val nearFirst = exactFirst || isNeighbor(collapsedTarget.first(), firstChar)
            val nearLast = exactLast || isNeighbor(collapsedTarget.last(), lastChar)
            if (!nearFirst || !nearLast) continue

            val subseqMatched = isSubsequence(collapsedTarget, raw)
            val dist = levenshtein(collapsedTarget, raw)
            if (!subseqMatched && dist > (raw.length / 2).coerceAtLeast(2)) continue

            var score = unigramWeight
            if (exactFirst) score += 90 else score += 25
            if (exactLast) score += 90 else score += 25
            if (subseqMatched) score += 220
            score -= dist * 28
            score -= kotlin.math.abs(collapsedTarget.length - raw.length) * 10
            score += (contextWeights[word] ?: 0) * 65
            scored.add(word to score)
        }

        return scored
            .sortedByDescending { it.second }
            .map { it.first }
            .distinct()
            .take(limit)
    }

    private fun isNeighbor(a: Char, b: Char): Boolean =
        a == b || KEY_NEIGHBORS[a]?.contains(b) == true

    private fun collapseRepeats(s: String): String {
        if (s.length <= 1) return s
        val sb = StringBuilder(s.length)
        var prev = '\u0000'
        for (c in s) {
            if (c != prev) {
                sb.append(c)
                prev = c
            }
        }
        return sb.toString()
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
