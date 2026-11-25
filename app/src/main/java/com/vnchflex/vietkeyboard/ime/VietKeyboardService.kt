package com.vnchflex.vietkeyboard.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.IBinder
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.vnchflex.vietkeyboard.R
import com.vnchflex.vietkeyboard.engine.VietnameseEngine
import com.vnchflex.vietkeyboard.macro.MacroEngine
import com.vnchflex.vietkeyboard.macro.MacroStore
import com.vnchflex.vietkeyboard.settings.KeyboardPreferences
import com.vnchflex.vietkeyboard.settings.SettingsActivity
import com.vnchflex.vietkeyboard.suggestion.SuggestionEngine

class VietKeyboardService : InputMethodService() {
    private enum class Mode { LETTERS, SYMBOLS1, SYMBOLS2 }
    private enum class ShiftState { LOWER, SHIFTED, CAPS_LOCK }

    private lateinit var prefs: KeyboardPreferences
    private lateinit var vnEngine: VietnameseEngine
    private lateinit var macroEngine: MacroEngine
    private lateinit var macroStore: MacroStore
    private lateinit var suggestionEngine: SuggestionEngine

    private var keyboardView: View? = null
    private var topStripContainer: View? = null
    private var suggestionBar: LinearLayout? = null
    private var rowNumbers: View? = null
    private var panelLetters: View? = null
    private var panelSymbols1: View? = null
    private var panelSymbols2: View? = null

    private var mode = Mode.LETTERS
    private var shiftState = ShiftState.LOWER
    private var lastShiftClickTime = 0L
    private var lastSpaceClickTime = 0L
    private var prevCommittedWord2 = ""
    private var prevCommittedWord1 = ""

    private val letterButtons = mutableMapOf<GboardKeyButton, String>()

    override fun onCreate() {
        super.onCreate()
        prefs = KeyboardPreferences(this)
        vnEngine = VietnameseEngine()
        macroStore = MacroStore(this)
        macroEngine = MacroEngine(macroStore.load())
        suggestionEngine = SuggestionEngine(this)
        syncPreferencesToEngines()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView = view
        topStripContainer = view.findViewById(R.id.top_strip_container)
        suggestionBar = view.findViewById(R.id.suggestion_bar)
        rowNumbers = view.findViewById(R.id.row_numbers)
        panelLetters = view.findViewById(R.id.panel_letters)
        panelSymbols1 = view.findViewById(R.id.panel_symbols1)
        panelSymbols2 = view.findViewById(R.id.panel_symbols2)
        bindKeys(view)
        applyPreferencesToView()
        showMode(Mode.LETTERS)
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        syncPreferencesToEngines()
        applyPreferencesToView()
        updateAutoCapitalization()
        updateSuggestions(vnEngine.composingText)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        syncPreferencesToEngines()
        vnEngine.reset()
        updateSuggestions("")
        showMode(Mode.LETTERS)
    }

    private fun syncPreferencesToEngines() {
        macroEngine.update(macroStore.load())
        vnEngine.freeTonePlacement = prefs.freeTonePlacement
        vnEngine.modernOrthography = prefs.modernOrthography
        vnEngine.quickTelex = prefs.quickTelex
        vnEngine.relaxedCoda = prefs.relaxedCoda
        vnEngine.southernDialectAndGenZ = prefs.southernDialectAndGenZ
    }

    private fun applyPreferencesToView() {
        val root = keyboardView ?: return

        // 1. Gboard Number row option: Pure numbers 1..0 only at the top row
        rowNumbers?.visibility = if (prefs.showNumberRow) View.VISIBLE else View.GONE

        // 2. Suggestion strip visibility
        topStripContainer?.visibility = if (prefs.showSuggestionStrip) View.VISIBLE else View.GONE

        // 3. Language switch key & Emoji switch key options
        val switchVisibility = if (prefs.showLanguageSwitchKey) View.VISIBLE else View.GONE
        listOf(R.id.key_switch_ime, R.id.key_switch_ime_s1, R.id.key_switch_ime_s2).forEach { id ->
            root.findViewById<View>(id)?.visibility = switchVisibility
        }
        root.findViewById<View>(R.id.key_emoji)?.visibility =
            if (prefs.showEmojiSwitchKey) View.VISIBLE else View.GONE

        // 4. Spacebar label
        listOf(R.id.key_space, R.id.key_space_s1, R.id.key_space_s2).forEach { id ->
            root.findViewById<Button>(id)?.text = getString(R.string.key_space)
        }

        // 5. Theme (Gboard Light vs Dark)
        val isDark = prefs.themeMode == "gboard_dark"
        root.setBackgroundColor(getColor(if (isDark) R.color.keyboard_bg_dark else R.color.keyboard_bg))

        // 6. Refresh letter labels (secondary symbols shown ONLY on letter keys)
        refreshLetterKeyLabels()
    }

    private fun bindKeys(root: View) {
        letterButtons.clear()
        val letterIds = listOf(
            R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t, R.id.key_y,
            R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
            R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g, R.id.key_h,
            R.id.key_j, R.id.key_k, R.id.key_l,
            R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b, R.id.key_n, R.id.key_m,
        )
        letterIds.forEach { id ->
            val btn = root.findViewById<GboardKeyButton>(id)
            val baseLetter = btn.text.toString().lowercase()
            letterButtons[btn] = baseLetter

            btn.setOnClickListener {
                performKeyFeedback(btn)
                val ch = if (shiftState != ShiftState.LOWER) baseLetter.uppercase()[0] else baseLetter[0]
                onChar(ch)
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.LOWER
                    refreshLetterKeyLabels()
                }
            }
            btn.setOnLongClickListener {
                if (!prefs.longPressForSymbols) return@setOnLongClickListener false
                val sec = secondarySymbolForLetter(baseLetter) ?: return@setOnLongClickListener false
                performKeyFeedback(btn, isLongPress = true)
                onSecondarySymbol(sec)
                true
            }
        }

        // Gboard Top Number Row: PURE DIGITS ONLY (1..0), no secondary symbol badge or popup
        val numIds = listOf(
            R.id.key_num_1, R.id.key_num_2, R.id.key_num_3, R.id.key_num_4, R.id.key_num_5,
            R.id.key_num_6, R.id.key_num_7, R.id.key_num_8, R.id.key_num_9, R.id.key_num_0,
        )
        numIds.forEach { id ->
            val btn = root.findViewById<Button>(id)
            val digit = btn.text.toString()
            btn.text = digit // Pure number only
            btn.setOnClickListener {
                performKeyFeedback(btn)
                onDigitOrSymbol(digit)
            }
            btn.setOnLongClickListener(null)
            btn.isLongClickable = false
        }

        val symbolIds = listOf(
            R.id.s1_1, R.id.s1_2, R.id.s1_3, R.id.s1_4, R.id.s1_5, R.id.s1_6, R.id.s1_7, R.id.s1_8, R.id.s1_9, R.id.s1_0,
            R.id.s1_at, R.id.s1_hash, R.id.s1_dollar, R.id.s1_underscore, R.id.s1_amp, R.id.s1_minus, R.id.s1_plus,
            R.id.s1_lparen, R.id.s1_rparen, R.id.s1_slash, R.id.s1_star, R.id.s1_quote, R.id.s1_apos, R.id.s1_colon, R.id.s1_semi,
            R.id.s1_excl, R.id.s1_quest, R.id.s1_comma, R.id.s1_dot, R.id.s1_percent,
            R.id.s2_tilde, R.id.s2_grave, R.id.s2_pipe, R.id.s2_bullet, R.id.s2_sqrt, R.id.s2_pi, R.id.s2_div, R.id.s2_mul,
            R.id.s2_para, R.id.s2_delta, R.id.s2_pound, R.id.s2_cent, R.id.s2_euro, R.id.s2_yen, R.id.s2_caret, R.id.s2_deg,
            R.id.s2_eq, R.id.s2_lbrace, R.id.s2_rbrace, R.id.s2_bslash, R.id.s2_copy, R.id.s2_reg, R.id.s2_tm, R.id.s2_check,
            R.id.s2_lbrack, R.id.s2_rbrack, R.id.s2_lt, R.id.s2_gt,
        )
        symbolIds.forEach { id ->
            val btn = root.findViewById<Button>(id) ?: return@forEach
            btn.setOnClickListener {
                performKeyFeedback(btn)
                commitRaw(btn.text.toString())
            }
        }

        bindClick(R.id.key_shift, ::onShiftPressed)
        bindClick(R.id.key_emoji) { commitRaw("😊") }

        bindRepeat(R.id.key_backspace, ::onBackspace)
        bindRepeat(R.id.key_backspace_s1, ::onBackspace)
        bindRepeat(R.id.key_backspace_s2, ::onBackspace)

        listOf(R.id.key_space, R.id.key_space_s1, R.id.key_space_s2).forEach { bindClick(it, ::onSpacePressed) }
        listOf(R.id.key_enter, R.id.key_enter_s1, R.id.key_enter_s2).forEach { bindClick(it, ::onEnter) }
        listOf(R.id.key_comma, R.id.key_dot).forEach { id ->
            bindClick(id) { onPunctuation(root.findViewById<Button>(id).text.first()) }
        }

        // Toolbar quick actions
        bindClick(R.id.key_strip_diacritics, ::onStripDiacritics)
        bindClick(R.id.btn_toolbar_macro) { openSettingsSection("macro") }
        bindClick(R.id.btn_toolbar_clipboard, ::onPasteClipboard)
        bindClick(R.id.btn_toolbar_settings) { openSettingsSection(null) }

        bindClick(R.id.key_mode_symbols) { showMode(Mode.SYMBOLS1) }
        bindClick(R.id.key_mode_symbols2) { showMode(Mode.SYMBOLS2) }
        bindClick(R.id.key_mode_symbols1) { showMode(Mode.SYMBOLS1) }
        bindClick(R.id.key_mode_letters_s1) { showMode(Mode.LETTERS) }
        bindClick(R.id.key_mode_letters_s2) { showMode(Mode.LETTERS) }

        listOf(R.id.key_switch_ime, R.id.key_switch_ime_s1, R.id.key_switch_ime_s2).forEach {
            bindClick(it, ::switchKeyboard)
        }
    }

    private fun refreshLetterKeyLabels() {
        val showSymbols = prefs.longPressForSymbols
        letterButtons.forEach { (btn, base) ->
            val displayPrimary = if (shiftState != ShiftState.LOWER) base.uppercase() else base.lowercase()
            val sec = if (showSymbols) secondarySymbolForLetter(base) else null
            applyLetterKeyLabel(btn, displayPrimary, sec)
        }
        val shiftBtn = keyboardView?.findViewById<Button>(R.id.key_shift)
        shiftBtn?.text = when (shiftState) {
            ShiftState.LOWER -> "⇧"
            ShiftState.SHIFTED -> "⬆"
            ShiftState.CAPS_LOCK -> "⇪"
        }
    }

    private fun onShiftPressed() {
        val now = System.currentTimeMillis()
        shiftState = when (shiftState) {
            ShiftState.LOWER -> ShiftState.SHIFTED
            ShiftState.SHIFTED -> if (now - lastShiftClickTime < 400L) ShiftState.CAPS_LOCK else ShiftState.LOWER
            ShiftState.CAPS_LOCK -> ShiftState.LOWER
        }
        lastShiftClickTime = now
        refreshLetterKeyLabels()
    }

    private fun updateAutoCapitalization() {
        if (!prefs.autoCapitalization || shiftState == ShiftState.CAPS_LOCK) return
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
        val trimmed = before.trimEnd()
        val shouldCap = trimmed.isEmpty() || trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?") || before.endsWith("\n")
        shiftState = if (shouldCap) ShiftState.SHIFTED else ShiftState.LOWER
        refreshLetterKeyLabels()
    }

    private fun bindClick(id: Int, action: () -> Unit) {
        keyboardView?.findViewById<View>(id)?.setOnClickListener { view ->
            performKeyFeedback(view)
            action()
        }
    }

    private fun bindRepeat(id: Int, action: () -> Unit) {
        val view = keyboardView?.findViewById<View>(id) ?: return
        view.setOnClickListener {
            performKeyFeedback(view)
            action()
        }
        view.setOnLongClickListener {
            // Gboard gesture/long-press delete whole word
            performKeyFeedback(view, isLongPress = true)
            deleteWordBeforeCursor()
            true
        }
    }

    private fun showMode(newMode: Mode) {
        mode = newMode
        panelLetters?.visibility = if (newMode == Mode.LETTERS) View.VISIBLE else View.GONE
        panelSymbols1?.visibility = if (newMode == Mode.SYMBOLS1) View.VISIBLE else View.GONE
        panelSymbols2?.visibility = if (newMode == Mode.SYMBOLS2) View.VISIBLE else View.GONE
    }

    private fun onDigitOrSymbol(text: String) {
        commitRaw(text)
    }

    private fun onSecondarySymbol(symbol: String) {
        val ic = currentInputConnection ?: return
        // Nếu ký hiệu phụ là chữ cái tiếng Việt (vd: phím D có ký hiệu phụ 'đ'/'Đ' chuẩn Gboard Tiếng Việt),
        // tự động khớp chữ HOA/thường theo trạng thái Shift và đưa vào bộ gõ.
        if (symbol.length == 1 && symbol[0].isLetter()) {
            val ch = if (shiftState != ShiftState.LOWER) symbol.uppercase()[0] else symbol.lowercase()[0]
            onChar(ch)
            if (shiftState == ShiftState.SHIFTED) {
                shiftState = ShiftState.LOWER
                refreshLetterKeyLabels()
            }
            return
        }
        commitRaw(symbol)
    }

    private fun onSpacePressed() {
        val ic = currentInputConnection ?: return
        val now = System.currentTimeMillis()
        // Gboard Double-space period option
        if (prefs.doubleSpacePeriod && now - lastSpaceClickTime < 350L && !vnEngine.isComposing) {
            val before = ic.getTextBeforeCursor(2, 0)?.toString().orEmpty()
            if (before.length == 2 && before[1] == ' ' && before[0].isLetterOrDigit()) {
                ic.beginBatchEdit()
                ic.deleteSurroundingText(1, 0)
                ic.commitText(". ", 1)
                ic.endBatchEdit()
                lastSpaceClickTime = 0L
                updateAutoCapitalization()
                updateSuggestions("")
                return
            }
        }
        lastSpaceClickTime = now
        onChar(' ')
    }

    private fun onPunctuation(c: Char) {
        onChar(c)
        if (prefs.autoSpaceAfterPunctuation && (c == ',' || c == '.' || c == '!' || c == '?')) {
            currentInputConnection?.commitText(" ", 1)
        }
        updateAutoCapitalization()
    }

    private fun onChar(c: Char) {
        val ic = currentInputConnection ?: return
        if (mode != Mode.LETTERS) {
            commitRaw(c.toString())
            return
        }
        if (isMacroTrigger(c)) {
            val finishedWord = vnEngine.composingText
            vnEngine.commit(ic)
            if (finishedWord.isNotEmpty()) {
                recordCommittedWord(finishedWord)
            }
            if (prefs.macroEnabled && macroEngine.tryExpand(ic, c)) {
                syncContextFromEditor()
                updateAutoCapitalization()
                updateSuggestions("")
                return
            }
            ic.commitText(c.toString(), 1)
            updateAutoCapitalization()
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
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun onBackspace() {
        val ic = currentInputConnection ?: return
        if (mode == Mode.LETTERS && vnEngine.onBackspace(ic)) {
            updateSuggestions(vnEngine.composingText)
            return
        }
        ic.deleteSurroundingText(1, 0)
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun deleteWordBeforeCursor() {
        val ic = currentInputConnection ?: return
        if (vnEngine.isComposing) {
            vnEngine.reset(ic)
            updateSuggestions("")
            return
        }
        val before = ic.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        if (before.isEmpty()) return
        val trailingSpaces = before.takeLastWhile { it.isWhitespace() }.length
        val wordLen = before.dropLast(trailingSpaces).takeLastWhile { !it.isWhitespace() }.length
        val totalDelete = (trailingSpaces + wordLen).coerceAtLeast(1)
        ic.deleteSurroundingText(totalDelete, 0)
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun onEnter() {
        val ic = currentInputConnection ?: return
        vnEngine.commit(ic)
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun onStripDiacritics() {
        val ic = currentInputConnection ?: return
        vnEngine.removeDiacritics(ic)
        updateSuggestions(vnEngine.composingText)
    }

    private fun onPasteClipboard() {
        if (!prefs.enableClipboardHistory) return
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = cm.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
            if (text.isNotEmpty()) commitRaw(text)
        }
    }

    private fun openSettingsSection(section: String?) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (section != null) putExtra("section", section)
        }
        startActivity(intent)
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

    private fun performKeyFeedback(view: View, isLongPress: Boolean = false) {
        if (prefs.hapticOnKeypress) {
            val flag = if (isLongPress) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.KEYBOARD_TAP
            view.performHapticFeedback(flag)
        }
        if (prefs.soundOnKeypress) {
            val am = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val vol = (prefs.soundVolume / 100f).coerceIn(0f, 1f)
            am?.playSoundEffect(AudioManager.FX_KEY_CLICK, vol)
        }
    }

    /**
     * Chỉ phím chữ mới có symbol phụ ở góc trên bên phải (vẽ bằng GboardKeyButton,
     * chữ cái chính luôn nằm chính giữa phím, không bao giờ bị tụt hay bị cắt).
     */
    private fun applyLetterKeyLabel(btn: GboardKeyButton, primary: String, secondary: String?) {
        btn.text = primary
        btn.secondarySymbol = secondary
    }

    /**
     * Bảng ký hiệu phụ chuẩn Gboard Tiếng Việt dành riêng cho 26 phím chữ cái:
     * - Khi đã bật Hàng phím số (showNumberRow = true), hàng Q..P hiển thị bộ ký hiệu Gboard: % \ | = [ ] < > { }
     * - Khi tắt Hàng phím số (showNumberRow = false), hàng Q..P hiển thị số: 1 2 3 4 5 6 7 8 9 0
     * - Hàng A..L: @ # đ _ & - + ( )
     * - Hàng Z..M: * " ' : ; ! ?
     */
    private fun secondarySymbolForLetter(letterKey: String): String? = when (letterKey.lowercase()) {
        // Hàng chữ trên (QWERTYUIOP)
        "q" -> if (prefs.showNumberRow) "%" else "1"
        "w" -> if (prefs.showNumberRow) "\\" else "2"
        "e" -> if (prefs.showNumberRow) "|" else "3"
        "r" -> if (prefs.showNumberRow) "=" else "4"
        "t" -> if (prefs.showNumberRow) "[" else "5"
        "y" -> if (prefs.showNumberRow) "]" else "6"
        "u" -> if (prefs.showNumberRow) "<" else "7"
        "i" -> if (prefs.showNumberRow) ">" else "8"
        "o" -> if (prefs.showNumberRow) "{" else "9"
        "p" -> if (prefs.showNumberRow) "}" else "0"
        // Hàng chữ giữa (ASDFGHJKL) - chuẩn Gboard Tiếng Việt (D -> đ, F -> _)
        "a" -> "@"
        "s" -> "#"
        "d" -> if (shiftState != ShiftState.LOWER) "Đ" else "đ"
        "f" -> "_"
        "g" -> "&"
        "h" -> "-"
        "j" -> "+"
        "k" -> "("
        "l" -> ")"
        // Hàng chữ dưới (ZXCVBNM)
        "z" -> "*"
        "x" -> "\""
        "c" -> "'"
        "v" -> ":"
        "b" -> ";"
        "n" -> "!"
        "m" -> "?"
        else -> null
    }

    /**
     * Gboard 3-slot suggestion strip with Trigram Next-Word & Context-Aware Word Suggestions.
     */
    private fun updateSuggestions(prefix: String) {
        val bar = suggestionBar ?: return
        bar.removeAllViews()
        if (!prefs.showSuggestionStrip || mode != Mode.LETTERS) return

        syncContextFromEditor()

        val macroPreview = if (prefs.macroEnabled && prefix.isNotEmpty()) {
            macroEngine.lookupPreview(prefix)
        } else null

        val suggestions = when {
            prefix.isNotEmpty() -> suggestionEngine.suggest(
                prefix = prefix,
                prevWord1 = prevCommittedWord1,
                prevWord2 = prevCommittedWord2,
                limit = 3,
                includeEmoji = prefs.showEmojiSuggestions,
                macroPreview = macroPreview,
            )
            prefs.nextWordSuggestions -> suggestionEngine.suggestNextWord(
                prevWord1 = prevCommittedWord1,
                prevWord2 = prevCommittedWord2,
                limit = 3,
            )
            else -> emptyList()
        }

        suggestions.forEachIndexed { index, word ->
            if (index > 0) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(2, 42)
                    setBackgroundColor(getColor(R.color.strip_divider))
                }
                bar.addView(divider)
            }
            val isPrimaryCenter = (index == 1 && prefix.isNotEmpty()) || (macroPreview != null && word == macroPreview)
            val tv = TextView(this).apply {
                text = word
                gravity = Gravity.CENTER
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                setTextColor(
                    if (isPrimaryCenter) getColor(R.color.suggestion_primary)
                    else getColor(R.color.key_text)
                )
                setTypeface(typeface, if (isPrimaryCenter) Typeface.BOLD else Typeface.NORMAL)
                textSize = 15f
                setOnClickListener {
                    performKeyFeedback(this)
                    acceptSuggestion(word)
                }
            }
            bar.addView(tv)
        }
    }

    private fun acceptSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val hadComposing = vnEngine.isComposing
        vnEngine.reset(ic)
        if (!hadComposing) {
            val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
            val prefix = before.takeLastWhile { it.isLetter() }
            if (prefix.isNotEmpty()) ic.deleteSurroundingText(prefix.length, 0)
        }
        ic.commitText("$word ", 1)
        recordCommittedWord(word)
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun recordCommittedWord(word: String) {
        val clean = word.trim()
        if (clean.isEmpty()) return
        val tokens = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
        for (token in tokens) {
            suggestionEngine.learn(token, prevWord1 = prevCommittedWord1, prevWord2 = prevCommittedWord2)
            prevCommittedWord2 = prevCommittedWord1
            prevCommittedWord1 = token
        }
    }

    private fun syncContextFromEditor() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        val committedPart = if (vnEngine.isComposing && before.endsWith(vnEngine.composingText)) {
            before.dropLast(vnEngine.composingText.length)
        } else {
            before
        }
        val tokens = committedPart
            .trim()
            .split(Regex("[\\s.,;:!?()\\[\\]\"']+"))
            .filter { it.isNotEmpty() }
        prevCommittedWord1 = tokens.getOrNull(tokens.size - 1).orEmpty()
        prevCommittedWord2 = tokens.getOrNull(tokens.size - 2).orEmpty()
    }

    private fun isMacroTrigger(c: Char): Boolean = c.isWhitespace() || c in ".,;:!?\n"
}
