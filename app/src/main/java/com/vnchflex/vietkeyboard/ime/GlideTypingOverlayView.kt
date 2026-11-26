package com.vnchflex.vietkeyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
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

    private data class TrailPoint(val x: Float, val y: Float, val t: Long)

    private val trailPoints = ArrayDeque<TrailPoint>()
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1A73E8")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val trailLifetimeMs = 350L
    private val trailFadeOutMs = 220L
    private var trailFadeStartMs = 0L
    private val fadeRunnable = object : Runnable {
        override fun run() {
            pruneTrail(SystemClock.uptimeMillis())
            invalidate()
            if (trailPoints.isNotEmpty()) {
                postOnAnimationDelayed(this, 16L)
            }
        }
    }

    var glideThresholdPx: Float = 24f * resources.displayMetrics.density
    private val keyHitPaddingPx = (4f * resources.displayMetrics.density).toInt()
    private val minDwellDistancePx = 8f * resources.displayMetrics.density
    private var lastKeyRecordX = 0f
    private var lastKeyRecordY = 0f
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
                clearTrail()
                // Only start tracking if touch began on a letter key
                val startKey = findKeyAt(ev.x.toInt(), ev.y.toInt())
                if (startKey != null) {
                    swipedKeys.append(startKey)
                    appendTrailPoint(ev.x, ev.y)
                    lastKeyRecordX = ev.x
                    lastKeyRecordY = ev.y
                }
                return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Người dùng đang gõ nhanh bằng 2 ngón tay -> hủy chế độ vuốt ngay lập tức
                isGliding = false
                swipedKeys.clear()
                clearTrail()
                invalidate()
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (ev.pointerCount > 1) {
                    isGliding = false
                    swipedKeys.clear()
                    clearTrail()
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
                clearTrail()
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
                startTrailFadeOut()
                if (result.length >= 2) {
                    onGlideComplete?.invoke(result)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isGliding = false
                swipedKeys.clear()
                clearTrail()
                invalidate()
            }
        }
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!showTrail || trailPoints.size < 2) return
        val now = SystemClock.uptimeMillis()
        pruneTrail(now)
        if (trailPoints.size < 2) return

        val fadeProgress = if (!isGliding && trailFadeStartMs > 0L) {
            ((now - trailFadeStartMs).toFloat() / trailFadeOutMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
        val fadeAlphaMultiplier = 1f - fadeProgress
        if (fadeAlphaMultiplier <= 0f) {
            clearTrail()
            return
        }

        val baseWidth = 10f * resources.displayMetrics.density
        val minWidth = 4.5f * resources.displayMetrics.density
        val points = trailPoints.toList()
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val segProgress = (i + 1).toFloat() / (points.size - 1).coerceAtLeast(1).toFloat()
            val alpha = ((40 + 164 * segProgress) * fadeAlphaMultiplier).toInt().coerceIn(0, 204)
            if (alpha <= 0) continue
            trailPaint.strokeWidth = minWidth + (baseWidth - minWidth) * segProgress
            trailPaint.alpha = alpha
            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, trailPaint)
        }
        trailPaint.alpha = 204
    }

    private fun recordPoint(x: Float, y: Float) {
        appendTrailPoint(x, y)
        val key = findKeyAt(x.toInt(), y.toInt()) ?: return
        if (swipedKeys.isEmpty()) {
            swipedKeys.append(key)
            lastKeyRecordX = x
            lastKeyRecordY = y
            return
        }
        if (swipedKeys.last() != key) {
            val moved = hypot(x - lastKeyRecordX, y - lastKeyRecordY)
            if (moved >= minDwellDistancePx) {
                swipedKeys.append(key)
                lastKeyRecordX = x
                lastKeyRecordY = y
            }
        }
    }

    private fun appendTrailPoint(x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()
        trailPoints.addLast(TrailPoint(x, y, now))
        pruneTrail(now)
    }

    private fun pruneTrail(now: Long) {
        while (trailPoints.isNotEmpty() && now - trailPoints.first().t > trailLifetimeMs) {
            trailPoints.removeFirst()
        }
        if (!isGliding && trailFadeStartMs > 0L && now - trailFadeStartMs >= trailFadeOutMs) {
            trailPoints.clear()
            trailFadeStartMs = 0L
            removeCallbacks(fadeRunnable)
        }
    }

    private fun startTrailFadeOut() {
        trailFadeStartMs = SystemClock.uptimeMillis()
        removeCallbacks(fadeRunnable)
        invalidate()
        postOnAnimationDelayed(fadeRunnable, 16L)
    }

    private fun clearTrail() {
        trailPoints.clear()
        trailFadeStartMs = 0L
        removeCallbacks(fadeRunnable)
    }

    private fun findKeyAt(x: Int, y: Int): Char? {
        var bestChar: Char? = null
        var bestDistSq = Float.MAX_VALUE
        for ((ch, rect) in letterKeyRects) {
            tempRect.set(
                rect.left - keyHitPaddingPx,
                rect.top - keyHitPaddingPx / 2,
                rect.right + keyHitPaddingPx,
                rect.bottom + keyHitPaddingPx / 2
            )
            if (tempRect.contains(x, y)) {
                val dx = x - rect.exactCenterX()
                val dy = y - rect.exactCenterY()
                val distSq = dx * dx + dy * dy
                if (distSq < bestDistSq) {
                    bestDistSq = distSq
                    bestChar = ch
                }
            }
        }
        return bestChar
    }
}
