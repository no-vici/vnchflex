package com.antigravity.vietkeyboard.macro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Simple persistent macro store backed by SharedPreferences.
 * Replace with Room/DataStore for production.
 */
class MacroStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<Macro> {
        val raw = prefs.getString(KEY_MACROS, null) ?: return defaultMacros()
        return runCatching { parse(raw) }.getOrElse { defaultMacros() }
    }

    fun save(macros: List<Macro>) {
        prefs.edit().putString(KEY_MACROS, serialize(macros)).apply()
    }

    private fun parse(raw: String): List<Macro> {
        val arr = JSONArray(raw)
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Macro(
                shortcut = o.getString("shortcut"),
                expansion = o.getString("expansion"),
                caseSensitive = o.optBoolean("caseSensitive", false),
                enabled = o.optBoolean("enabled", true),
            )
        }
    }

    private fun serialize(macros: List<Macro>): String {
        val arr = JSONArray()
        macros.forEach { m ->
            arr.put(JSONObject().apply {
                put("shortcut", m.shortcut)
                put("expansion", m.expansion)
                put("caseSensitive", m.caseSensitive)
                put("enabled", m.enabled)
            })
        }
        return arr.toString()
    }

    private fun defaultMacros(): List<Macro> = listOf(
        Macro("addr", "123 Nguyễn Trãi, Hà Nội"),
        Macro("email", "ten@example.com"),
        Macro("shrug", "¯\\_(ツ)_/¯"),
    )

    private companion object {
        const val PREFS_NAME = "viet_keyboard_macros"
        const val KEY_MACROS = "macros"
    }
}
