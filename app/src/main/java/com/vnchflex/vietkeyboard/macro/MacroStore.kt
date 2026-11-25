package com.vnchflex.vietkeyboard.macro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistent macro store backed by SharedPreferences, preloaded with vnchKey default macros.
 * Caches in memory to avoid JSON parsing on hot paths.
 */
class MacroStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var cache: List<Macro>? = null

    fun load(): List<Macro> {
        cache?.let { return it }
        val raw = prefs.getString(KEY_MACROS, null)
        val loaded = if (raw == null) defaultMacros() else runCatching { parse(raw) }.getOrElse { defaultMacros() }
        cache = loaded
        return loaded
    }

    fun save(macros: List<Macro>) {
        cache = macros.toList()
        prefs.edit().putString(KEY_MACROS, serialize(macros)).apply()
    }

    fun resetToDefaults(): List<Macro> {
        val defaults = defaultMacros()
        save(defaults)
        return defaults
    }

    fun exportJson(): String {
        val obj = JSONObject()
        load().forEach { m -> obj.put(m.shortcut, m.expansion) }
        return obj.toString(2)
    }

    fun importJson(rawJson: String): Int {
        val current = load().toMutableList()
        var importedCount = 0
        val trimmed = rawJson.trim()
        if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next().trim().lowercase()
                val v = obj.optString(k, "").trim()
                if (k.isNotEmpty() && v.isNotEmpty()) {
                    current.removeAll { it.shortcut.equals(k, ignoreCase = true) }
                    current.add(0, Macro(k, v))
                    importedCount++
                }
            }
        } else if (trimmed.startsWith("[")) {
            val parsed = parse(trimmed)
            parsed.forEach { m ->
                current.removeAll { it.shortcut.equals(m.shortcut, ignoreCase = true) }
                current.add(0, m)
                importedCount++
            }
        }
        if (importedCount > 0) save(current)
        return importedCount
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

    fun defaultMacros(): List<Macro> = listOf(
        Macro("sg", "Sài Gòn"),
        Macro("hn", "Hà Nội"),
        Macro("dn", "Đồng Nai"),
        Macro("hp", "hạnh phúc"),
        Macro("ct", "Cần Thơ"),
        Macro("cx", "cảm xúc"),
        Macro("dl", "Đà Lạt"),
        Macro("vt", "Vũng Tàu"),
        Macro("bd", "Bình Dương"),
        Macro("ntr", "Nha Trang"),
        Macro("dc", "được"),
        Macro("ko", "không"),
        Macro("vs", "với"),
        Macro("ng", "người"),
        Macro("ch", "chưa"),
        Macro("nt", "nhắn tin"),
        Macro("tn", "tin nhắn"),
        Macro("ntn", "như thế nào"),
        Macro("kb", "không biết"),
        Macro("bh", "bây giờ"),
        Macro("tca", "tất cả"),
        Macro("tica", "tình cảm"),
        Macro("gd", "gia đình"),
        Macro("tvi", "Tiếng Việt"),
        Macro("cty", "công ty"),
        Macro("sđt", "số điện thoại"),
        Macro("stk", "số tài khoản"),
        Macro("tk", "tài khoản"),
        Macro("kh", "khách hàng"),
        Macro("sp", "sản phẩm"),
        Macro("dv", "dịch vụ"),
        Macro("tg", "thời gian"),
        Macro("tt", "thông tin"),
        Macro("lh", "liên hệ"),
        Macro("kn", "kinh nghiệm"),
        Macro("pt", "phát triển"),
        Macro("nv", "nhân viên"),
        Macro("ql", "quản lý"),
        Macro("email", "name@gmail.com"),
        Macro("shrug", "¯\\_(ツ)_/¯"),
    )

    private companion object {
        const val PREFS_NAME = "viet_keyboard_macros"
        const val KEY_MACROS = "macros_v2"
    }
}
