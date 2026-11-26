package com.vnchflex.vietkeyboard.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Gboard-identical Preferences store + vnchKey Vietnamese typing & Macro preferences.
 */
class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ==================== GBOARD: PREFERENCES (TÙY CHỌN) ====================
    /** Hàng phím số (Number row) - hiển thị hàng số 1..0 cố định phía trên */
    var showNumberRow: Boolean
        get() = prefs.getBoolean("pref_number_row", true)
        set(v) = prefs.edit().putBoolean("pref_number_row", v).apply()

    /** Hiện phím chuyển đổi ngôn ngữ (Show language switch key) */
    var showLanguageSwitchKey: Boolean
        get() = prefs.getBoolean("pref_show_language_switch_key", true)
        set(v) = prefs.edit().putBoolean("pref_show_language_switch_key", v).apply()

    /** Hiện phím biểu tượng cảm xúc (Show emoji switch key) */
    var showEmojiSwitchKey: Boolean
        get() = prefs.getBoolean("pref_show_emoji_switch_key", false)
        set(v) = prefs.edit().putBoolean("pref_show_emoji_switch_key", v).apply()

    /** Nhấn giữ để lấy ký hiệu (Long press for symbols) */
    var longPressForSymbols: Boolean
        get() = prefs.getBoolean("pref_long_press_symbols", true)
        set(v) = prefs.edit().putBoolean("pref_long_press_symbols", v).apply()

    /** Chiều cao bàn phím: "extra_short", "short", "mid_short", "normal", "mid_tall", "tall", "extra_tall" */
    var keyboardHeight: String
        get() = prefs.getString("pref_keyboard_height", "normal") ?: "normal"
        set(v) = prefs.edit().putString("pref_keyboard_height", v).apply()

    /** Chế độ một tay: "off", "left", "right" */
    var oneHandedMode: String
        get() = prefs.getString("pref_one_handed_mode", "off") ?: "off"
        set(v) = prefs.edit().putString("pref_one_handed_mode", v).apply()

    /** Phát âm thanh khi nhấn phím (Sound on keypress) */
    var soundOnKeypress: Boolean
        get() = prefs.getBoolean("pref_sound_on_keypress", false)
        set(v) = prefs.edit().putBoolean("pref_sound_on_keypress", v).apply()

    /** Âm lượng khi nhấn phím (0..100, -1 = Mặc định hệ thống) */
    var soundVolume: Int
        get() = prefs.getInt("pref_sound_volume", 50)
        set(v) = prefs.edit().putInt("pref_sound_volume", v).apply()

    /** Rung khi nhấn phím (Haptic feedback on keypress) */
    var hapticOnKeypress: Boolean
        get() = prefs.getBoolean("pref_haptic_on_keypress", true)
        set(v) = prefs.edit().putBoolean("pref_haptic_on_keypress", v).apply()

    /** Cường độ rung khi nhấn phím (ms, 0..100) */
    var vibrationStrengthMs: Int
        get() = prefs.getInt("pref_vibration_strength_ms", 15)
        set(v) = prefs.edit().putInt("pref_vibration_strength_ms", v).apply()

    /** Phóng to khi nhấn phím (Popup on keypress) */
    var popupOnKeypress: Boolean
        get() = prefs.getBoolean("pref_popup_on_keypress", true)
        set(v) = prefs.edit().putBoolean("pref_popup_on_keypress", v).apply()

    /** Độ trễ nhấn giữ phím (Key long press delay, ms, mặc định Gboard 300ms) */
    var longPressDelayMs: Int
        get() = prefs.getInt("pref_long_press_delay_ms", 300)
        set(v) = prefs.edit().putInt("pref_long_press_delay_ms", v).apply()

    // ==================== GBOARD: THEME (GIAO DIỆN) ====================
    /** Giao diện bàn phím: "gboard_light", "gboard_dark", "material_you" */
    var themeMode: String
        get() = prefs.getString("pref_theme_mode", "gboard_light") ?: "gboard_light"
        set(v) = prefs.edit().putString("pref_theme_mode", v).apply()

    /** Đường viền phím (Key borders) */
    var keyBorders: Boolean
        get() = prefs.getBoolean("pref_key_borders", true)
        set(v) = prefs.edit().putBoolean("pref_key_borders", v).apply()

    // ==================== GBOARD: TEXT CORRECTION (SỬA VĂN BẢN) ====================
    /** Hiển thị dải đề xuất (Show suggestion strip) */
    var showSuggestionStrip: Boolean
        get() = prefs.getBoolean("pref_show_suggestion_strip", true)
        set(v) = prefs.edit().putBoolean("pref_show_suggestion_strip", v).apply()

    /** Đề xuất từ tiếp theo (Next-word suggestions) */
    var nextWordSuggestions: Boolean
        get() = prefs.getBoolean("pref_next_word_suggestions", true)
        set(v) = prefs.edit().putBoolean("pref_next_word_suggestions", v).apply()

    /** Chặn từ phản cảm (Block offensive words) */
    var blockOffensiveWords: Boolean
        get() = prefs.getBoolean("pref_block_offensive_words", true)
        set(v) = prefs.edit().putBoolean("pref_block_offensive_words", v).apply()

    /** Hiển thị đề xuất biểu tượng cảm xúc (Show emoji suggestions) */
    var showEmojiSuggestions: Boolean
        get() = prefs.getBoolean("pref_show_emoji_suggestions", true)
        set(v) = prefs.edit().putBoolean("pref_show_emoji_suggestions", v).apply()

    /** Đề xuất danh bạ (Suggest contacts) */
    var suggestContacts: Boolean
        get() = prefs.getBoolean("pref_suggest_contacts", false)
        set(v) = prefs.edit().putBoolean("pref_suggest_contacts", v).apply()

    /** Tự động sửa (Auto-correction) */
    var autoCorrection: Boolean
        get() = prefs.getBoolean("pref_auto_correction", true)
        set(v) = prefs.edit().putBoolean("pref_auto_correction", v).apply()

    /** Hoàn tác tự động sửa khi nhấn phím xóa lùi (Undo auto-correct on backspace) */
    var undoAutoCorrectOnBackspace: Boolean
        get() = prefs.getBoolean("pref_undo_autocorrect_on_backspace", true)
        set(v) = prefs.edit().putBoolean("pref_undo_autocorrect_on_backspace", v).apply()

    /** Tự động thêm dấu cách sau dấu câu (Auto-space after punctuation) */
    var autoSpaceAfterPunctuation: Boolean
        get() = prefs.getBoolean("pref_auto_space_punctuation", false)
        set(v) = prefs.edit().putBoolean("pref_auto_space_punctuation", v).apply()

    /** Tự động viết hoa (Auto-capitalization) */
    var autoCapitalization: Boolean
        get() = prefs.getBoolean("pref_auto_capitalization", true)
        set(v) = prefs.edit().putBoolean("pref_auto_capitalization", v).apply()

    /** Nhấn đúp dấu cách để thêm dấu chấm (Double-space period) */
    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean("pref_double_space_period", true)
        set(v) = prefs.edit().putBoolean("pref_double_space_period", v).apply()

    /** Kiểm tra chính tả (Spell check) */
    var spellCheck: Boolean
        get() = prefs.getBoolean("pref_spell_check", true)
        set(v) = prefs.edit().putBoolean("pref_spell_check", v).apply()

    // ==================== GBOARD: GLIDE TYPING / VOICE / CLIPBOARD ====================
    var enableGlideTyping: Boolean
        get() = prefs.getBoolean("pref_glide_typing", true)
        set(v) = prefs.edit().putBoolean("pref_glide_typing", v).apply()

    var showGestureTrail: Boolean
        get() = prefs.getBoolean("pref_gesture_trail", true)
        set(v) = prefs.edit().putBoolean("pref_gesture_trail", v).apply()

    /** Độ nhạy vuốt Glide Typing (ngưỡng kích hoạt dp: 14 = rất nhạy, 24 = mặc định, 36 = ít nhạy) */
    var glideSensitivityDp: Int
        get() = prefs.getInt("pref_glide_sensitivity_dp", 24)
        set(v) = prefs.edit().putInt("pref_glide_sensitivity_dp", v.coerceIn(14, 36)).apply()

    var enableGestureDelete: Boolean
        get() = prefs.getBoolean("pref_gesture_delete", true)
        set(v) = prefs.edit().putBoolean("pref_gesture_delete", v).apply()

    var enableGestureCursorControl: Boolean
        get() = prefs.getBoolean("pref_gesture_cursor", true)
        set(v) = prefs.edit().putBoolean("pref_gesture_cursor", v).apply()

    var enableVoiceTyping: Boolean
        get() = prefs.getBoolean("pref_voice_typing", true)
        set(v) = prefs.edit().putBoolean("pref_voice_typing", v).apply()

    var enableClipboardHistory: Boolean
        get() = prefs.getBoolean("pref_clipboard_history", true)
        set(v) = prefs.edit().putBoolean("pref_clipboard_history", v).apply()

    // ==================== VNCHKEY: BỘ GÕ TIẾNG VIỆT TELEX & BỎ DẤU TỰ DO & MACRO ====================
    /** Bỏ dấu tự do theo phong cách vnchKey (cho phép gõ dấu ở cuối từ sau phụ âm cuối hoặc ngay sau nguyên âm) */
    var freeTonePlacement: Boolean
        get() = prefs.getBoolean("pref_vn_free_tone", true)
        set(v) = prefs.edit().putBoolean("pref_vn_free_tone", v).apply()

    /** Kiểu đặt dấu mới (oà, oè, uý) vs kiểu cũ/cổ điển (òa, òe, úy) */
    var modernOrthography: Boolean
        get() = prefs.getBoolean("pref_vn_modern_orthography", true)
        set(v) = prefs.edit().putBoolean("pref_vn_modern_orthography", v).apply()

    /** Tốc gõ phụ âm đầu (cc -> ch, gg -> gi, qq -> qu) */
    var quickTelex: Boolean
        get() = prefs.getBoolean("pref_vn_quick_telex", false)
        set(v) = prefs.edit().putBoolean("pref_vn_quick_telex", v).apply()

    /** Tốc gõ phụ âm cuối (-g -> -ng, -h -> -nh, -k -> -c) */
    var relaxedCoda: Boolean
        get() = prefs.getBoolean("pref_vn_relaxed_coda", false)
        set(v) = prefs.edit().putBoolean("pref_vn_relaxed_coda", v).apply()

    /** Phương ngữ Nam Bộ & Phụ âm mở rộng (dz/f/j/w/z: dzìa, dzách, dzô, zậy, fải, wá...) */
    var southernDialectAndGenZ: Boolean
        get() = prefs.getBoolean("pref_vn_southern_genz", true)
        set(v) = prefs.edit().putBoolean("pref_vn_southern_genz", v).apply()

    /** Bật Macro / Bảng gõ tắt (vnchKey) */
    var macroEnabled: Boolean
        get() = prefs.getBoolean("pref_macro_enabled", true)
        set(v) = prefs.edit().putBoolean("pref_macro_enabled", v).apply()

    /** Danh sách từ ngoại lệ (whitelist) do người dùng thêm để không chuyển sang Tiếng Việt */
    var englishWhitelist: Set<String>
        get() = prefs.getStringSet("pref_english_whitelist", emptySet())?.toSet() ?: emptySet()
        set(v) = prefs.edit().putStringSet(
            "pref_english_whitelist",
            v.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        ).apply()

    fun keyHeightScale(): Float = when (keyboardHeight) {
        "extra_short" -> 0.82f
        "short" -> 0.90f
        "mid_short" -> 0.95f
        "normal" -> 1.00f
        "mid_tall" -> 1.06f
        "tall" -> 1.12f
        "extra_tall" -> 1.20f
        else -> 1.00f
    }

    companion object {
        const val PREFS_NAME = "vnchflex_gboard_prefs"
    }
}
