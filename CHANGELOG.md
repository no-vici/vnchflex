# Changelog — vnchFlex

Tất cả các thay đổi đáng chú ý của dự án **vnchFlex** đều được ghi lại tại đây.

---

## [Unreleased / Latest] — Glide Typing & Tối ưu từ vựng

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
