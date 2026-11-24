package com.antigravity.vietkeyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.antigravity.vietkeyboard.R
import com.antigravity.vietkeyboard.engine.VietnameseEngine
import com.antigravity.vietkeyboard.macro.MacroEngine
import com.antigravity.vietkeyboard.macro.MacroStore
import com.antigravity.vietkeyboard.suggestion.SuggestionEngine

class VietKeyboardService : InputMethodService() {
    private lateinit var vnEngine: VietnameseEngine
    private lateinit var macroEngine: MacroEngine
    private lateinit var macroStore: MacroStore
    private lateinit var suggestionEngine: SuggestionEngine
    private var suggestionBar: LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        vnEngine = VietnameseEngine()
        macroStore = MacroStore(this)
        macroEngine = MacroEngine(macroStore.load())
        suggestionEngine = SuggestionEngine()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        suggestionBar = view.findViewById(R.id.suggestion_bar)
        bindKeys(view)
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        macroEngine.update(macroStore.load())
        vnEngine.reset()
        updateSuggestions("")
    }

    private fun bindKeys(root: View) {
        val keyIds = listOf(
            R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t, R.id.key_y,
            R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
            R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g, R.id.key_h,
            R.id.key_j, R.id.key_k, R.id.key_l,
            R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b, R.id.key_n, R.id.key_m,
        )
        keyIds.forEach { id ->
            root.findViewById<Button>(id).setOnClickListener { btn ->
                onChar((btn as Button).text.first())
            }
        }
        root.findViewById<Button>(R.id.key_space).setOnClickListener { onChar(' ') }
        root.findViewById<Button>(R.id.key_enter).setOnClickListener { onEnter() }
        root.findViewById<Button>(R.id.key_backspace).setOnClickListener { onBackspace() }
        root.findViewById<Button>(R.id.key_strip_diacritics).setOnClickListener { onStripDiacritics() }
        root.findViewById<Button>(R.id.key_dot).setOnClickListener { onChar('.') }
        root.findViewById<Button>(R.id.key_comma).setOnClickListener { onChar(',') }
    }

    private fun onChar(c: Char) {
        val ic = currentInputConnection ?: return
        if (isMacroTrigger(c)) {
            vnEngine.commit(ic)
            if (macroEngine.tryExpand(ic, c)) {
                updateSuggestions("")
                return
            }
            ic.commitText(c.toString(), 1)
            updateSuggestions("")
            return
        }

        val handled = vnEngine.onChar(c, ic)
        if (!handled) {
            vnEngine.commit(ic)
            ic.commitText(c.toString(), 1)
        }
        updateSuggestions(vnEngine.composingText)
    }

    private fun onBackspace() {
        val ic = currentInputConnection ?: return
        if (vnEngine.onBackspace(ic)) {
            updateSuggestions(vnEngine.composingText)
            return
        }
        ic.deleteSurroundingText(1, 0)
        updateSuggestions("")
    }

    private fun onEnter() {
        val ic = currentInputConnection ?: return
        vnEngine.commit(ic)
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        updateSuggestions("")
    }

    private fun onStripDiacritics() {
        val ic = currentInputConnection ?: return
        vnEngine.removeDiacritics(ic)
        updateSuggestions(vnEngine.composingText)
    }

    private fun updateSuggestions(prefix: String) {
        val bar = suggestionBar ?: return
        bar.removeAllViews()
        val suggestions = suggestionEngine.suggest(prefix)
        suggestions.forEach { word ->
            val tv = TextView(this).apply {
                text = word
                setPadding(24, 16, 24, 16)
                setOnClickListener { acceptSuggestion(word) }
            }
            bar.addView(tv)
        }
    }

    private fun acceptSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        vnEngine.reset(ic)
        // Replace composing prefix naively for MVP; production should track prefix length.
        val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
        val prefix = before.takeLastWhile { it.isLetter() }
        if (prefix.isNotEmpty()) ic.deleteSurroundingText(prefix.length, 0)
        ic.commitText("$word ", 1)
        suggestionEngine.learn(word)
        updateSuggestions("")
    }

    private fun isMacroTrigger(c: Char): Boolean = c.isWhitespace() || c in ".,;:!?\n"
}
