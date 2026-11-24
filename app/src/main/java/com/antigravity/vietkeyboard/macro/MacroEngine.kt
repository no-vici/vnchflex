package com.antigravity.vietkeyboard.macro

import android.view.inputmethod.InputConnection

class MacroEngine(initialMacros: List<Macro> = emptyList()) {
    @Volatile
    private var macros: List<Macro> = initialMacros

    fun update(newMacros: List<Macro>) {
        macros = newMacros
    }

    /**
     * Tries to expand the token before cursor when a delimiter is typed.
     * Returns true if expansion was performed; caller should skip committing trigger.
     */
    fun tryExpand(ic: InputConnection, trigger: Char): Boolean {
        val before = ic.getTextBeforeCursor(MAX_LOOKBACK, 0)?.toString() ?: return false
        val token = before.takeLastWhile { !it.isWhitespace() }
        if (token.isEmpty()) return false

        val macro = findMacro(token) ?: return false
        val replacement = resolveVariables(macro.expansion)

        ic.beginBatchEdit()
        try {
            val deleted = ic.deleteSurroundingText(token.length, 0)
            if (!deleted) return false
            ic.commitText(replacement + trigger, 1)
        } finally {
            ic.endBatchEdit()
        }
        return true
    }

    fun findMacro(token: String): Macro? = macros.firstOrNull { m ->
        m.enabled && (if (m.caseSensitive) m.shortcut == token else m.shortcut.equals(token, ignoreCase = true))
    }

    private fun resolveVariables(text: String): String {
        // MVP: static variables; extend with date/time/clipboard/cursor.
        val now = java.time.ZonedDateTime.now()
        return text
            .replace("{date}", now.toLocalDate().toString())
            .replace("{time}", now.toLocalTime().withNano(0).toString())
    }

    companion object {
        private const val MAX_LOOKBACK = 64
    }
}
