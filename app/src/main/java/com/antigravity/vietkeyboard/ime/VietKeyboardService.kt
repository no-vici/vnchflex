package com.antigravity.vietkeyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.antigravity.vietkeyboard.R
import com.antigravity.vietkeyboard.engine.VietnameseEngine
import com.antigravity.vietkeyboard.macro.MacroEngine
import com.antigravity.vietkeyboard.macro.MacroStore
import com.antigravity.vietkeyboard.suggestion.SuggestionEngine

class VietKeyboardService : InputMethodService() {
    private enum class Mode { LETTERS, SYMBOLS1, SYMBOLS2 }

    private lateinit var vnEngine: VietnameseEngine
    private lateinit var macroEngine: MacroEngine
    private lateinit var macroStore: MacroStore
    private lateinit var suggestionEngine: SuggestionEngine

    private var keyboardView: View? = null
    private var suggestionBar: LinearLayout? = null
    private var panelLetters: View? = null
    private var panelSymbols1: View? = null
    private var panelSymbols2: View? = null
    private var mode = Mode.LETTERS
    private var altPopup: PopupWindow? = null

    override fun onCreate() {
        super.onCreate()
        vnEngine = VietnameseEngine()
        macroStore = MacroStore(this)
        macroEngine = MacroEngine(macroStore.load())
        suggestionEngine = SuggestionEngine()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView = view
        suggestionBar = view.findViewById(R.id.suggestion_bar)
        panelLetters = view.findViewById(R.id.panel_letters)
        panelSymbols1 = view.findViewById(R.id.panel_symbols1)
        panelSymbols2 = view.findViewById(R.id.panel_symbols2)
        bindKeys(view)
        showMode(Mode.LETTERS)
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        macroEngine.update(macroStore.load())
        vnEngine.reset()
        updateSuggestions("")
        showMode(Mode.LETTERS)
    }

    private fun bindKeys(root: View) {
        val letterIds = listOf(
            R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t, R.id.key_y,
            R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
            R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g, R.id.key_h,
            R.id.key_j, R.id.key_k, R.id.key_l,
            R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b, R.id.key_n, R.id.key_m,
        )
        letterIds.forEach { id ->
            val btn = root.findViewById<Button>(id)
            val text = btn.text.toString()
            btn.setOnClickListener { onChar(text.first()) }
            bindLongPress(btn, alternatesFor(text))
        }

        val symbolIds = listOf(
            R.id.s1_1, R.id.s1_2, R.id.s1_3, R.id.s1_4, R.id.s1_5, R.id.s1_6, R.id.s1_7, R.id.s1_8, R.id.s1_9, R.id.s1_0,
            R.id.s1_at, R.id.s1_hash, R.id.s1_dollar, R.id.s1_percent, R.id.s1_amp, R.id.s1_minus, R.id.s1_plus,
            R.id.s1_lparen, R.id.s1_rparen, R.id.s1_star, R.id.s1_quote, R.id.s1_apos, R.id.s1_colon, R.id.s1_semi,
            R.id.s1_excl, R.id.s1_quest, R.id.s1_comma, R.id.s1_dot,
            R.id.s2_tilde, R.id.s2_grave, R.id.s2_pipe, R.id.s2_bullet, R.id.s2_sqrt, R.id.s2_pi, R.id.s2_div, R.id.s2_mul,
            R.id.s2_para, R.id.s2_delta, R.id.s2_pound, R.id.s2_cent, R.id.s2_euro, R.id.s2_yen, R.id.s2_caret, R.id.s2_deg,
            R.id.s2_eq, R.id.s2_lbrace, R.id.s2_rbrace, R.id.s2_bslash, R.id.s2_copy, R.id.s2_reg, R.id.s2_tm, R.id.s2_check,
            R.id.s2_lbrack, R.id.s2_rbrack, R.id.s2_lt, R.id.s2_gt,
        )
        symbolIds.forEach { id ->
            val btn = root.findViewById<Button>(id)
            btn.setOnClickListener { commitRaw(btn.text.toString()) }
        }

        val numIds = listOf(
            R.id.key_num_1, R.id.key_num_2, R.id.key_num_3, R.id.key_num_4, R.id.key_num_5,
            R.id.key_num_6, R.id.key_num_7, R.id.key_num_8, R.id.key_num_9, R.id.key_num_0,
        )
        numIds.forEach { id ->
            val btn = root.findViewById<Button>(id)
            val text = btn.text.toString()
            btn.setOnClickListener { commitRaw(text) }
            bindLongPress(btn, alternatesFor(text))
        }

        bindRepeat(R.id.key_backspace, ::onBackspace)
        bindRepeat(R.id.key_backspace_s1, ::onBackspace)
        bindRepeat(R.id.key_backspace_s2, ::onBackspace)

        listOf(R.id.key_space, R.id.key_space_s1, R.id.key_space_s2).forEach { bindClick(it) { onChar(' ') } }
        listOf(R.id.key_enter, R.id.key_enter_s1, R.id.key_enter_s2).forEach { bindClick(it, ::onEnter) }
        listOf(R.id.key_comma, R.id.key_dot).forEach { id -> bindClick(id) { onChar(root.findViewById<Button>(id).text.first()) } }

        bindClick(R.id.key_strip_diacritics, ::onStripDiacritics)
        bindClick(R.id.key_mode_symbols) { showMode(Mode.SYMBOLS1) }
        bindClick(R.id.key_mode_symbols2) { showMode(Mode.SYMBOLS2) }
        bindClick(R.id.key_mode_symbols1) { showMode(Mode.SYMBOLS1) }
        bindClick(R.id.key_mode_letters_s1) { showMode(Mode.LETTERS) }
        bindClick(R.id.key_mode_letters_s2) { showMode(Mode.LETTERS) }

        listOf(R.id.key_switch_ime, R.id.key_switch_ime_s1, R.id.key_switch_ime_s2).forEach { bindClick(it, ::switchKeyboard) }
    }

    private fun bindClick(id: Int, action: () -> Unit) {
        keyboardView?.findViewById<Button>(id)?.setOnClickListener { action() }
    }

    private fun bindRepeat(id: Int, action: () -> Unit) {
        keyboardView?.findViewById<Button>(id)?.setOnClickListener { action() }
    }

    private fun showMode(newMode: Mode) {
        mode = newMode
        panelLetters?.visibility = if (newMode == Mode.LETTERS) View.VISIBLE else View.GONE
        panelSymbols1?.visibility = if (newMode == Mode.SYMBOLS1) View.VISIBLE else View.GONE
        panelSymbols2?.visibility = if (newMode == Mode.SYMBOLS2) View.VISIBLE else View.GONE
    }

    private fun onChar(c: Char) {
        val ic = currentInputConnection ?: return
        if (mode != Mode.LETTERS) {
            commitRaw(c.toString())
            return
        }
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

    private fun commitRaw(text: String) {
        val ic = currentInputConnection ?: return
        vnEngine.commit(ic)
        ic.commitText(text, 1)
        updateSuggestions("")
    }

    private fun onBackspace() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.LETTERS && vnEngine.onBackspace(ic)) {
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

    private fun switchKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val token: IBinder? = window.window?.attributes?.token
        if (token != null && imm.shouldOfferSwitchingToNextInputMethod(token)) {
            imm.switchToNextInputMethod(token, false)
        } else {
            imm.showInputMethodPicker()
        }
    }

    private fun bindLongPress(anchor: View, alternates: List<String>) {
        if (alternates.isEmpty()) return
        anchor.setOnLongClickListener {
            showAlternates(anchor, alternates)
            true
        }
    }

    private fun showAlternates(anchor: View, alternates: List<String>) {
        altPopup?.dismiss()
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.key_bg)
            setPadding(8, 4, 8, 4)
        }
        alternates.forEach { alt ->
            val b = Button(this).apply {
                text = alt
                isAllCaps = false
                minWidth = 0
                minimumWidth = 0
                backgroundTintList = null
                setPadding(24, 12, 24, 12)
                setOnClickListener {
                    commitAlternate(alt)
                    altPopup?.dismiss()
                }
            }
            row.addView(b)
        }
        val popup = PopupWindow(row, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.isOutsideTouchable = true
        popup.elevation = 8f
        val loc = IntArray(2)
        anchor.getLocationInWindow(loc)
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, loc[0], loc[1] - anchor.height - 8)
        altPopup = popup
    }

    private fun commitAlternate(text: String) {
        if (mode != Mode.LETTERS) {
            commitRaw(text)
            return
        }
        // If it's a single letter-like char, feed through engine; else commit raw.
        if (text.length == 1) onChar(text[0]) else commitRaw(text)
    }

    private fun alternatesFor(key: String): List<String> = when (key.lowercase()) {
        "1" -> listOf("!", "¹", "½", "⅓")
        "2" -> listOf("@", "²", "⅔")
        "3" -> listOf("#", "³", "¾")
        "4" -> listOf("$", "⁴")
        "5" -> listOf("%", "⁵")
        "6" -> listOf("^", "⁶")
        "7" -> listOf("&", "⁷")
        "8" -> listOf("*", "⁸")
        "9" -> listOf("(", "⁹")
        "0" -> listOf(")", "⁰")
        "a" -> listOf("á", "à", "ả", "ã", "ạ", "ă", "â")
        "e" -> listOf("é", "è", "ẻ", "ẽ", "ẹ", "ê", "2")
        "i" -> listOf("í", "ì", "ỉ", "ĩ", "ị", "3")
        "o" -> listOf("ó", "ò", "ỏ", "õ", "ọ", "ô", "ơ", "4")
        "u" -> listOf("ú", "ù", "ủ", "ũ", "ụ", "ư", "5")
        "y" -> listOf("ý", "ỳ", "ỷ", "ỹ", "ỵ", "6")
        "d" -> listOf("đ", "7")
        "q" -> listOf("1")
        "w" -> listOf("2")
        "r" -> listOf("4")
        "t" -> listOf("5")
        "p" -> listOf("0")
        "s" -> listOf("ß", "š")
        "g" -> listOf("8")
        "h" -> listOf("9")
        "j" -> listOf("&")
        "k" -> listOf("(")
        "l" -> listOf(")")
        "z" -> listOf("*")
        "x" -> listOf("\"")
        "c" -> listOf("'")
        "v" -> listOf(":")
        "b" -> listOf(";")
        "n" -> listOf("!")
        "m" -> listOf("?")
        else -> emptyList()
    }

    private fun updateSuggestions(prefix: String) {
        val bar = suggestionBar ?: return
        bar.removeAllViews()
        val suggestions = if (mode == Mode.LETTERS) suggestionEngine.suggest(prefix) else emptyList()
        suggestions.forEach { word ->
            val tv = TextView(this).apply {
                text = word
                setPadding(28, 16, 28, 16)
                setTextColor(getColor(R.color.key_text))
                textSize = 16f
                setOnClickListener { acceptSuggestion(word) }
            }
            bar.addView(tv)
        }
    }

    private fun acceptSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        vnEngine.reset(ic)
        val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
        val prefix = before.takeLastWhile { it.isLetter() }
        if (prefix.isNotEmpty()) ic.deleteSurroundingText(prefix.length, 0)
        ic.commitText("$word ", 1)
        suggestionEngine.learn(word)
        updateSuggestions("")
    }

    private fun isMacroTrigger(c: Char): Boolean = c.isWhitespace() || c in ".,;:!?\n"
}
