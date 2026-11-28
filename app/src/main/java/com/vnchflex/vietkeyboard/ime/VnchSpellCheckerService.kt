package com.vnchflex.vietkeyboard.ime

import android.service.textservice.SpellCheckerService
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextInfo
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

/**
 * Bilingual Vietnamese + English SpellCheckerService for vnchFlex.
 * Prevents Android's default spell checker from flagging valid Vietnamese words
 * or English loanwords/contractions as typos.
 */
class VnchSpellCheckerService : SpellCheckerService() {

    private lateinit var suggestionEngine: SuggestionEngine

    override fun onCreate() {
        super.onCreate()
        suggestionEngine = SuggestionEngine(this)
    }

    override fun createSession(): Session = VnchSession(suggestionEngine)

    private class VnchSession(
        private val engine: SuggestionEngine
    ) : Session() {

        override fun onCreate() {}

        override fun onGetSuggestions(textInfo: TextInfo?, suggestionsLimit: Int): SuggestionsInfo {
            val word = textInfo?.text?.trim().orEmpty()
            if (!com.vnchflex.vietkeyboard.settings.KeyboardPreferences(this@VnchSpellCheckerService).spellCheck ||
                word.length < 2 || engine.isKnownWord(word)
            ) {
                return SuggestionsInfo(
                    SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY,
                    emptyArray()
                )
            }
            val suggestions = engine.suggest(
                prefix = word,
                prevWord1 = "",
                prevWord2 = "",
                limit = suggestionsLimit.coerceIn(1, 5),
                includeEmoji = false
            ).filter { !it.equals(word, ignoreCase = true) }.toTypedArray()

            val attr = if (suggestions.isNotEmpty()) {
                SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO or SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS
            } else {
                SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO
            }
            return SuggestionsInfo(attr, suggestions)
        }
    }
}
