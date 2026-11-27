# Changelog — vnchFlex

Tất cả các thay đổi đáng chú ý của dự án **vnchFlex** đều được ghi lại tại đây.

---

## [Unreleased / Latest] — ONNX Offline AI, Gesture Delete, Spatial Auto-Correct & ACTION_DOWN

### Added & Improved (Tính năng mới & Cải thiện)
- **Công cụ văn bản thông minh `✨` (chỉ hiện khi bôi đen)** (`VietKeyboardService`, `VietnameseTextNormalizer`, `OnnxVietnameseCorrector`):
  - Tự động hiện nút `✨` trên thanh gợi ý khi bôi đen văn bản (`newSelStart != newSelEnd`) với 4 tác vụ: *Khôi phục dấu Tiếng Việt*, *Chuẩn hóa dấu câu & viết hoa*, *Bỏ dấu Tiếng Việt*, *Đổi IN HOA / chữ thường*.
  - Tích hợp **ONNX Runtime Android** (`arm64-v8a`, `vi_corrector.onnx` INT8 ~6.92 MB huấn luyện trên 371k câu sạch) kèm ràng buộc giữ nguyên dấu câu & chữ cái gốc, tự động fallback sang `VietnameseTextNormalizer` (Trigram + Unigram) nếu chưa có model.
  - Thêm script huấn luyện [train_custom_corpus_onnx.py](scripts/train_custom_corpus_onnx.py) (hỗ trợ `.txt`, `.md`, `.pdf`, bảo toàn `dz-`).
- **Vuốt phím Xóa (`⌫`) sang trái kiểu Gboard** (`VietKeyboardService`):
  - Chạm và trượt từ `⌫` sang trái để bôi đen từng từ ngược về trước, trượt lại sang phải để bỏ chọn, thả tay để xóa.
- **Lọc bảo mật OOV & Tự động sửa lệch phím lân cận** (`SuggestionEngine`, `GlideDecoder`, `VietKeyboardService`):
  - Chặn học từ trên ô mật khẩu/email/URL và bỏ qua chuỗi chứa số/ký tự đặc biệt; từ mới ngoài từ điển cần lặp lại ≥ 2 lần (hoặc được chọn từ gợi ý) mới lưu.
  - Tự động sửa lỗi chạm lệch 1 phím hàng xóm (`KEY_NEIGHBORS`) khi bấm Space và hoàn tác ngay khi bấm `⌫`.
- **Tối ưu cảm ứng & giao diện**:
  - Commit ngay tại `ACTION_DOWN` cho Space, `,` (giữ `,` mở Emoji và tự hoàn tác dấu phẩy), `.`; sửa nuốt dấu cách sau macro cho mọi dấu câu (`.,;:!?`) kể cả khi nhấn giữ.
  - Căn giữa icon `😊` trên phím `,`, đổi phím Shift & Backspace sang vuông bo tròn với icon `28dp`.

---

## [Unreleased / Latest] — Bố cục Gboard gọn gàng, Space rộng & Voice Typing

### Added & Improved (Tính năng mới & Cải thiện)
- **Mở rộng phím Space & gộp Emoji vào phím dấu phẩy (`,`)**:
  - Bỏ phím `😊` rời cạnh dấu cách; chuyển `😊` thành ký hiệu phụ trên `key_comma` (`GboardKeyButton`), nhấn giữ `,` để mở bảng chọn Emoji.
  - Tăng `layout_weight` của phím Space (`key_space`, `key_space_s1`, `key_space_s2`) từ `4.0` lên `5.2` để bấm thoải mái và chính xác hơn.
- **Dọn gọn thanh công cụ gợi ý**:
  - Bỏ nút `"Gõ tắt"` trên thanh công cụ để dành thêm không gian cho 3 ô gợi ý từ & Next-word.
  - Chuyển nút `"Bỏ dấu"` thành tùy chọn trong **Cài đặt → Tùy chọn** (`showStripDiacriticsButton`, mặc định tắt).
- **Nhập liệu bằng giọng nói (`🎤` Voice Typing)**:
  - Thêm nút `🎤` (`btn_toolbar_voice`) trên thanh công cụ, ưu tiên chuyển sang bộ gõ giọng nói hệ thống hoặc mở `VoiceInputActivity` (`RecognizerIntent`) theo ngôn ngữ đang gõ (`vi-VN` / `en-US`).

---

## [Unreleased / Latest] — Tối ưu phím Space, SuggestionEngine & Icon English

### Added & Improved (Tính năng mới & Cải thiện)
- **Tối ưu độ mượt phím Space & loại bỏ IPC đồng bộ** (`VietKeyboardService`):
  - Loại bỏ 100% lệnh gọi IPC `getTextBeforeCursor()` trong `updateAutoCapitalization()`, dùng trực tiếp `lastCommittedChar` / `prevCommittedChar` nội bộ.
  - Chặn chuỗi `performClick()` / state drawable thừa của `Button` trên `key_space`, `key_space_s1`, `key_space_s2` bằng `setOnTouchListener` trả về `true`, giảm ngưỡng vuốt con trỏ xuống `22dp` và nhấn giữ xuống `750ms`.
- **Tối ưu `SuggestionEngine`**:
  - Chuyển `learnLock` sang `ReentrantReadWriteLock` để luồng đọc gợi ý trên Main Thread không bị chặn bởi luồng học từ nền.
  - Dựng sẵn cây Trie (`ensureTrieBuilt()`) trên luồng nền ngay trong `init`.
- **Icon chuyển ngôn ngữ English (`vncharkey_logo`)**:
  - Thêm VectorDrawable `ic_lang_vncharkey_e.xml` và tự động chuyển đổi giữa `ic_lang_vncharkey_logo` (Tiếng Việt) và `ic_lang_vncharkey_e` (English).

---

## [Unreleased / Latest] — Mượt gõ, Composing ổn định, Glide trail, Backup/Restore

### Added & Improved (Tính năng mới & Cải thiện)
- **Sao lưu & Khôi phục (Backup/Restore)** (`4ee2cca`):
  - Thêm màn "Sao lưu & khôi phục" trong Cài đặt, xuất/nhập JSON qua SAF.
  - Bao gồm cài đặt, macro (đầy đủ), từ điển cá nhân, từ đã học, bigram/trigram; hỗ trợ **Gộp** hoặc **Thay thế**.
- **Clipboard prompt kiểu Gboard** (`8430381`): Nút 📋 hiển thị trạng thái bật clipboard hoặc hướng dẫn khi lịch sử trống.
- **Glide trail mờ dần** (`f7cfefe`): Vệt vuốt giữ điểm gần ~350ms, alpha/độ dày giảm dần về đuôi, tự fade out ~220ms khi nhấc tay.
- **Composing ổn định hơn** (`f7cfefe`): `VietnameseEngine` chuyển sang `setComposingText` (không UnderlineSpan), `commit/rehydrate/reset` xử lý composing span đúng; giảm rủi ro dính/mất chữ trên Chrome/Zalo/WebView.
- **Học từ nền & debounce lưu** (`f7cfefe`): `recordCommittedWord` đẩy `SuggestionEngine.learn` sang `learnExecutor` single-thread; `PersonalDictionaryStore` debounce lưu learned words cùng n-gram; `SuggestionEngine` đồng bộ lock cho learn/suggest.

### Fixed (Sửa lỗi)
- **Long-press alternates bị mất** (`d42060e`): Không hủy long-press runnable khi self-flush ACTION_DOWN; `flushPendingRollover` luôn gọi cancel.
- **Glide + Enter + Backspace** (`f7cfefe`): `onEnter()` reset `lastGlideCommittedLength` và track `\n`.
- **Clipboard panel làm lệch hit-box glide** (`f7cfefe`): `closeClipboardPanel()` rebuild `glideOverlay.registerLetterKeys(...)`.
- **Icon phím chuyển ngôn ngữ** (`f3e1829`, `90f6c92`): Thêm tùy chọn globe/logo VnCharKey và tinh chỉnh hiển thị.

---

## [Previous] — Gboard Clipboard & Local VersionCode (`6af3027`)

### Added & Improved (Tính năng mới & Cải thiện)
- **Bảng nhớ tạm (Clipboard) kiểu Gboard**:
  - Thêm `ClipboardStore` lưu lịch sử clipboard bền vững qua `SharedPreferences`, tự động xóa các mục chưa ghim sau **1 giờ** và giữ các mục đã ghim lên đầu.
  - Thêm `ClipboardPanelController` hiển thị panel clipboard ngay trong bàn phím với thanh tiêu đề (`←`, `Switch` bật/tắt, nút chỉnh sửa `✎`), thẻ onboarding khi tắt, mẹo sử dụng khi trống, và lưới thẻ **2 cột** (chạm để dán, nhấn giữ để ghim, chế độ chỉnh sửa để ghim/xóa).
- **Đồng bộ `versionCode` khi build local** (`app/build.gradle.kts`):
  - Hỗ trợ `-PversionCode=<N>`, `GITHUB_RUN_NUMBER` trên CI, và fallback `100 + git rev-list --count HEAD` khi build local để tránh lỗi downgrade khi cài lên điện thoại.

---

## [Previous] — Whitelist & Space Rollover (`09aea53`)

### Added (Tính năng mới)
- **Từ ngoại lệ không bỏ dấu (Whitelist)**:
  - Thêm `englishWhitelist` trong `KeyboardPreferences` và `userWhitelist` trong `VietnameseEngine` để giữ nguyên raw text cho các từ người dùng chỉ định (vd: `telegram`, `tele`).
  - Thêm màn hình quản lý Whitelist trong `SettingsActivity` (*Từ điển → Từ ngoại lệ không bỏ dấu*).

### Fixed & Improved (Sửa lỗi & Cải thiện)
- **Tối ưu phím Space & tốc độ gõ nhanh** (`VietKeyboardService`):
  - **Space rollover**: Tự động chốt `Space` ngay khi ngón kế tiếp chạm phím chữ, tránh dính chữ khi gõ nhanh.
  - **Loại bỏ IPC `getTextBeforeCursor` khi nhấn Space**: Dùng `trackCommittedChar()` nội bộ cho `doubleSpacePeriod`.
  - **Tách luồng gợi ý & học từ**: Debounce `scheduleSuggestionsUpdate()` (25ms) và chạy `recordCommittedWord()` trên `suggestionExecutor`.
- **Unit Test**: Thêm `testImplementation("junit:junit:4.13.2")` vào `app/build.gradle.kts`.

---

## Glide Typing & Tối ưu từ vựng

### Added (Tính năng mới)
- **Glide Typing (Vuốt để gõ)** (`d93d9c2`, `29a4895`):
  - Thêm `GlideTypingOverlayView` bắt sự kiện vuốt (ngưỡng `24dp`), vẽ vệt vuốt xanh kiểu Gboard (`#CC1A73E8`), tự động tính lại tọa độ phím (`keyRectsDirty`) khi đổi layout/xoay màn hình.
  - Thêm `GlideDecoder` giải mã đường vuốt dựa trên ký tự đầu/cuối, lân cận phím (`KEY_NEIGHBORS`), gộp ký tự lặp (`collapseRepeats`), Levenshtein và xếp hạng lại bằng **Trigram**.
  - Hiển thị 3 ứng viên trên thanh gợi ý sau khi vuốt để chạm đổi nhanh; bấm `Backspace` 1 lần để xóa nguyên từ vừa vuốt (`lastGlideCommittedLength`).
  - Mở khóa công tắc bật/tắt Glide Typing và vệt vuốt trong `SettingsActivity` & `KeyboardPreferences` (bật mặc định).

### Fixed & Improved (Sửa lỗi & Cải thiện)
- **Làm sạch & mở rộng từ điển tiếng Việt** (`ce74c67`, `29a4895`):
  - Mở rộng `coreUnigramWeights` lên ~5.900 âm tiết tiếng Việt chuẩn.
  - Lọc bỏ 32 token lỗi/dính chữ/ký tự ngoại lai và thêm bộ lọc regex tiếng Việt (`2..7` ký tự) khi nạp từ vựng.
- **Học ngữ cảnh Trigram khi chọn từ thay thế** (`ce74c67`):
  - Gọi `recordCommittedWord(word)` trong `replaceLastGlideWordOrAccept()` để mô hình ngôn ngữ học đúng từ người dùng chọn lại.
- **Sửa lỗi khai báo trùng** (`85eb72d`): Xóa khai báo trùng `audioManager` và `suggestionSlots` trong `VietKeyboardService`.

---

## Tối ưu hiệu năng & Độ trễ (Low-Latency)

- **Loại bỏ nghẽn cổ chai phím Space & Backspace** (`acaa56c`, `3240f0e`, `be37f81`):
  - Loại bỏ các cuộc gọi IPC (`InputConnection.getTextBeforeCursor`) thừa trên từng phím bấm.
  - Tái sử dụng View cho thanh gợi ý và Popup xem trước phím thay vì khởi tạo lại liên tục.
  - Cache dữ liệu `MacroStore` và `PersonalDictionaryStore` trong bộ nhớ.
  - Tăng tốc độ phản hồi khi nhấn giữ `Backspace` và giảm độ trễ popup phím.
- **Sửa lỗi Backspace lặp chữ** (`869abc9`): Khắc phục lỗi rehydrate làm nhân đôi composing text khi xóa.

---

## Nâng cấp UX, Macro & Cử chỉ

- **Tính năng nâng cao** (`2713135`):
  - **Post-commit edit**: Chạm vào từ đã gõ xong hoặc xóa lùi để tiếp tục bỏ dấu/sửa dấu.
  - **Cử chỉ (Gestures)**: Vuốt trên phím `Space` để di chuyển con trỏ; vuốt từ phím `Backspace` để xóa nhanh cả từ.
  - **Macro Undo**: Bấm Double Backspace ngay sau khi macro bung để hoàn nguyên về từ viết tắt gốc.
  - **Macro JSON Import/Export**: Sao lưu và khôi phục danh sách gõ tắt qua JSON.
  - **Tùy biến giao diện**: Chế độ một tay (One-handed) và chỉnh chiều cao bàn phím.
- **Chuyển ngôn ngữ & Khôi phục tiếng Anh** (`d5d3fa2`):
  - Bấm phím ngôn ngữ để chuyển nhanh VN/EN; nhấn giữ `Space` để mở hộp thoại chọn IME hệ thống.
  - Tự động khôi phục từ tiếng Anh khi gõ nhầm dấu Telex.
- **Long-press ký tự phụ kiểu Gboard** (`b7730c3`, `41649d9`): Nhấn giữ phím để hiện bảng ký tự phụ và trượt ngón tay để chọn.

---

## Giao diện chuẩn Gboard & Engine Tiếng Việt

- **Đồng bộ giao diện Gboard** (`278306f`, `e3903a8`, `dd9b256`, `4b536fb`, `b95292e`, `b06f432`, `0fd281c`):
  - Phím hình oval/bo tròn chuẩn Gboard, bỏ gạch chân composing text, dùng icon vector Material sắc nét cho `Shift`, `Backspace`, `Enter`.
  - Thêm hàng số tùy chọn, bàn phím ký tự (`?123`, `=\<`), vẽ ký tự phụ ở góc trên bên phải phím (`GboardKeyButton`).
- **Engine Tiếng Việt & Đặt dấu** (`09459c5`, `a3e18c2`):
  - Hỗ trợ Telex bỏ dấu tự do kiểu **vnchKey**, phương ngữ Nam (`dz-`), tùy chọn đặt dấu kiểu cũ/mới, và gợi ý Trigram.
- **Hạ tầng & Đóng gói** (`db98a18`, `fbea588`):
  - Đổi package & `applicationId` thành `com.vnchflex.vietkeyboard`.
  - Thiết lập keystore ký APK và tự động tăng `versionCode` trên GitHub Actions CI.
