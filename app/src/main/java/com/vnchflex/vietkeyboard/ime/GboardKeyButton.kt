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

    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Let AppCompatButton draw the centered single-line primary letter
        super.onDraw(canvas)

        // 2. Draw the secondary symbol badge in the top-right corner of the key (Gboard style)
        val sec = secondarySymbol
        if (!sec.isNullOrEmpty()) {
            val density = resources.displayMetrics.density
            val scaledDensity = resources.displayMetrics.scaledDensity
            symbolPaint.textSize = 10.5f * scaledDensity
            symbolPaint.color = context.getColor(R.color.key_hint)

            val x = width - (4.5f * density)
            val fm = symbolPaint.fontMetrics
            val y = (3.5f * density) - fm.ascent
            canvas.drawText(sec, x, y, symbolPaint)
        }
    }
}
