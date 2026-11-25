# Kiến trúc & Cách hoạt động của vnchFlex (Dành cho người mới)

Tài liệu này giải thích **từng bước một** cách bàn phím **vnchFlex** hoạt động trên hệ điều hành Android — viết bằng ngôn ngữ bình dân, không cần biết sâu về Android vẫn đọc hiểu được.

---

## 1. Bàn phím Android thực chất là gì?

Trên Android, một ứng dụng bình thường có `Activity` (màn hình bạn mở lên và bấm). Nhưng bàn phím thì khác: nó chạy ngầm bên dưới mọi ứng dụng khác (Zalo, Messenger, Chrome, Ghi chú...).

Trong Android, bàn phím là một **Service** đặc biệt gọi là `InputMethodService` (viết tắt là **IME**).
- Trong dự án này, file đóng vai trò "nhạc trưởng" đó là:
  [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt)
- Nó làm 3 việc chính:
  1. **Vẽ** cái bàn phím lên nửa dưới màn hình.
  2. **Nghe** xem ngón tay bạn chạm vào đâu (chạm phím thường, nhấn giữ, hay vuốt).
  3. **Gửi chữ** lên ô nhập liệu của ứng dụng đang mở (qua một đường ống gọi là `InputConnection`).

---

## 2. Sơ đồ tổng thể các thành phần

Hãy tưởng tượng **vnchFlex** như một nhà hàng:

```text
[Ngón tay bạn]
      │
      ├──► (Chạm phím) ──────────────────┐
      │                                  ▼
      └──► (Vuốt chữ) ──► [GlideTypingOverlayView] ──► [GlideDecoder]
                                                              │
                                                              ▼
                                                  [VietKeyboardService] (Nhạc trưởng)
                                                     /      |       \
                                                    /       |        \
                                                   ▼        ▼         ▼
                                    [VietnameseEngine]  [MacroEngine]  [SuggestionEngine]
                                    (Bỏ dấu Telex)      (Gõ tắt)       (Gợi ý & Trigram)
                                                   \        |        /
                                                    \       |       /
                                                     ▼      ▼      ▼
                                                [InputConnection] ──► (Ô chat Zalo/Chrome)
```

Mỗi bộ phận nằm trong một thư mục (package) riêng biệt để dễ bảo trì:

| Thư mục | Vai trò dễ hiểu | Các file chính |
| :--- | :--- | :--- |
| `ime/` | **Mặt tiền & Nhạc trưởng**: Vẽ phím, bắt cử chỉ chạm/vuốt, nối với Android. | `VietKeyboardService.kt`, `GboardKeyButton.kt`, `GlideTypingOverlayView.kt`, `GlideDecoder.kt` |
| `engine/` | **Bộ não Tiếng Việt**: Biết ghép `a + a -> â`, `s -> sắc`, `z -> xóa dấu`. | `VietnameseEngine.kt`, `DiacriticUtils.kt` |
| `macro/` | **Trợ lý Gõ tắt**: Thấy gõ `addr` + `Space` thì tự bung thành địa chỉ nhà. | `Macro.kt`, `MacroStore.kt`, `MacroEngine.kt` |
| `suggestion/` | **Nhà tiên tri**: Đoán từ bạn sắp gõ dựa vào ~5.900 từ + thói quen của bạn. | `SuggestionEngine.kt`, `PersonalDictionaryStore.kt` |
| `settings/` | **Bảng điều khiển**: Lưu sở thích của bạn (rung, âm thanh, bật/tắt vuốt...). | `KeyboardPreferences.kt`, `SettingsActivity.kt` |

---

## 3. Chuyện gì xảy ra khi bạn gõ một chữ? (Luồng đi chi tiết)

Giả sử bạn muốn gõ chữ **"tôi"** bằng kiểu Telex (`t` -> `o` -> `o` -> `i`):

1. **Bạn chạm phím `t`**:
   - `VietKeyboardService` nhận sự kiện, phát tiếng "tách" (hoặc rung nhẹ nếu bạn bật).
   - Nó đẩy chữ `t` vào một vùng nhớ tạm gọi là **composing buffer** (chữ đang gõ dở, chưa chốt).
   - Nó gửi `t` lên ô nhập liệu qua `InputConnection.setComposingText("t", 1)`.

2. **Bạn chạm phím `o`, rồi `o` lần nữa**:
   - `VietKeyboardService` đưa chuỗi đang gõ cho `VietnameseEngine`.
   - `VietnameseEngine` thấy `to` + `o` -> tự động biến thành **`tô`**.
   - Nếu bạn gõ tiếp `i` -> thành **`tôi`**.

3. **Bạn chạm phím `Space` (Dấu cách)**:
   - Lúc này từ `"tôi"` đã hoàn chỉnh!
   - **Bước A (Kiểm tra tiếng Anh)**: Nếu bạn bật tự khôi phục tiếng Anh và từ đó là tiếng Anh hợp lệ (ví dụ gõ `test` mà lỡ dính dấu), engine sẽ trả lại nguyên gốc.
   - **Bước B (Kiểm tra Macro)**: `MacroEngine` kiểm tra xem `"tôi"` có phải từ viết tắt không. Nếu có (ví dụ bạn gõ `ko`), nó tự thay thành `"không"` và nhớ lại để nếu bạn bấm **Backspace 2 lần** liên tiếp thì hoàn nguyên về `ko`.
   - **Bước C (Chốt từ & Học thói quen)**: `VietKeyboardService` chốt từ `"tôi "` vào ô nhập liệu, đồng thời báo cho `SuggestionEngine` biết: *"Người dùng vừa gõ chữ `tôi`"*, để lần sau gợi ý chuẩn hơn.

---

## 4. Cơ chế Glide Typing (Vuốt để gõ) hoạt động ra sao?

Khi bạn lướt ngón tay qua các phím `x` -> `i` -> `n` mà không nhấc tay lên:

1. **Bắt chuyển động (`GlideTypingOverlayView.kt`)**:
   - Bình thường, lớp phủ này đứng im để bạn bấm phím như bình thường.
   - Khi ngón tay bạn di chuyển quá **24dp**, nó hiểu: *"À, người dùng đang vuốt!"* -> Nó chặn sự kiện bấm phím thường, hủy popup phím đang hiện, và bắt đầu vẽ **vệt màu xanh** theo ngón tay bạn.
   - Trong lúc vuốt, cứ đi qua phím nào, nó ghi nhận chữ cái của phím đó (ví dụ: `x`, `c`, `d`, `i`, `n`).

2. **Giải mã đường vuốt (`GlideDecoder.kt`)**:
   - Khi bạn nhấc ngón tay lên, chuỗi phím đi qua được gửi cho `GlideDecoder`.
   - Thuật toán sẽ quét qua từ điển (~5.900 âm tiết tiếng Việt + từ cá nhân) và chấm điểm từng từ theo các tiêu chí:
     - **Chữ đầu & chữ cuối**: Phải khớp với điểm bắt đầu và kết thúc của đường vuốt (có châm chước nếu ngón tay đặt lệch nhẹ sang phím hàng xóm nhờ bảng `KEY_NEIGHBORS`).
     - **Chuỗi con (Subsequence)**: Các chữ cái của từ có nằm đúng thứ tự trong đường vuốt không?
     - **Gộp chữ lặp (`collapseRepeats`)**: Nếu từ có 2 chữ giống nhau cạnh nhau (như `cần` hoặc `book`), thuật toán không trừ điểm.
     - **Ngữ cảnh Trigram**: Nếu trước đó bạn vừa gõ `"rất vui"`, từ `"được"` sẽ được cộng điểm ưu tiên cao hơn hẳn các từ khác.

3. **Hiển thị & Sửa nhanh**:
   - Từ điểm cao nhất được tự động điền vào ô nhập liệu kèm dấu cách.
   - 3 từ ứng viên tốt nhất hiện lên thanh gợi ý: nếu máy đoán chưa đúng ý, bạn chỉ cần chạm vào từ thứ 2 hoặc thứ 3 để thay thế ngay (và hệ thống cũng học luôn từ bạn vừa chọn).
   - Nếu muốn xóa nguyên từ vừa vuốt, chỉ cần bấm **Backspace 1 lần**.

---

## 5. Tại sao vnchFlex lại gõ mượt và không bị lag phím Space?

Ở các phiên bản đầu, mỗi lần bấm phím, bàn phím hay hỏi đi hỏi lại ứng dụng đang mở: *"Trước con trỏ đang có chữ gì vậy?"* (`getTextBeforeCursor`). Việc hỏi qua lại giữa 2 tiến trình (gọi là **IPC**) rất tốn thời gian, làm phím `Space` bị khựng và gõ nhanh hay bị dính chữ.

**Cách vnchFlex giải quyết:**
1. **Tự nhớ trạng thái ở local**: Tự theo dõi từ đang gõ và từ vừa gõ trong biến bộ nhớ, hạn chế tối đa việc hỏi `InputConnection` trên mỗi phím bấm.
2. **Tái sử dụng View (View Recycling)**: Thanh gợi ý và Popup xem trước phím được tạo sẵn 1 lần rồi dùng lại, không tạo mới (`inflate`) liên tục gây giật khung hình.
3. **Cache dữ liệu**: Danh sách Macro và Từ điển cá nhân được nạp sẵn vào RAM khi khởi động bàn phím, không đọc từ ổ cứng (`SharedPreferences`) mỗi lần gõ.
