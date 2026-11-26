package com.vnchflex.vietkeyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.hypot

/**
 * Transparent overlay wrapping `panel_letters` for Gboard-style Glide Typing:
 * - Zero overhead on normal taps: only intercepts touch when finger moves > glideThresholdPx.
 * - Automatically refreshes key hit rects on every layout pass (`onLayout`) and lazily on touch
 *   so toggling the number row, changing keyboard height, or rotating the screen never causes stale coordinates.
 * - Draws a smooth Gboard-blue gesture trail when `showTrail` is enabled.
 * - Tracks the sequence of letter keys (`q..m`) traversed and notifies `onGlideStart` / `onGlideComplete`.
 */
class GlideTypingOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var glideEnabled: Boolean = true
    var showTrail: Boolean = true
    var onGlideStart: (() -> Unit)? = null
    var onGlideComplete: ((swipedKeys: String) -> Unit)? = null

    private var registeredButtons: Map<GboardKeyButton, String> = emptyMap()
    private val letterKeyRects = mutableListOf<Pair<Char, Rect>>()
    private var keyRectsDirty = true

    private val trailPath = Path()
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1A73E8")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 10f * resources.displayMetrics.density
    }

    private val glideThresholdPx = 24f * resources.displayMetrics.density
    private var downX = 0f
    private var downY = 0f
    private var isGliding = false
    private val swipedKeys = StringBuilder()
    private val tempRect = Rect()

    init {
        setWillNotDraw(false)
    }

    fun registerLetterKeys(keys: Map<GboardKeyButton, String>) {
        registeredButtons = keys
        keyRectsDirty = true
        post { rebuildKeyRects() }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        keyRectsDirty = true
        rebuildKeyRects()
    }

    private fun rebuildKeyRects() {
        if (registeredButtons.isEmpty() || width == 0 || height == 0) return
        letterKeyRects.clear()
        registeredButtons.forEach { (btn, letter) ->
            if (letter.length == 1 && btn.width > 0 && btn.height > 0) {
                val r = Rect()
                btn.getDrawingRect(r)
                offsetDescendantRectToMyCoords(btn, r)
                letterKeyRects.add(letter[0] to r)
            }
        }
        if (letterKeyRects.isNotEmpty()) {
            keyRectsDirty = false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!glideEnabled) return false
        if (keyRectsDirty || letterKeyRects.isEmpty()) {
            rebuildKeyRects()
        }
        if (letterKeyRects.isEmpty()) return false

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                isGliding = false
                swipedKeys.clear()
                trailPath.reset()
                // Only start tracking if touch began on a letter key
                val startKey = findKeyAt(ev.x.toInt(), ev.y.toInt())
                if (startKey != null) {
                    swipedKeys.append(startKey)
                    trailPath.moveTo(ev.x, ev.y)
                }
                return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Người dùng đang gõ nhanh bằng 2 ngón tay -> hủy chế độ vuốt ngay lập tức
                isGliding = false
                swipedKeys.clear()
                trailPath.reset()
                invalidate()
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (ev.pointerCount > 1) {
                    isGliding = false
                    swipedKeys.clear()
                    trailPath.reset()
                    return false
                }
                if (swipedKeys.isEmpty()) return false
                val dist = hypot(ev.x - downX, ev.y - downY)
                if (dist >= glideThresholdPx) {
                    isGliding = true
                    onGlideStart?.invoke()
                    recordPoint(ev.x, ev.y)
                    return true // Intercept from child button
                }
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isGliding = false
                swipedKeys.clear()
                trailPath.reset()
                return false
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGliding) return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                recordPoint(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                recordPoint(event.x, event.y)
                val result = swipedKeys.toString()
                isGliding = false
                swipedKeys.clear()
                trailPath.reset()
                invalidate()
                if (result.length >= 2) {
                    onGlideComplete?.invoke(result)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isGliding = false
                swipedKeys.clear()
                trailPath.reset()
                invalidate()
            }
        }
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isGliding && showTrail && !trailPath.isEmpty) {
            canvas.drawPath(trailPath, trailPaint)
        }
    }

    private fun recordPoint(x: Float, y: Float) {
        trailPath.lineTo(x, y)
        val key = findKeyAt(x.toInt(), y.toInt())
        if (key != null && (swipedKeys.isEmpty() || swipedKeys.last() != key)) {
            swipedKeys.append(key)
        }
    }

    private fun findKeyAt(x: Int, y: Int): Char? {
        for ((ch, rect) in letterKeyRects) {
            tempRect.set(rect)
            if (tempRect.contains(x, y)) return ch
        }
        return null
    }
}
