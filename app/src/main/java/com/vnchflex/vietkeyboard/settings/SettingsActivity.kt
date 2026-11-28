package com.vnchflex.vietkeyboard.settings

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vnchflex.vietkeyboard.R
import com.vnchflex.vietkeyboard.macro.Macro
import com.vnchflex.vietkeyboard.macro.MacroStore
import com.vnchflex.vietkeyboard.suggestion.PersonalDictionaryStore
import com.vnchflex.vietkeyboard.suggestion.PersonalWord
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gboard-identical Settings Activity + vnchKey Free Diacritics & Macro Editor.
 *
 * Menu structure identical to Gboard:
 * 1. Ngôn ngữ & Kiểu gõ (Languages & vnchKey Free Diacritics: Telex Tự Do, VNI, Dấu trước âm SenFlex, Chính tả)
 * 2. Tùy chọn (Preferences: Hàng phím số, Phím chuyển ngôn ngữ, Nhấn giữ lấy ký hiệu, Chiều cao, Một tay, Âm thanh, Rung, Phóng to phím, Độ trễ nhấn giữ)
 * 3. Giao diện (Theme: Gboard Light, Gboard Dark, Material You, Đường viền phím)
 * 4. Sửa văn bản (Text correction: Dải đề xuất, Đề xuất từ tiếp theo, Chặn từ phản cảm, Tự động sửa, Hoàn tác khi xóa lùi, Tự động viết hoa, Nhấn đúp dấu cách)
 * 5. Từ điển (Dictionary: Từ điển cá nhân - Personal dictionary, Thêm/Sửa/Xóa từ)
 * 6. Bảng gõ tắt / Macro (vnchKey Personal Shorthand & Macro Editor: Thêm/Sửa/Xóa, Khôi phục mặc định)
 * 7. Nhập bằng cử chỉ (Glide typing)
 * 8. Nhập bằng giọng nói & Bảng nhớ tạm (Voice typing & Clipboard)
 * 9. Nâng cao (Advanced: Xóa các từ đã học)
 * 10. Giới thiệu (About vnchFlex)
 */
class SettingsActivity : AppCompatActivity() {
    private enum class Screen {
        MAIN,
        LANGUAGES_VNCHKEY,
        PREFERENCES,
        THEME,
        TEXT_CORRECTION,
        DICTIONARY,
        PERSONAL_DICTIONARY,
        ENGLISH_WHITELIST,
        MACRO_EDITOR,
        GLIDE_TYPING,
        VOICE_CLIPBOARD,
        BACKUP_RESTORE,
        ADVANCED,
        ABOUT
    }

    private lateinit var prefs: KeyboardPreferences
    private lateinit var macroStore: MacroStore
    private lateinit var dictStore: PersonalDictionaryStore

    private lateinit var container: LinearLayout
    private lateinit var toolbarTitle: TextView
    private lateinit var btnBack: ImageView
    private var currentScreen = Screen.MAIN

    private var pendingExportJson: String? = null
    private val exportBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri != null && json != null) writeBackupToUri(uri, json)
    }
    private val importBackupLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) readBackupFromUri(uri)
    }
    private val importCorpusFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                contentResolver.openInputStream(uri)?.use { it.bufferedReader(Charsets.UTF_8).readText() }.orEmpty()
            }.onSuccess { text ->
                processCorpusText(text)
            }.onFailure { e ->
                Toast.makeText(this, "Không đọc được file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val importOnnxModelLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val modelsDir = java.io.File(filesDir, "models").apply { mkdirs() }
                val dest = java.io.File(modelsDir, "vi_corrector.onnx")
                contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Không mở được file")
                dest
            }.onSuccess { dest ->
                val sizeMb = dest.length() / (1024f * 1024f)
                Toast.makeText(this, String.format("Đã nạp model ONNX (%.2f MB)", sizeMb), Toast.LENGTH_SHORT).show()
                navigateTo(Screen.ADVANCED)
            }.onFailure { e ->
                Toast.makeText(this, "Lỗi nạp model: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = KeyboardPreferences(this)
        macroStore = MacroStore(this)
        dictStore = PersonalDictionaryStore(this)

        container = findViewById(R.id.settings_container)
        toolbarTitle = findViewById(R.id.tv_toolbar_title)
        btnBack = findViewById(R.id.btn_nav_back)

        btnBack.setOnClickListener { navigateTo(Screen.MAIN) }

        findViewById<Button>(R.id.btn_open_ime_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        val initialSection = intent?.getStringExtra("section")
        when (initialSection) {
            "macro" -> navigateTo(Screen.MACRO_EDITOR)
            "dictionary" -> navigateTo(Screen.PERSONAL_DICTIONARY)
            "theme" -> navigateTo(Screen.THEME)
            else -> navigateTo(Screen.MAIN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentScreen == Screen.PERSONAL_DICTIONARY || currentScreen == Screen.ENGLISH_WHITELIST) {
            navigateTo(Screen.DICTIONARY)
        } else if (currentScreen != Screen.MAIN) {
            navigateTo(Screen.MAIN)
        } else {
            super.onBackPressed()
        }
    }

    private fun navigateTo(screen: Screen) {
        currentScreen = screen
        container.removeAllViews()
        btnBack.visibility = if (screen == Screen.MAIN) View.GONE else View.VISIBLE

        when (screen) {
            Screen.MAIN -> renderMainScreen()
            Screen.LANGUAGES_VNCHKEY -> renderLanguagesVnchKeyScreen()
            Screen.PREFERENCES -> renderPreferencesScreen()
            Screen.THEME -> renderThemeScreen()
            Screen.TEXT_CORRECTION -> renderTextCorrectionScreen()
            Screen.DICTIONARY -> renderDictionaryScreen()
            Screen.PERSONAL_DICTIONARY -> renderPersonalDictionaryScreen()
            Screen.ENGLISH_WHITELIST -> renderEnglishWhitelistScreen()
            Screen.MACRO_EDITOR -> renderMacroEditorScreen()
            Screen.GLIDE_TYPING -> renderGlideTypingScreen()
            Screen.VOICE_CLIPBOARD -> renderVoiceClipboardScreen()
            Screen.BACKUP_RESTORE -> renderBackupRestoreScreen()
            Screen.ADVANCED -> renderAdvancedScreen()
            Screen.ABOUT -> renderAboutScreen()
        }
    }

    // ==================== MAIN MENU (IDENTICAL TO GBOARD + VNCHKEY) ====================
    private fun renderMainScreen() {
        toolbarTitle.text = "Cài đặt vnchFlex (Gboard)"

        addSectionHeader("Kích hoạt nhanh")
        addActionItem(
            title = "Chọn bàn phím (IME Picker)",
            subtitle = "Chuyển sang bàn phím vnchFlex ngay lập tức"
        ) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        addSectionHeader("Cài đặt bàn phím Gboard & vnchKey")
        addNavRow(
            title = "Ngôn ngữ & Bỏ dấu tự do (Telex)",
            subtitle = "Tiếng Việt (Telex) • Bỏ dấu tự do theo phong cách vnchKey",
            target = Screen.LANGUAGES_VNCHKEY
        )
        addNavRow(
            title = "Tùy chọn (Preferences)",
            subtitle = "Hàng phím số, nhấn giữ lấy ký hiệu, chiều cao, âm thanh, rung",
            target = Screen.PREFERENCES
        )
        addNavRow(
            title = "Giao diện (Theme)",
            subtitle = "Gboard Light / Dark, đường viền phím",
            target = Screen.THEME
        )
        addNavRow(
            title = "Sửa văn bản (Text correction)",
            subtitle = "Dải đề xuất, tự động sửa, tự động viết hoa, chặn từ phản cảm",
            target = Screen.TEXT_CORRECTION
        )
        addNavRow(
            title = "Từ điển (Dictionary)",
            subtitle = "Từ điển cá nhân (${dictStore.loadPersonalWords().size} từ), đồng bộ & từ đã học",
            target = Screen.DICTIONARY
        )
        val macroCount = macroStore.load().size
        addNavRow(
            title = "Bảng gõ tắt / Macro (vnchKey)",
            subtitle = if (prefs.macroEnabled) "Đang bật • $macroCount cụm từ viết tắt" else "Đang tắt",
            target = Screen.MACRO_EDITOR
        )
        addNavRow(
            title = "Nhập bằng cử chỉ (Glide typing)",
            subtitle = "Vuốt để nhập, vệt cử chỉ, vuốt xóa từ, di chuyển con trỏ",
            target = Screen.GLIDE_TYPING
        )
        addNavRow(
            title = "Nhập bằng giọng nói & Bảng nhớ tạm",
            subtitle = "Voice typing & Clipboard history",
            target = Screen.VOICE_CLIPBOARD
        )
        addNavRow(
            title = "Sao lưu & khôi phục (Backup & restore)",
            subtitle = "Xuất/nhập dữ liệu đã học, từ điển, macro, cài đặt",
            target = Screen.BACKUP_RESTORE
        )
        addNavRow(
            title = "Nâng cao (Advanced)",
            subtitle = "Xóa các từ đã học và đặt lại cài đặt",
            target = Screen.ADVANCED
        )
        addNavRow(
            title = "Giới thiệu (About)",
            subtitle = "vnchFlex v1.0.0 • Giao diện Gboard + Lõi vnchKey",
            target = Screen.ABOUT
        )
    }

    // ==================== 1. LANGUAGES & VNCHKEY FREE DIACRITICS (TELEX ONLY) ====================
    private fun renderLanguagesVnchKeyScreen() {
        toolbarTitle.text = "Ngôn ngữ & Bỏ dấu tự do"

        addSectionHeader("Bàn phím Tiếng Việt (Telex)")
        addActionItem(
            title = "Kiểu gõ: Tiếng Việt (Telex)",
            subtitle = "Dấu thanh: s (sắc), f (huyền), r (hỏi), x (ngã), j (nặng), z (xóa dấu) • Mũ/móc: aa, ee, oo, aw, ow, uw, dd"
        ) {}

        addSectionHeader("Bỏ dấu tự do (Phong cách vnchKey)")
        addSwitchItem(
            title = "Bỏ dấu tự do (Free tone placement)",
            subtitle = "Cho phép gõ dấu thanh và dấu mũ/móc tự do ở cuối từ sau phụ âm cuối hoặc ngay sau nguyên âm (vd: vieetj hoặc vietej → việt, truwongr hoặc truongwr → trưởng)",
            checked = prefs.freeTonePlacement
        ) { prefs.freeTonePlacement = it }

        addSwitchItem(
            title = "Đặt dấu kiểu mới (oà, oè, uý)",
            subtitle = "Tắt để dùng kiểu cũ/cổ điển (òa, òe, úy)",
            checked = prefs.modernOrthography
        ) { prefs.modernOrthography = it }

        addSectionHeader("Phương ngữ Nam Bộ & Tốc gõ phụ âm (vnchKey)")
        addSwitchItem(
            title = "Phương ngữ Nam Bộ & Phụ âm mở rộng (dz/f/j/w/z)",
            subtitle = "Cho phép gõ các từ Nam Bộ như dzìa, dzách, dzô, dzậy, dzọt, zậy, fải, wá... giống vnchKey",
            checked = prefs.southernDialectAndGenZ
        ) { prefs.southernDialectAndGenZ = it }

        addSwitchItem(
            title = "Tốc gõ phụ âm đầu (cc/gg/qq)",
            subtitle = "Gõ nhanh cc → ch, gg → gi, qq → qu",
            checked = prefs.quickTelex
        ) { prefs.quickTelex = it }

        addSwitchItem(
            title = "Tốc gõ phụ âm cuối (-g/-h/-k)",
            subtitle = "Gõ nhanh ở cuối âm tiết: -g → -ng, -h → -nh, -k → -c",
            checked = prefs.relaxedCoda
        ) { prefs.relaxedCoda = it }
    }

    // ==================== 2. GBOARD PREFERENCES (TÙY CHỌN) ====================
    private fun renderPreferencesScreen() {
        toolbarTitle.text = "Tùy chọn (Preferences)"

        addSectionHeader("Phím (Keys)")
        addSwitchItem(
            title = "Hàng phím số (Number row)",
            subtitle = "Luôn hiển thị hàng phím số 1–0 ở hàng trên cùng (chỉ hiển thị số giống Gboard)",
            checked = prefs.showNumberRow
        ) { prefs.showNumberRow = it }

        addSwitchItem(
            title = "Hiện phím chuyển đổi ngôn ngữ",
            subtitle = "Hiển thị nút chuyển ngôn ngữ ở hàng dưới cùng",
            checked = prefs.showLanguageSwitchKey
        ) { prefs.showLanguageSwitchKey = it }

        val langIconOptions = listOf(
            "Logo vnchFlex (mặc định)" to "logo",
            "V/E VnCharKey (theo ngôn ngữ)" to "ev",
            "Globe" to "globe",
            "Logo VnCharKey" to "vncharkey_logo",
        )
        val currentLangIconLabel = langIconOptions.firstOrNull { it.second == prefs.langSwitchKeyIcon }?.first
            ?: "Logo vnchFlex (mặc định)"
        addSingleChoiceItem(
            title = "Biểu tượng phím chuyển ngôn ngữ",
            currentValue = currentLangIconLabel,
            options = langIconOptions,
            selectedKey = prefs.langSwitchKeyIcon
        ) {
            prefs.langSwitchKeyIcon = it
            navigateTo(Screen.PREFERENCES)
        }

        addSwitchItem(
            title = "Hiện nút \"Bỏ dấu\" trên thanh gợi ý",
            subtitle = "Hiển thị nút Bỏ dấu nhanh ở góc trái thanh công cụ (tắt mặc định để rộng chỗ cho gợi ý từ)",
            checked = prefs.showStripDiacriticsButton
        ) { prefs.showStripDiacriticsButton = it }

        addSwitchItem(
            title = "Nhấn giữ để lấy ký hiệu (Long press for symbols)",
            subtitle = "Hiển thị ký hiệu phụ trên phím chữ và nhấn giữ để nhập trực tiếp ký hiệu đó",
            checked = prefs.longPressForSymbols
        ) { prefs.longPressForSymbols = it }

        addSectionHeader("Bố cục (Layout)")
        val heightOptions = listOf(
            "Rất thấp (Extra-short)" to "extra_short",
            "Thấp (Short)" to "short",
            "Hơi thấp (Mid-short)" to "mid_short",
            "Bình thường (Normal)" to "normal",
            "Hơi cao (Mid-tall)" to "mid_tall",
            "Cao (Tall)" to "tall",
            "Rất cao (Extra-tall)" to "extra_tall",
        )
        val currentHeightLabel = heightOptions.firstOrNull { it.second == prefs.keyboardHeight }?.first ?: "Bình thường (Normal)"
        addSingleChoiceItem(
            title = "Chiều cao bàn phím (Keyboard height)",
            currentValue = currentHeightLabel,
            options = heightOptions,
            selectedKey = prefs.keyboardHeight
        ) {
            prefs.keyboardHeight = it
            navigateTo(Screen.PREFERENCES)
        }

        val oneHandedOptions = listOf(
            "Tắt (Off)" to "off",
            "Tay trái (Left-handed)" to "left",
            "Tay phải (Right-handed)" to "right",
        )
        val currentOneHandedLabel = oneHandedOptions.firstOrNull { it.second == prefs.oneHandedMode }?.first ?: "Tắt (Off)"
        addSingleChoiceItem(
            title = "Chế độ một tay (One-handed mode)",
            currentValue = currentOneHandedLabel,
            options = oneHandedOptions,
            selectedKey = prefs.oneHandedMode
        ) {
            prefs.oneHandedMode = it
            navigateTo(Screen.PREFERENCES)
        }

        addSectionHeader("Nhấn phím (Key press)")
        addSwitchItem(
            title = "Phát âm thanh khi nhấn phím",
            subtitle = "Sound on keypress",
            checked = prefs.soundOnKeypress
        ) { prefs.soundOnKeypress = it }

        addSliderItem(
            title = "Âm lượng khi nhấn phím",
            unit = "%",
            min = 0,
            max = 100,
            current = prefs.soundVolume
        ) { prefs.soundVolume = it }

        addSwitchItem(
            title = "Báo rung khi nhấn phím",
            subtitle = "Haptic feedback on keypress",
            checked = prefs.hapticOnKeypress
        ) { prefs.hapticOnKeypress = it }

        addSliderItem(
            title = "Cường độ rung khi nhấn phím",
            unit = " ms",
            min = 0,
            max = 100,
            current = prefs.vibrationStrengthMs
        ) { prefs.vibrationStrengthMs = it }

        addSwitchItem(
            title = "Phóng to khi nhấn phím",
            subtitle = "Popup on keypress",
            checked = prefs.popupOnKeypress
        ) { prefs.popupOnKeypress = it }

        addSliderItem(
            title = "Độ trễ nhấn giữ phím (Key long press delay)",
            unit = " ms",
            min = 100,
            max = 700,
            current = prefs.longPressDelayMs
        ) { prefs.longPressDelayMs = it }
    }

    // ==================== 3. GBOARD THEME (GIAO DIỆN) ====================
    private fun renderThemeScreen() {
        toolbarTitle.text = "Giao diện (Theme)"

        addSectionHeader("Chủ đề Gboard")
        val themes = listOf(
            "Gboard Sáng (Default Light)" to "gboard_light",
            "Gboard Tối (Default Dark)" to "gboard_dark",
            "Material You (Dynamic Light)" to "material_you",
        )
        val currentThemeLabel = themes.firstOrNull { it.second == prefs.themeMode }?.first ?: "Gboard Sáng (Default Light)"
        addSingleChoiceItem(
            title = "Chủ đề màu sắc",
            currentValue = currentThemeLabel,
            options = themes,
            selectedKey = prefs.themeMode
        ) {
            prefs.themeMode = it
            navigateTo(Screen.THEME)
        }

        addSwitchItem(
            title = "Đường viền phím (Key borders)",
            subtitle = "Hiển thị khung bo tròn và bóng đổ cho từng phím chuẩn Gboard",
            checked = prefs.keyBorders
        ) { prefs.keyBorders = it }
    }

    // ==================== 4. GBOARD TEXT CORRECTION (SỬA VĂN BẢN) ====================
    private fun renderTextCorrectionScreen() {
        toolbarTitle.text = "Sửa văn bản (Text correction)"

        addSectionHeader("Đề xuất (Suggestions)")
        addSwitchItem(
            title = "Hiển thị dải đề xuất (Show suggestion strip)",
            subtitle = "Hiển thị thanh gợi ý 3 từ và công cụ nhanh phía trên bàn phím",
            checked = prefs.showSuggestionStrip
        ) { prefs.showSuggestionStrip = it }

        addSwitchItem(
            title = "Đề xuất từ tiếp theo Trigram (Next-word suggestions)",
            subtitle = "Dự đoán từ kế tiếp bằng mô hình ngôn ngữ Trigram (2 từ trước) & Bigram có tự học",
            checked = prefs.nextWordSuggestions
        ) { prefs.nextWordSuggestions = it }

        addSwitchItem(
            title = "Chặn từ phản cảm (Block offensive words)",
            subtitle = "Không đề xuất các từ ngữ có thể gây phản cảm",
            checked = prefs.blockOffensiveWords
        ) { prefs.blockOffensiveWords = it }

        addSwitchItem(
            title = "Hiển thị đề xuất biểu tượng cảm xúc",
            subtitle = "Đề xuất emoji khi đang nhập từ khóa tương ứng",
            checked = prefs.showEmojiSuggestions
        ) { prefs.showEmojiSuggestions = it }

        addSwitchItem(
            title = "Đề xuất danh bạ (Suggest contacts)",
            subtitle = "Sử dụng tên trong danh bạ cho mục đề xuất",
            checked = prefs.suggestContacts
        ) { prefs.suggestContacts = it }

        addSectionHeader("Sửa lỗi (Corrections)")
        addSwitchItem(
            title = "Tự động sửa (Auto-correction)",
            subtitle = "Phím cách và dấu câu tự động sửa các từ gõ sai",
            checked = prefs.autoCorrection
        ) { prefs.autoCorrection = it }

        addSwitchItem(
            title = "Hoàn tác tự động sửa khi nhấn xóa lùi",
            subtitle = "Undo auto-correct on backspace",
            checked = prefs.undoAutoCorrectOnBackspace
        ) { prefs.undoAutoCorrectOnBackspace = it }

        addSwitchItem(
            title = "Tự động thêm dấu cách sau dấu câu",
            subtitle = "Auto-space after punctuation",
            checked = prefs.autoSpaceAfterPunctuation
        ) { prefs.autoSpaceAfterPunctuation = it }

        addSwitchItem(
            title = "Tự động viết hoa (Auto-capitalization)",
            subtitle = "Viết hoa chữ cái đầu tiên của mỗi câu",
            checked = prefs.autoCapitalization
        ) { prefs.autoCapitalization = it }

        addSwitchItem(
            title = "Nhấn đúp dấu cách để thêm dấu chấm",
            subtitle = "Nhấn đúp vào phím cách để chèn dấu chấm theo sau là khoảng trắng",
            checked = prefs.doubleSpacePeriod
        ) { prefs.doubleSpacePeriod = it }

        addSwitchItem(
            title = "Kiểm tra chính tả (Spell check)",
            subtitle = "Đánh dấu các từ sai chính tả khi nhập",
            checked = prefs.spellCheck
        ) { prefs.spellCheck = it }
    }

    // ==================== 5. GBOARD DICTIONARY (TỪ ĐIỂN & TỪ ĐIỂN CÁ NHÂN) ====================
    private fun renderDictionaryScreen() {
        toolbarTitle.text = "Từ điển (Dictionary)"

        addSectionHeader("Quản lý từ điển Gboard")
        val count = dictStore.loadPersonalWords().size
        addNavRow(
            title = "Từ điển cá nhân (Personal dictionary)",
            subtitle = "Tiếng Việt (vi_VN) • $count từ và lối tắt",
            target = Screen.PERSONAL_DICTIONARY
        )

        val whitelistCount = prefs.englishWhitelist.size
        addNavRow(
            title = "Từ ngoại lệ không bỏ dấu (Whitelist)",
            subtitle = "Giữ nguyên raw text khi gõ (vd: telegram, tele...) • $whitelistCount từ",
            target = Screen.ENGLISH_WHITELIST
        )

        addActionItem(
            title = "Học từ văn bản mẫu (Dán đoạn văn)",
            subtitle = "Dán email, ghi chú hoặc đoạn chat để bàn phím học ngay cách dùng từ & cụm từ của bạn"
        ) {
            showCorpusPasteDialog()
        }

        addActionItem(
            title = "Học từ file văn bản (.txt)",
            subtitle = "Chọn file .txt trên máy để huấn luyện gợi ý từ & Trigram cục bộ"
        ) {
            importCorpusFileLauncher.launch(arrayOf("text/plain", "text/*"))
        }

        addActionItem(
            title = "Xóa các từ đã học (Delete learned words)",
            subtitle = "Xóa toàn bộ các từ mà bàn phím đã tự động học trong quá trình gõ"
        ) {
            AlertDialog.Builder(this)
                .setTitle("Xóa các từ đã học?")
                .setMessage("Thao tác này sẽ xóa tất cả từ gợi ý đã học khỏi thiết bị.")
                .setPositiveButton("Xóa") { _, _ ->
                    dictStore.clearLearnedWords()
                    Toast.makeText(this, "Đã xóa các từ đã học", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun renderPersonalDictionaryScreen() {
        toolbarTitle.text = "Từ điển cá nhân"

        addSectionHeader("Thêm từ vào Từ điển cá nhân")
        addActionItem(
            title = "+ Thêm từ mới (Add word)",
            subtitle = "Nhập từ hoặc cụm từ kèm lối tắt không bắt buộc (giống Gboard)"
        ) {
            showPersonalWordDialog(null)
        }

        val words = dictStore.loadPersonalWords()
        addSectionHeader("Danh sách từ cá nhân (${words.size})")
        words.forEach { item ->
            val sub = if (item.shortcut.isNotEmpty()) "Lối tắt: ${item.shortcut} • ${item.locale}" else item.locale
            addActionItem(
                title = item.word,
                subtitle = sub
            ) {
                showPersonalWordDialog(item)
            }
        }
    }

    private fun showPersonalWordDialog(existing: PersonalWord?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etWord = EditText(this).apply {
            hint = "Từ hoặc cụm từ (vd: Việt Nam)"
            setText(existing?.word.orEmpty())
        }
        val etShortcut = EditText(this).apply {
            hint = "Lối tắt không bắt buộc (vd: vn)"
            setText(existing?.shortcut.orEmpty())
        }
        layout.addView(etWord)
        layout.addView(etShortcut)

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Thêm vào Từ điển cá nhân" else "Sửa từ cá nhân")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val word = etWord.text.toString().trim()
                val sc = etShortcut.text.toString().trim()
                if (word.isNotEmpty()) {
                    if (existing != null && !existing.word.equals(word, ignoreCase = true)) {
                        dictStore.removePersonalWord(existing.word)
                    }
                    dictStore.addPersonalWord(word, sc)
                    navigateTo(Screen.PERSONAL_DICTIONARY)
                }
            }
            .setNegativeButton("Hủy", null)

        if (existing != null) {
            builder.setNeutralButton("Xóa") { _, _ ->
                dictStore.removePersonalWord(existing.word)
                navigateTo(Screen.PERSONAL_DICTIONARY)
            }
        }
        builder.show()
    }

    // ==================== 6. VNCHKEY MACRO EDITOR (BẢNG GÕ TẮT CÁ NHÂN) ====================
    private fun renderEnglishWhitelistScreen() {
        toolbarTitle.text = "Từ ngoại lệ không bỏ dấu"

        addSectionHeader("Thêm từ giữ nguyên raw text")
        addActionItem(
            title = "+ Thêm từ mới vào Whitelist",
            subtitle = "Các từ này sẽ không bao giờ bị chuyển sang Tiếng Việt khi gõ"
        ) {
            val input = EditText(this).apply {
                hint = "Ví dụ: telegram, tele, docker..."
                setSingleLine(true)
            }
            AlertDialog.Builder(this)
                .setTitle("Thêm từ vào Whitelist")
                .setView(input)
                .setPositiveButton("Thêm") { _, _ ->
                    val word = input.text.toString().trim().lowercase()
                    if (word.isNotEmpty()) {
                        prefs.englishWhitelist = prefs.englishWhitelist + word
                        navigateTo(Screen.ENGLISH_WHITELIST)
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        val words = prefs.englishWhitelist.sorted()
        addSectionHeader("Danh sách từ (${words.size})")
        if (words.isEmpty()) {
            addActionItem("Chưa có từ ngoại lệ nào", "Nhấn '+ Thêm từ mới' ở trên để thêm") {}
        } else {
            words.forEach { word ->
                addActionItem(
                    title = word,
                    subtitle = "Chạm để xóa khỏi Whitelist"
                ) {
                    prefs.englishWhitelist = prefs.englishWhitelist - word
                    navigateTo(Screen.ENGLISH_WHITELIST)
                }
            }
        }
    }

    private fun renderMacroEditorScreen() {
        toolbarTitle.text = "Bảng gõ tắt / Macro (vnchKey)"

        addSectionHeader("Cấu hình gõ tắt")
        addSwitchItem(
            title = "Bật Bảng gõ tắt (Macro Expansion)",
            subtitle = "Gõ từ viết tắt rồi nhấn phím Cách hoặc dấu câu để tự động bung cụm từ",
            checked = prefs.macroEnabled
        ) { prefs.macroEnabled = it }

        addActionItem(
            title = "+ Thêm phím tắt mới",
            subtitle = "Hỗ trợ thích ứng chữ HOA/Thường tự động (hn → Hà Nội, HN → HÀ NỘI) và biến {date}, {time}"
        ) {
            showMacroDialog(null)
        }

        addActionItem(
            title = "Xuất / Sao chép Macro JSON (macros.json)",
            subtitle = "Sao chép toàn bộ bảng gõ tắt sang JSON để dùng chung với vnchKey"
        ) {
            val json = macroStore.exportJson()
            val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("macros.json", json))
            Toast.makeText(this, "Đã sao chép macros.json vào Clipboard", Toast.LENGTH_SHORT).show()
        }

        addActionItem(
            title = "Nhập Macro từ JSON (Import macros.json)",
            subtitle = "Dán nội dung JSON từ vnchKey để nhập nhanh vào bảng gõ tắt"
        ) {
            val etJson = EditText(this).apply {
                hint = "{\"hn\": \"Hà Nội\", \"sg\": \"Sài Gòn\"}"
                minLines = 4
            }
            AlertDialog.Builder(this)
                .setTitle("Nhập Macro từ JSON")
                .setView(etJson)
                .setPositiveButton("Nhập") { _, _ ->
                    val count = runCatching { macroStore.importJson(etJson.text.toString()) }.getOrDefault(0)
                    Toast.makeText(this, "Đã nhập $count phím tắt", Toast.LENGTH_SHORT).show()
                    navigateTo(Screen.MACRO_EDITOR)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        addActionItem(
            title = "Khôi phục danh sách gõ tắt mặc định (vnchKey)",
            subtitle = "Khôi phục 40+ từ viết tắt tiếng Việt thông dụng (hn, sg, tphcm, dc, ko, sđt...)"
        ) {
            AlertDialog.Builder(this)
                .setTitle("Khôi phục mặc định?")
                .setMessage("Toàn bộ bảng gõ tắt sẽ trở về danh sách chuẩn của vnchKey.")
                .setPositiveButton("Khôi phục") { _, _ ->
                    macroStore.resetToDefaults()
                    navigateTo(Screen.MACRO_EDITOR)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        val macros = macroStore.load()
        addSectionHeader("Danh sách phím tắt (${macros.size} cụm từ)")
        macros.forEach { macro ->
            addActionItem(
                title = "${macro.shortcut}  →  ${macro.expansion}",
                subtitle = "Chạm để chỉnh sửa hoặc xóa"
            ) {
                showMacroDialog(macro)
            }
        }
    }

    private fun showMacroDialog(existing: Macro?) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val etShortcut = EditText(this).apply {
            hint = "Từ viết tắt (vd: hn, cty, addr)"
            setText(existing?.shortcut.orEmpty())
        }
        val etExpansion = EditText(this).apply {
            hint = "Cụm từ thay thế (vd: Hà Nội)"
            setText(existing?.expansion.orEmpty())
        }
        layout.addView(etShortcut)
        layout.addView(etExpansion)

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Thêm phím tắt mới" else "Chỉnh sửa phím tắt")
            .setView(layout)
            .setPositiveButton("Lưu") { _, _ ->
                val sc = etShortcut.text.toString().trim().lowercase()
                val exp = etExpansion.text.toString().trim()
                if (sc.isNotEmpty() && exp.isNotEmpty()) {
                    val list = macroStore.load().toMutableList()
                    if (existing != null) {
                        list.removeAll { it.shortcut.equals(existing.shortcut, ignoreCase = true) }
                    }
                    list.removeAll { it.shortcut.equals(sc, ignoreCase = true) }
                    list.add(0, Macro(sc, exp))
                    macroStore.save(list)
                    navigateTo(Screen.MACRO_EDITOR)
                }
            }
            .setNegativeButton("Hủy", null)

        if (existing != null) {
            builder.setNeutralButton("Xóa") { _, _ ->
                val list = macroStore.load().filterNot { it.shortcut.equals(existing.shortcut, ignoreCase = true) }
                macroStore.save(list)
                navigateTo(Screen.MACRO_EDITOR)
            }
        }
        builder.show()
    }

    // ==================== 7. GLIDE TYPING ====================
    private fun renderGlideTypingScreen() {
        toolbarTitle.text = "Nhập bằng cử chỉ (Glide typing)"

        addSectionHeader("Cử chỉ Gboard")
        addSwitchItem(
            title = "Bật nhập bằng cử chỉ (Enable glide typing)",
            subtitle = "Nhập từ bằng cách trượt ngón tay qua các chữ cái",
            checked = prefs.enableGlideTyping
        ) { prefs.enableGlideTyping = it }

        addSwitchItem(
            title = "Hiển thị vệt cử chỉ (Show gesture trail)",
            subtitle = "Hiện đường dẫn màu xanh khi lướt trên bàn phím",
            checked = prefs.showGestureTrail
        ) { prefs.showGestureTrail = it }

        addSliderItem(
            title = "Độ nhạy vuốt (nhỏ = nhạy hơn)",
            unit = " dp",
            min = 14,
            max = 36,
            current = prefs.glideSensitivityDp
        ) { prefs.glideSensitivityDp = it }

        addSwitchItem(
            title = "Bật xóa bằng cử chỉ (Enable gesture delete)",
            subtitle = "Trượt sang trái từ phím xóa lùi để xóa nhanh cả từ",
            checked = prefs.enableGestureDelete
        ) { prefs.enableGestureDelete = it }

        addSwitchItem(
            title = "Bật điều khiển con trỏ bằng cử chỉ",
            subtitle = "Di chuyển con trỏ bằng cách trượt ngang trên phím cách",
            checked = prefs.enableGestureCursorControl
        ) { prefs.enableGestureCursorControl = it }
    }

    // ==================== 8. VOICE & CLIPBOARD ====================
    private fun renderVoiceClipboardScreen() {
        toolbarTitle.text = "Giọng nói & Bảng nhớ tạm"

        addSectionHeader("Nhập bằng giọng nói (Voice typing)")
        addSwitchItem(
            title = "Sử dụng tính năng nhập bằng giọng nói",
            subtitle = "Hiển thị nút micro trên thanh gợi ý Gboard",
            checked = prefs.enableVoiceTyping
        ) { prefs.enableVoiceTyping = it }

        addSectionHeader("Bảng nhớ tạm (Clipboard)")
        addSwitchItem(
            title = "Bật Bảng nhớ tạm Gboard",
            subtitle = "Hiển thị văn bản đã sao chép gần đây trên thanh công cụ",
            checked = prefs.enableClipboardHistory
        ) { prefs.enableClipboardHistory = it }
    }

    // ==================== 8.5 BACKUP & RESTORE ====================
    private fun renderBackupRestoreScreen() {
        toolbarTitle.text = "Sao lưu & khôi phục"

        addSectionHeader("Xuất dữ liệu (Export)")
        addActionItem(
            title = "Xuất tất cả dữ liệu",
            subtitle = "Cài đặt, macro/gõ tắt, từ điển cá nhân, từ đã học, n-gram"
        ) { exportBackup() }

        addSectionHeader("Nhập dữ liệu (Import)")
        addActionItem(
            title = "Nhập dữ liệu từ file JSON",
            subtitle = "Chọn file backup vnchFlex để gộp hoặc thay thế dữ liệu"
        ) { confirmAndPickImport() }

        addSectionHeader("Lưu ý")
        addActionItem(
            title = "GBoard learned data",
            subtitle = "Android không cho app khác đọc dữ liệu học riêng của GBoard. Bạn có thể dùng Export/Import của vnchFlex để chuyển máy."
        ) {}
    }

    private fun exportBackup() {
        try {
            val json = buildBackupJson().toString(2)
            pendingExportJson = json
            exportBackupLauncher.launch("vnchflex-backup.json")
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể tạo backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmAndPickImport() {
        AlertDialog.Builder(this)
            .setTitle("Nhập dữ liệu")
            .setMessage("Bạn muốn gộp vào dữ liệu hiện tại hay thay thế hoàn toàn?")
            .setPositiveButton("Gộp") { _, _ ->
                pendingImportReplace = false
                importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            }
            .setNeutralButton("Hủy", null)
            .setNegativeButton("Thay thế") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Thay thế toàn bộ?")
                    .setMessage("Dữ liệu đã học/từ điển/macro/cài đặt tương ứng trong backup sẽ thay thế dữ liệu hiện tại.")
                    .setPositiveButton("Thay thế") { _, _ ->
                        pendingImportReplace = true
                        importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
            .show()
    }

    private var pendingImportReplace: Boolean = false

    private fun buildBackupJson(): JSONObject {
        val root = JSONObject()
        root.put("format", "vnchflex-backup")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Settings
        val settingsObj = JSONObject()
        val sp = getSharedPreferences(KeyboardPreferences.PREFS_NAME, MODE_PRIVATE)
        for ((k, v) in sp.all) {
            when (v) {
                is Boolean -> settingsObj.put(k, v)
                is Int -> settingsObj.put(k, v)
                is Long -> settingsObj.put(k, v)
                is Float -> settingsObj.put(k, v.toDouble())
                is String -> settingsObj.put(k, v)
                is Set<*> -> {
                    val arr = JSONArray()
                    v.filterIsInstance<String>().forEach { arr.put(it) }
                    settingsObj.put(k, JSONObject().put("__type", "string_set").put("values", arr))
                }
            }
        }
        root.put("settings", settingsObj)

        // Macros (full format)
        root.put("macros", JSONArray(macroStore.exportBackupJson()))

        // Personal dictionary + learned data
        root.put("dictionary", dictStore.exportBackupJson())
        return root
    }

    private fun writeBackupToUri(uri: Uri, json: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { os ->
                os.writer(Charsets.UTF_8).use { it.write(json) }
            } ?: throw IllegalStateException("Không mở được file để ghi")
            Toast.makeText(this, "Đã xuất backup", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi xuất backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun readBackupFromUri(uri: Uri) {
        try {
            val raw = contentResolver.openInputStream(uri)?.use { ins ->
                ins.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IllegalStateException("Không đọc được file")
            val root = JSONObject(raw)
            val replace = pendingImportReplace
            val summary = applyBackupJson(root, replace)
            Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
            navigateTo(Screen.BACKUP_RESTORE)
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi nhập backup: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyBackupJson(root: JSONObject, replace: Boolean): String {
        var macrosCount = 0
        var dictCount = 0
        var settingsCount = 0

        val settingsObj = root.optJSONObject("settings")
        if (settingsObj != null) {
            val sp = getSharedPreferences(KeyboardPreferences.PREFS_NAME, MODE_PRIVATE)
            val editor = sp.edit()
            if (replace) editor.clear()
            val keys = settingsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = settingsObj.opt(k)
                when (v) {
                    is Boolean -> editor.putBoolean(k, v)
                    is Int -> editor.putInt(k, v)
                    is Long -> editor.putLong(k, v)
                    is Double -> editor.putFloat(k, v.toFloat())
                    is String -> editor.putString(k, v)
                    is JSONObject -> {
                        if (v.optString("__type") == "string_set") {
                            val arr = v.optJSONArray("values") ?: JSONArray()
                            val set = mutableSetOf<String>()
                            for (i in 0 until arr.length()) set.add(arr.optString(i))
                            editor.putStringSet(k, set)
                        }
                    }
                }
                settingsCount++
            }
            editor.apply()
        }

        val macrosAny = root.opt("macros")
        if (macrosAny is JSONArray) {
            macrosCount = macroStore.importJson(macrosAny.toString(), replace)
        } else if (macrosAny is JSONObject) {
            macrosCount = macroStore.importJson(macrosAny.toString(), replace)
        }

        val dictObj = root.optJSONObject("dictionary")
        if (dictObj != null) {
            dictCount = dictStore.importBackupJson(dictObj, replace)
        }

        return "Đã nhập backup (${if (replace) "thay thế" else "gộp"}): $settingsCount cài đặt, $macrosCount macro, $dictCount mục từ điển/học."
    }

    // ==================== 9. ADVANCED ====================
    private fun renderAdvancedScreen() {
        toolbarTitle.text = "Nâng cao (Advanced)"

        addSectionHeader("Quyền riêng tư & Dữ liệu")
        addActionItem(
            title = "Xóa các từ và dữ liệu đã học",
            subtitle = "Xóa toàn bộ bộ nhớ từ đã học cục bộ"
        ) {
            dictStore.clearLearnedWords()
            Toast.makeText(this, "Đã xóa toàn bộ từ đã học", Toast.LENGTH_SHORT).show()
        }

        addSectionHeader("Mô hình AI sửa dấu Tiếng Việt (.onnx)")
        val customModelFile = java.io.File(filesDir, "models/vi_corrector.onnx")
        val modelStatus = if (customModelFile.exists()) {
            String.format("Đang dùng model tùy chỉnh (%.2f MB)", customModelFile.length() / (1024f * 1024f))
        } else {
            "Đang dùng model mặc định tích hợp sẵn"
        }
        addActionItem(
            title = "Nạp file .onnx từ bộ nhớ điện thoại",
            subtitle = modelStatus
        ) {
            importOnnxModelLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        }
        if (customModelFile.exists()) {
            addActionItem(
                title = "Khôi phục model .onnx mặc định",
                subtitle = "Xóa model tùy chỉnh và dùng lại model gốc trong APK"
            ) {
                customModelFile.delete()
                Toast.makeText(this, "Đã khôi phục model mặc định", Toast.LENGTH_SHORT).show()
                navigateTo(Screen.ADVANCED)
            }
        }
    }

    // ==================== 10. ABOUT ====================
    private fun renderAboutScreen() {
        toolbarTitle.text = "Giới thiệu vnchFlex"

        addSectionHeader("Thông tin ứng dụng")
        addActionItem(
            title = "vnchFlex Keyboard v1.0.0",
            subtitle = "Giao diện và cài đặt chuẩn Gboard • Tích hợp Telex Bỏ dấu tự do & Bảng gõ tắt Macro phong cách vnchKey • 100% Offline"
        ) {}
    }

    // ==================== UI BUILDER HELPERS ====================
    private fun addSectionHeader(title: String) {
        val tv = TextView(this).apply {
            text = title.uppercase()
            textSize = 11.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF1A73E8"))
            setPadding(40, 36, 40, 12)
        }
        container.addView(tv)
    }

    private fun addNavRow(title: String, subtitle: String, target: Screen) {
        addActionItem(title, subtitle) { navigateTo(target) }
    }

    private fun addActionItem(title: String, subtitle: String, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 28, 40, 28)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
        val tvTitle = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(Color.parseColor("#FF202124"))
        }
        val tvSub = TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.parseColor("#FF5F6368"))
            setPadding(0, 4, 0, 0)
        }
        row.addView(tvTitle)
        row.addView(tvSub)
        container.addView(row)
        addDivider()
    }

    private fun addSwitchItem(
        title: String,
        subtitle: String,
        checked: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 24, 32, 24)
        }
        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvTitle = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(Color.parseColor("#FF202124"))
        }
        val tvSub = TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.parseColor("#FF5F6368"))
            setPadding(0, 4, 0, 0)
        }
        textCol.addView(tvTitle)
        textCol.addView(tvSub)

        val sw = Switch(this).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        }
        row.setOnClickListener { sw.isChecked = !sw.isChecked }
        row.addView(textCol)
        row.addView(sw)
        container.addView(row)
        addDivider()
    }

    private fun addSingleChoiceItem(
        title: String,
        currentValue: String,
        options: List<Pair<String, String>>,
        selectedKey: String,
        onSelected: (String) -> Unit
    ) {
        addActionItem(title, currentValue) {
            val labels = options.map { it.first }.toTypedArray()
            val checkedIdx = options.indexOfFirst { it.second == selectedKey }.coerceAtLeast(0)
            AlertDialog.Builder(this)
                .setTitle(title)
                .setSingleChoiceItems(labels, checkedIdx) { dialog, which ->
                    onSelected(options[which].second)
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun addSliderItem(
        title: String,
        unit: String,
        min: Int,
        max: Int,
        current: Int,
        onChanged: (Int) -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 24, 40, 24)
        }
        val tvTitle = TextView(this).apply {
            text = "$title: $current$unit"
            textSize = 15f
            setTextColor(Color.parseColor("#FF202124"))
        }
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = (current - min).coerceIn(0, max - min)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + progress
                    tvTitle.text = "$title: $value$unit"
                    onChanged(value)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        row.addView(tvTitle)
        row.addView(seek)
        container.addView(row)
        addDivider()
    }

    private fun addDivider() {
        val div = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#FFE8EAED"))
        }
        container.addView(div)
    }

    private fun showCorpusPasteDialog() {
        val etCorpus = EditText(this).apply {
            hint = "Dán đoạn văn bản tiếng Việt hoặc tiếng Anh vào đây..."
            minLines = 6
            maxLines = 12
            gravity = Gravity.TOP or Gravity.START
        }
        val containerBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
            addView(etCorpus)
        }
        AlertDialog.Builder(this)
            .setTitle("Học phong cách gõ từ văn bản")
            .setMessage("Dữ liệu chỉ được xử lý và lưu 100% offline trên thiết bị của bạn.")
            .setView(containerBox)
            .setPositiveButton("Học ngay") { _, _ ->
                processCorpusText(etCorpus.text.toString())
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun processCorpusText(rawText: String) {
        if (rawText.isBlank()) {
            Toast.makeText(this, "Văn bản trống, chưa có gì để học", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            val engine = com.vnchflex.vietkeyboard.suggestion.SuggestionEngine(this)
            val count = engine.learnFromCorpus(rawText)
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Đã học xong $count từ và cập nhật gợi ý ngữ cảnh Trigram!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }
}
