package com.vnchflex.vietkeyboard.macro

import android.view.inputmethod.InputConnection

/**
 * Macro replacement engine with vnchKey smart case adaptation
 * (e.g. "hn" -> "Hà Nội", "HN" -> "HÀ NỘI", "dc" -> "được", "Dc" -> "Được", "DC" -> "ĐƯỢC")
 * and technical delimiter suppression (e.g. `.vn`, `user@hn`, `file_dc.txt`).
 */
class MacroEngine(initialMacros: List<Macro> = emptyList()) {
    @Volatile
    private var macros: List<Macro> = initialMacros

    /** Stores (originalTokenWithTrigger, expandedTextWithTrigger) for double-backspace undo */
    var lastExpansion: Pair<String, String>? = null
        private set

    fun clearLastExpansion() {
        lastExpansion = null
    }

    fun update(newMacros: List<Macro>) {
        macros = newMacros
    }

    fun tryExpand(
        ic: InputConnection,
        trigger: Char,
        beforeCursor: String? = null,
        composingLength: Int = 0
    ): Boolean {
        if (isTechnicalDelimiter(trigger) || trigger in "([{<") return false

        val before = beforeCursor ?: ic.getTextBeforeCursor(MAX_LOOKBACK, 0)?.toString() ?: return false
        val token = before.takeLastWhile { !it.isWhitespace() && !isTechnicalDelimiter(it) }
        if (token.isEmpty()) return false

        val charBeforeTokenIdx = before.length - token.length - 1
        if (charBeforeTokenIdx >= 0 && isTechnicalDelimiter(before[charBeforeTokenIdx])) {
            return false
        }

        val macro = findMacro(token) ?: return false
        val expanded = adaptCase(token, resolveVariables(macro.expansion))
        val expandedWithTrigger = expanded + trigger

        ic.beginBatchEdit()
        try {
            if (composingLength > 0) {
                ic.finishComposingText()
            }
            val deleted = ic.deleteSurroundingText(token.length, 0)
            if (!deleted) return false
            ic.commitText(expandedWithTrigger, 1)
            lastExpansion = (token + trigger) to expandedWithTrigger
        } finally {
            ic.endBatchEdit()
        }
        return true
    }

    fun findMacro(token: String): Macro? = macros.firstOrNull { m ->
        m.enabled && (if (m.caseSensitive) m.shortcut == token else m.shortcut.equals(token, ignoreCase = true))
    }

    fun lookupPreview(token: String): String? {
        if (token.isEmpty()) return null
        val macro = findMacro(token) ?: return null
        return adaptCase(token, resolveVariables(macro.expansion))
    }

    private fun adaptCase(inputToken: String, expansion: String): String {
        if (expansion.isEmpty()) return expansion
        if (inputToken.length >= 2 && inputToken == inputToken.uppercase() && inputToken != inputToken.lowercase()) {
            return expansion.uppercase()
        }
        if (inputToken.first().isUpperCase()) {
            return expansion.replaceFirstChar { it.uppercaseChar() }
        }
        return expansion
    }

    private fun resolveVariables(text: String): String {
        val now = java.time.ZonedDateTime.now()
        return text
            .replace("{date}", now.toLocalDate().toString())
            .replace("{time}", now.toLocalTime().withNano(0).toString())
    }

    private fun isTechnicalDelimiter(c: Char): Boolean = c in "/@_-#\\=$~`"

    companion object {
        private const val MAX_LOOKBACK = 64
    }
}
