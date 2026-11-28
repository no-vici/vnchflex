package com.vnchflex.vietkeyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.vnchflex.vietkeyboard.R

/**
 * Gboard-architecture Single-Canvas Keyboard Renderer & Multitouch PointerDispatcher:
 * - Renders all keys (Letters, Number Row, Symbols 1 & 2, Shift, Backspace, Space, Enter) on a single Canvas
 *   with zero nested ViewGroup layout passes and zero per-frame allocations.
 * - Tracks each finger independently via [SparseArray] of [PointerTracker] (`ACTION_DOWN`, `ACTION_POINTER_DOWN`,
 *   `ACTION_MOVE`, `ACTION_UP`, `ACTION_POINTER_UP`) for true 2-thumb rollover.
 * - Integrates Glide Typing trail, Spacebar cursor control, and Backspace slide-to-select gesture delete
 *   in one unified touch pipeline.
 */
class KeyboardCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Page { LETTERS, SYMBOLS1, SYMBOLS2 }
    enum class KeyRole { CHAR, NUMBER, SHIFT, BACKSPACE, MODE_SYMBOLS1, MODE_SYMBOLS2, MODE_LETTERS, COMMA, DOT, SWITCH_LANG, SPACE, ENTER }

    data class KeySpec(
        val id: String,
        val role: KeyRole,
        val primaryLabel: String,
        val secondaryLabel: String? = null,
        val weight: Float = 1f,
        val rect: RectF = RectF(),
        val hitRect: RectF = RectF()
    )

    private class PointerTracker(
        val pointerId: Int,
        var downKey: KeySpec?,
        var currentKey: KeySpec?,
        val startX: Float,
        val startY: Float,
        var lastX: Float,
        var lastY: Float,
        var committedOnDown: Boolean = false,
        var longPressTriggered: Boolean = false,
        var isSpaceSwiping: Boolean = false,
        var isBackspaceSwiping: Boolean = false,
        var longPressRunnable: Runnable? = null
    )

    // Callbacks to VietKeyboardService
    var onLetterDown: ((Char) -> Unit)? = null
    var onDigitOrSymbol: ((String) -> Unit)? = null
    var onSecondaryCommit: ((String) -> Unit)? = null
    var onUndoDownLetter: (() -> Unit)? = null
    var onShiftClick: (() -> Unit)? = null
    var onBackspaceDown: (() -> Unit)? = null
    var onBackspaceRepeatEnd: (() -> Unit)? = null
    var onBackspaceGestureSelect: ((wordsCount: Int, isFirstTrigger: Boolean) -> Unit)? = null
    var onBackspaceGestureFinish: ((wordsCount: Int, cancelled: Boolean) -> Unit)? = null
    var onSpaceDown: (() -> Unit)? = null
    var onUndoDownSpace: (() -> Unit)? = null
    var onSpaceCursorMove: ((delta: Int) -> Unit)? = null
    var onSpaceLongPress: (() -> Unit)? = null
    var onCommaDown: (() -> Unit)? = null
    var onCommaLongPress: ((anchorRect: RectF) -> Unit)? = null
    var onDotDown: (() -> Unit)? = null
    var onUndoDownPunctuation: ((Char) -> Unit)? = null
    var onSwitchLanguage: (() -> Unit)? = null
    var onEnterClick: (() -> Unit)? = null
    var onGlideStart: (() -> Unit)? = null
    var onGlideComplete: ((String) -> Unit)? = null
    var onGlideCompleteWithPoints: ((swipedKeys: String, normalizedPoints: FloatArray) -> Unit)? = null
    var onHaptic: ((isLongPress: Boolean) -> Unit)? = null

    // Configurable state synced from KeyboardPreferences & VietKeyboardService
    var page: Page = Page.LETTERS
        set(value) {
            if (field != value) {
                field = value
                rebuildKeys()
                invalidate()
            }
        }
    var showNumberRow: Boolean = true
    var showLanguageSwitchKey: Boolean = true
    var longPressForSymbols: Boolean = true
    var isDarkTheme: Boolean = false
    var showKeyBorders: Boolean = true
    var isShifted: Boolean = false
    var isCapsLock: Boolean = false
    var isVietnameseMode: Boolean = true
    var spaceLabel: String = "Tiếng Việt"
    var keyHeightScale: Float = 1.0f
    var longPressDelayMs: Long = 300L
    var enableGlideTyping: Boolean = true
    var showGestureTrail: Boolean = true
    var glideThresholdDp: Int = 24
    var enableGestureCursor: Boolean = true
    var enableGestureDelete: Boolean = true
    var popupOnKeypress: Boolean = true
    var langSwitchIconRes: Int = R.drawable.ic_switch_ime_logo

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity
    private val handler = Handler(Looper.getMainLooper())

    private val rows = ArrayList<List<KeySpec>>(5)
    private val allKeys = ArrayList<KeySpec>(44)
    private val pointers = SparseArray<PointerTracker>(4)

    // Glide state
    private var isGliding = false
    private var glidePointerId = -1
    private val glideKeys = StringBuilder()
    private val glideTrailPoints = ArrayList<Pair<Float, Float>>(64)
    private val fadingTrailPoints = ArrayList<Pair<Float, Float>>(64)
    private var fadeStartMs: Long = 0L
    private val glidePath = Path()

    // Inline alternates popup (e.g. long-press on '.' or letter with multiple accents)
    private var activeAltOptions: List<String>? = null
    private var activeAltSelectedIdx: Int = 0
    private var activeAltAnchorRect: RectF? = null

    // Backspace repeat state
    private var backspaceRepeating = false
    private var backspaceGestureWords = 0
    private val backspaceRepeatRunnable = object : Runnable {
        override fun run() {
            if (!backspaceRepeating) return
            onBackspaceDown?.invoke()
            handler.postDelayed(this, 50L)
        }
    }

    // Pre-allocated Paints (Zero allocation during onDraw)
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val primaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1A73E8")
        style = Paint.Style.STROKE
        strokeWidth = 6f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = Color.parseColor("#331A73E8")
    }
    private val tempRect = RectF()

    private var drawableShift: Drawable? = null
    private var drawableShiftFilled: Drawable? = null
    private var drawableBackspace: Drawable? = null
    private var drawableEnter: Drawable? = null
    private var drawableLangIcon: Drawable? = null
    private var cachedLangIconRes: Int = 0

    init {
        drawableShift = ContextCompat.getDrawable(context, R.drawable.ic_gboard_shift)?.mutate()
        drawableShiftFilled = ContextCompat.getDrawable(context, R.drawable.ic_gboard_shift_filled)?.mutate()
        drawableBackspace = ContextCompat.getDrawable(context, R.drawable.ic_gboard_backspace)?.mutate()
        drawableEnter = ContextCompat.getDrawable(context, R.drawable.ic_gboard_enter)?.mutate()
    }

    fun syncStateAndInvalidate() {
        if (cachedLangIconRes != langSwitchIconRes) {
            cachedLangIconRes = langSwitchIconRes
            drawableLangIcon = ContextCompat.getDrawable(context, langSwitchIconRes)?.mutate()
        }
        val letterRowH = (46f * density * keyHeightScale).toInt()
        val numRowH = (40f * density * keyHeightScale).toInt()
        val hasNumRow = (page == Page.LETTERS && showNumberRow)
        val targetH = (if (hasNumRow) numRowH else 0) + (letterRowH * 4) + (8f * density).toInt()
        layoutParams?.let { lp ->
            if (lp.height != targetH) {
                lp.height = targetH
                layoutParams = lp
            }
        }
        rebuildKeys()
        if (width > 0) {
            layoutKeyRects(width, targetH)
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val letterRowH = (46f * density * keyHeightScale).toInt()
        val numRowH = (40f * density * keyHeightScale).toInt()
        val hasNumRow = (page == Page.LETTERS && showNumberRow)
        val totalH = (if (hasNumRow) numRowH else 0) + (letterRowH * 4) + (8f * density).toInt()
        setMeasuredDimension(w, totalH)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildKeys()
    }

    private fun rebuildKeys() {
        rows.clear()
        allKeys.clear()
        when (page) {
            Page.LETTERS -> buildLettersPage()
            Page.SYMBOLS1 -> buildSymbols1Page()
            Page.SYMBOLS2 -> buildSymbols2Page()
        }
        layoutKeyRects(width, height)
    }

    private fun buildLettersPage() {
        if (showNumberRow) {
            rows.add(("1234567890").map { d ->
                KeySpec(id = "num_$d", role = KeyRole.NUMBER, primaryLabel = d.toString())
            })
        }
        rows.add(("qwertyuiop").map { ch ->
            val label = if (isShifted || isCapsLock) ch.uppercase() else ch.toString()
            val sec = if (longPressForSymbols) secondaryForLetter(ch) else null
            KeySpec(id = "key_$ch", role = KeyRole.CHAR, primaryLabel = label, secondaryLabel = sec)
        })
        rows.add(("asdfghjkl").map { ch ->
            val label = if (isShifted || isCapsLock) ch.uppercase() else ch.toString()
            val sec = if (longPressForSymbols) secondaryForLetter(ch) else null
            KeySpec(id = "key_$ch", role = KeyRole.CHAR, primaryLabel = label, secondaryLabel = sec)
        })
        val row3 = ArrayList<KeySpec>(9)
        row3.add(KeySpec(id = "key_shift", role = KeyRole.SHIFT, primaryLabel = "⇧", weight = 1.5f))
        for (ch in "zxcvbnm") {
            val label = if (isShifted || isCapsLock) ch.uppercase() else ch.toString()
            val sec = if (longPressForSymbols) secondaryForLetter(ch) else null
            row3.add(KeySpec(id = "key_$ch", role = KeyRole.CHAR, primaryLabel = label, secondaryLabel = sec))
        }
        row3.add(KeySpec(id = "key_backspace", role = KeyRole.BACKSPACE, primaryLabel = "⌫", weight = 1.5f))
        rows.add(row3)

        val row4 = ArrayList<KeySpec>(6)
        row4.add(KeySpec(id = "key_mode_s1", role = KeyRole.MODE_SYMBOLS1, primaryLabel = "?123", weight = 1.5f))
        row4.add(KeySpec(id = "key_comma", role = KeyRole.COMMA, primaryLabel = ",", secondaryLabel = "😊", weight = 0.9f))
        if (showLanguageSwitchKey) {
            row4.add(KeySpec(id = "key_lang", role = KeyRole.SWITCH_LANG, primaryLabel = "🌐", weight = 1.0f))
        }
        row4.add(KeySpec(id = "key_space", role = KeyRole.SPACE, primaryLabel = spaceLabel, weight = 5.2f))
        row4.add(KeySpec(id = "key_dot", role = KeyRole.DOT, primaryLabel = ".", weight = 0.9f))
        row4.add(KeySpec(id = "key_enter", role = KeyRole.ENTER, primaryLabel = "↵", weight = 1.5f))
        rows.add(row4)
    }

    private fun buildSymbols1Page() {
        rows.add(("1234567890").map { KeySpec("s1_$it", KeyRole.NUMBER, it.toString()) })
        rows.add(listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/").map { KeySpec("s1_$it", KeyRole.NUMBER, it) })
        val row3 = ArrayList<KeySpec>(9)
        row3.add(KeySpec("key_mode_s2", KeyRole.MODE_SYMBOLS2, "=\\<", weight = 1.5f))
        listOf("*", "\"", "'", ":", ";", "!", "?").forEach { row3.add(KeySpec("s1_$it", KeyRole.NUMBER, it)) }
        row3.add(KeySpec("key_backspace", KeyRole.BACKSPACE, "⌫", weight = 1.5f))
        rows.add(row3)

        val row4 = ArrayList<KeySpec>(6)
        row4.add(KeySpec("key_mode_abc", KeyRole.MODE_LETTERS, "ABC", weight = 1.5f))
        row4.add(KeySpec("s1_comma", KeyRole.COMMA, ",", weight = 0.9f))
        if (showLanguageSwitchKey) row4.add(KeySpec("key_lang", KeyRole.SWITCH_LANG, "🌐", weight = 1.0f))
        row4.add(KeySpec("key_space", KeyRole.SPACE, spaceLabel, weight = 5.2f))
        row4.add(KeySpec("s1_dot", KeyRole.DOT, ".", weight = 0.9f))
        row4.add(KeySpec("key_enter", KeyRole.ENTER, "↵", weight = 1.5f))
        rows.add(row4)
    }

    private fun buildSymbols2Page() {
        rows.add(listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map { KeySpec("s2_$it", KeyRole.NUMBER, it) })
        rows.add(listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "%").map { KeySpec("s2_$it", KeyRole.NUMBER, it) })
        val row3 = ArrayList<KeySpec>(9)
        row3.add(KeySpec("key_mode_s1", KeyRole.MODE_SYMBOLS1, "?123", weight = 1.5f))
        listOf("\\", "©", "®", "™", "✓", "[", "]").forEach { row3.add(KeySpec("s2_$it", KeyRole.NUMBER, it)) }
        row3.add(KeySpec("key_backspace", KeyRole.BACKSPACE, "⌫", weight = 1.5f))
        rows.add(row3)

        val row4 = ArrayList<KeySpec>(6)
        row4.add(KeySpec("key_mode_abc", KeyRole.MODE_LETTERS, "ABC", weight = 1.5f))
        row4.add(KeySpec("s2_lt", KeyRole.NUMBER, "<", weight = 0.9f))
        if (showLanguageSwitchKey) row4.add(KeySpec("key_lang", KeyRole.SWITCH_LANG, "🌐", weight = 1.0f))
        row4.add(KeySpec("key_space", KeyRole.SPACE, spaceLabel, weight = 5.2f))
        row4.add(KeySpec("s2_gt", KeyRole.NUMBER, ">", weight = 0.9f))
        row4.add(KeySpec("key_enter", KeyRole.ENTER, "↵", weight = 1.5f))
        rows.add(row4)
    }

    private fun layoutKeyRects(totalW: Int, totalH: Int) {
        if (totalW <= 0 || totalH <= 0 || rows.isEmpty()) return
        val letterRowH = 46f * density * keyHeightScale
        val numRowH = 40f * density * keyHeightScale
        val gapX = 2.5f * density
        val gapY = 3.5f * density
        var curY = 2f * density

        rows.forEachIndexed { rIdx, row ->
            val isNumRow = (page == Page.LETTERS && showNumberRow && rIdx == 0)
            val rowH = if (isNumRow) numRowH else letterRowH
            val isIndentedRow2 = (page == Page.LETTERS && ((showNumberRow && rIdx == 2) || (!showNumberRow && rIdx == 1)))
            val sideInset = if (isIndentedRow2) 16f * density else 0f
            val availW = (totalW - sideInset * 2f).coerceAtLeast(1f)
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f)

            var curX = sideInset
            for (key in row) {
                val cellW = availW * (key.weight / totalWeight)
                key.hitRect.set(curX, curY, curX + cellW, curY + rowH)
                key.rect.set(curX + gapX, curY + gapY, curX + cellW - gapX, curY + rowH - gapY)
                allKeys.add(key)
                curX += cellW
            }
            curY += rowH
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerR = 6.5f * density
        val keyColor = context.getColor(if (isDarkTheme) R.color.key_bg_dark else R.color.key_bg)
        val funcColor = context.getColor(if (isDarkTheme) R.color.key_func_bg_dark else R.color.key_func_bg)
        val pressedColor = Color.parseColor(if (isDarkTheme) "#FF4A4E54" else "#FFD0D6DF")
        val enterColor = Color.parseColor("#FF1A73E8")
        val textColor = context.getColor(if (isDarkTheme) R.color.key_text_dark else R.color.key_text)
        val hintColor = context.getColor(R.color.key_hint)
        keyShadowPaint.color = Color.parseColor(if (isDarkTheme) "#55000000" else "#22000000")

        // Collect pressed keys across active pointers
        var pressedKey1: KeySpec? = null
        var pressedKey2: KeySpec? = null
        for (i in 0 until pointers.size()) {
            val k = pointers.valueAt(i).currentKey
            if (pressedKey1 == null) pressedKey1 = k else if (pressedKey2 == null) pressedKey2 = k
        }

        for (key in allKeys) {
            val isPressed = (key === pressedKey1 || key === pressedKey2)
            val r = key.rect
            val isFunc = key.role != KeyRole.CHAR && key.role != KeyRole.NUMBER && key.role != KeyRole.SPACE

            if (showKeyBorders || isFunc || key.role == KeyRole.SPACE || isPressed) {
                if (showKeyBorders && !isPressed) {
                    tempRect.set(r.left, r.top + 1.2f * density, r.right, r.bottom + 1.2f * density)
                    val rad = if (key.role == KeyRole.ENTER || key.role == KeyRole.MODE_SYMBOLS1 || key.role == KeyRole.MODE_LETTERS || (!showKeyBorders && key.role == KeyRole.SPACE)) r.height() / 2f else cornerR
                    canvas.drawRoundRect(tempRect, rad, rad, keyShadowPaint)
                }
                keyBgPaint.color = when {
                    isPressed -> pressedColor
                    key.role == KeyRole.ENTER -> enterColor
                    isFunc -> funcColor
                    else -> keyColor
                }
                val rad = if (key.role == KeyRole.ENTER || key.role == KeyRole.MODE_SYMBOLS1 || key.role == KeyRole.MODE_LETTERS || (!showKeyBorders && key.role == KeyRole.SPACE)) r.height() / 2f else cornerR
                canvas.drawRoundRect(r, rad, rad, keyBgPaint)
            }

            // Draw icon or label
            when (key.role) {
                KeyRole.SHIFT -> {
                    val d = if (isShifted || isCapsLock) drawableShiftFilled else drawableShift
                    drawCenteredDrawable(canvas, d, r, 24f * density, textColor)
                }
                KeyRole.BACKSPACE -> {
                    drawCenteredDrawable(canvas, drawableBackspace, r, 24f * density, textColor)
                }
                KeyRole.ENTER -> {
                    drawCenteredDrawable(canvas, drawableEnter, r, 22f * density, Color.WHITE)
                }
                KeyRole.SWITCH_LANG -> {
                    drawCenteredDrawable(canvas, drawableLangIcon, r, 24f * density, null)
                }
                else -> {
                    primaryTextPaint.color = textColor
                    primaryTextPaint.textSize = when (key.role) {
                        KeyRole.SPACE -> 13.5f * scaledDensity
                        KeyRole.MODE_SYMBOLS1, KeyRole.MODE_SYMBOLS2, KeyRole.MODE_LETTERS -> 13.5f * scaledDensity
                        KeyRole.NUMBER -> 17f * scaledDensity
                        else -> 21f * scaledDensity
                    }
                    val fm = primaryTextPaint.fontMetrics
                    val centerY = r.centerY() - (fm.ascent + fm.descent) / 2f +
                        if (key.role == KeyRole.COMMA && key.secondaryLabel != null) 4f * density else 0f
                    canvas.drawText(key.primaryLabel, r.centerX(), centerY, primaryTextPaint)

                    val sec = key.secondaryLabel
                    if (!sec.isNullOrEmpty()) {
                        secondaryTextPaint.color = hintColor
                        secondaryTextPaint.textSize = 10.5f * scaledDensity
                        val sfm = secondaryTextPaint.fontMetrics
                        if (key.role == KeyRole.COMMA) {
                            secondaryTextPaint.textAlign = Paint.Align.CENTER
                            canvas.drawText(sec, r.centerX(), r.top + 4.5f * density - sfm.ascent, secondaryTextPaint)
                        } else {
                            secondaryTextPaint.textAlign = Paint.Align.RIGHT
                            canvas.drawText(sec, r.right - 4f * density, r.top + 3f * density - sfm.ascent, secondaryTextPaint)
                        }
                    }
                }
            }
        }

        // Glide trail (active or fading out over 220ms)
        if (showGestureTrail) {
            if (isGliding && glideTrailPoints.size >= 2) {
                trailPaint.alpha = 204
                drawTrailPoints(canvas, glideTrailPoints)
            } else if (fadingTrailPoints.size >= 2) {
                val elapsed = System.currentTimeMillis() - fadeStartMs
                if (elapsed < 220L) {
                    trailPaint.alpha = (204f * (1f - elapsed / 220f)).toInt().coerceIn(0, 204)
                    drawTrailPoints(canvas, fadingTrailPoints)
                    postInvalidateOnAnimation()
                } else {
                    fadingTrailPoints.clear()
                }
            }
        }

        // Key preview popup (floating above pressed letter/number key)
        if (popupOnKeypress && !isGliding && activeAltOptions == null) {
            val pk = pressedKey1
            if (pk != null && (pk.role == KeyRole.CHAR || pk.role == KeyRole.NUMBER || pk.role == KeyRole.DOT)) {
                drawKeyPreviewBubble(canvas, pk, keyColor, textColor)
            }
        }

        // Inline alternates strip (for long-press on '.' or letter accents)
        val alts = activeAltOptions
        val anchor = activeAltAnchorRect
        if (alts != null && anchor != null && alts.isNotEmpty()) {
            drawAlternatesStrip(canvas, alts, activeAltSelectedIdx, anchor, keyColor, textColor)
        }
    }

    private fun drawTrailPoints(canvas: Canvas, pts: List<Pair<Float, Float>>) {
        glidePath.reset()
        glidePath.moveTo(pts[0].first, pts[0].second)
        for (i in 1 until pts.size) {
            val prev = pts[i - 1]
            val cur = pts[i]
            glidePath.quadTo(prev.first, prev.second, (prev.first + cur.first) / 2f, (prev.second + cur.second) / 2f)
        }
        canvas.drawPath(glidePath, trailPaint)
    }

    private fun drawCenteredDrawable(canvas: Canvas, d: Drawable?, rect: RectF, sizePx: Float, tintColor: Int?) {
        d ?: return
        val half = (sizePx / 2f).toInt()
        val cx = rect.centerX().toInt()
        val cy = rect.centerY().toInt()
        d.setBounds(cx - half, cy - half, cx + half, cy + half)
        if (tintColor != null) d.setTint(tintColor)
        d.draw(canvas)
    }

    private fun drawKeyPreviewBubble(canvas: Canvas, key: KeySpec, bgColor: Int, textColor: Int) {
        val r = key.rect
        val bw = r.width() * 1.25f
        val bh = r.height() * 1.25f
        val left = (r.centerX() - bw / 2f).coerceIn(4f * density, width - bw - 4f * density)
        val top = (r.top - bh - 6f * density).coerceAtLeast(2f * density)
        tempRect.set(left, top, left + bw, top + bh)
        popupBgPaint.color = bgColor
        canvas.drawRoundRect(tempRect, 8f * density, 8f * density, popupBgPaint)
        canvas.drawRoundRect(tempRect, 8f * density, 8f * density, popupStrokePaint)
        primaryTextPaint.color = textColor
        primaryTextPaint.textSize = 26f * scaledDensity
        val fm = primaryTextPaint.fontMetrics
        val cy = tempRect.centerY() - (fm.ascent + fm.descent) / 2f
        canvas.drawText(key.primaryLabel, tempRect.centerX(), cy, primaryTextPaint)
    }

    private fun drawAlternatesStrip(
        canvas: Canvas,
        options: List<String>,
        selectedIdx: Int,
        anchor: RectF,
        bgColor: Int,
        textColor: Int
    ) {
        val cellW = 34f * density
        val cellH = 42f * density
        val stripW = cellW * options.size + 8f * density
        val left = (anchor.centerX() - stripW / 2f).coerceIn(4f * density, (width - stripW - 4f * density).coerceAtLeast(4f * density))
        val top = (anchor.top - cellH - 8f * density).coerceAtLeast(2f * density)
        tempRect.set(left, top, left + stripW, top + cellH)
        popupBgPaint.color = bgColor
        canvas.drawRoundRect(tempRect, 10f * density, 10f * density, popupBgPaint)
        canvas.drawRoundRect(tempRect, 10f * density, 10f * density, popupStrokePaint)

        for (i in options.indices) {
            val cx = left + 4f * density + i * cellW + cellW / 2f
            if (i == selectedIdx) {
                tempRect.set(cx - cellW / 2f + 2f * density, top + 3f * density, cx + cellW / 2f - 2f * density, top + cellH - 3f * density)
                keyBgPaint.color = Color.parseColor("#FF1A73E8")
                canvas.drawRoundRect(tempRect, 7f * density, 7f * density, keyBgPaint)
            }
            primaryTextPaint.color = if (i == selectedIdx) Color.WHITE else textColor
            primaryTextPaint.textSize = 18f * scaledDensity
            val fm = primaryTextPaint.fontMetrics
            val cy = top + cellH / 2f - (fm.ascent + fm.descent) / 2f
            canvas.drawText(options[i], cx, cy, primaryTextPaint)
        }
    }

    private fun findKeyAt(x: Float, y: Float): KeySpec? {
        for (i in 0 until allKeys.size) {
            val k = allKeys[i]
            if (k.hitRect.contains(x, y)) return k
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val idx = event.actionIndex
        val pid = event.getPointerId(idx)
        val x = event.getX(idx)
        val y = event.getY(idx)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (action == MotionEvent.ACTION_POINTER_DOWN && isGliding) {
                    // Second finger tapped while gliding -> cancel glide immediately (2-thumb typing!)
                    isGliding = false
                    glideKeys.clear()
                    glideTrailPoints.clear()
                }
                // Cancel pending long-press on any previous finger (rollover)
                for (i in 0 until pointers.size()) {
                    val prev = pointers.valueAt(i)
                    prev.longPressRunnable?.let { handler.removeCallbacks(it) }
                }
                val key = findKeyAt(x, y)
                val tracker = PointerTracker(
                    pointerId = pid,
                    downKey = key,
                    currentKey = key,
                    startX = x,
                    startY = y,
                    lastX = x,
                    lastY = y
                )
                pointers.put(pid, tracker)
                if (key != null) {
                    handleKeyDown(tracker, key)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                for (p in 0 until event.pointerCount) {
                    val curPid = event.getPointerId(p)
                    val tracker = pointers.get(curPid) ?: continue
                    val mx = event.getX(p)
                    val my = event.getY(p)
                    handlePointerMove(tracker, mx, my, event.pointerCount)
                    tracker.lastX = mx
                    tracker.lastY = my
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val tracker = pointers.get(pid)
                if (tracker != null) {
                    handlePointerUp(tracker)
                    pointers.remove(pid)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                for (i in 0 until pointers.size()) {
                    pointers.valueAt(i).longPressRunnable?.let { handler.removeCallbacks(it) }
                }
                pointers.clear()
                stopBackspaceRepeat()
                isGliding = false
                glideKeys.clear()
                glideTrailPoints.clear()
                activeAltOptions = null
                invalidate()
                return true
            }
        }
        return true
    }

    private fun handleKeyDown(tracker: PointerTracker, key: KeySpec) {
        onHaptic?.invoke(false)
        when (key.role) {
            KeyRole.CHAR -> {
                val ch = key.primaryLabel.firstOrNull() ?: return
                onLetterDown?.invoke(ch)
                tracker.committedOnDown = true
                val alts = alternatesForLetter(ch, key.secondaryLabel)
                if (alts.isNotEmpty()) {
                    val r = Runnable {
                        tracker.longPressTriggered = true
                        if (tracker.committedOnDown) {
                            onUndoDownLetter?.invoke()
                            tracker.committedOnDown = false
                        }
                        onHaptic?.invoke(true)
                        if (alts.size == 1) {
                            onSecondaryCommit?.invoke(alts[0])
                        } else {
                            activeAltOptions = alts
                            activeAltSelectedIdx = 0
                            activeAltAnchorRect = key.rect
                        }
                        invalidate()
                    }
                    tracker.longPressRunnable = r
                    handler.postDelayed(r, longPressDelayMs.coerceAtLeast(220L))
                }
            }
            KeyRole.NUMBER -> {
                onDigitOrSymbol?.invoke(key.primaryLabel)
                tracker.committedOnDown = true
            }
            KeyRole.SPACE -> {
                onSpaceDown?.invoke()
                tracker.committedOnDown = true
                val r = Runnable {
                    if (!tracker.isSpaceSwiping) {
                        tracker.longPressTriggered = true
                        if (tracker.committedOnDown) {
                            onUndoDownSpace?.invoke()
                            tracker.committedOnDown = false
                        }
                        onHaptic?.invoke(true)
                        onSpaceLongPress?.invoke()
                    }
                }
                tracker.longPressRunnable = r
                handler.postDelayed(r, 750L)
            }
            KeyRole.COMMA -> {
                onCommaDown?.invoke()
                tracker.committedOnDown = true
                if (key.secondaryLabel != null) {
                    val r = Runnable {
                        tracker.longPressTriggered = true
                        if (tracker.committedOnDown) {
                            onUndoDownPunctuation?.invoke(',')
                            tracker.committedOnDown = false
                        }
                        onHaptic?.invoke(true)
                        onCommaLongPress?.invoke(key.rect)
                    }
                    tracker.longPressRunnable = r
                    handler.postDelayed(r, longPressDelayMs.coerceAtLeast(250L))
                }
            }
            KeyRole.DOT -> {
                onDotDown?.invoke()
                tracker.committedOnDown = true
                val r = Runnable {
                    tracker.longPressTriggered = true
                    if (tracker.committedOnDown) {
                        onUndoDownPunctuation?.invoke('.')
                        tracker.committedOnDown = false
                    }
                    onHaptic?.invoke(true)
                    activeAltOptions = listOf("?", "!", ",", ":", ";", "-", "(", ")", "/")
                    activeAltSelectedIdx = 0
                    activeAltAnchorRect = key.rect
                    invalidate()
                }
                tracker.longPressRunnable = r
                handler.postDelayed(r, longPressDelayMs.coerceAtLeast(250L))
            }
            KeyRole.BACKSPACE -> {
                backspaceGestureWords = 0
                onBackspaceDown?.invoke()
                backspaceRepeating = true
                handler.removeCallbacks(backspaceRepeatRunnable)
                handler.postDelayed(backspaceRepeatRunnable, 400L)
            }
            KeyRole.SHIFT -> onShiftClick?.invoke()
            KeyRole.SWITCH_LANG -> onSwitchLanguage?.invoke()
            KeyRole.ENTER -> onEnterClick?.invoke()
            KeyRole.MODE_SYMBOLS1 -> page = Page.SYMBOLS1
            KeyRole.MODE_SYMBOLS2 -> page = Page.SYMBOLS2
            KeyRole.MODE_LETTERS -> page = Page.LETTERS
        }
    }

    private fun handlePointerMove(tracker: PointerTracker, mx: Float, my: Float, pointerCount: Int) {
        val downKey = tracker.downKey ?: return

        // 1. Inline alternates strip selection (e.g. long-press on '.')
        val alts = activeAltOptions
        val anchor = activeAltAnchorRect
        if (tracker.longPressTriggered && alts != null && anchor != null) {
            val cellW = 34f * density
            val stripW = cellW * alts.size + 8f * density
            val left = (anchor.centerX() - stripW / 2f).coerceIn(4f * density, (width - stripW - 4f * density).coerceAtLeast(4f * density))
            val idx = ((mx - left - 4f * density) / cellW).toInt().coerceIn(0, alts.lastIndex)
            activeAltSelectedIdx = idx
            return
        }

        // 2. Spacebar gesture cursor control
        if (downKey.role == KeyRole.SPACE && enableGestureCursor) {
            val thresholdPx = 28f * density
            val stepPx = 14f * density
            if (!tracker.isSpaceSwiping && kotlin.math.abs(mx - tracker.startX) > thresholdPx) {
                tracker.isSpaceSwiping = true
                tracker.longPressRunnable?.let { handler.removeCallbacks(it) }
                if (tracker.committedOnDown) {
                    onUndoDownSpace?.invoke()
                    tracker.committedOnDown = false
                }
            }
            if (tracker.isSpaceSwiping) {
                val dx = mx - tracker.lastX
                if (dx > stepPx) {
                    onSpaceCursorMove?.invoke(1)
                } else if (dx < -stepPx) {
                    onSpaceCursorMove?.invoke(-1)
                }
            }
            return
        }

        // 3. Backspace slide-to-select gesture delete
        if (downKey.role == KeyRole.BACKSPACE && enableGestureDelete) {
            val swipeStartPx = 24f * density
            val stepPerWordPx = 36f * density
            val dragLeftPx = tracker.startX - mx
            if (!tracker.isBackspaceSwiping && dragLeftPx > swipeStartPx) {
                tracker.isBackspaceSwiping = true
                stopBackspaceRepeat()
                onHaptic?.invoke(true)
                backspaceGestureWords = 1
                onBackspaceGestureSelect?.invoke(1, true)
            }
            if (tracker.isBackspaceSwiping) {
                val extraPx = (dragLeftPx - swipeStartPx).coerceAtLeast(0f)
                val targetWords = if (dragLeftPx <= swipeStartPx * 0.5f) 0 else 1 + (extraPx / stepPerWordPx).toInt()
                if (targetWords != backspaceGestureWords) {
                    backspaceGestureWords = targetWords
                    onBackspaceGestureSelect?.invoke(targetWords, false)
                }
            }
            return
        }

        // 4. Glide typing across letter keys
        if (page == Page.LETTERS && enableGlideTyping && pointerCount == 1 && downKey.role == KeyRole.CHAR && !tracker.longPressTriggered) {
            val dist = kotlin.math.hypot((mx - tracker.startX).toDouble(), (my - tracker.startY).toDouble()).toFloat()
            if (!isGliding && dist > glideThresholdDp * density) {
                isGliding = true
                glidePointerId = tracker.pointerId
                tracker.longPressRunnable?.let { handler.removeCallbacks(it) }
                if (tracker.committedOnDown) {
                    onGlideStart?.invoke()
                    tracker.committedOnDown = false
                }
                glideKeys.clear()
                glideTrailPoints.clear()
                downKey.primaryLabel.firstOrNull()?.lowercaseChar()?.let { glideKeys.append(it) }
                glideTrailPoints.add(tracker.startX to tracker.startY)
            }
            if (isGliding && glidePointerId == tracker.pointerId) {
                if (glideTrailPoints.size < 128) {
                    glideTrailPoints.add(mx to my)
                }
                val overKey = findKeyAt(mx, my)
                if (overKey != null && overKey.role == KeyRole.CHAR) {
                    tracker.currentKey = overKey
                    val ch = overKey.primaryLabel.firstOrNull()?.lowercaseChar()
                    if (ch != null && (glideKeys.isEmpty() || glideKeys.last() != ch)) {
                        glideKeys.append(ch)
                    }
                }
            }
        }
    }

    private fun handlePointerUp(tracker: PointerTracker) {
        tracker.longPressRunnable?.let { handler.removeCallbacks(it) }
        val downKey = tracker.downKey

        if (activeAltOptions != null && tracker.longPressTriggered) {
            val chosen = activeAltOptions?.getOrNull(activeAltSelectedIdx)
            activeAltOptions = null
            activeAltAnchorRect = null
            if (!chosen.isNullOrEmpty()) {
                if (chosen.length == 1 && chosen[0].isLetter()) {
                    onSecondaryCommit?.invoke(chosen)
                } else {
                    onDigitOrSymbol?.invoke(chosen)
                }
            }
            return
        }

        if (downKey?.role == KeyRole.BACKSPACE) {
            if (tracker.isBackspaceSwiping) {
                onBackspaceGestureFinish?.invoke(backspaceGestureWords, false)
                backspaceGestureWords = 0
            } else {
                val wasRepeating = backspaceRepeating
                stopBackspaceRepeat()
                if (wasRepeating) onBackspaceRepeatEnd?.invoke()
            }
            return
        }

        if (isGliding && glidePointerId == tracker.pointerId) {
            val swiped = glideKeys.toString()
            val normPts = buildNormalizedGlidePoints()
            if (showGestureTrail && glideTrailPoints.size >= 2) {
                fadingTrailPoints.clear()
                fadingTrailPoints.addAll(glideTrailPoints)
                fadeStartMs = System.currentTimeMillis()
            }
            isGliding = false
            glidePointerId = -1
            glideKeys.clear()
            glideTrailPoints.clear()
            if (swiped.length >= 2) {
                onGlideCompleteWithPoints?.invoke(swiped, normPts)
                onGlideComplete?.invoke(swiped)
            }
        }
    }

    private fun buildNormalizedGlidePoints(): FloatArray {
        if (glideTrailPoints.isEmpty()) return FloatArray(0)
        var minX = Float.MAX_VALUE
        var maxX = 0f
        var minY = Float.MAX_VALUE
        var maxY = 0f
        for (k in allKeys) {
            if (k.role == KeyRole.CHAR) {
                if (k.rect.left < minX) minX = k.rect.left
                if (k.rect.right > maxX) maxX = k.rect.right
                if (k.rect.top < minY) minY = k.rect.top
                if (k.rect.bottom > maxY) maxY = k.rect.bottom
            }
        }
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val out = FloatArray(glideTrailPoints.size * 2)
        for (i in glideTrailPoints.indices) {
            val (px, py) = glideTrailPoints[i]
            out[i * 2] = ((px - minX) / spanX).coerceIn(0f, 1f)
            out[i * 2 + 1] = ((py - minY) / spanY).coerceIn(0f, 1f)
        }
        return out
    }

    private fun stopBackspaceRepeat() {
        backspaceRepeating = false
        handler.removeCallbacks(backspaceRepeatRunnable)
    }

    private fun alternatesForLetter(ch: Char, secondary: String?): List<String> {
        if (!isVietnameseMode) {
            return if (!secondary.isNullOrEmpty()) listOf(secondary) else emptyList()
        }
        val base = when (ch.lowercaseChar()) {
            'a' -> listOf("á", "à", "ả", "ã", "ạ", "ă", "â")
            'e' -> listOf("é", "è", "ẻ", "ẽ", "ẹ", "ê")
            'i' -> listOf("í", "ì", "ỉ", "ĩ", "ị")
            'o' -> listOf("ó", "ò", "ỏ", "õ", "ọ", "ô", "ơ")
            'u' -> listOf("ú", "ù", "ủ", "ũ", "ụ", "ư")
            'y' -> listOf("ý", "ỳ", "ỷ", "ỹ", "ỵ")
            'd' -> listOf("đ")
            else -> emptyList()
        }
        val list = if (isShifted || isCapsLock) base.map { it.uppercase() } else base
        if (!secondary.isNullOrEmpty() && !list.contains(secondary)) {
            return list + secondary
        }
        return list
    }

    private fun secondaryForLetter(ch: Char): String? = when (ch.lowercaseChar()) {
        'q' -> if (showNumberRow) "%" else "1"
        'w' -> if (showNumberRow) "\\" else "2"
        'e' -> if (showNumberRow) "|" else "3"
        'r' -> if (showNumberRow) "=" else "4"
        't' -> if (showNumberRow) "[" else "5"
        'y' -> if (showNumberRow) "]" else "6"
        'u' -> if (showNumberRow) "<" else "7"
        'i' -> if (showNumberRow) ">" else "8"
        'o' -> if (showNumberRow) "{" else "9"
        'p' -> if (showNumberRow) "}" else "0"
        'a' -> "@"
        's' -> "#"
        'd' -> if (!isVietnameseMode) null else if (isShifted || isCapsLock) "Đ" else "đ"
        'f' -> "_"
        'g' -> "&"
        'h' -> "-"
        'j' -> "+"
        'k' -> "("
        'l' -> ")"
        'z' -> "*"
        'x' -> "\""
        'c' -> "'"
        'v' -> ":"
        'b' -> ";"
        'n' -> "!"
        'm' -> "?"
        else -> null
    }
}
