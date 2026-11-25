package com.vnchflex.vietkeyboard.macro

/** A text-expansion rule triggered by a shortcut token. */
data class Macro(
    val shortcut: String,
    val expansion: String,
    val caseSensitive: Boolean = false,
    val enabled: Boolean = true,
)
