# Viet Keyboard (MVP)

Scaffold bàn phím Android (IME) tập trung vào 3 tính năng được yêu cầu:

- **Macro/Gõ tắt**: thay thế shortcut khi gõ dấu phân cách (`space`, `.`, `,`, ...).
- **Bỏ dấu tự do**: nút `Bỏ dấu` để bỏ dấu vùng chọn, từ đang composing, hoặc từ gần con trỏ.
- **Gợi ý từ cơ bản**: suggestion bar theo prefix (dictionary MVP).

> Đây là MVP để triển khai nhanh, **không phải bản copy đầy đủ GBoard**. 

## Cấu trúc

```text
viet-keyboard/
  app/src/main/java/com/vnchflex/vietkeyboard/
    ime/        # InputMethodService, wiring phím
    engine/     # Telex MVP + DiacriticUtils (bỏ dấu tự do)
    macro/      # Macro model/store/engine
    suggestion/ # Gợi ý từ cơ bản
    settings/   # Màn hình mở cài đặt IME
  app/src/main/res/
    layout/keyboard_view.xml
    xml/method.xml
```

## Tính năng đã có

### 1. Macro / Gõ tắt
- `MacroEngine` kiểm tra token trước con trỏ khi gặp trigger.
- `MacroStore` lưu macro bằng `SharedPreferences` (JSON).
- Macro mẫu: `addr`, `email`, `shrug`.
- Biến MVP: `{date}`, `{time}`.

Ví dụ: gõ `addr` + `Space` => thay bằng địa chỉ + khoảng trắng.

### 2. Bỏ dấu tự do (kiểu vnchKey)
- `DiacriticUtils.strip()` chuẩn hóa NFD, bỏ `\p{Mn}`, đổi `đ/Đ -> d/D`.
- Nút `Bỏ dấu` trên bàn phím:
  - Nếu có selection: bỏ dấu selection.
  - Nếu đang composing: bỏ dấu buffer composing.
  - Nếu không: bỏ dấu từ ngay trước con trỏ.

### 3. Gợi ý từ cơ bản
- `SuggestionEngine` gợi ý prefix từ dictionary nhỏ + từ người dùng đã chọn.
- Chạm suggestion để thay prefix hiện tại.

### 4. Engine tiếng Việt (MVP)
- `VietnameseEngine` có Telex cơ bản cho tone (`s/f/r/x/j`, `z` bỏ dấu) và shape (`a/e/o/w/d`) ở mức đơn giản.
- Cần nâng cấp thành parser âm tiết đầy đủ cho độ chính xác production.

## Chạy dự án

Yêu cầu: Android Studio / Android SDK + JDK 17.

```bash
# từ thư mục viet-keyboard
gradle wrapper   # nếu chưa có wrapper jar
./gradlew assembleDebug
```

Sau khi cài APK:
1. Mở app Viet Keyboard.
2. Bấm **Mở cài đặt bàn phím** và bật Viet Keyboard.
3. Chọn Viet Keyboard khi nhập liệu.

## Hướng cải tiến tiếp theo

- **Engine VN đầy đủ**: parser âm tiết, đặt dấu đúng quy tắc + chế độ đặt dấu tự do, Telex/VNI đầy đủ.
- **Macro nâng cao**: UI quản lý macro, import/export, `{clipboard}`, `{cursor}`, undo macro, đồng bộ.
- **Gợi ý chất lượng cao**: n-gram/on-device LM, autocorrect, cá nhân hóa, tiếng Việt + Anh.
- **UX kiểu GBoard**: long-press popup, gesture vuốt, emoji/clipboard panel, theme, one-handed.
- **Tương thích InputConnection**: test Chrome/WebView/rich editor, batch edit, selection edge cases.
- **Kiểm thử**: unit test cho engine/macro/diacritics, instrumented test IME.

## Lưu ý

- Không sao chép asset/thương hiệu GBoard. Chỉ xây tính năng tương tự.
- Macro và dữ liệu gõ nên giữ local-by-default, rõ ràng về quyền riêng tư.
