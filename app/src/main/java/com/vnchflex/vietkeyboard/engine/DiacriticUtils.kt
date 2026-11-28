package com.vnchflex.vietkeyboard.engine

import java.text.Normalizer

object DiacriticUtils {
    private val COMBINING_MARKS = Regex("\\p{Mn}+")

    /** Removes Vietnamese diacritics, including đ/Đ. */
    fun strip(input: String): String {
        if (input.isEmpty()) return input
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return COMBINING_MARKS.replace(normalized, "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }
}
