package com.vnchflex.vietkeyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.vnchflex.vietkeyboard.R

/**
 * Custom Gboard-identical key button:
 * - Draws the primary letter (`Q`, `W`, `E`... or `q`, `w`, `e`...) perfectly centered in the key
 *   with zero multi-line clipping.
 * - Draws the secondary symbol badge (`%`, `\`, `|`, `=`, `[`, `]`, `<`, `>`, `{`, `}`, `@`, `#`, `đ`...)
 *   pinned neatly in the top-right corner of the key, exactly matching Gboard's layout.
 */
class GboardKeyButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    var secondarySymbol: String? = null
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var centerSecondarySymbol: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        val density = resources.displayMetrics.density
        // 1. Let AppCompatButton draw the centered single-line primary letter
        if (centerSecondarySymbol) {
            canvas.save()
            canvas.translate(0f, 5f * density)
            super.onDraw(canvas)
            canvas.restore()
        } else {
            super.onDraw(canvas)
        }

        // 2. Draw the secondary symbol badge (top-right for letters, centered-top for comma+emoji)
        val sec = secondarySymbol
        if (!sec.isNullOrEmpty()) {
            val scaledDensity = resources.displayMetrics.scaledDensity
            symbolPaint.textSize = (if (centerSecondarySymbol) 11f else 10.5f) * scaledDensity
            symbolPaint.color = context.getColor(R.color.key_hint)
            val fm = symbolPaint.fontMetrics
            if (centerSecondarySymbol) {
                symbolPaint.textAlign = Paint.Align.CENTER
                val x = width / 2f
                val y = (5.5f * density) - fm.ascent
                canvas.drawText(sec, x, y, symbolPaint)
            } else {
                symbolPaint.textAlign = Paint.Align.RIGHT
                val x = width - (4.5f * density)
                val y = (3.5f * density) - fm.ascent
                canvas.drawText(sec, x, y, symbolPaint)
            }
        }
    }
}
