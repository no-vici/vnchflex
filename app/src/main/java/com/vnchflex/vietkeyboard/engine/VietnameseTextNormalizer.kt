package com.vnchflex.vietkeyboard.engine

import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

/**
 * 100% Offline Vietnamese Text Normalizer & Diacritic Restorer for selected text:
 * 1. Restores missing Vietnamese diacritics using [SuggestionEngine] Unigram + Bigram/Trigram context.
 * 2. Normalizes spacing around punctuation (`. , ! ? : ;`) and capitalizes sentence beginnings.
 * 3. Strips Vietnamese diacritics (`DiacriticUtils.strip`).
 * 4. Cycles capitalization (`UPPERCASE` <-> `lowercase` <-> `Title Case`).
 */
object VietnameseTextNormalizer {

    private val wordTokenRegex = Regex("[\\p{L}]+")

    fun restoreDiacriticsAndNormalize(text: String, suggestionEngine: SuggestionEngine): String {
        if (text.isBlank()) return text
        val normalizedPunct = normalizePunctuationSpacing(text)
        var prev1 = ""
        var prev2 = ""
        val restored = wordTokenRegex.replace(normalizedPunct) { match ->
            val word = match.value
            val lower = word.lowercase()
            val stripped = DiacriticUtils.strip(lower)
            val replacedLower = if (lower == stripped && lower.length in 2..7) {
                suggestionEngine.bestAccentedMatch(lower, prev1, prev2)
            } else {
                lower
            }
            prev2 = prev1
            prev1 = replacedLower
            when {
                word.all { it.isUpperCase() } && word.length > 1 -> replacedLower.uppercase()
                word.first().isUpperCase() -> replacedLower.replaceFirstChar { it.uppercaseChar() }
                else -> replacedLower
            }
        }
        return capitalizeSentences(restored)
    }

    fun normalizePunctuationAndCase(text: String): String {
        if (text.isBlank()) return text
        return capitalizeSentences(normalizePunctuationSpacing(text))
    }

    fun stripDiacritics(text: String): String = DiacriticUtils.strip(text)

    fun cycleCase(text: String): String {
        if (text.isEmpty()) return text
        return when {
            text == text.lowercase() -> {
                // lowercase -> Title Case
                wordTokenRegex.replace(text) { m ->
                    m.value.lowercase().replaceFirstChar { it.uppercaseChar() }
                }
            }
            text == text.uppercase() -> text.lowercase()
            else -> text.uppercase()
        }
    }

    private fun normalizePunctuationSpacing(input: String): String {
        return input
            // Collapse multiple horizontal spaces
            .replace(Regex("[ \\t]+"), " ")
            // Remove space before punctuation
            .replace(Regex(" +([.,!?:;])"), "$1")
            // Ensure single space after punctuation when followed by a letter
            .replace(Regex("([.,!?:;])([\\p{L}])"), "$1 $2")
            .trim()
    }

    private fun capitalizeSentences(input: String): String {
        val sb = StringBuilder(input.length)
        var capNext = true
        for (ch in input) {
            if (capNext && ch.isLetter()) {
                sb.append(ch.uppercaseChar())
                capNext = false
            } else {
                sb.append(ch)
                if (ch == '.' || ch == '!' || ch == '?' || ch == '\n') {
                    capNext = true
                } else if (!ch.isWhitespace() && ch != '"' && ch != '\'' && ch != '(') {
                    capNext = false
                }
            }
        }
        return sb.toString()
    }
}
