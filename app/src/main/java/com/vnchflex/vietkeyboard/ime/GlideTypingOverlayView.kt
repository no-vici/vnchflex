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
 * - Draws a smooth Gboard-blue gesture trail when `showTrail` is enabled.
 * - Tracks the sequence of letter keys (`q..m`) traversed and notifies `onGlideComplete`.
 */
class GlideTypingOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var glideEnabled: Boolean = true
    var showTrail: Boolean = true
    var onGlideComplete: ((swipedKeys: String) -> Unit)? = null

    private val letterKeyRects = mutableListOf<Pair<Char, Rect>>()
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
        post {
            letterKeyRects.clear()
            keys.forEach { (btn, letter) ->
                if (letter.length == 1) {
                    val r = Rect()
                    btn.getDrawingRect(r)
                    offsetDescendantRectToMyCoords(btn, r)
                    letterKeyRects.add(letter[0] to r)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!glideEnabled || letterKeyRects.isEmpty()) return false

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
            MotionEvent.ACTION_MOVE -> {
                if (swipedKeys.isEmpty()) return false
                val dist = hypot(ev.x - downX, ev.y - downY)
                if (dist >= glideThresholdPx) {
                    isGliding = true
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
