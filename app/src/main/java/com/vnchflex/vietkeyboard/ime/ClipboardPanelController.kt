package com.vnchflex.vietkeyboard.ime

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.vnchflex.vietkeyboard.R
import com.vnchflex.vietkeyboard.settings.KeyboardPreferences

/**
 * Renders and manages the Gboard-style inline Clipboard panel inside keyboard_root.
 */
class ClipboardPanelController(
    private val context: Context,
    private val prefs: KeyboardPreferences,
    private val store: ClipboardStore,
    private val onPaste: (String) -> Unit,
    private val onClose: () -> Unit,
    private val onCaptureCurrentClip: () -> Unit,
    private val onHaptic: (View) -> Unit
) {
    private val density = context.resources.displayMetrics.density
    private var editMode = false
    private var panelView: LinearLayout? = null

    fun createOrUpdateView(targetHeightPx: Int): View {
        val root = panelView ?: LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.getColor(R.color.keyboard_bg))
            setPadding(dp(6), dp(4), dp(6), dp(6))
        }.also { panelView = it }

        root.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (targetHeightPx > 0) targetHeightPx else dp(260)
        )
        render(root)
        return root
    }

    fun resetEditMode() {
        editMode = false
    }

    private fun render(root: LinearLayout) {
        root.removeAllViews()
        val items = if (prefs.enableClipboardHistory) store.getItems() else emptyList()
        if (!prefs.enableClipboardHistory || items.isEmpty()) {
            editMode = false
        }

        // 1. Top Header Bar (<- Clipboard   [Switch]  [Edit])
        val topBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44))
            setPadding(dp(2), 0, dp(4), 0)
        }

        val btnBack = TextView(context).apply {
            text = "←"
            textSize = 20f
            setTextColor(context.getColor(R.color.key_text))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
            setOnClickListener {
                onHaptic(this)
                editMode = false
                onClose()
            }
        }

        val title = TextView(context).apply {
            text = "Bảng nhớ tạm"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(context.getColor(R.color.key_text))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(4)
            }
        }

        @Suppress("UseSwitchCompatOrMaterialCode")
        val toggle = Switch(context).apply {
            isChecked = prefs.enableClipboardHistory
            setOnCheckedChangeListener { _, isChecked ->
                onHaptic(this)
                prefs.enableClipboardHistory = isChecked
                if (isChecked) {
                    onCaptureCurrentClip()
                } else {
                    editMode = false
                }
                render(root)
            }
        }

        val canEdit = prefs.enableClipboardHistory && items.isNotEmpty()
        val btnEdit = TextView(context).apply {
            text = if (editMode) "✓" else "✎"
            textSize = 18f
            gravity = Gravity.CENTER
            isEnabled = canEdit
            alpha = if (canEdit) 1f else 0.32f
            setTextColor(
                if (editMode) Color.parseColor("#1A73E8")
                else context.getColor(R.color.key_text)
            )
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply {
                marginStart = dp(4)
            }
            setOnClickListener {
                if (!canEdit) return@setOnClickListener
                onHaptic(this)
                editMode = !editMode
                render(root)
            }
        }

        topBar.addView(btnBack)
        topBar.addView(title)
        topBar.addView(toggle)
        topBar.addView(btnEdit)
        root.addView(topBar)

        // 2. Content Area
        val scroll = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        if (!prefs.enableClipboardHistory) {
            content.addView(buildOnboardingCard(root))
        } else if (items.isEmpty()) {
            content.addView(buildTipsSection())
        } else {
            content.addView(buildItemsGrid(root, items))
        }

        scroll.addView(content)
        root.addView(scroll)
    }

    private fun buildOnboardingCard(root: LinearLayout): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedCardBg(context.getColor(R.color.key_bg), 16f)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        val header = TextView(context).apply {
            text = "📋  Sao chép nhiều mục, dán nhanh"
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setTextColor(context.getColor(R.color.key_text))
        }

        val body = TextView(context).apply {
            text = "Bảng nhớ tạm lưu các đoạn văn bản bạn sao chép trong 1 giờ để dán nhanh nhiều mục cùng lúc."
            textSize = 13f
            setTextColor(context.getColor(R.color.key_text))
            alpha = 0.75f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(14)
            }
        }

        val btnRow = LinearLayout(context).apply {
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val btnTurnOn = Button(context).apply {
            text = "Bật bảng nhớ tạm"
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            background = roundedCardBg(Color.parseColor("#1A73E8"), 20f)
            setPadding(dp(18), dp(8), dp(18), dp(8))
            minHeight = 0
            minimumHeight = 0
            setOnClickListener {
                onHaptic(this)
                prefs.enableClipboardHistory = true
                onCaptureCurrentClip()
                render(root)
            }
        }

        btnRow.addView(btnTurnOn)
        card.addView(header)
        card.addView(body)
        card.addView(btnRow)
        return card
    }

    private fun buildTipsSection(): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val header = TextView(context).apply {
            text = "MẸO"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(context.getColor(R.color.key_text))
            alpha = 0.6f
            setPadding(dp(4), dp(2), dp(4), dp(6))
        }
        container.addView(header)

        val tips = listOf(
            "📋  Chạm vào một mục để dán vào ô nhập liệu.",
            "📌  Nhấn giữ một mục để ghim. Mục chưa ghim sẽ tự xóa sau 1 giờ.",
            "✎  Dùng biểu tượng bút chì để ghim hoặc xóa các mục."
        )
        container.addView(buildTwoColumnCards(tips.map { tip ->
            TextView(context).apply {
                text = tip
                textSize = 13f
                setTextColor(context.getColor(R.color.key_text))
                background = roundedCardBg(context.getColor(R.color.key_bg), 12f)
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
        }))
        return container
    }

    private fun buildItemsGrid(root: LinearLayout, items: List<ClipboardItem>): View {
        val cards = items.map { item -> buildClipCard(root, item) }
        return buildTwoColumnCards(cards)
    }

    private fun buildClipCard(root: LinearLayout, item: ClipboardItem): View {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedCardBg(
                context.getColor(R.color.key_bg),
                12f,
                strokeColor = if (item.isPinned) Color.parseColor("#1A73E8") else null
            )
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }

        if (item.isPinned || editMode) {
            val actionRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(4) }
            }

            val pinBtn = TextView(context).apply {
                text = if (item.isPinned) "📌" else "📍"
                textSize = 13f
                alpha = if (item.isPinned) 1f else 0.45f
                setPadding(dp(2), 0, dp(6), 0)
                setOnClickListener {
                    if (!editMode) return@setOnClickListener
                    onHaptic(this)
                    store.togglePin(item.id)
                    render(root)
                }
            }
            actionRow.addView(pinBtn)

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
            }
            actionRow.addView(spacer)

            if (editMode) {
                val deleteBtn = TextView(context).apply {
                    text = "✕"
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#D93025"))
                    setPadding(dp(6), 0, dp(2), 0)
                    setOnClickListener {
                        onHaptic(this)
                        store.delete(item.id)
                        render(root)
                    }
                }
                actionRow.addView(deleteBtn)
            }
            card.addView(actionRow)
        }

        val textView = TextView(context).apply {
            text = item.text.trim()
            textSize = 13.5f
            maxLines = 4
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(context.getColor(R.color.key_text))
        }
        card.addView(textView)

        card.setOnClickListener {
            onHaptic(card)
            if (editMode) {
                store.togglePin(item.id)
                render(root)
            } else {
                onPaste(item.text)
                onClose()
            }
        }
        card.setOnLongClickListener {
            onHaptic(card)
            store.togglePin(item.id)
            render(root)
            true
        }
        return card
    }

    private fun buildTwoColumnCards(cards: List<View>): View {
        val grid = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        var i = 0
        while (i < cards.size) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            val left = cards[i].apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginEnd = dp(4)
                }
            }
            row.addView(left)

            if (i + 1 < cards.size) {
                val right = cards[i + 1].apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                        marginStart = dp(4)
                    }
                }
                row.addView(right)
            } else {
                val empty = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f).apply {
                        marginStart = dp(4)
                    }
                }
                row.addView(empty)
            }
            grid.addView(row)
            i += 2
        }
        return grid
    }

    private fun roundedCardBg(bgColor: Int, radiusDp: Float, strokeColor: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * density
            setColor(bgColor)
            if (strokeColor != null) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int = (value * density).toInt()
}
