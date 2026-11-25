package com.antigravity.vietkeyboard.macro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistent macro store backed by SharedPreferences, preloaded with vnchKey default macros.
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

    fun resetToDefaults(): List<Macro> {
        val defaults = defaultMacros()
        save(defaults)
        return defaults
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
        // Địa danh Việt Nam phổ biến (chuẩn vnchKey)
        Macro("sg", "Sài Gòn"),
        Macro("hn", "Hà Nội"),
        Macro("dn", "Đà Nẵng"),
        Macro("hp", "Hải Phòng"),
        Macro("ct", "Cần Thơ"),
        Macro("dl", "Đà Lạt"),
        Macro("vt", "Vũng Tàu"),
        Macro("bd", "Bình Dương"),
        Macro("đn", "Đồng Nai"),
        Macro("qn", "Quảng Ninh"),
        Macro("hue", "Thừa Thiên Huế"),
        Macro("ntr", "Nha Trang"),
        Macro("hl", "Hạ Long"),
        Macro("hcm", "Hồ Chí Minh"),
        Macro("tphcm", "TP. Hồ Chí Minh"),
        // Từ giao tiếp & đời sống
        Macro("dc", "được"),
        Macro("đc", "được"),
        Macro("ko", "không"),
        Macro("vs", "với"),
        Macro("ng", "người"),
        Macro("ch", "chưa"),
        Macro("nt", "nhắn tin"),
        Macro("kb", "không biết"),
        Macro("tn", "thế nào"),
        Macro("bh", "bây giờ"),
        Macro("tc", "tất cả"),
        // Từ công việc & văn phòng
        Macro("cty", "công ty"),
        Macro("sđt", "số điện thoại"),
        Macro("sdt", "số điện thoại"),
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
        Macro("gd", "giám đốc"),
        // Biến động mẫu
        Macro("addr", "123 Nguyễn Trãi, Hà Nội"),
        Macro("email", "ten@example.com"),
        Macro("shrug", "¯\\_(ツ)_/¯"),
    )

    private companion object {
        const val PREFS_NAME = "viet_keyboard_macros"
        const val KEY_MACROS = "macros_v2"
    }
}
