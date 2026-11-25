# Hành trình phát triển & Tối ưu kiến trúc vnchFlex

Tài liệu này tóm tắt quá trình xây dựng **vnchFlex** từ một bản thử nghiệm nhỏ (MVP) thành một bộ gõ tiếng Việt hoàn chỉnh, mượt mà trên Android.

---

## Giai đoạn 1: Khởi tạo MVP (Minimum Viable Product)
- **Mục tiêu**: Có một bàn phím Android chạy được với 3 tính năng cốt lõi: gõ Telex cơ bản, nút `Bỏ dấu` tự do kiểu `vnchKey`, và Macro gõ tắt đơn giản.
- **Kết quả**: Hình thành bộ khung `VietKeyboardService`, `VietnameseEngine`, `MacroEngine`, và `SuggestionEngine` với từ điển nhỏ.

## Giai đoạn 2: Lột xác giao diện chuẩn Gboard
- **Vấn đề**: Giao diện MVP ban đầu thô sơ, phím vuông vức, thiếu hàng số và bảng ký tự phụ.
- **Giải pháp**:
  - Thiết kế lại phím hình oval/bo tròn chuẩn Gboard, dùng icon vector Material sắc nét cho `Shift`, `Backspace`, `Enter`.
  - Tạo `GboardKeyButton` để tự vẽ ký tự phụ nhỏ ở góc trên bên phải mỗi phím.
  - Thêm hàng số tùy chọn, bàn phím ký tự (`?123`, `=\<`), popup xem trước phím và bảng chọn ký tự phụ khi nhấn giữ.
  - Bỏ gạch chân (underline) ở composing text để nhìn tự nhiên hơn.

## Giai đoạn 3: Hoàn thiện Engine Tiếng Việt, Macro & Cử chỉ
- **Engine Tiếng Việt**:
  - Hỗ trợ **Telex bỏ dấu tự do (vnchKey)**, tùy chọn đặt dấu kiểu cũ/mới, và hỗ trợ phương ngữ Nam (`dz-`).
  - Thêm **Post-commit edit** (chạm vào từ cũ để sửa tiếp) và **tự động khôi phục từ tiếng Anh** khi gõ nhầm dấu.
  - Thêm nút chuyển nhanh **VN / EN** và nhấn giữ `Space` để đổi bàn phím hệ thống.
- **Macro & Cử chỉ**:
  - Thêm **Double Backspace** để hoàn nguyên macro vừa bung.
  - Hỗ trợ **Import / Export JSON** danh sách Macro trong `SettingsActivity`.
  - Thêm cử chỉ vuốt trên `Space` để di chuyển con trỏ, vuốt từ `Backspace` để xóa nhanh cả từ, chế độ một tay và chỉnh chiều cao bàn phím.

## Giai đoạn 4: Cuộc chiến tối ưu độ trễ (Fix lag phím Space)
- **Vấn đề thực tế**: Khi gõ nhanh, phím `Space` bị khựng (lag), khiến các từ bị dính vào nhau.
- **Nguyên nhân**: Gọi `InputConnection` (IPC) quá nhiều lần trên mỗi phím bấm, cộng với việc tạo mới View popup/gợi ý và đọc `SharedPreferences` liên tục.
- **Giải pháp**:
  - Loại bỏ các cuộc gọi IPC thừa trên từng phím bấm.
  - Cache `MacroStore` và `PersonalDictionaryStore` trên RAM.
  - Tái sử dụng View cho thanh gợi ý và popup phím; tối ưu tốc độ lặp khi nhấn giữ `Backspace`.
- **Kết quả**: Cảm giác gõ nhẹ, phím `Space` nhả tức thì, không còn hiện tượng dính chữ.

## Giai đoạn 5: Triển khai Glide Typing gọn nhẹ
- **Mục tiêu**: Mang tính năng vuốt để gõ (Glide Typing) lên `vnchFlex` mà không làm nặng bộ gõ.
- **Cách làm**:
  - Xây dựng `GlideTypingOverlayView` phủ lên bàn phím, chỉ kích hoạt khi vuốt quá `24dp` và tự động cập nhật tọa độ phím (`onLayout`) khi đổi cấu hình.
  - Xây dựng `GlideDecoder` gọn nhẹ kết hợp bảng `KEY_NEIGHBORS` và mô hình ngôn ngữ **Trigram**.
  - Mở rộng từ điển lên **~5.900 âm tiết tiếng Việt chuẩn** (đã lọc sạch ký tự lạ và từ dính chữ).
  - Bổ sung UX chuẩn Gboard: hiện 3 ứng viên để chạm đổi nhanh (và học lại từ vừa chọn), bấm `Backspace` 1 lần để xóa nguyên từ vừa vuốt.
