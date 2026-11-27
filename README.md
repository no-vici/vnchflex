# vnchFlex — Bàn phím Tiếng Việt Thông minh cho Android

**vnchFlex** là bộ gõ tiếng Việt (Android IME — `InputMethodService`) mã nguồn mở, được thiết kế với giao diện và độ mượt lấy cảm hứng từ **Gboard**, kết hợp các tính năng đặc trưng cho người dùng Việt Nam như **Telex bỏ dấu tự do kiểu vnchKey**, **hỗ trợ phương ngữ Nam (`dz-`)**, **Gõ tắt (Macro) có hoàn nguyên**, và **Glide Typing (vuốt để gõ)** gọn nhẹ.

> Tài liệu chi tiết cho người mới bắt đầu:
> - [Kiến trúc & Cách hoạt động chi tiết (từ A đến Z)](docs/ARCHITECTURE.md)
> - [Hành trình phát triển & Tối ưu kiến trúc](docs/DEVELOPMENT_JOURNEY.md)
> - [Lịch sử thay đổi (Changelog)](CHANGELOG.md)

---

## Tính năng nổi bật

### 1. Bộ gõ Tiếng Việt linh hoạt (`VietnameseEngine`)
- **Telex đầy đủ & Bỏ dấu tự do (kiểu vnchKey)**: Cho phép gõ dấu thanh (`s/f/r/x/j`, `z` để xóa dấu) và dấu mũ/móc (`a/e/o/w/d`) ở bất kỳ vị trí nào trong từ, không bị khóa cứng thứ tự.
- **Đặt dấu kiểu cũ / kiểu mới**: Tùy chọn đặt dấu thanh ở vị trí truyền thống (`hòa`, `thúy`) hoặc hiện đại (`hoà`, `thuý`).
- **Hỗ trợ phương ngữ Nam (`dz-`)**: Hỗ trợ phụ âm kép phương ngữ Nam Bộ (`dzìa`, `dzách`, `dzô`...) chuẩn vnchKey.
- **Sửa từ sau khi commit (Post-commit edit) & Tự động khôi phục từ tiếng Anh**: Chạm lại vào từ đã gõ hoặc bấm Backspace để tiếp tục sửa dấu; tự động trả lại từ gốc nếu phát hiện từ tiếng Anh hợp lệ.
- **Từ ngoại lệ (Whitelist) giữ nguyên raw text**: Cho phép người dùng tự thêm/xóa các từ tiếng Anh hoặc từ đặc biệt (vd: `telegram`, `tele`, `docker`) trong *Cài đặt → Từ điển* để không bao giờ bị chuyển sang Tiếng Việt.
- **Chuyển nhanh VN / EN**: Bấm phím chuyển ngôn ngữ để bật/tắt nhanh chế độ gõ tiếng Việt.

### 2. Glide Typing (Vuốt để gõ) & Kiến trúc Gboard 1-Canvas 120FPS
- **Vẽ phím & vệt vuốt trên 1 Canvas duy nhất (`KeyboardCanvasView`)**: Thay thế hoàn toàn hệ thống `ViewGroup`/`Button` lồng nhau bằng 1 Canvas duy nhất với `SparseArray<PointerTracker>` theo dõi từng ngón tay theo `pointerId`, vẽ vệt vuốt xanh kiểu Gboard, bong bóng xem trước phím và thanh dấu câu nhanh trên phím `.` ở 60–120 FPS.
- **Giải mã thông minh (`GlideDecoder`)**: Khớp ký tự đầu/cuối (có châm chước phím lân cận `KEY_NEIGHBORS`), kiểm tra chuỗi con, gộp ký tự lặp (`collapseRepeats`), kết hợp khoảng cách Levenshtein và xếp hạng lại bằng **Trigram** (`prevWord2`, `prevWord1`).
- **Từ vựng ~5.900 âm tiết tiếng Việt chuẩn**: Được lọc sạch ký tự lạ và từ dính chữ.
- **UX tiện lợi**: Hiện 3 ứng viên trên thanh gợi ý để chạm đổi nhanh; bấm **Backspace 1 lần** ngay sau khi vuốt để xóa nguyên từ vừa vuốt.

### 3. Macro / Gõ tắt nâng cao (`MacroEngine`, `MacroStore`)
- Tự động bung từ viết tắt khi gõ ký tự phân cách (`Space`, `.`, `,`, `!`, `?`, ...).
- Hỗ trợ biến động như `{date}`, `{time}`.
- **Hoàn nguyên Macro (Undo)**: Bấm **Double Backspace** ngay sau khi macro vừa bung để khôi phục lại từ viết tắt ban đầu.
- **Quản lý trực quan**: Thêm/sửa/xóa macro trong Cài đặt, hỗ trợ **Import / Export JSON**.

### 4. Giao diện & Cử chỉ chuẩn Gboard
- **Bố cục chuẩn Gboard**: Phím bo tròn/oval, hàng số tùy chọn, bàn phím ký tự (`?123`, `=\<`), nút chuyển IME khi nhấn giữ phím `Space`.
- **Popup xem trước phím & Ký tự phụ**: Nhấn giữ phím để hiện bảng ký tự phụ và trượt ngón tay để chọn.
- **Cử chỉ (Gestures)**:
  - Vuốt trái/phải trên phím `Space` để di chuyển con trỏ.
  - Vuốt trái từ phím `Backspace` để xóa nhanh từng từ.
- **Tùy biến linh hoạt**: Chế độ một tay (One-handed), điều chỉnh chiều cao bàn phím, âm thanh/rung phản hồi.

### 5. Clipboard & Sao lưu/khôi phục
- **Clipboard kiểu Gboard**: Panel clipboard trong bàn phím, ghim mục, tự xóa mục chưa ghim sau 1 giờ, giao diện 2 cột.
- **Prompt bật clipboard**: Khi clipboard tắt hoặc trống, nút 📋 hiển thị trạng thái/hướng dẫn rõ ràng thay vì im lặng.
- **Backup & Restore JSON**: Xuất/nhập qua SAF (gộp hoặc thay thế) cho cài đặt, macro đầy đủ, từ điển cá nhân, từ đã học, bigram/trigram. Hữu ích để chuyển dữ liệu vnchFlex giữa các máy.

### 6. Tối ưu độ trễ (Low-Latency)
- **Space rollover**: Tự động chốt phím `Space` ngay khi ngón tay kế tiếp chạm phím chữ, loại bỏ tình trạng dính chữ khi gõ nhanh.
- **Loại bỏ IPC thừa**: Theo dõi ký tự đã commit nội bộ (`trackCommittedChar`) cho `doubleSpacePeriod` thay vì gọi `getTextBeforeCursor` trên mỗi lần nhấn `Space`.
- **Tách luồng gợi ý & học từ**: Debounce cập nhật thanh gợi ý (`scheduleSuggestionsUpdate`), đẩy học từ/ngữ cảnh sang `learnExecutor` nền (single-thread) và debounce lưu learned words/ngram để phím `Space` và `Backspace` phản hồi tức thì.
- **Composing ổn định**: Cập nhật từ đang gõ bằng `setComposingText` (không UnderlineSpan) thay vì delete+commit lặp lại, giảm rủi ro dính/mất chữ trên Chrome/Zalo/WebView khi gõ nhanh.
- Tái sử dụng View gợi ý và Popup thay vì tạo mới liên tục.

---

## Cấu trúc dự án

```text
vnchflex/
├── app/src/main/java/com/vnchflex/vietkeyboard/
│   ├── ime/
│   │   ├── VietKeyboardService.kt      # Trái tim của bộ gõ (InputMethodService)
│   │   ├── GboardKeyButton.kt          # Nút bấm custom vẽ ký tự chính + ký tự phụ góc trên
│   │   ├── GlideTypingOverlayView.kt   # Lớp phủ bắt cử chỉ vuốt & vẽ vệt vuốt
│   │   ├── GlideDecoder.kt             # Thuật toán giải mã đường vuốt thành từ
│   │   ├── ClipboardStore.kt           # Lưu lịch sử clipboard (ghim/tự hết hạn)
│   │   └── ClipboardPanelController.kt # UI panel clipboard trong bàn phím
│   ├── engine/
│   │   ├── VietnameseEngine.kt         # Bộ xử lý Telex, bỏ dấu tự do, hậu kỳ
│   │   └── DiacriticUtils.kt           # Tiện ích chuẩn hóa & bỏ dấu tiếng Việt
│   ├── macro/
│   │   ├── Macro.kt                    # Data model cho Macro
│   │   ├── MacroStore.kt               # Lưu trữ & Import/Export JSON
│   │   └── MacroEngine.kt              # Engine khớp & bung từ viết tắt
│   ├── suggestion/
│   │   ├── SuggestionEngine.kt         # Unigram (~5.900 từ) + Bigram + Trigram
│   │   └── PersonalDictionaryStore.kt  # Lưu từ vựng cá nhân người dùng hay gõ
│   └── settings/
│       ├── KeyboardPreferences.kt      # Quản lý cấu hình (SharedPreferences)
│       └── SettingsActivity.kt         # Giao diện Cài đặt đầy đủ
├── docs/
│   ├── ARCHITECTURE.md                 # Giải thích kiến trúc chi tiết cho người mới
│   └── DEVELOPMENT_JOURNEY.md          # Tiến trình xây dựng & tối ưu bộ gõ
├── CHANGELOG.md                        # Lịch sử các phiên bản & thay đổi
└── generate_codebase.py                # Script xuất toàn bộ codebase ra codebase_dump.txt
```

---

## Hướng dẫn Build & Cài đặt

### Yêu cầu
- Android Studio hoặc Android SDK (Compile/Target SDK mới nhất)
- JDK 17

### Build từ dòng lệnh
```bash
./gradlew assembleDebug
```
File APK sẽ được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
Ngoài ra, mỗi lần push lên nhánh `main`, GitHub Actions sẽ tự động build và ký APK phát hành.

### Kích hoạt trên điện thoại Android
1. Cài đặt file APK vừa build.
2. Mở ứng dụng **vnchFlex** trên màn hình chính.
3. Bấm **Bật bàn phím trong Cài đặt** và gạt bật **vnchFlex**.
4. Chọn **vnchFlex** làm phương thức nhập mặc định.
