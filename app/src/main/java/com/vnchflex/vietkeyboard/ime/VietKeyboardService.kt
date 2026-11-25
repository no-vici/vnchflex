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
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
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
    private var glideOverlay: GlideTypingOverlayView? = null
    private var rowNumbers: View? = null
    private var panelLetters: View? = null
    private var panelSymbols1: View? = null
    private var panelSymbols2: View? = null

    private var mode = Mode.LETTERS
    private var isVietnameseMode = true
    private val enComposing = StringBuilder()
    private val clipboardHistory = ArrayDeque<String>()
    private var clipboardManager: ClipboardManager? = null
    private var shiftState = ShiftState.LOWER
    private var lastShiftClickTime = 0L
    private var lastSpaceClickTime = 0L
    private var lastBackspaceClickTime = 0L
    private var backspaceRepeating = false
    private var lastGlideCommittedLength = 0
    private var prevCommittedWord2 = ""
    private var prevCommittedWord1 = ""
    private var keyPopup: PopupWindow? = null
    private var keyPopupText: TextView? = null
    private var audioManager: AudioManager? = null
    private val popupHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var pendingPopupAnchor: View? = null
    private var pendingPopupLabel: String = ""
    private val showPopupRunnable = Runnable {
        val anchor = pendingPopupAnchor ?: return@Runnable
        if (anchor.isAttachedToWindow) showKeyPopup(anchor, pendingPopupLabel)
    }

    private var altPopup: PopupWindow? = null
    private var altPopupRow: LinearLayout? = null
    private val altButtons = mutableListOf<TextView>()
    private var altOptions: List<String> = emptyList()
    private var altSelectedIndex = -1
    private var altAnchor: View? = null

    private val suggestionSlots = mutableListOf<TextView>()
    private val suggestionDividers = mutableListOf<View>()
    private var lastSuggestionKey: String? = null

    private var lastAppliedPrefsKey: String? = null
    private val letterButtons = mutableMapOf<GboardKeyButton, String>()

    override fun onCreate() {
        super.onCreate()
        prefs = KeyboardPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        vnEngine = VietnameseEngine()
        macroStore = MacroStore(this)
        macroEngine = MacroEngine(macroStore.load())
        suggestionEngine = SuggestionEngine(this)
        syncPreferencesToEngines()
        clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also { cm ->
            cm.addPrimaryClipChangedListener {
                if (!prefs.enableClipboardHistory) return@addPrimaryClipChangedListener
                val clip = cm.primaryClip ?: return@addPrimaryClipChangedListener
                if (clip.itemCount > 0) {
                    val text = clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
                    if (text.isNotBlank()) {
                        clipboardHistory.remove(text)
                        clipboardHistory.addFirst(text)
                        while (clipboardHistory.size > 15) clipboardHistory.removeLast()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::suggestionEngine.isInitialized) suggestionEngine.flushPendingSaves()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardView = view
        topStripContainer = view.findViewById(R.id.top_strip_container)
        suggestionBar = view.findViewById(R.id.suggestion_bar)
        glideOverlay = view.findViewById(R.id.glide_overlay)
        rowNumbers = view.findViewById(R.id.row_numbers)
        panelLetters = view.findViewById(R.id.panel_letters)
        panelSymbols1 = view.findViewById(R.id.panel_symbols1)
        panelSymbols2 = view.findViewById(R.id.panel_symbols2)
        suggestionSlots.clear()
        suggestionDividers.clear()
        lastSuggestionKey = null
        lastAppliedPrefsKey = null
        keyPopup?.dismiss()
        keyPopup = null
        keyPopupText = null
        altPopup?.dismiss()
        altPopup = null
        altPopupRow = null
        altButtons.clear()
        altOptions = emptyList()
        altSelectedIndex = -1
        altAnchor = null
        bindKeys(view)
        setupGlideTyping()
        initSuggestionSlots()
        applyPreferencesToView()
        showMode(Mode.LETTERS)
        return view
    }

    private fun setupGlideTyping() {
        glideOverlay?.registerLetterKeys(letterButtons)
        glideOverlay?.onGlideStart = {
            dismissKeyPopup()
            dismissAltPopup()
        }
        glideOverlay?.onGlideComplete = { swipedKeys ->
            dismissKeyPopup()
            val ic = currentInputConnection
            if (ic != null && mode == Mode.LETTERS) {
                vnEngine.commit(ic)
                val candidates = GlideDecoder.decode(
                    swipedKeys = swipedKeys,
                    suggestionEngine = suggestionEngine,
                    prevWord1 = prevCommittedWord1,
                    prevWord2 = prevCommittedWord2,
                    limit = 3,
                    isVietnameseMode = isVietnameseMode
                )
                val best = candidates.firstOrNull()
                if (best != null) {
                    val out = if (shiftState != ShiftState.LOWER) {
                        best.replaceFirstChar { it.uppercaseChar() }
                    } else best
                    val committedText = "$out "
                    ic.commitText(committedText, 1)
                    lastGlideCommittedLength = committedText.length
                    recordCommittedWord(out)
                    if (shiftState == ShiftState.SHIFTED) {
                        shiftState = ShiftState.LOWER
                        refreshLetterKeyLabels()
                    }
                    updateAutoCapitalization()
                    renderGlideCandidates(candidates)
                }
            }
        }
    }

    private fun renderGlideCandidates(candidates: List<String>) {
        if (!prefs.showSuggestionStrip || mode != Mode.LETTERS) return
        if (suggestionSlots.isEmpty()) initSuggestionSlots()
        lastSuggestionKey = "GLIDE:" + candidates.joinToString("\u0002")
        for (i in 0 until 3) {
            val tv = suggestionSlots[i]
            val word = candidates.getOrNull(i)
            if (word == null) {
                tv.visibility = View.INVISIBLE
                tv.text = ""
                tv.setOnClickListener(null)
            } else {
                val cased = if (shiftState == ShiftState.CAPS_LOCK) word.uppercase() else word
                tv.text = cased
                tv.setTextColor(if (i == 0) getColor(R.color.suggestion_primary) else getColor(R.color.key_text))
                tv.setTypeface(null, if (i == 0) Typeface.BOLD else Typeface.NORMAL)
                tv.setOnClickListener {
                    performKeyFeedback(tv)
                    replaceLastGlideWordOrAccept(cased)
                }
                tv.visibility = View.VISIBLE
            }
        }
        suggestionDividers.forEachIndexed { idx, div ->
            val leftVisible = suggestionSlots.getOrNull(idx)?.visibility == View.VISIBLE
            val rightVisible = suggestionSlots.getOrNull(idx + 1)?.visibility == View.VISIBLE
            div.visibility = if (leftVisible && rightVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun replaceLastGlideWordOrAccept(word: String) {
        val ic = currentInputConnection ?: return
        if (lastGlideCommittedLength > 0) {
            ic.beginBatchEdit()
            ic.deleteSurroundingText(lastGlideCommittedLength, 0)
            val newCommitted = "$word "
            ic.commitText(newCommitted, 1)
            ic.endBatchEdit()
            lastGlideCommittedLength = newCommitted.length
            recordCommittedWord(word)
            updateAutoCapitalization()
            updateSuggestions("")
            return
        }
        acceptSuggestion(word)
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
        val prefsKey = listOf(
            prefs.showNumberRow, prefs.showSuggestionStrip, prefs.showLanguageSwitchKey,
            prefs.showEmojiSwitchKey, prefs.themeMode, prefs.oneHandedMode, prefs.keyboardHeight,
            prefs.longPressForSymbols, prefs.enableGlideTyping, prefs.showGestureTrail,
            isVietnameseMode, shiftState.name
        ).joinToString("|")
        if (prefsKey == lastAppliedPrefsKey) return
        lastAppliedPrefsKey = prefsKey

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

        // 4. Spacebar label shows active language (Tiếng Việt / English)
        val spaceLabel = if (isVietnameseMode) getString(R.string.key_space) else "English"
        listOf(R.id.key_space, R.id.key_space_s1, R.id.key_space_s2).forEach { id ->
            root.findViewById<Button>(id)?.text = spaceLabel
        }

        // 5. Theme (Gboard Light vs Dark)
        val isDark = prefs.themeMode == "gboard_dark"
        root.setBackgroundColor(getColor(if (isDark) R.color.keyboard_bg_dark else R.color.keyboard_bg))

        // 6. One-handed mode & Keyboard height
        val density = resources.displayMetrics.density
        val sidePad = (resources.displayMetrics.widthPixels * 0.16f).toInt()
        val baseHPad = (3 * density).toInt()
        val topPad = (3 * density).toInt()
        val bottomPad = (6 * density).toInt()
        when (prefs.oneHandedMode) {
            "left" -> root.setPadding(baseHPad, topPad, sidePad, bottomPad)
            "right" -> root.setPadding(sidePad, topPad, baseHPad, bottomPad)
            else -> root.setPadding(baseHPad, topPad, baseHPad, bottomPad)
        }
        val scale = prefs.keyHeightScale()
        val letterH = (46 * density * scale).toInt()
        val numH = (40 * density * scale).toInt()
        letterButtons.keys.forEach { it.layoutParams = it.layoutParams.apply { height = letterH } }
        listOf(
            R.id.key_num_1, R.id.key_num_2, R.id.key_num_3, R.id.key_num_4, R.id.key_num_5,
            R.id.key_num_6, R.id.key_num_7, R.id.key_num_8, R.id.key_num_9, R.id.key_num_0,
        ).forEach { id ->
            root.findViewById<View>(id)?.let { it.layoutParams = it.layoutParams.apply { height = numH } }
        }

        // 7. Refresh letter labels & glide overlay settings
        glideOverlay?.glideEnabled = prefs.enableGlideTyping
        glideOverlay?.showTrail = prefs.showGestureTrail
        glideOverlay?.registerLetterKeys(letterButtons)
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
            var longPressHandled = false
            var downX = 0f
            var downY = 0f
            val longPressRunnable = Runnable {
                if (!prefs.longPressForSymbols) return@Runnable
                val alts = alternatesForLetter(baseLetter)
                if (alts.isEmpty()) return@Runnable
                performKeyFeedback(btn, isLongPress = true)
                if (alts.size == 1) {
                    dismissKeyPopup()
                    showKeyPopup(btn, alts[0])
                    onSecondarySymbol(alts[0])
                    longPressHandled = true
                } else {
                    showAlternatesPopup(btn, alts)
                    longPressHandled = true
                }
            }

            btn.setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        longPressHandled = false
                        downX = event.x
                        downY = event.y
                        if (prefs.popupOnKeypress) scheduleKeyPopup(v, btn.text.toString())
                        popupHandler.removeCallbacks(longPressRunnable)
                        popupHandler.postDelayed(longPressRunnable, prefs.longPressDelayMs.toLong().coerceAtLeast(150L))
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (altPopup?.isShowing == true && altAnchor === v) {
                            updateAltSelectionFromTouch(v, event)
                            return@setOnTouchListener true
                        }
                        if (!longPressHandled && (kotlin.math.abs(event.x - downX) > 24f || kotlin.math.abs(event.y - downY) > 24f)) {
                            popupHandler.removeCallbacks(longPressRunnable)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        popupHandler.removeCallbacks(longPressRunnable)
                        if (altPopup?.isShowing == true && altAnchor === v) {
                            commitAltSelection()
                            dismissKeyPopup()
                            return@setOnTouchListener true
                        }
                        dismissKeyPopup()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        popupHandler.removeCallbacks(longPressRunnable)
                        dismissAltPopup()
                        dismissKeyPopup()
                    }
                }
                false
            }
            btn.setOnClickListener {
                if (longPressHandled) {
                    longPressHandled = false
                    return@setOnClickListener
                }
                dismissKeyPopup()
                performKeyFeedback(btn)
                val ch = if (shiftState != ShiftState.LOWER) baseLetter.uppercase()[0] else baseLetter[0]
                onChar(ch)
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.LOWER
                    refreshLetterKeyLabels()
                }
            }
            btn.setOnLongClickListener(null)
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
            btn.setOnTouchListener { v, event ->
                if (prefs.popupOnKeypress) {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> scheduleKeyPopup(v, btn.text.toString())
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dismissKeyPopup()
                    }
                }
                false
            }
            btn.setOnClickListener {
                dismissKeyPopup()
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
            btn.setOnTouchListener { v, event ->
                if (prefs.popupOnKeypress) {
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> scheduleKeyPopup(v, btn.text.toString())
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dismissKeyPopup()
                    }
                }
                false
            }
            btn.setOnClickListener {
                dismissKeyPopup()
                performKeyFeedback(btn)
                commitRaw(btn.text.toString())
            }
        }

        bindClick(R.id.key_shift, ::onShiftPressed)
        bindClick(R.id.key_emoji) { commitRaw("😊") }
        root.findViewById<View>(R.id.key_emoji)?.setOnLongClickListener { emojiBtn ->
            performKeyFeedback(emojiBtn, isLongPress = true)
            showEmojiPickerPopup(emojiBtn)
            true
        }

        bindRepeat(R.id.key_backspace, ::onBackspace)
        bindRepeat(R.id.key_backspace_s1, ::onBackspace)
        bindRepeat(R.id.key_backspace_s2, ::onBackspace)

        listOf(R.id.key_space, R.id.key_space_s1, R.id.key_space_s2).forEach { id ->
            val btn = root.findViewById<Button>(id) ?: return@forEach
            var startX = 0f
            var lastStepX = 0f
            var didSwipe = false
            btn.setOnTouchListener { _, event ->
                if (!prefs.enableGestureCursorControl) return@setOnTouchListener false
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        lastStepX = event.x
                        didSwipe = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val density = resources.displayMetrics.density
                        val swipeThresholdPx = 24f * density
                        val stepPx = 14f * density
                        val dx = event.x - lastStepX
                        if (kotlin.math.abs(event.x - startX) > swipeThresholdPx) didSwipe = true
                        if (didSwipe) {
                            if (dx > stepPx) {
                                moveCursor(1)
                                lastStepX = event.x
                            } else if (dx < -stepPx) {
                                moveCursor(-1)
                                lastStepX = event.x
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (didSwipe) return@setOnTouchListener true
                        performKeyFeedback(btn)
                        onSpacePressed()
                        return@setOnTouchListener true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (didSwipe) return@setOnTouchListener true
                    }
                }
                false
            }
            btn.setOnClickListener {
                performKeyFeedback(btn)
                onSpacePressed()
            }
            // Gboard behavior: Long-press Spacebar opens IME picker (choose Gboard / vnchFlex)
            btn.setOnLongClickListener {
                if (didSwipe) return@setOnLongClickListener true
                performKeyFeedback(btn, isLongPress = true)
                showImePicker()
                true
            }
        }
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
        val shiftBtn = keyboardView?.findViewById<android.widget.ImageButton>(R.id.key_shift)
        shiftBtn?.setImageResource(
            when (shiftState) {
                ShiftState.LOWER -> R.drawable.ic_gboard_shift
                ShiftState.SHIFTED, ShiftState.CAPS_LOCK -> R.drawable.ic_gboard_shift_filled
            }
        )
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
        val before = ic.getTextBeforeCursor(4, 0)?.toString().orEmpty()
        val trimmed = before.trimEnd()
        val shouldCap = before.isEmpty() || trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?") || before.endsWith("\n")
        val newShift = if (shouldCap) ShiftState.SHIFTED else ShiftState.LOWER
        if (shiftState != newShift) {
            shiftState = newShift
            refreshLetterKeyLabels()
        }
    }

    private fun bindClick(id: Int, action: () -> Unit) {
        keyboardView?.findViewById<View>(id)?.setOnClickListener { view ->
            performKeyFeedback(view)
            action()
        }
    }

    private fun bindRepeat(id: Int, action: () -> Unit) {
        val view = keyboardView?.findViewById<View>(id) ?: return
        var startX = 0f
        var didSwipeDelete = false
        var repeating = false
        val repeatHandler = android.os.Handler(android.os.Looper.getMainLooper())
        lateinit var repeatRunnable: Runnable
        repeatRunnable = object : Runnable {
            override fun run() {
                if (didSwipeDelete) return
                repeating = true
                backspaceRepeating = true
                action()
                repeatHandler.postDelayed(this, 50L)
            }
        }

        view.setOnClickListener(null)
        view.setOnLongClickListener(null)
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    didSwipeDelete = false
                    repeating = false
                    backspaceRepeating = false
                    performKeyFeedback(view)
                    action()
                    repeatHandler.removeCallbacks(repeatRunnable)
                    repeatHandler.postDelayed(repeatRunnable, 400L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (prefs.enableGestureDelete && !didSwipeDelete && startX - event.x > 60f) {
                        didSwipeDelete = true
                        backspaceRepeating = false
                        repeatHandler.removeCallbacks(repeatRunnable)
                        performKeyFeedback(view, isLongPress = true)
                        deleteWordBeforeCursor()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    repeatHandler.removeCallbacks(repeatRunnable)
                    if (backspaceRepeating) {
                        backspaceRepeating = false
                        resyncAfterBackspaceRepeat()
                        updateAutoCapitalization()
                        updateSuggestions(vnEngine.composingText)
                    }
                    true
                }
            }
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
        lastGlideCommittedLength = 0
        if (altPopup?.isShowing == true) dismissAltPopup()
        val ic = currentInputConnection ?: return
        if (mode != Mode.LETTERS) {
            commitRaw(c.toString())
            return
        }
        if (!isVietnameseMode) {
            if (isMacroTrigger(c)) {
                val finishedEn = enComposing.toString()
                enComposing.clear()
                val lookup = if (finishedEn.isNotEmpty()) finishedEn else prevCommittedWord1
                if (finishedEn.isNotEmpty()) {
                    recordCommittedWord(finishedEn)
                }
                if (prefs.macroEnabled && lookup.isNotEmpty() && macroEngine.tryExpand(ic, c, beforeCursor = lookup)) {
                    updateAutoCapitalization()
                    updateSuggestions("")
                    return
                }
                ic.commitText(c.toString(), 1)
                updateAutoCapitalization()
                updateSuggestions("")
                return
            }
            if (c.isLetter() || c == '\'') {
                enComposing.append(c)
            } else {
                enComposing.clear()
            }
            ic.commitText(c.toString(), 1)
            updateAutoCapitalization()
            updateSuggestions(enComposing.toString())
            return
        }
        if (isMacroTrigger(c)) {
            val finishedWord = vnEngine.composingText
            vnEngine.commit(ic)
            if (finishedWord.isNotEmpty()) {
                recordCommittedWord(finishedWord)
                if (prefs.macroEnabled && macroEngine.tryExpand(ic, c, beforeCursor = finishedWord)) {
                    updateAutoCapitalization()
                    updateSuggestions("")
                    return
                }
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
        lastGlideCommittedLength = 0
        if (altPopup?.isShowing == true) dismissAltPopup()
        val ic = currentInputConnection ?: return
        vnEngine.commit(ic)
        ic.commitText(text, 1)
        updateAutoCapitalization()
        updateSuggestions("")
    }

    private fun resyncAfterBackspaceRepeat() {
        val ic = currentInputConnection ?: return
        if (mode != Mode.LETTERS || !isVietnameseMode) {
            vnEngine.reset()
            return
        }
        if (vnEngine.isComposing) return
        val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
        val prevWord = before.takeLastWhile { it.isLetter() }
        if (prevWord.isNotEmpty()) {
            vnEngine.rehydrateWord(prevWord, ic)
        } else {
            vnEngine.reset()
        }
    }

    private fun onBackspace() {
        val ic = currentInputConnection ?: return
        val now = System.currentTimeMillis()

        // 0. Gboard Glide Typing behavior: Single Backspace immediately after a glided word deletes the whole glided word
        if (lastGlideCommittedLength > 0 && !backspaceRepeating) {
            ic.deleteSurroundingText(lastGlideCommittedLength, 0)
            lastGlideCommittedLength = 0
            syncContextFromEditor()
            updateAutoCapitalization()
            updateSuggestions("")
            return
        }
        lastGlideCommittedLength = 0

        // 1. Double-backspace to undo last macro expansion (e.g. "Hà Nội " -> "hn ")
        val lastMacro = macroEngine.lastExpansion
        if (lastMacro != null) {
            if (now - lastBackspaceClickTime < 600L && !backspaceRepeating) {
                val (orig, expanded) = lastMacro
                val before = ic.getTextBeforeCursor(expanded.length + 2, 0)?.toString().orEmpty()
                // After first backspace, 1 char was already deleted from `expanded`
                val expandedAfterOneDelete = expanded.dropLast(1)
                if (expandedAfterOneDelete.isNotEmpty() && before.endsWith(expandedAfterOneDelete)) {
                    ic.beginBatchEdit()
                    ic.deleteSurroundingText(expandedAfterOneDelete.length, 0)
                    ic.commitText(orig, 1)
                    ic.endBatchEdit()
                    macroEngine.clearLastExpansion()
                    lastBackspaceClickTime = 0L
                    updateSuggestions("")
                    return
                }
            } else if (now - lastBackspaceClickTime >= 600L) {
                macroEngine.clearLastExpansion()
            }
        }
        lastBackspaceClickTime = now

        // 2. Normal Vietnamese composing backspace
        if (mode == Mode.LETTERS && isVietnameseMode && vnEngine.onBackspace(ic)) {
            if (!backspaceRepeating) updateSuggestions(vnEngine.composingText)
            return
        }

        // 3. Delete 1 char; if we land on a word, rehydrate it as composing in one batch
        if (!backspaceRepeating && mode == Mode.LETTERS && isVietnameseMode) {
            val before = ic.getTextBeforeCursor(33, 0)?.toString().orEmpty()
            if (before.isNotEmpty()) {
                val afterDel = before.dropLast(1)
                val prevWord = afterDel.takeLastWhile { it.isLetter() }
                if (prevWord.isNotEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                    vnEngine.rehydrateWord(prevWord)
                    updateSuggestions(vnEngine.composingText)
                    updateAutoCapitalization()
                    return
                }
            }
        }
        ic.deleteSurroundingText(1, 0)
        if (!backspaceRepeating) {
            updateAutoCapitalization()
            updateSuggestions("")
        }
    }

    private fun moveCursor(delta: Int) {
        val ic = currentInputConnection ?: return
        val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    private fun alternatesForLetter(base: String): List<String> {
        val b = base.lowercase()
        val sec = secondarySymbolForLetter(b)
        val list = mutableListOf<String>()
        if (sec != null) list.add(sec)
        val extras = when (b) {
            "a" -> listOf("á", "à", "ả", "ã", "ạ", "ă", "â")
            "e" -> listOf("é", "è", "ẻ", "ẽ", "ẹ", "ê")
            "i" -> listOf("í", "ì", "ỉ", "ĩ", "ị")
            "o" -> listOf("ó", "ò", "ỏ", "õ", "ọ", "ô", "ơ")
            "u" -> listOf("ú", "ù", "ủ", "ũ", "ụ", "ư")
            "y" -> listOf("ý", "ỳ", "ỷ", "ỹ", "ỵ")
            "d" -> listOf("đ")
            "c" -> listOf("ç")
            "n" -> listOf("ñ")
            "s" -> listOf("ß")
            else -> emptyList()
        }
        for (e in extras) {
            val cased = if (shiftState != ShiftState.LOWER) e.uppercase() else e
            if (cased != sec && cased.lowercase() != b) list.add(cased)
        }
        return list.distinct()
    }

    private fun showAlternatesPopup(anchor: View, options: List<String>) {
        dismissKeyPopup()
        altOptions = options
        altSelectedIndex = 0
        altAnchor = anchor

        val row = altPopupRow ?: LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.popup_bg)
            elevation = 10f
            val p = (4 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
            altPopupRow = this
        }
        row.removeAllViews()
        altButtons.clear()

        val padH = (12 * resources.displayMetrics.density).toInt()
        val padV = (8 * resources.displayMetrics.density).toInt()
        options.forEachIndexed { idx, opt ->
            val tv = TextView(this).apply {
                text = opt
                textSize = 20f
                gravity = Gravity.CENTER
                minWidth = (36 * resources.displayMetrics.density).toInt()
                setPadding(padH, padV, padH, padV)
                setTextColor(getColor(R.color.key_text))
            }
            altButtons.add(tv)
            row.addView(tv)
        }
        updateAltHighlight()

        val popup = altPopup ?: PopupWindow(
            row,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            false
        ).also { altPopup = it }
        if (popup.contentView !== row) popup.contentView = row
        popup.isClippingEnabled = false

        row.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        row.layout(0, 0, row.measuredWidth, row.measuredHeight)
        val loc = IntArray(2)
        anchor.getLocationInWindow(loc)
        val popupW = row.measuredWidth
        val popupH = row.measuredHeight
        val x = (loc[0] + anchor.width / 2 - popupW / 2).coerceAtLeast(0)
        val y = (loc[1] - popupH - 8).coerceAtLeast(0)
        if (popup.isShowing) popup.dismiss()
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }

    private fun updateAltHighlight() {
        altButtons.forEachIndexed { i, tv ->
            if (i == altSelectedIndex) {
                tv.setBackgroundColor(getColor(R.color.accent))
                tv.setTextColor(getColor(R.color.enter_text))
            } else {
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.setTextColor(getColor(R.color.key_text))
            }
        }
    }

    private fun updateAltSelectionFromTouch(anchor: View, event: MotionEvent): Boolean {
        if (altPopup?.isShowing != true || altAnchor !== anchor || altButtons.isEmpty()) return false
        val row = altPopupRow ?: return false
        val rowLoc = IntArray(2)
        row.getLocationOnScreen(rowLoc)
        val touchXOnScreen = event.rawX.toInt()
        val relativeX = touchXOnScreen - rowLoc[0]
        var idx = altButtons.indexOfFirst { btn ->
            val left = btn.left
            val right = btn.right
            relativeX in left..right
        }
        if (idx < 0) {
            idx = when {
                relativeX < 0 -> 0
                relativeX > row.width -> altButtons.lastIndex
                else -> altSelectedIndex
            }
        }
        if (idx != altSelectedIndex) {
            altSelectedIndex = idx
            updateAltHighlight()
        }
        return true
    }

    private fun commitAltSelection(): Boolean {
        val popup = altPopup ?: return false
        if (!popup.isShowing) return false
        val sel = altOptions.getOrNull(altSelectedIndex) ?: return false
        popup.dismiss()
        altOptions = emptyList()
        altButtons.clear()
        altSelectedIndex = -1
        altAnchor = null
        commitPopupSelection(sel)
        return true
    }

    private fun dismissAltPopup() {
        altPopup?.dismiss()
        altOptions = emptyList()
        altButtons.clear()
        altSelectedIndex = -1
        altAnchor = null
    }

    private fun showEmojiPickerPopup(anchor: View) {
        dismissKeyPopup()
        dismissAltPopup()
        val density = resources.displayMetrics.density
        val emojis = listOf(
            "😊", "😂", "🤣", "❤️", "😍", "🥰", "😘", "😭",
            "😢", "😅", "😎", "🤔", "😴", "😡", "🥵", "🥶",
            "👍", "👎", "👌", "🙏", "👏", "🤝", "✌️", "💪",
            "🔥", "✨", "🎉", "🎂", "🎁", "⭐", "💯", "🎶",
            "🍺", "🍻", "☕", "🍵", "🍚", "🍜", "🍰", "🍷",
            "☔", "☀️", "🌤️", "🌧️", "🌸", "🐶", "🐱", "🏠",
            "🚗", "✈️", "📱", "💻", "📷", "📚", "💰", "⏰"
        )
        val grid = android.widget.GridLayout(this).apply {
            columnCount = 8
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
        }
        var popupRef: PopupWindow? = null
        val cellSize = (40 * density).toInt()
        for (emoji in emojis) {
            val tv = TextView(this).apply {
                text = emoji
                textSize = 22f
                gravity = android.view.Gravity.CENTER
                layoutParams = android.widget.GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                }
                setOnClickListener {
                    performKeyFeedback(this)
                    commitRaw(emoji)
                    popupRef?.dismiss()
                }
            }
            grid.addView(tv)
        }
        val scroll = android.widget.ScrollView(this).apply {
            addView(grid)
            setBackgroundColor(getColor(R.color.keyboard_bg))
            elevation = 8 * density
        }
        val widthPx = (336 * density).toInt()
        val heightPx = (210 * density).toInt()
        val popup = PopupWindow(scroll, widthPx, heightPx, false).apply {
            isOutsideTouchable = true
            isTouchable = true
        }
        popupRef = popup
        popup.showAtLocation(anchor.rootView, android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL, 0, (56 * density).toInt())
    }

    private fun commitPopupSelection(text: String) {
        if (text.length == 1) {
            val c = text[0]
            val isPlainAsciiLetter = c in 'a'..'z' || c in 'A'..'Z'
            if (isPlainAsciiLetter) {
                val ch = if (shiftState != ShiftState.LOWER) c.uppercaseChar() else c.lowercaseChar()
                onChar(ch)
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.LOWER
                    refreshLetterKeyLabels()
                }
                return
            }
            if (c.isLetter()) {
                val ch = if (shiftState != ShiftState.LOWER) c.uppercaseChar() else c.lowercaseChar()
                commitRaw(ch.toString())
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.LOWER
                    refreshLetterKeyLabels()
                }
                return
            }
        }
        commitRaw(text)
    }

    private fun scheduleKeyPopup(anchor: View, label: String) {
        popupHandler.removeCallbacks(showPopupRunnable)
        pendingPopupAnchor = anchor
        pendingPopupLabel = label
        val delay = if (prefs.enableGlideTyping) 65L else 35L
        popupHandler.postDelayed(showPopupRunnable, delay)
    }

    private fun showKeyPopup(anchor: View, label: String) {
        val tv = keyPopupText ?: TextView(this).apply {
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.key_text))
            setBackgroundResource(R.drawable.key_bg)
            setPadding(28, 14, 28, 14)
            elevation = 8f
            keyPopupText = this
        }
        tv.text = label
        val popup = keyPopup ?: PopupWindow(
            tv,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            false
        ).also { keyPopup = it }
        if (popup.contentView !== tv) popup.contentView = tv
        val loc = IntArray(2)
        anchor.getLocationInWindow(loc)
        val y = (loc[1] - anchor.height - 16).coerceAtLeast(0)
        if (popup.isShowing) popup.dismiss()
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, loc[0], y)
    }

    private fun dismissKeyPopup() {
        popupHandler.removeCallbacks(showPopupRunnable)
        pendingPopupAnchor = null
        keyPopup?.let { if (it.isShowing) it.dismiss() }
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
        val cm = clipboardManager ?: (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also { clipboardManager = it } ?: return
        val current = cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (current.isNotBlank() && clipboardHistory.firstOrNull() != current) {
            clipboardHistory.remove(current)
            clipboardHistory.addFirst(current)
            while (clipboardHistory.size > 15) clipboardHistory.removeLast()
        }
        val anchor = rootView?.findViewById<View>(R.id.btn_toolbar_clipboard) ?: return
        if (clipboardHistory.isEmpty()) return

        val density = resources.displayMetrics.density
        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
        }
        var popupRef: PopupWindow? = null
        for (item in clipboardHistory) {
            val tv = TextView(this).apply {
                text = item.trim()
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                textSize = 14f
                setTextColor(getColor(R.color.key_text))
                setBackgroundColor(getColor(R.color.key_bg))
                setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (6 * density).toInt() }
                setOnClickListener {
                    performKeyFeedback(this)
                    commitRaw(item)
                    popupRef?.dismiss()
                }
            }
            listLayout.addView(tv)
        }
        val scroll = android.widget.ScrollView(this).apply {
            addView(listLayout)
            setBackgroundColor(getColor(R.color.keyboard_bg))
            elevation = 8 * density
        }
        val popup = PopupWindow(scroll, (320 * density).toInt(), (220 * density).toInt(), false).apply {
            isOutsideTouchable = true
            isTouchable = true
        }
        popupRef = popup
        popup.showAtLocation(anchor.rootView, android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL, 0, (56 * density).toInt())
    }

    private fun openSettingsSection(section: String?) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (section != null) putExtra("section", section)
        }
        startActivity(intent)
    }

    /**
     * Gboard behavior: Tap language switch key toggles Tiếng Việt <-> English.
     * Long-press Spacebar opens the system IME Picker.
     */
    private fun switchKeyboard() {
        currentInputConnection?.let { vnEngine.commit(it) }
        isVietnameseMode = !isVietnameseMode
        applyPreferencesToView()
        updateSuggestions("")
    }

    private fun showImePicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    private fun performKeyFeedback(view: View, isLongPress: Boolean = false) {
        if (prefs.hapticOnKeypress) {
            val flag = if (isLongPress) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.KEYBOARD_TAP
            view.performHapticFeedback(flag)
        }
        if (prefs.soundOnKeypress) {
            val am = audioManager ?: (getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.also { audioManager = it }
            val vol = (prefs.soundVolume / 100f).coerceIn(0f, 1f)
            am?.playSoundEffect(AudioManager.FX_KEY_CLICK, vol)
        }
    }

    /**
     * Chỉ phím chữ mới có symbol phụ ở góc trên bên phải (vẽ bằng GboardKeyButton,
     * chữ cái chính luôn nằm chính giữa phím, không bao giờ bị tụt hay bị cắt).
     */
    private fun applyLetterKeyLabel(btn: GboardKeyButton, primary: String, secondary: String?) {
        if (btn.text?.toString() != primary) btn.text = primary
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

    private fun initSuggestionSlots() {
        val bar = suggestionBar ?: return
        if (suggestionSlots.isNotEmpty()) return
        bar.removeAllViews()
        for (i in 0 until 3) {
            if (i > 0) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(2, 42)
                    setBackgroundColor(getColor(R.color.strip_divider))
                    visibility = View.INVISIBLE
                }
                suggestionDividers.add(divider)
                bar.addView(divider)
            }
            val tv = TextView(this).apply {
                gravity = Gravity.CENTER
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                textSize = 15f
                visibility = View.INVISIBLE
            }
            suggestionSlots.add(tv)
            bar.addView(tv)
        }
    }

    /**
     * Gboard 3-slot suggestion strip with Trigram Next-Word & Context-Aware Word Suggestions.
     * Reuses fixed slots to avoid view churn on every keystroke.
     */
    private fun updateSuggestions(prefix: String) {
        val bar = suggestionBar ?: return
        if (suggestionSlots.isEmpty()) initSuggestionSlots()
        if (!prefs.showSuggestionStrip || mode != Mode.LETTERS) {
            if (lastSuggestionKey != null) {
                suggestionSlots.forEach { it.visibility = View.INVISIBLE; it.text = "" }
                suggestionDividers.forEach { it.visibility = View.INVISIBLE }
                lastSuggestionKey = null
            }
            return
        }

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

        val key = prefix + "\u0001" + prevCommittedWord2 + "\u0001" + prevCommittedWord1 + "\u0001" + suggestions.joinToString("\u0002")
        if (key == lastSuggestionKey) return
        lastSuggestionKey = key

        for (i in 0 until 3) {
            val tv = suggestionSlots[i]
            val word = suggestions.getOrNull(i)
            if (word == null) {
                tv.visibility = View.INVISIBLE
                tv.text = ""
                tv.setOnClickListener(null)
            } else {
                val isPrimaryCenter = (i == 1 && prefix.isNotEmpty()) || (macroPreview != null && word == macroPreview)
                tv.text = word
                tv.setTextColor(
                    if (isPrimaryCenter) getColor(R.color.suggestion_primary)
                    else getColor(R.color.key_text)
                )
                tv.setTypeface(null, if (isPrimaryCenter) Typeface.BOLD else Typeface.NORMAL)
                tv.setOnClickListener {
                    performKeyFeedback(tv)
                    acceptSuggestion(word)
                }
                tv.visibility = View.VISIBLE
            }
        }
        suggestionDividers.forEachIndexed { idx, div ->
            val leftVisible = suggestionSlots.getOrNull(idx)?.visibility == View.VISIBLE
            val rightVisible = suggestionSlots.getOrNull(idx + 1)?.visibility == View.VISIBLE
            div.visibility = if (leftVisible && rightVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun acceptSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val composing = vnEngine.composingText
        if (composing.isNotEmpty()) {
            // Replace composing text with suggestion
            vnEngine.reset(ic)
            ic.beginBatchEdit()
            ic.deleteSurroundingText(composing.length, 0)
            ic.commitText("$word ", 1)
            ic.endBatchEdit()
        } else if (!isVietnameseMode && enComposing.isNotEmpty()) {
            val len = enComposing.length
            enComposing.clear()
            ic.beginBatchEdit()
            ic.deleteSurroundingText(len, 0)
            ic.commitText("$word ", 1)
            ic.endBatchEdit()
        } else {
            vnEngine.reset(ic)
            val before = ic.getTextBeforeCursor(32, 0)?.toString().orEmpty()
            val prefix = before.takeLastWhile { it.isLetter() }
            ic.beginBatchEdit()
            if (prefix.isNotEmpty()) ic.deleteSurroundingText(prefix.length, 0)
            ic.commitText("$word ", 1)
            ic.endBatchEdit()
        }
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
