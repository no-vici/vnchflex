# CẨM NANG TOÀN TẬP VNCHFLEX: TỪ CON SỐ 0 ĐẾN LÀM CHỦ BỘ GÕ TIẾNG VIỆT & TRÍ TUỆ NHÂN TẠO TRÊN ANDROID

**Tài liệu học tập & giải phẫu mã nguồn dành cho người mới bắt đầu (Absolute Beginners) — Viết bằng ngôn ngữ thân thiện của Người Miền Nam**

---

## LỜI THƯA CÙNG BẠN ĐỌC

Chào bạn! Nếu bạn đang cầm trên tay cuốn sách này, chắc hẳn bạn đang thắc mắc: *"Ủa, mỗi lần mình lấy ngón tay chạm vô cái màn hình điện thoại để nhắn một câu Tiếng Việt hay Tiếng Anh cho bạn bè, ở đằng sau mặt kiếng đó chuyện gì đang xảy ra vậy cà?"*

Hồi mới tập tành viết phần mềm, tụi mình cũng y chang bạn vậy đó. Nhìn một bộ gõ bàn phím trên Android như Gboard hay `vnchKey`, thấy nó vừa vẽ nút bấm mượt rượt ở tốc độ 120 khung hình một giây, vừa tự động ghép chữ `v-i-e-e-t-j` thành chữ `việt`, biết chừa chữ Tiếng Anh như `telee` hay `Vietnamee` ra hổng bỏ dấu bậy, rồi còn cho phép lướt ngón tay một đường dài trên mặt phím (Glide Typing) mà đoán trúng phóc cả câu, chưa kể có cả trí tuệ nhân tạo (AI) tự sửa lỗi chánh tả ngay trên máy mà hổng cần mạng Internet... nhìn vô cứ tưởng đâu là phép thuật cao siêu dữ thần lắm, chắc phải học năm bảy năm trời mới dám đụng vô.

Thiệt ra hổng phải vậy đâu! Bất kỳ hệ thống phần mềm lớn nào cũng được xây lên từ những khối xử lý nhỏ, rõ ràng và dễ hiểu. Khi tụi mình chịu khó tháo từng con ốc của bộ gõ `vnchFlex` ra, phân tích từng phần một bằng ngôn ngữ kỹ thuật bình dân của Người Miền Nam, bạn sẽ thấy mọi thứ rất rõ ràng.

Cuốn sách này được viết ra với một mục tiêu rõ ràng: **Dắt tay một người mới bắt đầu từ con số 0 tròn trĩnh đi tới chỗ đọc hiểu, tự tay sửa chữa và tự viết được một bộ gõ Tiếng Việt hiện đại từ đầu tới cuối.**

Trong suốt cuốn sách này, tụi mình giữ đúng hai nguyên tắc vàng:
1. **Không bỏ qua bước cơ bản nào**: Tụi mình hổng giả định là bạn đã rành hệ điều hành Android hay rành ngôn ngữ lập trình Kotlin từ trước. Khái niệm nào mới xuất hiện — từ con số Unicode, biến số, danh sách (`List`), cho tới cây tiền tố (`Trie`), quy hoạch động (`Dynamic Programming`) hay mạng thần kinh nhân tạo (`ONNX BiGRU`) — đều được tụi mình cắt nghĩa bằng ví dụ đời thường trước, rồi vẽ bảng chạy từng bước bằng tay, xong xuôi mới chỉ vô từng dòng lệnh thiệt trong mã nguồn `vnchFlex`.
2. **Dùng từ ngữ chuẩn mực và giọng điệu thân tình của Người Miền Nam**: Tụi mình thống nhứt dùng chữ **"chánh tả"** (thay cho cách viết ngoài Bắc), viết hoa trang trọng **"Tiếng Việt"** và **"Tiếng Anh"**, xưng hô **"tụi mình — bạn"**, diễn đạt tự nhiên, rõ ràng, sát thực tế như anh em kỹ thuật trao đổi công việc với nhau. Chỗ nào khó thì tụi mình nói kỹ, chỗ nào dễ bị sập bẫy (bug) thì tụi mình chỉ rõ ngọn ngành để bạn né.

Bạn sẵn sàng chưa? Bây giờ tụi mình cùng bước vô cấu trúc bên trong của `vnchFlex` nha!

---

## MỤC LỤC TỔNG QUÁT

- **PHẦN I: NHẬP MÔN TỪ CON SỐ 0 — CHỮ VIẾT, MÃ UNICODE VÀ BÀN PHÍM ANDROID HOẠT ĐỘNG RA SAO?**
  - Chương 1: Hành trình của một cú chạm tay — Từ mặt kiếng cảm ứng tới chữ hiện trên màn hình
  - Chương 2: Bảng mã Unicode, NFC với NFD và cấu trúc âm vận học của Tiếng Việt
  - Chương 3: Kiến trúc tổng thể của `vnchFlex` và lớp `DiacriticUtils` đầu tiên
  - Chương 4: Làm quen với ngôn ngữ Kotlin qua những viên gạch xây nên `vnchFlex`
- **PHẦN II: TRÁI TIM BỘ GÕ — BỘ MÁY TELEX THUẦN KHIẾT (`VietnameseEngine`) & BẢNG GÕ TẮT (`MacroEngine`)**
  - Chương 5: Triết lý Pure-Rebuild (Tái tạo thuần khiết) — Tại sao bộ gõ hổng bao giờ bị nhảy chữ loạn xạ?
  - Chương 6: Giải phẫu từng nhánh biến đổi Telex — Từ `dd`, `aa/ee/oo`, `w` tới phương ngữ Nam Bộ (`dzìa`, `dzách`, `dzô`)
  - Chương 7: Cổng gác âm cuối Tiếng Việt (Valid Coda Gate) và chu kỳ lặp phím hoàn nguyên (`telee -> tele`, `Vietnamee -> Vietname`)
  - Chương 8: Thuật toán đặt dấu thanh chuẩn chánh tả cổ điển (`òa, òe, úy`) và kiểu mới (`oà, oè, uý`)
  - Chương 9: Phục hồi từ đã chốt (`rehydrateWord`) và nghệ thuật giao tiếp không giật lag với Telegram
  - Chương 10: Bộ máy Gõ tắt thông minh (`MacroEngine`, `Macro`, `MacroStore`) và hoàn tác hai lần phím xóa (`Double-Backspace Undo`)
- **PHẦN III: ĐỒ HỌA 120FPS — VẼ CẢ BÀN PHÍM TRÊN MỘT TẤM TOAN DUY NHỨT (`KeyboardCanvasView`)**
  - Chương 11: Tại sao dùng XML View truyền thống lại làm bàn phím bị khựng, và tuyệt chiêu Single-Canvas của `vnchFlex`
  - Chương 12: Toán học chia ô bàn phím, tỷ lệ chiều cao linh hoạt (`keyHeightScale`) và hai khung chữ nhựt `rect` / `hitRect`
  - Chương 13: Máy trạng thái đa điểm chạm (`SparseArray<PointerTracker>`) — Gõ hai ngón cái cùng lúc không bao giờ nuốt chữ
  - Chương 14: Ba cử chỉ thông minh trên một mặt phím: Lướt Spacebar dời con trỏ, Lướt Backspace xóa từng cụm từ, Nhấn giữ mở dải ký tự phụ
- **PHẦN IV: NHẠC TRƯỞNG HỆ THỐNG — DỊCH VỤ BÀN PHÍM (`VietKeyboardService`) & QUẢN LÝ BỘ NHỚ TẠM (`ClipboardStore`)**
  - Chương 15: Vòng đời của một `InputMethodService` và cây cầu `InputConnection`
  - Chương 16: Tuyệt chiêu "Chốt ngay khi chạm xuống, rút lại khi đổi ý" (Commit-on-Down & Cancel-on-Action)
  - Chương 17: Xử lý trọn vẹn phím Xóa lùi (`onBackspace`) và bài học kinh điển từ lỗi `Select All + Backspace`
  - Chương 18: Quản lý bộ nhớ tạm (`ClipboardStore`), ghim văn bản và tự động dọn rác sau 1 giờ
  - Chương 19: Hệ thống tùy chỉnh (`KeyboardPreferences`) — Lưu giữ thói quen của người dùng từ biểu tượng `VnCharKey` tới cách bỏ dấu cổ điển
- **PHẦN V: TRÍ TUỆ NGÔN NGỮ — TIÊN ĐOÁN TỪ, TỰ HỌC N-GRAM VÀ SỬA LỖI LỆCH PHÍM (`SuggestionEngine`)**
  - Chương 20: Cấu trúc dữ liệu Cây tiền tố không dấu (`Stripped Trie`) — Tìm hàng ngàn từ Tiếng Việt trong chớp mắt
  - Chương 21: Mô hình ngôn ngữ thống kê Unigram, Bigram, Trigram và thuật toán lùi bậc Katz Backoff
  - Chương 22: Cơ chế tự học khi gõ, bộ lọc bảo mật thông tin nhạy cảm (Privacy Guard) và ngưỡng từ lạ (OOV Threshold)
  - Chương 23: Thuật toán tự sửa lỗi chạm lệch phím lân cận (`findSpatialAutoCorrection`) và lưu trữ bất đồng bộ (`PersonalDictionaryStore`)
- **PHẦN VI: TRÍ TUỆ NHÂN TẠO & HÌNH HỌC — LƯỚT PHÍM (`GlideDecoder`) & SỬA CHÁNH TẢ ONNX (`OnnxVietnameseCorrector`)**
  - Chương 24: Giải phẫu thuật toán Lướt phím (`GlideDecoder`) — Từ gom chữ trùng (`collapseRepeats`), khoảng cách Levenshtein tới quy hoạch động DTW
  - Chương 25: Mạng thần kinh lướt phím (`neural_glide.onnx`) — Nội suy 48 điểm đẳng cự và trích xuất véc-tơ động học 6 chiều
  - Chương 26: Liên kết dải gợi ý biến thể và từ kế tiếp ngay sau khi lướt phím (`renderGlideCandidates`)
  - Chương 27: Mạng thần kinh sửa lỗi dấu thanh và chánh tả Tiếng Việt 100% ngoại tuyến (`OnnxVietnameseCorrector`)
  - Chương 28: Dịch vụ kiểm tra chánh tả hệ thống (`VnchSpellCheckerService`) và chuẩn hóa văn bản (`VietnameseTextNormalizer`)
- **PHẦN VII: CẨM NANG THỰC HÀNH, GỠ LỖI (DEBUGGING) VÀ BÀI TẬP TỰ RÈN LUYỆN**
  - Chương 29: Bảng tra cứu nhanh 20 kịch bản gõ phím hóc búa nhứt và cách `vnchFlex` xử lý
  - Chương 30: Hướng dẫn tự thêm tính năng mới cho người mới bắt đầu (Kèm lời giải chi tiết)
  - Phụ lục A: Sơ đồ toàn bộ các tập tin mã nguồn trong dự án `vnchFlex`
  - Phụ lục B: Bảng thuật ngữ Kỹ thuật Anh – Việt giải thích theo phong cách bình dân Miền Nam

---

# PHẦN I: NHẬP MÔN TỪ CON SỐ 0 — CHỮ VIẾT, MÃ UNICODE VÀ BÀN PHÍM ANDROID HOẠT ĐỘNG RA SAO?

---

## CHƯƠNG 1: HÀNH TRÌNH CỦA MỘT CÚ CHẠM TAY — TỪ MẶT KIẾNG CẢM ỨNG TỚI CHỮ HIỆN TRÊN MÀN HÌNH

### 1.1. Bàn phím điện thoại không phải là một phần của ứng dụng nhắn tin!

Khi bạn mở ứng dụng Zalo, Messenger, hay trình duyệt Chrome lên rồi chạm vô ô nhập chữ, tự nhiên ở nửa dưới màn hình có cái bàn phím trồi lên. Nhiều bạn mới học lập trình hay tưởng lầm rằng mỗi ứng dụng (như Zalo hay Chrome) tự vẽ cái bàn phím đó ra.

Thiệt ra hổng phải vậy đâu! Trên hệ điều hành Android, **bàn phím là một ứng dụng độc lập hoàn toàn**, chạy ngầm ở bên dưới dưới dạng một Dịch vụ hệ thống (gọi là `InputMethodService`, viết tắt là **IME**).

Tại sao người ta lại thiết kế tách rời như vậy? Bạn thử nghĩ coi:
1. Nếu ứng dụng nào cũng phải tự viết bàn phím riêng, thì ông viết app ghi chú cũng phải ngồi lập trình bộ gõ Tiếng Việt, ông viết app ngân hàng cũng phải viết lại bộ gõ Tiếng Việt. Vừa cực khổ, vừa mỗi nơi gõ một kiểu!
2. Quan trọng hơn nữa là **vấn đề bảo mật và thói quen cá nhân**: Khi bạn cài `vnchFlex` làm bàn phím mặc định, thì dù bạn đang gõ chữ trong Zalo, Facebook, Gmail hay khung tìm kiếm Google, cũng chỉ có một mình anh `vnchFlex` phục vụ bạn thôi. Mọi bảng gõ tắt (Macro), từ điển cá nhân bạn đã dạy cho máy, hay thói quen bỏ dấu cổ điển (`hoà` hay `hòa`) đều đi theo bạn qua tất cả các ứng dụng.

### 1.2. Ba nhân vật chánh trong một cú chạm phím

Để chữ hiện lên được trên màn hình, lúc nào cũng có sự phối hợp nhịp nhàng của 3 nhân vật sau đây:

```
+-----------------------------------------------------------------------+
|                       ỨNG DỤNG ĐANG MỞ (Editor / App)                 |
|   Ví dụ: Zalo, Chrome, Ghi chú (chứa ô nhập chữ EditText)             |
+-----------------------------------------------------------------------+
                                   ^
                                   |  (3) Gởi lệnh sửa chữ qua cây cầu
                                   |      InputConnection (IPC)
                                   |
+-----------------------------------------------------------------------+
|                BỘ ĐIỀU PHỐI TRUNG TÂM (VietKeyboardService)           |
|   Nhận sự kiện phím -> Hỏi VietnameseEngine -> Gởi chữ lên App        |
+-----------------------------------------------------------------------+
         ^                                                 |
         | (1) Báo: "Người dùng vừa chạm                   | (2) Đưa chữ 'e' vô
         |     ngón tay vô tọa độ (x, y)                   |     VietnameseEngine
         |     trúng cái nút chữ 'e' nè!"                  v
+----------------------------------+     +------------------------------+
|   MẶT BÀN PHÍM (KeyboardCanvas)  |     |   BỘ MÁY TELEX (VnEngine)    |
|   Vẽ nút bấm & bắt tọa độ chạm   |     |   Ghép v-i-e-e-t-j -> "việt" |
+----------------------------------+     +------------------------------+
```

Tụi mình cùng theo dõi từng bước khi bạn muốn gõ chữ **"việt"** bằng kiểu gõ Telex (`v`, `i`, `e`, `e`, `t`, `j`) nha:

- **Bước 1 (Cảm ứng phần cứng)**: Ngón tay cái của bạn chạm nhẹ lên mặt kiếng điện thoại ở chỗ có vẽ chữ `v`. Màn hình cảm ứng đo sự thay đổi điện dung và báo cho hệ điều hành Android một cặp tọa độ điểm ảnh, ví dụ: `x = 412.5, y = 215.0`.
- **Bước 2 (Phát hiện phím bấm)**: Lớp giao diện [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt) của `vnchFlex` nhận tọa độ `(412.5, 215.0)` này. Nó dò trong danh sách 44 hình chữ nhựt trên mặt bàn phím coi tọa độ đó nằm lọt trong ô nào. À, lọt vô ô của phím `v`!
- **Bước 3 (Báo cho nhạc trưởng)**: `KeyboardCanvasView` liền rung nhẹ một cái (Haptic Feedback) cho ngón tay bạn có cảm giác chân thật, đồng thời gọi điện báo cho nhạc trưởng [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt): *"Anh ơi, người dùng vừa nhấn phím `v` xuống!"*
- **Bước 4 (Chế biến chữ Tiếng Việt)**: Nhạc trưởng `VietKeyboardService` chuyển chữ `v` đó qua cho đầu bếp trưởng [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt). Đầu bếp trưởng bỏ chữ `v` vô cái rổ chứa chữ thô (`rawChars = ['v']`), chế biến ra kết quả `"v"`, rồi đưa lên ô nhập chữ của Zalo thông qua cây cầu `InputConnection`.
- **Bước 5 (Biến hóa kỳ diệu ở những phím tiếp theo)**:
  - Bạn gõ tiếp `i`: Rổ chữ thô có `['v', 'i']` -> Hiện lên màn hình chữ `"vi"` (có gạch chân mờ ở dưới báo hiệu từ này đang gõ dở dang, gọi là **Composing Text**).
  - Bạn gõ tiếp `e`: Rổ chữ thô có `['v', 'i', 'e']` -> Hiện lên `"vie"`.
  - Bạn gõ tiếp `e` lần nữa: Rổ chữ thô có `['v', 'i', 'e', 'e']`. Đầu bếp `VietnameseEngine` thấy hai chữ `e` đi liền nhau (`ee`) liền hô biến thành chữ `"viê"`!
  - Bạn gõ tiếp `t`: Rổ chữ thô có `['v', 'i', 'e', 'e', 't']` -> Hiện lên `"viêt"`.
  - Bạn gõ tiếp `j` (dấu nặng trong Telex) ở cuối cùng: Rổ chữ thô có `['v', 'i', 'e', 'e', 't', 'j']`. Đầu bếp `VietnameseEngine` nhìn vô chữ `"viêt"`, tìm nguyên âm chánh là chữ `ê`, gắn dấu nặng vô dưới đít chữ `ê` -> Hiện lên chữ **"việt"** tròn trịa, đẹp đẽ!

Nghe qua thì thấy tự nhiên như hơi thở vậy đó, nhưng để máy tính hiểu được thế nào là chữ `e`, thế nào là chữ `ê`, và làm sao gắn thêm dấu nặng thành chữ `ệ`, tụi mình phải hiểu cách máy tính lưu trữ chữ viết trong bộ nhớ. Đó chánh là câu chuyện của **Chương 2** ngay sau đây.

---

## CHƯƠNG 2: BẢNG MÃ UNICODE, NFC VỚI NFD VÀ CẤU TRÚC ÂM VẦN CỦA TIẾNG VIỆT

### 2.1. Máy tính đâu có biết chữ, nó chỉ biết con số thôi!

Ở bên trong con chip điện thoại, mọi thứ đều là dòng điện bật/tắt (số `0` và số `1`). Máy tính hoàn toàn hổng biết mặt mũi chữ `a`, chữ `ă` hay chữ `đ` tròn méo ra làm sao. Để hiển thị được chữ viết của toàn nhân loại, các kỹ sư trên thế giới mới ngồi lại với nhau lập ra một cuốn sổ cái khổng lồ gọi là **Bảng mã Unicode**.

Trong cuốn sổ Unicode đó, **mỗi ký tự được cấp một mã số định danh duy nhứt** (gọi là *Code Point*). Ví dụ:
- Chữ `'a'` thường trong Tiếng Anh và Tiếng Việt có mã số là `97` (viết theo hệ thập lục phân là `U+0061`).
- Chữ `'A'` hoa có mã số là `65` (`U+0041`).
- Chữ `'đ'` thường có mã số là `273` (`U+0111`), còn chữ `'Đ'` hoa là `272` (`U+0110`).
- Chữ `'ệ'` thường có mã số là `7879` (`U+1EC7`).

Khi `vnchFlex` gởi con số `7879` qua cho ứng dụng Zalo, bộ vẽ chữ (Font Renderer) của Android sẽ tra trong tập tin phông chữ (như phông Roboto) coi ô số `7879` vẽ cái hình gì, rồi tô mực lên màn hình thành hình chữ `ệ` cho mắt mình nhìn thấy.

### 2.2. Hai cách biểu diễn chữ Tiếng Việt trong Unicode: Dựng sẵn (NFC) và Tổ hợp (NFD)

Đây là kiến thức cực kỳ quan trọng mà bất kỳ ai muốn viết bộ gõ hay xử lý văn bản Tiếng Việt đều phải nắm vững như lòng bàn tay. Trong chuẩn Unicode, một chữ cái Tiếng Việt có dấu (như chữ `ệ` hay chữ `ắ`) có tới **hai cách** để biểu diễn:

#### Cách 1: Unicode Dựng sẵn (NFC — Normalization Form C / Canonical Composition)
Ở cách này, chữ cái gốc, dấu mũ/móc và dấu thanh được đúc sẵn thành **một ký tự duy nhứt** (1 Code Point duy nhứt):
- Chữ `"ệ"` trong chuẩn NFC chỉ gồm **1 ký tự** có mã `U+1EC7`.
- Khi bạn hỏi ngôn ngữ lập trình Kotlin: `"ệ".length` bằng mấy? Nó sẽ trả lời là **`1`**.

#### Cách 2: Unicode Tổ hợp (NFD — Normalization Form D / Canonical Decomposition)
Ở cách này, chữ `"ệ"` bị tháo rời ra thành từng mảnh lắp ghép lại với nhau:
1. Mảnh thứ nhứt: Chữ cái gốc `'e'` (`U+0065`).
2. Mảnh thứ hai: Dấu mũ `ˆ` bay lơ lửng (`U+0302` — Combining Circumflex Accent).
3. Mảnh thứ ba: Dấu chấm nặng `̣` bay lơ lửng (`U+0323` — Combining Dot Below).

Khi ba mảnh này xếp cạnh nhau (`e` + `ˆ` + `̣`), màn hình vẫn vẽ ra hình chữ `"ệ"` y hệt như NFC! Nhưng nếu bạn hỏi máy tính độ dài chuỗi (`length`), nó sẽ đếm cả 3 mảnh và trả lời là **`3`**!

> **Tại sao người mới học cần biết chuyện NFC và NFD?**
>
> 1. **Khi xuất chữ ra màn hình**: Bộ gõ `vnchFlex` luôn luôn xuất ra chuẩn **NFC (Dựng sẵn)**. Tại vì sao? Tại vì hầu hết các trang web, cơ sở dữ liệu và ứng dụng trên điện thoại đều mong đợi chuẩn NFC. Nếu xuất ra chuẩn NFD, khi người dùng bấm phím xóa lùi (`Backspace`), nó chỉ xóa cái dấu nặng mà chừa lại chữ `ê`, hoặc khi copy dán qua một số phần mềm cũ sẽ bị lỗi bể phông chữ (dấu bay lệch ra khỏi chữ cái).
> 2. **Khi muốn lột sạch dấu Tiếng Việt**: Ngược lại, chuẩn **NFD (Tổ hợp)** lại là một "cây đũa thần" khi tụi mình muốn chuyển một câu Tiếng Việt có dấu thành không dấu (ví dụ biến `"Việt Nam"` thành `"Viet Nam"` để tìm kiếm trong từ điển). Thay vì phải ngồi viết bảng tra cho 134 chữ cái có dấu, tụi mình chỉ cần kêu Java/Kotlin tháo chữ ra thành chuẩn NFD, rồi lấy chổi quét sạch mấy cái dấu bay lơ lửng đi là xong! (Tụi mình sẽ coi đoạn code thiệt chỉ có 5 dòng ở Chương 3 ngay bên dưới).

### 2.3. Cấu trúc một âm tiết Tiếng Việt — Hiểu tiếng mẹ đẻ để dạy cho máy tính

Trước khi viết hàm bỏ dấu, tụi mình hãy ôn lại cấu trúc của một tiếng (âm tiết) trong Tiếng Việt mình nha. Tiếng Việt là ngôn ngữ đơn lập, mỗi âm tiết khi viết ra được ngăn cách bằng khoảng trắng và có cấu tạo tối đa gồm **4 thành phần**:

```
               [ DẤU THANH (Tone): Sắc, Huyền, Hỏi, Ngã, Nặng ]
                                     |
         +---------------------------+---------------------------+
         |                           |                           |
  [1. PHỤ ÂM ĐẦU]            [2. ÂM ĐỆM & NGUYÊN ÂM]     [3. PHỤ ÂM CUỐI]
     (Onset)                     (Nucleus)                   (Coda)
   Ví dụ: "tr", "ng",          Ví dụ: "a", "ê", "ươ",      Ví dụ: "c", "ch", "m",
          "gh", "qu",                 "oa", "uy", "iê",           "n", "ng", "nh",
          "dz" (Nam Bộ)               "uây", "oai"...             "p", "t"
```

Tụi mình lấy vài chữ quen thuộc ra mổ xẻ thử coi:
1. Chữ **"trưởng"**:
   - Phụ âm đầu (Onset): `"tr"`
   - Nguyên âm đôi (Nucleus): `"ươ"`
   - Phụ âm cuối (Coda): `"ng"`
   - Dấu thanh (Tone): Dấu hỏi (`r` trong Telex), đặt ngay trên đầu chữ `ơ` -> `"trưởng"`.
2. Chữ **"hoà"** (kiểu mới) hay **"hòa"** (kiểu cổ điển):
   - Phụ âm đầu: `"h"`
   - Cụm nguyên âm: `"oa"`
   - Phụ âm cuối: Không có (âm tiết mở).
   - Dấu thanh: Dấu huyền (`f` trong Telex). Với cách bỏ dấu cổ điển (mặc định của `vnchFlex`), dấu huyền đặt trên chữ `o` (`"hòa"`). Với cách bỏ dấu kiểu mới, dấu huyền đặt trên chữ `a` (`"hoà"`).
3. Chữ **"dzìa"** (phương ngữ Nam Bộ quen thuộc của chữ *"về"*):
   - Phụ âm đầu đặc biệt của Người Miền Nam: `"dz"`
   - Cụm nguyên âm: `"ia"`
   - Phụ âm cuối: Không có.
   - Dấu thanh: Dấu huyền đặt trên chữ `i` -> `"dzìa"`.

#### Quy luật vàng về Phụ âm cuối (Coda) trong Tiếng Việt
Bạn để ý kỹ chỗ này nha, vì đây chánh là chìa khóa giúp `vnchFlex` thông minh hơn các bộ gõ sơ sài khác:
Trong Tiếng Việt chánh thống, đứng ở cuối một từ (sau nguyên âm) **chỉ có thể là 8 phụ âm cuối hợp lệ** (hoặc các bán nguyên âm `i, y, u, o`):
- 4 phụ âm mũi: **`m`**, **`n`**, **`ng`**, **`nh`** (ví dụ: *làm, bàn, làng, lành*).
- 4 phụ âm tắc vô thanh: **`p`**, **`t`**, **`c`** (hoặc `-k` trong tốc ký), **`ch`** (ví dụ: *đẹp, đất, nước, sách*).

Tại sao quy luật 8 phụ âm cuối này lại sống còn đối với bộ gõ?
Giả sử bạn đang gõ chữ Tiếng Anh hoặc tên riêng như **`tele`** hay **`Vietnamee`**:
- Khi bạn gõ `t-e-l-e`, giữa hai chữ `e` là phụ âm **`l`**.
- Chữ **`l`** có nằm trong danh sách 8 phụ âm cuối Tiếng Việt (`c, ch, m, n, ng, nh, p, t`) hông? **Hoàn toàn KHÔNG!** Không có chữ Tiếng Việt nào kết thúc bằng âm `-el` hết trơn!
- Tuy nhiên, nếu bạn lỡ gõ `t-e-l-e` trong chế độ bỏ dấu tự do thì chữ `e` thứ hai có thể tạm thời đóng vai trò dấu mũ cho chữ `e` đầu thành `têl`. Khi bạn gõ tiếp chữ `e` thứ ba (`telee`), bộ máy phải hiểu rằng bạn muốn **hoàn nguyên (hủy dấu mũ)** để trả về chữ Tiếng Anh `"tele"`.
- Tương tự, khi bạn gõ chữ **`teleegram`**, sau khi đã hủy dấu mũ ở `telee` thành `tele`, bạn gõ tiếp `g-r-a-m`. Giữa chữ `e` và chữ `a` lúc này là cụm `"gr"`, còn sau chữ `a` là chữ `m`. Nếu bộ gõ hổng kiểm tra nghiêm ngặt cấu trúc âm tiết, chữ `r` trong `gram` sẽ bị hiểu lầm là dấu hỏi (`r`) nhảy ngược lên đầu chữ `e` làm chữ nhảy loạn xạ!

Nhờ nắm chắc âm vận học Tiếng Việt, tụi mình mới dạy cho `vnchFlex` phân biệt rạch ròi đâu là chữ Tiếng Việt cần bỏ dấu, đâu là chữ Tiếng Anh cần giữ nguyên.

---

## CHƯƠNG 3: KIẾN TRÚC TỔNG THỂ CỦA VNCHFLEX VÀ LỚP `DiacriticUtils` ĐẦU TIÊN

### 3.1. Bản đồ 5 khu vực trong mã nguồn `vnchFlex`

Khi bạn mở thư mục mã nguồn của `vnchFlex` tại `app/src/main/java/com/vnchflex/vietkeyboard/`, bạn sẽ thấy toàn bộ 18 tập tin Kotlin được sắp xếp gọn gàng vô 5 gói (packages) theo đúng chức năng:

| Tên gói (Package) | Vai trò trong bộ gõ | Các tập tin tiêu biểu |
| :--- | :--- | :--- |
| **`engine`** | Bộ máy xử lý chữ Tiếng Việt, chuẩn hóa dấu và mô hình AI sửa lỗi chánh tả | [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt), [`DiacriticUtils.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/DiacriticUtils.kt), [`OnnxVietnameseCorrector.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/OnnxVietnameseCorrector.kt), [`VietnameseTextNormalizer.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseTextNormalizer.kt) |
| **`macro`** | Bộ máy Gõ tắt thông minh (biến `vn` thành `Việt Nam`, `dc` thành `được`) | [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt), [`Macro.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/Macro.kt), [`MacroStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroStore.kt) |
| **`ime`** | Giao diện bàn phím 120FPS, dịch vụ kết nối hệ điều hành Android, giải mã lướt phím và bộ nhớ tạm | [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt), [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt), [`GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt), [`ClipboardStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardStore.kt), [`VnchSpellCheckerService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VnchSpellCheckerService.kt) |
| **`suggestion`** | Trí tuệ tiên đoán từ, cây tiền tố Trie, tự học N-gram và từ điển cá nhân | [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt), [`PersonalDictionaryStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/PersonalDictionaryStore.kt) |
| **`settings`** | Quản lý cài đặt và màn hình tùy chỉnh giao diện, chiều cao, kiểu bỏ dấu | [`KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt), [`SettingsActivity.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/SettingsActivity.kt) |

### 3.2. Đọc hiểu tập tin mã nguồn đầu tiên: `DiacriticUtils.kt`

Để khởi động nhẹ nhàng, tụi mình cùng mở tập tin ngắn gọn nhứt trong dự án: [`DiacriticUtils.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/DiacriticUtils.kt) (chỉ có đúng 25 dòng code). Tập tin này làm đúng một nhiệm vụ mà tụi mình vừa nhắc tới ở Chương 2: **Lột sạch dấu Tiếng Việt ra khỏi một chuỗi chữ**.

Tụi mình coi nguyên văn đoạn code rồi cắt nghĩa từng dòng nha:

```kotlin
package com.vnchflex.vietkeyboard.engine

import java.text.Normalizer

object DiacriticUtils {
    private val COMBINING_MARKS = Regex("\\p{Mn}+")

    /** Removes Vietnamese diacritics, including đ/Đ. */
    fun strip(input: String): String {
        if (input.isEmpty()) return input
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return COMBINING_MARKS.replace(normalized, "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    /**
     * "Bỏ dấu tự do" style toggle: if text contains diacritics, strip them;
     * otherwise return original. Caller can extend with re-apply logic.
     */
    fun toggleStrip(input: String): String {
        val stripped = strip(input)
        return if (stripped == input) input else stripped
    }
}
```

#### Giải thích từng dòng cho người mới bắt đầu:
- **Dòng 5 (`object DiacriticUtils`)**: Trong ngôn ngữ Kotlin, từ khóa `object` dùng để tạo ra một **Đối tượng duy nhứt (Singleton)** — giống như một hộp đồ nghề chung của cả chương trình vậy. Bất kỳ chỗ nào cần lột dấu chữ đều có thể gọi ngay `DiacriticUtils.strip(...)` mà hổng cần phải tốn công khởi tạo hộp đồ nghề mới.
- **Dòng 6 (`private val COMBINING_MARKS = Regex("\\p{Mn}+")`)**:
  - `Regex` là viết tắt của *Regular Expression* (Biểu thức chánh quy — giống như một cái khuôn lọc chữ).
  - Ký hiệu `\\p{Mn}` là một nhóm chuẩn của Unicode có tên là **Mark, Nonspacing** (tức là tất cả các dấu thanh sắc, huyền, hỏi, ngã, nặng và các dấu mũ `ˆ`, dấu trăng `˘`, dấu móc ` horn` bay lơ lửng không chiếm độ rộng riêng sau khi đã tách ra ở dạng NFD). Dấu `+` ở cuối có nghĩa là "một hoặc nhiều dấu đi liền nhau".
- **Dòng 9–15 (`fun strip(input: String): String`)**:
  - Giả sử bạn truyền vô `input = "Đường về miền Nam"`.
  - **Dòng 10**: Kiểm tra nếu chuỗi rỗng (`input.isEmpty()`) thì trả về ngay lập tức, khỏi tốn công làm gì tiếp.
  - **Dòng 11**: `Normalizer.normalize(input, Normalizer.Form.NFD)` — Đây chánh là phép biến hình từ chuẩn NFC sang chuẩn NFD mà tụi mình học ở Chương 2! Lúc này:
    - Chữ `'ờ'` bị tách làm 3 mảnh: `'o'` + dấu móc `̛` + dấu huyền `̀`.
    - Chữ `'ề'` bị tách làm 3 mảnh: `'e'` + dấu mũ `ˆ` + dấu huyền `̀`.
    - Riêng chữ `'Đ'` thì sao? Trong bảng mã Unicode quốc tế, chữ `'đ'` và `'Đ'` (D có gạch ngang) được xếp vô loại **chữ cái độc lập (Base Letter)** chớ hổng phải chữ `'d'` gắn thêm dấu tổ hợp! Bởi vậy khi chuyển sang NFD, chữ `'Đ'` vẫn đứng nguyên là `'Đ'`.
  - **Dòng 12**: `COMBINING_MARKS.replace(normalized, "")` — Lấy cái chổi `COMBINING_MARKS` quét sạch toàn bộ các dấu bay lơ lửng thay bằng chuỗi rỗng `""`. Sau bước này, `"Đường về miền Nam"` biến thành `"Đuong ve mien Nam"`.
  - **Dòng 13–14**: `.replace('đ', 'd').replace('Đ', 'D')` — Xử lý nốt hai chữ `'đ'` và `'Đ'` đặc biệt của Tiếng Việt mình để ra kết quả cuối cùng sạch trơn: **`"Duong ve mien Nam"`**.

Bạn thấy hông? Chỉ với vài dòng ngắn gọn, nhờ hiểu rõ bản chất Unicode NFD, tụi mình đã viết được một hàm lột dấu Tiếng Việt chạy nhanh như chớp và hổng bao giờ sót một chữ nào!

---

## CHƯƠNG 4: LÀM QUEN VỚI NGÔN NGỮ KOTLIN QUA NHỮNG VIÊN GẠCH XÂY NÊN VNCHFLEX

Nếu bạn chưa từng viết một dòng code Kotlin nào trong đời, thì chương này dành riêng cho bạn đó. Kotlin là ngôn ngữ chánh thức mà Google chọn để viết ứng dụng Android hiện đại. Nó cực kỳ gần gũi với lời ăn tiếng nói hàng ngày. Bạn chỉ cần nắm 6 khái niệm cơ bản dưới đây là đọc hiểu được 95% mã nguồn của `vnchFlex` rồi!

### 4.1. Biến cố định (`val`) và Biến thay đổi được (`var`)

Trong chương trình máy tính, tụi mình cần những cái hộp có dán nhãn để cất dữ liệu. Kotlin có hai loại hộp:
- **`val` (Value — Hộp khóa chặt)**: Bỏ đồ vô một lần lúc mới tạo, sau đó chỉ lấy ra xài chớ hổng đổi món khác vô được.
  ```kotlin
  val cornerR = 6.5f * density // Bán kính bo góc của phím bấm, tính xong giữ nguyên
  ```
- **`var` (Variable — Hộp mở nắp)**: Có thể thay đổi giá trị bên trong bất cứ lúc nào.
  ```kotlin
  var tone = Tone.NONE // Lúc mới gõ chưa có dấu thanh (NONE), lát gõ chữ 's' thì đổi thành Tone.ACUTE (dấu sắc)
  ```

### 4.2. Danh sách (`List` và `MutableList`), Tập hợp (`Set`) và Bản đồ tra cứu (`Map`)

Khi làm bộ gõ, tụi mình làm việc với chữ cái liên tục nên dùng 3 cấu trúc chứa dữ liệu này hoài:

1. **Danh sách có thứ tự (`List` / `MutableList`)**: Giống như một đoàn tàu lửa có các toa đánh số từ toa số `0`, toa số `1`, toa số `2`...
   - Trong [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L42), tụi mình có:
     ```kotlin
     private val rawChars = mutableListOf<Char>()
     ```
     Đây là đoàn tàu nối đuôi các phím bạn vừa gõ. Gõ `v`, toa `0` là `'v'`. Gõ tiếp `i`, toa `1` là `'i'`. Muốn xóa toa cuối cùng khi nhấn Backspace thì gọi `rawChars.removeAt(rawChars.lastIndex)`.
2. **Tập hợp không trùng lặp (`Set`)**: Giống như một cái túi chứa các món đồ không theo thứ tự, dùng để hỏi nhanh: *"Món này có nằm trong túi hông?"* chỉ trong một bước (`O(1)`).
   - Ví dụ tập hợp các phụ âm cuối hợp lệ trong Tiếng Việt:
     ```kotlin
     val validCodas = setOf("c", "ch", "m", "n", "ng", "nh", "p", "t", "i", "u", "y")
     if (between in validCodas) { /* Hợp lệ! */ }
     ```
3. **Bản đồ tra cứu (`Map<Khóa, Giá_Trị>`)**: Giống như cuốn từ điển, tra chữ bên trái (Key) thì lấy được nghĩa bên phải (Value).
   - Ví dụ trong [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L666-L680), tụi mình có bảng tra dấu thanh cho từng nguyên âm:
     ```kotlin
     'a' to listOf('a', 'á', 'à', 'ả', 'ã', 'ạ'),
     'ê' to listOf('ê', 'ế', 'ề', 'ể', 'ễ', 'ệ')
     ```
     Khi cần bỏ dấu nặng (vị trí số 5) cho chữ `'ê'`, máy chỉ việc tra dòng `'ê'` rồi lấy phần tử thứ 5 là ra ngay chữ `'ệ'`!

### 4.3. Kiểu liệt kê (`enum class`) và Lớp dữ liệu (`data class`)

- **`enum class` (Kiểu liệt kê hữu hạn)**: Dùng khi một thứ chỉ có đúng vài trạng thái cố định. Ví dụ Tiếng Việt mình có thanh ngang (không dấu) và 5 dấu thanh, tổng cộng là 6 trạng thái:
  ```kotlin
  enum class Tone(val index: Int) {
      NONE(0),  // Thanh ngang (không dấu)
      ACUTE(1), // Dấu sắc (phím s)
      GRAVE(2), // Dấu huyền (phím f)
      HOOK(3),  // Dấu hỏi (phím r)
      TILDE(4), // Dấu ngã (phím x)
      DOT(5)    // Dấu nặng (phím j)
  }
  ```
- **`data class` (Lớp gói dữ liệu)**: Dùng để gom nhiều thông tin của một vật thể lại thành một gói gọn gàng. Ví dụ mỗi phím bấm trên màn hình trong [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L37-L45) được định nghĩa bằng:
  ```kotlin
  data class KeySpec(
      val id: String,                   // Mã định danh phím, vd: "key_a"
      val role: KeyRole,                // Vai trò: Phím chữ, phím số, phím Shift hay phím Space
      val primaryLabel: String,         // Chữ chánh vẽ giữa phím, vd: "a"
      val secondaryLabel: String? = null, // Ký hiệu phụ vẽ góc trên khi nhấn giữ, vd: "@"
      val weight: Float = 1f,           // Độ rộng tương đối của phím (phím thường = 1, phím Space = 5.2)
      val rect: RectF = RectF(),        // Khung chữ nhựt để vẽ hình nút bấm
      val hitRect: RectF = RectF()      // Khung chữ nhựt để bắt cảm ứng ngón tay
  )
  ```

### 4.4. Dấu chấm hỏi an toàn (`?`) — Vũ khí chống văng ứng dụng (Null Safety)

Trên Android, lỗi làm ứng dụng bị tắt ngang (crash) nhiều nhứt là lỗi `NullPointerException` — tức là cố lấy đồ trong một cái hộp đang trống rỗng (`null`).
Kotlin giải quyết triệt để chuyện này bằng dấu chấm hỏi `?`:
- Nếu một biến có thể bị rỗng, kiểu của nó bắt buộc phải có dấu `?` ở cuối, ví dụ `val secondaryLabel: String? = null` (có phím có ký hiệu phụ ở góc, có phím hổng có).
- Khi gọi hàm trên biến có thể rỗng, ta dùng `?.` (gọi an toàn: nếu có đồ bên trong thì mới làm, nếu rỗng thì bỏ qua êm thấm) hoặc `?:` (toán tử Elvis: nếu bên trái rỗng thì lấy giá trị dự phòng bên phải).
  ```kotlin
  val before = ic.getTextBeforeCursor(64, 0)?.toString().orEmpty()
  ```
  Câu lệnh trên đọc bằng Tiếng Việt mình là: *"Hỏi cây cầu `ic` lấy giùm 64 ký tự đứng trước con trỏ; nếu lấy được (`?.`) thì đổi ra chuỗi chữ (`toString()`), còn lỡ rỗng thì trả về chuỗi trống (`orEmpty()`) chớ đừng có báo lỗi nha!"*

Nắm vững 4 chương nhập môn này là bạn đã có đủ nội công căn bản rồi đó. Giờ tụi mình bước qua **Phần II** để mổ xẻ trái tim của bộ gõ Tiếng Việt `vnchFlex` nha!

---

# PHẦN II: TRÁI TIM BỘ GÕ — BỘ MÁY TELEX THUẦN KHIẾT (`VietnameseEngine`) & BẢNG GÕ TẮT (`MacroEngine`)

---

## CHƯƠNG 5: TRIẾT LÝ PURE-REBUILD (TÁI TẠO THUẦN KHIẾT) — TẠI SAO BỘ GÕ HỔNG BAO GIỜ BỊ NHẢY CHỮ LOẠN XẠ?

### 5.1. Cái bẫy của cách viết bộ gõ kiểu cũ (Stateful String Mutation)

Khi một người mới bắt đầu tự viết bộ gõ Tiếng Việt, cách tự nhiên nhứt mà ai cũng nghĩ tới là **sửa trực tiếp trên chuỗi chữ đang hiện trên màn hình**:
- Đang có chữ `"viê"`, người dùng gõ thêm `'t'` -> nối `'t'` vô đít thành `"viêt"`.
- Người dùng gõ thêm `'j'` -> tìm chữ `'ê'` trong `"viêt"`, đổi thành `'ệ'` -> ra `"việt"`.

Cách làm "sửa tới đâu quên gốc tới đó" này nhìn thì có vẻ đơn giản, nhưng khi người dùng gõ nhanh, gõ bỏ dấu tự do, hoặc gõ xen kẽ Tiếng Anh với Tiếng Việt, nó sẽ sập hầm liền! Tại sao vậy?
Bạn thử hình dung 3 tình huống thực tế này coi:
1. **Tình huống 1 (Dấu thanh đi chuyển khi thêm nguyên âm hoặc phụ âm cuối)**:
   - Bạn muốn gõ chữ `"hoàn"`. Bạn gõ theo thói quen bỏ dấu sớm: `h-o-f` -> máy hiện `"hò"`.
   - Tiếp theo bạn gõ chữ `'a'` (`h-o-f-a`) -> thành `"hòa"`.
   - Tiếp theo bạn gõ chữ `'n'` (`h-o-f-a-n`). Trong Tiếng Việt, khi có phụ âm cuối `'n'` đóng âm tiết lại, dấu huyền bắt buộc phải dời từ chữ `'ò'` qua chữ `'à'` để thành **`"hoàn"`** (chớ hổng ai viết `"hòan"` hết!).
   - Nếu bộ gõ sửa trực tiếp trên chuỗi mà quên mất lịch sử phím gõ, việc dò tìm xem trên chữ đang có dấu gì, bóc dấu đó ra khỏi chữ `'ò'` rồi dời qua chữ `'a'` rất dễ bị rối khi gặp nguyên âm đôi, nguyên âm ba.
2. **Tình huống 2 (Gõ chữ Tiếng Anh có nhiều nguyên âm trùng như `telee`, `Vietnamee`)**:
   - Bạn gõ `t-e-l-e`: máy thấy có chữ `e` ở cuối nên tưởng bạn bỏ dấu mũ tự do cho chữ `e` đầu, biến thành `"têl"`.
   - Bạn nhận ra mình đang muốn gõ chữ `"tele"` (hoặc `"telegram"`), nên theo thói quen chuẩn của bộ gõ `vnchKey`, bạn gõ bồi thêm một chữ `e` nữa (`t-e-l-e-e`).
   - Lúc này bạn mong muốn nó hủy dấu mũ ở chữ `ê` và trả về chữ **`"tele"`** nguyên vẹn. Nếu bộ gõ trước đó chỉ lưu chữ `"têl"` mà làm mất dấu vết quá trình biến đổi, khi nhận thêm chữ `'e'` nó sẽ chẳng biết phải khôi phục lại làm sao, thậm chí còn biến chữ cuối thành `"telê"` làm người dùng nổi quạo!

### 5.2. Giải pháp của `vnchFlex`: Kiến trúc Pure-Rebuild (Tái tạo thuần khiết từ chuỗi phím thô)

Để bộ gõ đạt tới độ ổn định tuyệt đối như `vnchKey`, lớp [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt) của `vnchFlex` áp dụng kiến trúc **Pure-Rebuild (Tái tạo thuần khiết)**:

> **Nguyên lý cốt lõi của Pure-Rebuild**:
> Bộ máy lưu giữ toàn bộ chuỗi phím thô (`rawChars`) mà người dùng đã chạm từ đầu âm tiết tới giờ. Mỗi khi có một phím chữ cái mới chạm vô, bộ máy chỉ việc bỏ phím đó vô cuối danh sách `rawChars`, rồi chạy một hàm thuần khiết duy nhứt:
> $$\text{renderedOutput} = \text{transformTelex}(\text{rawChars})$$
> Hàm `transformTelex` sẽ đọc lại danh sách `rawChars` từ đầu tới cuối, áp dụng các quy tắc Telex, quy tắc hủy dấu (escape) và quy tắc đặt dấu thanh để tính ra chuỗi chữ hiển thị mới nhứt.

Tụi mình mở [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L42-L58) coi phần khai báo trạng thái và hàm nhận phím `onChar` nha:

```kotlin
private val rawChars = mutableListOf<Char>()
private var renderedOutput: String = ""

val composingText: String get() = renderedOutput
val isComposing: Boolean get() = rawChars.isNotEmpty()

fun onChar(input: Char, ic: InputConnection): Boolean {
    if (input.isLetter()) {
        val prevText = renderedOutput
        rawChars.add(input)
        renderedOutput = transformTelex(rawChars)
        replaceComposingDirect(ic, prevText, renderedOutput)
        return true
    }
    commit(ic)
    return false
}
```

Bạn nhìn hàm `onChar` kìa, nó trong sáng tới mức bất ngờ luôn:
1. Kiểm tra `if (input.isLetter())`: Nếu phím vừa bấm là một chữ cái (từ `a` tới `z`, hoặc chữ có dấu từ phím phụ như `đ`), bộ máy giữ lại kết quả cũ trong `prevText`.
2. `rawChars.add(input)`: Thêm chữ cái vừa gõ vô cuối danh sách phím thô.
3. `renderedOutput = transformTelex(rawChars)`: Gọi hàm `transformTelex` tính toán lại kết quả Tiếng Việt từ `rawChars`.
4. `replaceComposingDirect(ic, prevText, renderedOutput)`: Đưa chữ mới lên màn hình thay thế chữ cũ, rồi trả về `true` (báo là bộ máy đã xử lý xong phím này).
5. Ngược lại, nếu phím vừa bấm **không phải là chữ cái** (ví dụ: dấu cách `' '`, dấu chấm `'.'`, dấu phẩy `','`, chữ số `'1'`...): Bộ máy gọi `commit(ic)` để chốt chữ đang gõ xuống văn bản, xóa sạch `rawChars` để chuẩn bị cho từ tiếp theo, và trả về `false` để `VietKeyboardService` xuất ký tự dấu câu/khoảng trắng đó ra.

---

## CHƯƠNG 6: GIẢI PHẪU TỪNG NHÁNH BIẾN ĐỔI TELEX — TỪ `dd`, `aa/ee/oo`, `w` TỚI PHƯƠNG NGỮ NAM BỘ (`dzìa`, `dzách`, `dzô`)

Bây giờ tụi mình sẽ đi sâu vô bên trong hàm `transformTelex` và `transformTelexCore` tại [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L177-L521). Đây là nơi hội tụ toàn bộ tinh hoa xử lý Tiếng Việt của `vnchFlex`.

### 6.1. Tầng bảo vệ đầu tiên: Khôi phục từ Tiếng Anh và hỗ trợ Phương ngữ Nam Bộ

Trước khi bắt tay vô ghép dấu Telex, hàm `transformTelex(chars: List<Char>)` ([dòng 177–220](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L177-L220)) thực hiện 4 bước kiểm tra thông minh ở cửa ngõ:

```kotlin
fun transformTelex(chars: List<Char>): String {
    if (chars.isEmpty()) return ""
    val lowerRaw = chars.map { it.lowercaseChar() }.joinToString("")

    // 1. Giữ nguyên các từ trong whitelist của người dùng
    if (lowerRaw in userWhitelist) {
        return chars.joinToString("")
    }

    // 2. Tự động khôi phục từ Tiếng Anh chuẩn vnchKey (vd: text -> text, next -> next, flex -> flex, more -> more...)
    if (autoRestoreEnglish && lowerRaw in ENGLISH_AUTO_RESTORE_WORDS) {
        return chars.joinToString("")
    }
```

Tại sao phải có `ENGLISH_AUTO_RESTORE_WORDS`?
Khi bạn đang bật chế độ gõ Tiếng Việt mà nhắn tin có chèn mấy chữ Tiếng Anh thông dụng như `"text"`, `"next"`, `"flex"`, `"fix"`, `"box"`, `"more"`, `"user"`, `"test"`, `"best"`, `"world"`, `"window"`...
- Nếu bộ gõ cứ máy móc áp dụng Telex:
  - Gõ `t-e-x-t`: chữ `x` ở giữa bị hiểu là dấu ngã -> biến thành **`"tẽt"`**!
  - Gõ `f-l-e-x`: chữ `x` ở cuối biến thành **`"flẽ"`**!
  - Gõ `m-o-r-e`: chữ `r` biến thành dấu hỏi -> **`"mỏe"`**!
- Nhờ bảng `ENGLISH_AUTO_RESTORE_WORDS` ([dòng 625–647](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L625-L647)), ngay khoảnh khắc chuỗi phím thô khớp với từ Tiếng Anh thông dụng (ví dụ bạn vừa gõ xong chữ `t` cuối của `text`), bộ gõ lập tức trả về nguyên mẫu **`"text"`** mà bạn hổng cần phải bấm nút chuyển ngôn ngữ qua lại!

#### Đặc sản của Người Miền Nam: Phụ âm đầu `dz-`, `z-`, `f-`, `j-`, `w-`
Trong Tiếng Việt sách giáo khoa chuẩn, bảng chữ cái hổng có các phụ âm đầu `dz-`, `z-`, `f-`, `j-`, `w-`. Nhiều bộ gõ cứng nhắc thấy từ bắt đầu bằng `z`, `f`, `j`, `w` hay `dz` là khóa luôn chức năng bỏ dấu vì tưởng là Tiếng Anh.
Nhưng Người Miền Nam mình nhắn tin hàng ngày thì sao? Tụi mình xài phương ngữ Nam Bộ nhiều lắm chớ:
- *"Chừng nào mày **dzìa**?"* (gõ `dziaf` -> `dzìa`)
- *"Ngon bá **dzách** luôn!"* (gõ `dzachs` -> `dzách`)
- *"Trăm phần trăm **dzô** nha!"* (gõ `dzoo` -> `dzô`)
- *"Sao **dzậy** / **zậy**?"* (gõ `dzaayj` -> `dzậy`, `zaayj` -> `zậy`)
- *"Thôi tao **dzọt** lẹ!"* (gõ `dzotj` -> `dzọt`)
- *"Ngon **wá** trời!"* (gõ `was` -> `wá`)

Hãy coi cách `vnchFlex` chiều lòng Người Miền Nam mình ở [dòng 191–219](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L191-L219):

```kotlin
    val firstLower = lowerRaw.first()
    val isDzOnset = lowerRaw.startsWith("dz")
    if (!southernDialectAndGenZ && (isDzOnset || firstLower == 'f' || firstLower == 'j' || firstLower == 'z')) {
        return chars.joinToString("")
    }
    if (southernDialectAndGenZ && !isDzOnset && (firstLower == 'f' || firstLower == 'j' || firstLower == 'z')) {
        // Bảo vệ các từ Tiếng Anh thông dụng bắt đầu bằng f/j/z (free, fee, zero, zone, zip, zest, just...)
        if (lowerRaw in ENGLISH_FJZ_PROTECTED_WORDS ||
            (firstLower == 'f' && lowerRaw.startsWith("fr")) ||
            ((firstLower == 'z' || firstLower == 'j') && lowerRaw.endsWith("st"))
        ) {
            return chars.joinToString("")
        }
    }

    // Xử lý từ bắt đầu bằng 'w' trong chế độ Nam Bộ / Gen-Z (vd: was -> wá, waayj -> wậy, wans -> wán)
    if (southernDialectAndGenZ && firstLower == 'w' && chars.size >= 3) {
        val c1 = chars[1].lowercaseChar()
        if ((c1 == 'a' || c1 == 'e' || c1 == 'y') && lowerRaw !in ENGLISH_W_PROTECTED_WORDS) {
            val zChars = chars.toMutableList().apply { this[0] = if (chars[0].isUpperCase()) 'Z' else 'z' }
            val zTransformed = transformTelexCore(zChars, isDzOnset = false)
            if (zTransformed.lowercase() != zChars.joinToString("").lowercase()) {
                return chars[0] + zTransformed.drop(1)
            }
        }
    }

    return transformTelexCore(chars, isDzOnset = isDzOnset)
```

Cách xử lý chữ `'w'` đứng đầu từ (`was -> wá`, `waayj -> wậy`) ở đoạn code trên khéo lắm nha:
- Bình thường trong Telex, chữ `'w'` là phím biến đổi nguyên âm (`aw -> ă, ow -> ơ, uw -> ư`). Nếu để `'w'` ở đầu từ đi thẳng vô bộ lõi, nó có thể bị rối với phím móc.
- Cho nên khi thấy từ bắt đầu bằng `'w'` đi kèm nguyên âm `a, e, y` và không nằm trong danh sách từ Tiếng Anh được bảo vệ (`water, want, wait, walk, web, week...`), bộ máy tạm thời mượn chữ `'z'` thế vô chỗ chữ `'w'` đầu tiên (`was` -> `zas`), đưa vô `transformTelexCore` để biến thành `"zá"`, rồi trả chữ `'w'` về lại đầu từ thành **`"wá"`**!
- Còn với cụm phụ âm đầu Nam Bộ `"dz"` (`isDzOnset = true`), ngay khi bước vô `transformTelexCore` ([dòng 233–237](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L233-L237)):
  ```kotlin
  if (isDzOnset && chars.size >= 2) {
      result.add(chars[0])
      result.add(chars[1])
      i = 2
  }
  ```
  Bộ máy cất ngay hai chữ `'d'` và `'z'` vô kết quả `result`, dời con trỏ `i` tới vị trí số `2`. Nhờ vậy chữ `'z'` trong `"dz"` không bị hiểu lầm là phím xóa dấu (`z`), và chữ `'d'` ở đầu cũng không bị biến thành `"đz"` khi người dùng gõ thêm chữ `'d'` ở sau!

---

### 6.2. Sáu biến trạng thái điều khiển vòng lặp `transformTelexCore`

Bên trong hàm `transformTelexCore` ([dòng 222–230](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L222-L230)), tụi mình khởi tạo 6 biến trạng thái để theo dõi quá trình duyệt qua từng ký tự `chars[i]`:

```kotlin
val result = mutableListOf<Char>()
var tone = Tone.NONE
var toneEscapeActive = false
var hornCancelled = false
var circumflexCancelledKey: Char? = null
var strokeDCancelled = false
var i = 0
```

Tụi mình làm quen với mặt mũi từng biến này nha:
1. **`result`**: Danh sách các chữ cái kết quả đang được xây dựng (chưa gắn dấu thanh; dấu thanh sẽ được gắn ở bước cuối cùng sau khi vòng lặp kết thúc).
2. **`tone`**: Lưu dấu thanh hiện tại của âm tiết (`NONE`, `ACUTE` sắc, `GRAVE` huyền, `HOOK` hỏi, `TILDE` ngã, `DOT` nặng). Trong một âm tiết Tiếng Việt chỉ có tối đa 1 dấu thanh. Nếu bạn gõ `s` (sắc) rồi đổi ý gõ `f` (huyền), biến `tone` sẽ cập nhật từ `ACUTE` sang `GRAVE`.
3. **`toneEscapeActive`**: Cờ báo hiệu người dùng đã **nhấn đúp phím dấu thanh** để hủy dấu (ví dụ gõ `canhf` -> `"cành"`, gõ thêm `f` nữa thành `canhff` -> hủy dấu huyền, bật `toneEscapeActive = true` và xuất ra `"canhf"`).
4. **`hornCancelled`**: Cờ báo hiệu người dùng đã nhấn đúp `ww` để hủy dấu móc/trăng và lấy chữ `'w'` thường.
5. **`circumflexCancelledKey`**: Ghi nhớ nguyên âm (`'a'`, `'e'`, hoặc `'o'`) vừa bị người dùng hủy dấu mũ (do gõ `aaa`, `eee`, `ooo` hoặc gõ lặp nguyên âm ở cuối từ như `telee -> tele`). Khi cờ này đang giữ chữ `'e'`, bộ máy sẽ không tự động đội nón `ê` cho chữ `'e'` trong từ đó nữa!
6. **`strokeDCancelled`**: Cờ báo hiệu người dùng đã hủy gạch ngang chữ `'đ'` (do gõ `ddd -> dd` hoặc gõ `d` lần hai ở cuối từ) để lấy chữ `'d'` thường.

---

### 6.3. Nhánh 1, 2, 3, 4: Xử lý `dd -> đ`, Tốc gõ phụ âm đầu và Dấu mũ liền kề (`aa, ee, oo`)

Khi vòng lặp `while (i < chars.size)` chạy qua từng ký tự `ch = chars[i]`, nó kiểm tra theo thứ tự ưu tiên từ trên xuống dưới:

#### Nhánh 1: Biến đổi `dd -> đ` và hoàn nguyên `ddd -> dd` ([dòng 244–261](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L244-L261))
```kotlin
if (!isDzOnset && lower == 'd' && !result.any { isVietnameseVowel(it) }) {
    if (i + 2 < chars.size &&
        chars[i + 1].lowercaseChar() == 'd' &&
        chars[i + 2].lowercaseChar() == 'd'
    ) {
        result.add(ch)
        result.add(chars[i + 1])
        strokeDCancelled = true
        toneEscapeActive = true
        i += 3
        continue
    }
    if (i + 1 < chars.size && chars[i + 1].lowercaseChar() == 'd') {
        result.add(if (ch.isUpperCase()) 'Đ' else 'đ')
        i += 2
        continue
    }
}
```
- **Tại sao lại có điều kiện `!result.any { isVietnameseVowel(it) }`?**
  Vì trong Tiếng Việt, chữ `'đ'` chỉ có thể đứng ở vị trí **phụ âm đầu** (trước khi xuất hiện nguyên âm).
- Nếu bạn gõ 2 chữ `d` liên tiếp (`dd`), con trỏ `i` nhảy qua 2 bước (`i += 2`) và bỏ chữ `'đ'` (hoặc `'Đ'` nếu viết hoa) vô `result`.
- Nếu bạn gõ tới 3 chữ `d` liên tiếp (`ddd`, ví dụ khi viết từ Tiếng Anh viết tắt như `ddd`), nhánh `i + 2` bắt dính liền! Nó trả lại 2 chữ `"dd"`, bật cờ `strokeDCancelled = true` và nhảy `i += 3`.

#### Nhánh 2: Tốc gõ phụ âm đầu `cc -> ch`, `gg -> gi`, `qq -> qu` ([dòng 264–281](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L264-L281))
Khi người dùng bật tùy chọn `quickTelex` trong Cài đặt và đang ở đầu từ (`result.isEmpty()`), gõ hai chữ `c` liền nhau sẽ bung thành `"ch"`, `gg` thành `"gi"`, `qq` thành `"qu"`.

#### Nhánh 3 & 4: Dấu mũ liền kề (`aa -> â`, `ee -> ê`, `oo -> ô`) và hoàn nguyên 3 chữ (`aaa -> aa`) ([dòng 284–315](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L284-L315))
- Nếu gõ 3 nguyên âm giống nhau liên tiếp (`aaa`, `eee`, `ooo`): Bộ máy hiểu ngay bạn muốn hủy dấu mũ để lấy 2 chữ cái gốc (`"aa"`, `"ee"`, `"oo"`). Nó gán `circumflexCancelledKey = lower` và nhảy `i += 3`.
- Nếu gõ 2 nguyên âm giống nhau liên tiếp (`aa`, `ee`, `oo`) và trong `result` chưa có chữ nào mang dấu mũ trước đó (`!hasPriorCircumflex`): Bộ máy biến thành `'â'`, `'ê'`, `'ô'` và nhảy `i += 2`.

---

## CHƯƠNG 7: CỔNG GÁC ÂM CUỐI TIẾNG VIỆT (VALID CODA GATE) VÀ CHU KỲ LẶP PHÍM HOÀN NGUYÊN (`telee -> tele`, `Vietnamee -> Vietname`)

Đây là chương giải thích cặn kẽ nhứt về những tình huống thực tế mà bạn đã cùng tụi mình tinh chỉnh cho `vnchFlex` giống hệt với bộ gõ `vnchKey`! Bạn đọc kỹ đoạn code từ [dòng 317 đến 374](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L317-L374) trong `VietnameseEngine.kt` nha:

```kotlin
// 5. Bỏ dấu mũ & gạch ngang chữ đ tự do ở cuối từ (nhana -> nhân, vietej -> việt, trongo -> trông, dodongf -> đồng)
if (freeTonePlacement && !toneEscapeActive && result.isNotEmpty()) {
    if (!isDzOnset && lower == 'd' && result.any { isVietnameseVowel(it) }) {
        val firstLower = result.first().lowercaseChar()
        if (firstLower == 'd' && !strokeDCancelled) {
            result[0] = if (result[0].isUpperCase()) 'Đ' else 'đ'
            i += 1
            continue
        } else if (firstLower == 'đ') {
            result[0] = if (result[0].isUpperCase()) 'D' else 'd'
            strokeDCancelled = true
            result.add(ch)
            i += 1
            continue
        }
    }

    if ((lower == 'a' || lower == 'e' || lower == 'o') && circumflexCancelledKey != lower) {
        val circumflexChar = when (lower) {
            'a' -> 'â'
            'e' -> 'ê'
            else -> 'ô'
        }
        val existingCircIdx = result.indexOfLast { it.lowercaseChar() == circumflexChar }
        if (existingCircIdx >= 0) {
            val wasUpper = result[existingCircIdx].isUpperCase()
            result[existingCircIdx] = if (wasUpper) lower.uppercaseChar() else lower
            circumflexCancelledKey = lower
            result.add(ch)
            i += 1
            continue
        }
        val targetIdx = result.indexOfLast {
            val c = it.lowercaseChar()
            c == lower || (lower == 'o' && c == 'ơ') || (lower == 'a' && c == 'ă')
        }
        if (targetIdx >= 0 && (targetIdx < result.lastIndex || result[targetIdx].lowercaseChar() != lower)) {
            val between = if (targetIdx + 1 < result.size) {
                result.subList(targetIdx + 1, result.size).joinToString("") { it.lowercaseChar().toString() }
            } else ""
            val validCoda = between.isEmpty() || between in setOf("c", "ch", "m", "n", "ng", "nh", "p", "t", "i", "u", "y")
            if (validCoda) {
                val isUpper = result[targetIdx].isUpperCase()
                if (lower == 'o' && targetIdx >= 1 && result[targetIdx - 1].lowercaseChar() == 'ư') {
                    result[targetIdx - 1] = if (result[targetIdx - 1].isUpperCase()) 'U' else 'u'
                }
                result[targetIdx] = when (lower) {
                    'a' -> if (isUpper) 'Â' else 'â'
                    'e' -> if (isUpper) 'Ê' else 'ê'
                    else -> if (isUpper) 'Ô' else 'ô'
                }
                i += 1
                continue
            }
        }
    }
}
```

Đoạn code trên nhìn ngắn vậy thôi chớ chứa đựng tới **3 cơ chế tinh vi** để xử lý bỏ dấu mũ tự do ở cuối từ mà không làm nhảy chữ khi gõ Tiếng Anh. Tụi mình phân tích từng cơ chế và chạy bảng kiểm tra từng phím một nha!

### 7.1. Cơ chế 1: Bỏ dấu mũ tự do cách quãng (`nhana -> nhân`, `vietej -> việt`) và Cổng gác âm cuối (`validCoda`)

Khi người dùng bật `freeTonePlacement = true` (Bỏ dấu tự do chuẩn `vnchKey`), người ta hổng nhứt thiết phải gõ `n-h-a-a-n` để ra chữ `"nhân"`, mà có thể gõ `n-h-a-n` trước rồi mới gõ chữ `'a'` chốt ở cuối (`nhana`).
Khi gặp chữ `'a'`, `'e'`, hoặc `'o'` ở vị trí `i`:
1. Bộ máy tìm ngược trong `result` coi trước đó đã có chữ `'a'`, `'e'`, `'o'` nào chưa (`targetIdx`).
2. Nó trích ra phần chữ nằm giữa nguyên âm `targetIdx` và cuối từ hiện tại, đặt tên là biến **`between`**:
   - Với `nhana`: `result` đang là `['n', 'h', 'a', 'n']`, `targetIdx = 2` (chữ `'a'`). Phần nằm sau chữ `'a'` là `between = "n"`.
   - Với `trongo`: `result` đang là `['t', 'r', 'o', 'n', 'g']`, `targetIdx = 2` (chữ `'o'`). Phần nằm sau chữ `'o'` là `between = "ng"`.
3. **Cổng gác `validCoda`**:
   ```kotlin
   val validCoda = between.isEmpty() || between in setOf("c", "ch", "m", "n", "ng", "nh", "p", "t", "i", "u", "y")
   ```
   Cổng gác này hỏi: *"Cái phần `between` nằm chắn giữa nguyên âm trước và phím đang gõ có phải là một âm cuối hợp lệ của Tiếng Việt hông?"*
   - Với `nhana`: `between = "n"` -> Nằm trong tập hợp! Hợp lệ -> Đội nón cho chữ `'a'` thành **`"nhân"`**!
   - Với `Vietname`: Khi bạn đang gõ chữ Tiếng Anh `"Vietnam"` rồi gõ thêm chữ `'e'` (`V-i-e-t-n-a-m-e`), trong `result` có chữ `'e'` ở vị trí `2` (`Vi[e]tnam`). Phần chữ nằm sau chữ `'e'` đó là `between = "tnam"`.
   - `"tnam"` có nằm trong `setOf("c", "ch", "m", "n", "ng", "nh", "p", "t", "i", "u", "y")` hông? **KHÔNG!**
   - Nhờ cổng gác `validCoda` chặn lại, khi bạn gõ `Vietname`, chữ `'e'` ở cuối sẽ **không bao giờ nhảy ngược lên đầu** để biến chữ thành `"Viêtnam"`! Nó giữ nguyên là **`"Vietname"`**, và khi gõ thêm `'s'` (`Vietnames`), nó ra đúng chữ Tiếng Anh **`"Vietnames"`** (chớ hổng bị biến thành `"Vietnám"` hay `"Vietnamê"`).

### 7.2. Cơ chế 2: Hủy dấu mũ khi gõ lặp ở cuối từ (`existingCircIdx`)

Bây giờ, giả sử bạn gõ một từ có phần giữa lọt qua cổng hoặc đã có dấu mũ từ trước, ví dụ bạn gõ `v-i-e-t-e` -> `"viêt"`, rồi bạn gõ thêm một chữ `'e'` nữa (`vietee`) vì bạn muốn hủy dấu mũ `ê` để lấy chữ `"viete"`.
Hoặc giả sử trong `result` đã có một chữ mang dấu mũ (`â`, `ê`, `ô`), được tìm thấy qua dòng lệnh:
```kotlin
val existingCircIdx = result.indexOfLast { it.lowercaseChar() == circumflexChar }
if (existingCircIdx >= 0) {
    val wasUpper = result[existingCircIdx].isUpperCase()
    result[existingCircIdx] = if (wasUpper) lower.uppercaseChar() else lower
    circumflexCancelledKey = lower
    result.add(ch)
    i += 1
    continue
}
```
Khi thấy trong `result` đã có chữ `'ê'` (`existingCircIdx >= 0`), mà bạn lại gõ thêm một chữ `'e'` nữa:
1. Bộ máy lập tức **gỡ chiếc nón trên đầu chữ `'ê'` xuống**, trả nó về chữ `'e'` thường (`result[existingCircIdx] = 'e'`).
2. Đặt biển cấm `circumflexCancelledKey = 'e'` để từ nay tới cuối từ không đội nón cho chữ `'e'` nữa.
3. Gắn chữ `'e'` bạn vừa gõ vô cuối từ (`result.add(ch)`).

### 7.3. Bảng chạy từng bước (Trace Table) để thấy sự mượt mà

Tụi mình cùng lập bảng theo dõi từng phím gõ trong 2 ví dụ thực tế để bạn thấy rõ từng bánh răng bên trong `VietnameseEngine` quay như thế nào nha:

#### Bảng Trace 1: Gõ chữ `"vietej"` -> `"việt"` và hoàn nguyên `"vietee"` -> `"viete"`

| Bước | Phím gõ | Danh sách `rawChars` | Diễn biến trong `transformTelexCore` | Kết quả hiển thị (`renderedOutput`) |
| :---: | :---: | :--- | :--- | :---: |
| 1 | `v` | `['v']` | Chữ thường, thêm `'v'` vô `result` | **`v`** |
| 2 | `i` | `['v', 'i']` | Nguyên âm `'i'`, thêm vô `result` | **`vi`** |
| 3 | `e` | `['v', 'i', 'e']` | Nguyên âm `'e'`, thêm vô `result` | **`vie`** |
| 4 | `t` | `['v', 'i', 'e', 't']` | Phụ âm cuối `'t'`, thêm vô `result` | **`viet`** |
| 5 | `e` | `['v', 'i', 'e', 't', 'e']` | Gặp `'e'` ở cuối. Tìm thấy `'e'` tại `targetIdx = 2`. Phần giữa `between = "t"` thuộc `validCoda`! Biến `result[2]` thành `'ê'`. | **`viêt`** |
| 6a | `j` (nếu gõ Tiếng Việt) | `['v', 'i', 'e', 't', 'e', 'j']` | Gặp `'j'` -> `tone = Tone.DOT`. Cuối hàm `applyTone` đặt dấu nặng vô `'ê'`. | **`việt`** |
| 6b | `e` (nếu muốn hoàn nguyên) | `['v', 'i', 'e', 't', 'e', 'e']` | Ở `i = 4` đã tạo `'ê'`. Tới `i = 5` gặp `'e'`, thấy `existingCircIdx = 2` (chữ `'ê'`) -> Hạ mũ `'ê'` về `'e'`, bật `circumflexCancelledKey = 'e'`, thêm `'e'` vô cuối! | **`viete`** |

#### Bảng Trace 2: Gõ chữ `"telee"` và `"Vietnamee"`

| Chuỗi phím gõ (`rawChars`) | Kiểm tra `between` & `validCoda` | Diễn biến trong `transformTelexCore` | Kết quả hiển thị (`renderedOutput`) |
| :--- | :--- | :--- | :---: |
| `t-e-l-e` | `targetIdx = 1` (`'e'`), `between = "l"`. Chữ `"l"` **không** thuộc `validCoda` (`c, ch, m, n, ng, nh, p, t, i, u, y`)! | Cổng `validCoda` trả về `false`. Chữ `'e'` ở cuối được thêm bình thường như một chữ cái Tiếng Anh. | **`tele`** |
| `V-i-e-t-n-a-m-e` | `targetIdx = 2` (`'e'`), `between = "tnam"`. `"tnam"` **không** thuộc `validCoda`! | Cổng `validCoda` trả về `false`. Không nhảy dấu mũ lên chữ `'e'` đầu. | **`Vietname`** |
| `V-i-e-t-n-a-m-e-e` | Tại hai chữ `'e'` cuối (`i = 7, 8`): trong `result` đã có chữ `'e'` trước đó ở vị trí `2` (`result.none { it == 'e' }` là `false`). | Không gom hai chữ `'e'` cuối thành `'ê'`, giữ nguyên hai chữ `'e'`. | **`Vietnamee`** |
| `V-i-e-t-n-a-m-e-s` | Tại chữ `'s'` cuối: từ có nhiều cụm nguyên âm tách rời của Tiếng Anh (`ie` và `a` và `e`), kiểm tra cấu trúc âm tiết. | Giữ nguyên từ gốc không bỏ dấu bậy. | **`Vietnames`** |

---

### 7.4. Nhánh 6: Phím `'w'` biến hóa 4 ngả (`aw -> ă`, `ow -> ơ`, `uw -> ư`, `uow -> ươ`)

Phím `'w'` là phím đa năng nhứt trong kiểu gõ Telex. Ở [dòng 377–443](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L377-L443) của `VietnameseEngine.kt`, khi người dùng gõ `'w'`, bộ máy sẽ quét ngược từ cuối `result` về đầu để tìm nguyên âm phù hợp nhứt mà gắn dấu trăng (`ă`) hoặc dấu móc (`ơ`, `ư`):

1. **Nhấn đúp `ww` để thoát**: Nếu thấy hai chữ `'w'` đi liền nhau (`chars[i + 1] == 'w'`), bộ máy xuất ra đúng 1 chữ `'w'`, bật cờ `hornCancelled = true` và nhảy `i += 2`.
2. **Gặp nguyên âm `'a'` hoặc `'â'`**:
   - Trường hợp đặc biệt của Tiếng Việt: Nếu trước chữ `'a'` là chữ `'u'` mà **không phải** đi sau chữ `'q'` (ví dụ bạn gõ `m-u-a-w` cho chữ *"mưa"*, khác với `q-u-a-w` là chữ *"quă"* trong *"quăng"*), thì dấu móc được gắn vô chữ `'u'` đứng trước để thành **`"mưa"`**!
   - Các trường hợp còn lại (`t-a-m-w` hoặc `t-a-w-m`): Biến chữ `'a'` (hoặc `'â'`) thành **`'ă'`**.
3. **Gặp nguyên âm `'o'` hoặc `'ô'`**:
   - Biến chữ `'o'` (hoặc `'ô'`) thành **`'ơ'`**.
   - Đặc biệt: Nếu ngay trước chữ `'o'` là chữ `'u'` hoặc `'ư'` (và không đứng sau `'q'`, ví dụ bạn gõ `d-d-u-o-c-w-j` cho chữ *"được"*, hay `t-r-u-o-n-g-w-r` cho chữ *"trưởng"*), thì chỉ cần **một phím `'w'` duy nhứt** sẽ móc luôn cả hai chữ `'u'` và `'o'` thành cụm nguyên âm đôi **`"ươ"`**!
4. **Đồng bộ tự động `"ưo" -> "ươ"` ở cuối hàm ([dòng 509–515](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L509-L515))**:
   - Nhiều người có thói quen gõ chữ *"được"* là `d-d-u-w-o-c-j` (gõ `uw` thành `ư`, rồi gõ `o` thường, không gõ `w` lần hai ở chữ `o`).
   - Trong Tiếng Việt không có từ nào có vần `"ưoc"` hay `"ưong"` mà chữ `o` không có râu hết! Vì vậy, trước khi kết thúc hàm `transformTelexCore`, nếu có cụm `'ư'` đi liền với `'o'` và theo sau là một phụ âm cuối (khi `result.size >= 3`), bộ máy tự động móc râu luôn cho chữ `'o'` thành **`"ươ"`** (`dduwocj` -> **`"được"`**, `truwongr` -> **`"trưởng"`**). Quá sức tâm lý luôn phải hông bạn!

---

## CHƯƠNG 8: THUẬT TOÁN ĐẶT DẤU THANH CHUẨN CHÁNH TẢ CỔ ĐIỂN (`òa, òe, úy`) VÀ KIỂU MỚI (`oà, oè, uý`)

Sau khi vòng lặp `while` trong `transformTelexCore` đã dựng xong toàn bộ chữ cái và dấu mũ/móc trong danh sách `result`, đồng thời ghi nhận dấu thanh trong biến `tone` (qua các phím `s` sắc, `f` huyền, `r` hỏi, `x` ngã, `j` nặng, và phím `z` để xóa dấu thanh về `Tone.NONE`), bước cuối cùng là gọi hàm `applyTone(result, tone, modernOrthography)` ([dòng 524–580](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L524-L580)).

### 8.1. Bài toán hóc búa: Một từ có 2 hoặc 3 nguyên âm thì đặt dấu thanh lên đầu chữ nào?

Nếu âm tiết chỉ có 1 nguyên âm (như `"cá"`, `"mẹ"`, `"đi"`), chuyện đặt dấu dễ ợt: cứ đè đúng nguyên âm duy nhứt đó mà đặt.
Nhưng Tiếng Việt mình có rất nhiều nguyên âm đôi (2 nguyên âm đi liền nhau như `oa, oe, uy, ia, ua, ưa, iê, uô, ươ, ai, ao, au...`) và nguyên âm ba (3 nguyên âm đi liền nhau như `iêu, yêu, uôi, ươi, oai, oay, uây, uyê...`).

Tụi mình cùng coi hàm `applyTone` giải quyết bài toán này bằng 3 trường hợp (`when (vowelIndices.size)`) rõ ràng ra sao nha:

```kotlin
private fun applyTone(chars: MutableList<Char>, tone: Tone, modern: Boolean) {
    if (tone == Tone.NONE) return
    val vowelIndices = chars.indices.filter { isVietnameseVowel(chars[it]) }
    if (vowelIndices.isEmpty()) return

    val validCodaChars = setOf('c', 'm', 'n', 'p', 't', 'g', 'h', 'k')
    val targetIndex: Int = when (vowelIndices.size) {
        1 -> vowelIndices[0]
        2 -> {
            val firstV = chars[vowelIndices[0]].lowercaseChar()
            val secondV = chars[vowelIndices[1]].lowercaseChar()
            val isFallingOffglide = secondV == 'u' ||
                (secondV == 'o' && firstV != 'u') ||
                (secondV == 'i' && firstV != 'u') ||
                (secondV == 'y' && firstV != 'u')
            val trailingIsCoda = vowelIndices[1] < chars.lastIndex &&
                chars[vowelIndices[1] + 1].lowercaseChar() in validCodaChars
            val hasCoda = trailingIsCoda && !isFallingOffglide

            if (vowelIndices[0] > 0 && chars[vowelIndices[0] - 1].lowercaseChar() == 'q' && firstV == 'u') {
                vowelIndices[1]
            } else if (vowelIndices[0] > 0 && chars[vowelIndices[0] - 1].lowercaseChar() == 'g' && firstV == 'i') {
                vowelIndices[1]
            } else if (hasCoda) {
                vowelIndices[1]
            } else {
                val pair = "$firstV$secondV"
                when {
                    pair == "oa" || pair == "oe" || pair == "uy" || pair == "uê" ->
                        if (modern) vowelIndices[1] else vowelIndices[0]
                    pair == "iê" || pair == "yê" || pair == "uô" || pair == "ươ" || pair == "ưo" || pair == "uơ" ->
                        vowelIndices[1]
                    else -> vowelIndices[0]
                }
            }
        }
        else -> {
            val lastVIdx = vowelIndices.last()
            val hasCoda = lastVIdx < chars.lastIndex && chars[lastVIdx + 1].lowercaseChar() in validCodaChars
            if (hasCoda) {
                lastVIdx
            } else {
                val lastLower = chars[lastVIdx].lowercaseChar()
                if (lastLower == 'i' || lastLower == 'y' || lastLower == 'u') {
                    vowelIndices[vowelIndices.size - 2]
                } else {
                    val modIdx = vowelIndices.firstOrNull { chars[it].lowercaseChar() in "âêôơưă" }
                    modIdx ?: vowelIndices[vowelIndices.size - 2]
                }
            }
        }
    }
    chars[targetIndex] = addToneToVowel(chars[targetIndex], tone)
}
```

### 8.2. Cắt nghĩa từng quy tắc đặt dấu trong `applyTone`

1. **Khi có 2 nguyên âm (`vowelIndices.size == 2`)**:
   - **Quy tắc `qu-` và `gi-`**: Trong các chữ như `"quá"`, `"quà"`, `"giá"`, `"giò"`, chữ `'u'` đi sau `'q'` và chữ `'i'` đi sau `'g'` thực chất thuộc về phần phụ âm đầu (`qu`, `gi`), không phải nguyên âm chánh! Nên dấu thanh bắt buộc phải đặt ở nguyên âm thứ hai (`vowelIndices[1]`).
   - **Quy tắc âm tiết đóng (`hasCoda == true`)**: Khi đằng sau 2 nguyên âm có phụ âm cuối đóng lại (như `"hoàn"`, `"toán"`, `"biển"`, `"trường"`, `"quốc"`), trọng tâm âm tiết dồn về nguyên âm thứ hai, nên dấu thanh luôn đặt ở **`vowelIndices[1]`**.
   - **Quy tắc âm tiết mở (Không có phụ âm cuối) — Sự khác biệt giữa Chánh tả Cổ điển và Kiểu mới**:
     - Với các vần có nguyên âm đôi mang dấu mũ/móc (`"iê"`, `"yê"`, `"uô"`, `"ươ"`, ví dụ *"tiền"*, *"muốn"*, *"mượn"* hoặc khi đang gõ dở `"tiê"` + dấu): dấu luôn nằm ở nguyên âm thứ hai (`ê`, `ô`, `ơ`).
     - Với các vần `"ia"`, `"ua"`, `"ưa"` (như *"mía"*, *"dzìa"*, *"mùa"*, *"mưa"*): cả nước đều thống nhứt đặt dấu ở nguyên âm thứ nhứt (`vowelIndices[0]`).
     - Riêng với 3 vần **`"oa"`**, **`"oe"`**, **`"uy"`** (khi đứng cuối từ, không có phụ âm cuối, ví dụ: *hòa/hoà, khỏe/khoẻ, thủy/thuỷ, quý/quý*):
       - **Cách bỏ dấu cổ điển (`modernOrthography = false` — Mặc định trên `vnchFlex`)**: Đặt dấu thanh ở nguyên âm đầu (`vowelIndices[0]`) -> **`"hòa"`**, **`"khỏe"`**, **`"thủy"`**, **`"túy"`**. Đây là cách bỏ dấu truyền thống quen thuộc trên sách báo Miền Nam xưa nay và rất thuận mắt.
       - **Cách bỏ dấu kiểu mới (`modernOrthography = true`)**: Đặt dấu thanh ở nguyên âm thứ hai (`vowelIndices[1]`) -> **`"hoà"`**, **`"khoẻ"`**, **`"thuỷ"`**, **`"tuý"`**.

2. **Khi có 3 nguyên âm (`vowelIndices.size >= 3`)**:
   - Nếu có phụ âm cuối đóng lại (ví dụ chữ **`"chuyện"`** hay **`"khuyên"`**, trong đó 3 nguyên âm là `u - y - ê` và theo sau là phụ âm cuối `'n'`): `hasCoda` bằng `true`, dấu thanh đặt ngay trên nguyên âm cuối cùng trước phụ âm cuối (`lastVIdx`, tức chữ `'ê'` -> `"chuyện"`).
   - Nếu không có phụ âm cuối (ví dụ chữ **`"người"`**, **`"chuối"`**, **`"khoai"`**, **`"khuỷu"`**, **`"ngoằn"`**): Dấu thanh luôn nằm ở **nguyên âm đứng giữa** (`vowelIndices[vowelIndices.size - 2]`, hoặc nguyên âm có đội mũ/móc `â, ê, ô, ơ, ư, ă`).

---

## CHƯƠNG 9: PHỤC HỒI TỪ ĐÃ CHỐT (`rehydrateWord`) VÀ NGHỆ THUẬT GIAO TIẾP KHÔNG GIẬT LAG VỚI TELEGRAM

### 9.1. Chuyện gì xảy ra khi bạn gõ xong một chữ, bấm Space, rồi bấm Backspace quay lại sửa?

Bạn thử tưởng tượng: Bạn gõ chữ `"việt "` (đã bấm dấu cách Space). Lúc bạn bấm dấu cách, `VietnameseEngine.commit(ic)` đã được gọi, cái rổ `rawChars` đã bị đổ sạch trơn (`rawChars.clear()`).
Chợt bạn nhận ra mình muốn gõ chữ `"việc"` chớ hổng phải `"việt"`. Bạn liền bấm phím xóa lùi (`Backspace`):
- Nhát Backspace đầu tiên xóa mất dấu cách `' '`. Lúc này con trỏ đang đứng sát đít chữ `"việt"`.
- Nếu là một bộ gõ dở, khi bạn bấm Backspace nhát thứ hai để xóa chữ `'t'`, rồi gõ chữ `'c'`, nó sẽ ra chữ **`"việc"`** hay ra chữ **`"việ"` + `"c"` = `"việc"`**? Còn nếu bạn muốn đổi dấu nặng thành dấu sắc bằng cách gõ phím `'s'` thì sao? Nếu `rawChars` đang trống rỗng, bạn gõ `'s'` nó sẽ thành **`"việts"`**!

Để người dùng có thể sửa dấu hoặc xóa lùi mượt mà trên cả những từ **đã chốt trước đó**, `vnchFlex` trang bị hàm **`rehydrateWord`** (Phục hồi sinh khí cho từ đã chốt) kết hợp với hàm dịch ngược **`rawKeysFor(text)`** tại [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L109-L123):

```kotlin
fun rehydrateWord(word: String, ic: InputConnection? = null) {
    if (word.isEmpty()) return
    rawChars.clear()
    rawChars.addAll(rawKeysFor(word).toList())
    renderedOutput = word
    if (ic != null && useComposingSpan) {
        ic.beginBatchEdit()
        try {
            ic.deleteSurroundingText(word.length, 0)
            ic.setComposingText(word, 1)
        } finally {
            ic.endBatchEdit()
        }
    }
}
```

Và đây là hàm dịch ngược `rawKeysFor(text: String)` ([dòng 582–622](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L582-L622)):
- Khi nhận vô chữ `"việt"`, nó duyệt qua từng chữ cái:
  - `'v'` -> `"v"`
  - `'i'` -> `"i"`
  - `'ệ'` -> Tra bảng `REVERSE_BASE_VOWEL_MAP['ệ']` ra chữ gốc là `'ê'` (Telex là `"ee"`), tra bảng `TONE_INDEX_MAP['ệ']` ra dấu số `5` (dấu nặng, phím `'j'`).
  - `'t'` -> `"t"`
  - Cuối cùng gắn phím dấu thanh `'j'` vô đít -> Tạo lại chuỗi phím thô **`"vieetj"`** bỏ vô `rawChars`!
- Đồng thời, trên ô nhập chữ của ứng dụng, nó gói hai lệnh `deleteSurroundingText` và `setComposingText` vô trong cặp `beginBatchEdit() / endBatchEdit()` để biến chữ `"việt"` tĩnh thành chữ đang gõ (`Composing Span` có gạch chân). Nhờ vậy, khi bạn gõ tiếp phím `'s'`, chữ `"việt"` đổi ngay thành **`"viết"`** ngon lành!

### 9.2. Tại sao gõ chữ trong Telegram hay bị giật, và tuyệt chiêu `replaceComposingDirect`

Nếu bạn từng xài nhiều bộ gõ Tiếng Việt trên Android để nhắn tin trong ứng dụng **Telegram**, chắc bạn từng thấy hiện tượng chữ cứ nhấp nháy hoặc con trỏ nhảy cà giật mỗi khi gõ nhanh.
Nguyên nhân là do ô nhập chữ của Telegram gắn rất nhiều bộ lắng nghe sự kiện lên cái gạch chân `Composing Span` (`ic.setComposingText`). Mỗi lần bộ gõ cập nhật `setComposingText`, Telegram lại chạy cả đống hàm kiểm tra link, kiểm tra `@username` làm khung chat bị khựng.

Hãy coi cách `vnchFlex` trị dứt điểm căn bệnh này của Telegram:
1. Trong [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L276-L285), mỗi khi người dùng bấm vô một ô nhập chữ mới (`onStartInput`), nhạc trưởng kiểm tra tên gói ứng dụng (`packageName`):
   ```kotlin
   vnEngine.useComposingSpan = !isTelegramPackage(attribute?.packageName ?: currentInputEditorInfo?.packageName)
   ```
   Nếu phát hiện đang ở trong Telegram (`org.telegram.messenger`...), nó lập tức tắt `useComposingSpan = false`!
2. Khi `useComposingSpan = false`, hàm `replaceComposingDirect` trong [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L153-L174) sẽ chuyển sang **Thuật toán So khớp Tiền tố Chung (Common Prefix Diffing)**:

```kotlin
private fun replaceComposingDirect(ic: InputConnection, prevText: String, newText: String) {
    ic.beginBatchEdit()
    try {
        if (newText.isEmpty()) {
            if (prevText.isNotEmpty()) {
                if (useComposingSpan) ic.commitText("", 1) else ic.deleteSurroundingText(prevText.length, 0)
            }
        } else if (useComposingSpan) {
            ic.setComposingText(newText, 1)
        } else {
            var common = 0
            val maxCommon = minOf(prevText.length, newText.length)
            while (common < maxCommon && prevText[common] == newText[common]) common++
            val delCount = prevText.length - common
            val appendStr = newText.substring(common)
            if (delCount > 0) ic.deleteSurroundingText(delCount, 0)
            if (appendStr.isNotEmpty()) ic.commitText(appendStr, 1)
        }
    } finally {
        ic.endBatchEdit()
    }
}
```

Bạn coi cách thuật toán Common Prefix Diffing hoạt động nè:
- Giả sử trên màn hình Telegram đang có chữ `prevText = "trươn"`, bạn gõ thêm chữ `'g'` và dấu hỏi `'r'` để thành `newText = "trưởng"`.
- Thay vì xóa hết cả 5 chữ `"trươn"` rồi chèn lại 6 chữ `"trưởng"`, vòng lặp `while` đo xem hai chuỗi giống nhau được mấy chữ đầu:
  - `prevText[0] == newText[0]` (`'t'`) -> giống!
  - `prevText[1] == newText[1]` (`'r'`) -> giống!
  - `prevText[2] == newText[2]` (`'ư'`) -> giống!
  - `prevText[3]` (`'ơ'`) khác `newText[3]` (`'ở'`) -> dừng lại! Vậy `common = 3` (`"trư"`).
- Số ký tự cần xóa ở đuôi chỉ là `delCount = 5 - 3 = 2` (`"ơn"`).
- Phần chữ mới cần chèn thêm chỉ là `appendStr = "ởng"`.
- Nhờ chỉ xóa và chèn phần đuôi thực sự thay đổi bên trong một gói `beginBatchEdit()`, chữ trong Telegram hiện ra êm ru, hổng hề nhấp nháy một chút xíu nào!

---

## CHƯƠNG 10: BỘ MÁY GÕ TẮT THÔNG MINH (`MacroEngine`, `Macro`, `MacroStore`) VÀ HOÀN TÁC HAI LẦN PHÍM XÓA (`Double-Backspace Undo`)

Gõ Tiếng Việt nhanh mà thiếu bảng **Gõ tắt (Macro)** thì uổng lắm! Với `vnchFlex`, bạn gõ `vn` rồi bấm dấu cách là ra `"Việt Nam "`, gõ `dc` ra `"được "`, gõ `ko` ra `"không "`, gõ `tphcm` ra `"Thành phố Hồ Chí Minh "`.

Nhưng làm sao để bộ máy Gõ tắt thiệt sự thông minh, biết tự viết hoa theo ý người dùng và **không phá đám** khi người dùng đang gõ địa chỉ trang web như `google.com.vn`? Tụi mình cùng mở [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt) ra coi nha!

### 10.1. Chặn mở rộng gõ tắt trong tên miền, email và đường dẫn kỹ thuật (`isTechnicalDelimiter`)

Bạn thử nghĩ coi: Bạn đã cài từ viết tắt `vn` -> `"Việt Nam"`. Bữa đó bạn mở trình duyệt Chrome lên gõ địa chỉ trang báo: `tuoitre.vn` rồi bấm dấu cách hoặc bấm Enter.
Nếu bộ máy Gõ tắt khờ khạo, nó thấy chữ `vn` đứng cuối liền đổi thành `tuoitre.Việt Nam`! Người dùng thấy vậy là muốn tắt luôn chức năng gõ tắt cho rồi.

Trong [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L26-L43), tụi mình có hàng rào bảo vệ hai lớp cực kỳ chắc chắn:

```kotlin
fun tryExpand(
    ic: InputConnection,
    trigger: Char,
    beforeCursor: String? = null,
    composingLength: Int = 0
): Boolean {
    if (isTechnicalDelimiter(trigger) || trigger in "([{<") return false

    val before = beforeCursor ?: ic.getTextBeforeCursor(MAX_LOOKBACK, 0)?.toString() ?: return false
    val token = before.takeLastWhile { !it.isWhitespace() && !isTechnicalDelimiter(it) }
    if (token.isEmpty()) return false

    val charBeforeTokenIdx = before.length - token.length - 1
    if (charBeforeTokenIdx >= 0 && isTechnicalDelimiter(before[charBeforeTokenIdx])) {
        return false
    }
```
Với hàm kiểm tra ký tự kỹ thuật ở [dòng 90](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L90):
```kotlin
private fun isTechnicalDelimiter(c: Char): Boolean = c in "/@_-#\\=$~`"
```
Và trong `VietKeyboardService.kt`, khi người dùng gõ dấu chấm `.` trước chữ `vn` (như `.vn`), `currentTokenBuffer` giữ nguyên cả cụm hoặc dấu phân cách kỹ thuật (`/@_-#\=$~``) đứng ngay trước `token` (`before[charBeforeTokenIdx]`) sẽ khiến `tryExpand` trả về `false` ngay lập tức. Nhờ vậy, `user@hn`, `file_dc.txt`, hay `/vn/` đều được giữ nguyên an toàn tuyệt đối!

### 10.2. Tự động biến đổi chữ HOA / chữ thường theo ngữ cảnh (`adaptCase`)

Trong [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L72-L81), bạn chỉ cần khai báo 1 dòng macro duy nhứt là `dc` -> `"được"`, nhưng bộ máy sẽ tự động phục vụ bạn cả 3 kiểu viết hoa:

```kotlin
private fun adaptCase(inputToken: String, expansion: String): String {
    if (expansion.isEmpty()) return expansion
    if (inputToken.length >= 2 && inputToken == inputToken.uppercase() && inputToken != inputToken.lowercase()) {
        return expansion.uppercase()
    }
    if (inputToken.first().isUpperCase()) {
        return expansion.replaceFirstChar { it.uppercaseChar() }
    }
    return expansion
}
```
- Nếu bạn gõ chữ thường `dc` -> trả về `"được"`.
- Nếu bạn gõ đầu câu viết hoa chữ đầu `Dc` -> điều kiện `inputToken.first().isUpperCase()` đúng -> trả về **`"Được"`**.
- Nếu bạn bật Caps Lock gõ in hoa toàn bộ `DC` -> điều kiện `inputToken == inputToken.uppercase()` đúng -> trả về **`"ĐƯỢC"`**!

Ngoài ra, hàm `resolveVariables` ([dòng 83–88](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L83-L88)) còn cho phép bạn đặt biến động `{date}` và `{time}` trong nội dung gõ tắt để tự động điền ngày tháng năm và giờ giấc hiện tại vô văn bản nữa đó.

### 10.3. Hoàn tác Gõ tắt bằng hai lần nhấn phím xóa (`Double-Backspace Undo`)

Lỡ có lúc bạn muốn gõ chữ `"ko "` thiệt (ví dụ viết tên riêng Tiếng Nhật hay mật mã) mà quên mất mình có cài gõ tắt `ko` -> `"không"`, khi bạn vừa bấm dấu cách thì nó bung ra chữ `"không "`. Làm sao lấy lại chữ `"ko "` đây?
Trong `MacroEngine`, mỗi lần bung gõ tắt thành công, nó đều ghi sổ lại cặp `(chữ_gốc, chữ_đã_bung)` vô biến `lastExpansion`:
```kotlin
lastExpansion = (token + trigger) to expandedWithTrigger // Ví dụ: "ko " to "không "
```
Và bên trong hàm `onBackspace()` của [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L993-L1014):
- Khi bạn bấm `Backspace` lần thứ nhứt: Nó xóa mất dấu cách ở cuối chữ `"không "` (còn lại `"không"`).
- Nếu trong vòng **600 mili-giây** (`now - lastBackspaceClickTime < 600L`) bạn bấm bồi thêm phím `Backspace` lần thứ hai: Bộ gõ hiểu ngay ý bạn! Nó thu hồi chữ `"không"` và trả lại đúng chữ **`"ko "`** nguyên vẹn cho bạn gõ tiếp!

---
*(Hết Phần I và Phần II — Tiếp theo là Phần III và Phần IV về Đồ họa 120FPS và Nhạc trưởng `VietKeyboardService`)*

# PHẦN III: ĐỒ HỌA 120FPS — VẼ CẢ BÀN PHÍM TRÊN MỘT TẤM TOAN DUY NHỨT (`KeyboardCanvasView`)

---

## CHƯƠNG 11: TẠI SAO DÙNG XML VIEW TRUYỀN THỐNG LẠI LÀM BÀN PHÍM BỊ KHỰNG, VÀ TUYỆT CHIÊU SINGLE-CANVAS CỦA VNCHFLEX

### 11.1. Nỗi khổ của các bàn phím viết bằng 44 nút `<Button>` trong XML

Khi một bạn sinh viên mới học lập trình Android bắt đầu làm giao diện ứng dụng, sách vở thường dạy cách tạo một tập tin giao diện XML, bỏ vô đó mấy cái khung `LinearLayout` lồng nhau, rồi đặt từng cái nút `<Button>` hoặc `<TextView>` cho từng phím chữ cái `Q, W, E, R, T, Y...`.

Với một màn hình đăng nhập chỉ có 2 ô nhập chữ và 1 nút bấm, cách làm đó hoàn toàn ổn. Nhưng với một **bàn phím ảo** có tới **44 phím bấm**, kèm theo bong bóng phóng to chữ khi chạm, dải chọn dấu phụ khi nhấn giữ, và vệt mực xanh chạy theo ngón tay khi lướt phím (Glide Trail), cách dùng 44 cái `Button` riêng lẻ sẽ gây ra 3 thảm họa hiệu năng:

1. **Đo đạc chồng chéo (`Measure & Layout Overhead`)**: Mỗi khi bàn phím bật lên hoặc đổi từ trang chữ (`ABC`) sang trang ký hiệu (`?123`), hệ điều hành Android phải đi hỏi từng khung `LinearLayout` cha, rồi hỏi tiếp 44 cái `View` con bên trong để đo chiều ngang, chiều dọc. Quá trình này tốn hàng chục mili-giây, làm bàn phím trồi lên chậm chạp.
2. **Không thể lướt ngón tay xuyên qua các phím một cách mượt mà (`Touch Interception Hell`)**: Trong Android, khi ngón tay bạn chạm xuống nút `<Button>` chữ `V`, cái nút `V` đó sẽ "giữ khư khư" sự kiện cảm ứng của ngón tay bạn. Khi bạn kéo ngón tay lướt qua chữ `I`, chữ `E`, chữ `T` để lướt phím (Glide Typing), việc truyền sự kiện chạm từ cái `Button` này qua cái `Button` khác cực kỳ rắc rối và trễ nhịp.
3. **Rác bộ nhớ trong lúc vẽ (`Garbage Collection Stutter`)**: Trên các màn hình điện thoại đời mới có tần số quét **120Hz** (120 khung hình mỗi giây), máy tính chỉ có đúng:
   $$\frac{1000\text{ ms}}{120} \approx 8.33\text{ mili-giây}$$
   để vẽ xong một khung hình! Nếu trong hàm vẽ mà chương trình tạo ra hàng chục đối tượng mới vào bộ nhớ RAM, bộ dọn rác của Android (*Garbage Collector*) sẽ nhảy vô tạm dừng chương trình vài mili-giây để quét rác, khiến vệt lướt phím bị khựng lại thấy rõ.

### 11.2. Kiến trúc Single-Canvas (Một tấm toan duy nhứt) chuẩn Gboard của `vnchFlex`

Để đạt tốc độ phản hồi tức thì ở 120FPS, [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt) của `vnchFlex` học theo kiến trúc hiện đại của Gboard: **Toàn bộ mặt bàn phím chỉ là một `View` phẳng duy nhứt!**

Bạn hãy hình dung `KeyboardCanvasView` giống như một **tấm bảng gỗ phẳng lì** (gọi là tấm toan `Canvas`):
- Trên tấm bảng đó **không hề có một cái nút `<Button>` con nào hết**.
- Tụi mình tự dùng toán học chia tấm bảng thành 44 ô chữ nhựt (`KeySpec`), rồi tự cầm cọ (`Paint`) vẽ nền phím, đổ bóng, viết chữ cái chánh, viết ký hiệu phụ ở góc, vẽ bong bóng phóng to chữ và vẽ vệt mực lướt phím lên cùng một tấm bảng đó trong hàm `onDraw(canvas: Canvas)`.
- Đặc biệt hơn nữa, hãy nhìn vô [dòng 146–170](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L146-L170) của `KeyboardCanvasView.kt`:

```kotlin
// Pre-allocated Paints (Zero allocation during onDraw)
private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
private val keyShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
private val primaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    textAlign = Paint.Align.CENTER
    typeface = Typeface.create("sans-serif", Typeface.NORMAL)
}
private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    typeface = Typeface.create("sans-serif", Typeface.NORMAL)
}
private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#CC1A73E8")
    style = Paint.Style.STROKE
    strokeWidth = 6f * density
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
}
private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
private val popupStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 1f * density
    color = Color.parseColor("#331A73E8")
}
private val tempRect = RectF()
```

Đây gọi là kỹ thuật **Cấp phát trước — Không tạo rác khi vẽ (`Zero-Allocation onDraw`)**:
Tất cả 7 cây cọ vẽ (`Paint`) và khung chữ nhựt tạm (`tempRect`) đều được mua sắm một lần duy nhứt lúc mới mở bàn phím. Tới khi vẽ 120 khung hình mỗi giây trong hàm `onDraw`, bộ gõ chỉ việc chấm cọ vô màu rồi quẹt lên tấm toan, không tạo ra dù chỉ là 1 byte rác nào trong bộ nhớ RAM!

---

## CHƯƠNG 12: TOÁN HỌC CHIA Ô BÀN PHÍM, TỶ LỆ CHIỀU CAO LINH HOẠT (`keyHeightScale`) VÀ HAI KHUNG CHỮ NHỰT `rect` / `hitRect`

### 12.1. Công thức tính chiều cao bàn phím theo sở thích người dùng (`keyHeightScale`)

Mỗi người có một cỡ bàn tay và một cỡ màn hình điện thoại khác nhau: người tay nhỏ thích bàn phím thấp để thấy được nhiều nội dung tin nhắn phía trên, người tay lớn thích bàn phím cao ráo để bấm khỏi lộn nút.
Trong [`KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt#L220-L229), `vnchFlex` cung cấp 7 nấc chiều cao, tương ứng với hệ số nhân `keyHeightScale()` từ `0.82f` (Rất thấp) cho tới `1.20f` (Rất cao):

```kotlin
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
```

Khi người dùng đổi chiều cao bàn phím trong phần Cài đặt rồi quay lại gõ chữ, làm sao để tấm toan `KeyboardCanvasView` co giãn chiều cao ngay lập tức mà hổng bị kẹt ở chiều cao mặc định ban đầu?
Tụi mình coi hàm `syncStateAndInvalidate()` và `onMeasure()` tại [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L185-L215):

```kotlin
fun syncStateAndInvalidate() {
    if (cachedLangIconRes != langSwitchIconRes) {
        cachedLangIconRes = langSwitchIconRes
        drawableLangIcon = ContextCompat.getDrawable(context, langSwitchIconRes)?.mutate()
    }
    val letterRowH = (46f * density * keyHeightScale).toInt()
    val numRowH = (40f * density * keyHeightScale).toInt()
    val hasNumRow = (page == Page.LETTERS && showNumberRow)
    val targetH = (if (hasNumRow) numRowH else 0) + (letterRowH * 4) + (8f * density).toInt()
    layoutParams?.let { lp ->
        if (lp.height != targetH) {
            lp.height = targetH
            layoutParams = lp
        }
    }
    rebuildKeys()
    if (width > 0) {
        layoutKeyRects(width, targetH)
    }
    requestLayout()
    invalidate()
}
```

Bạn để ý cái mẹo cực hay ở đây nha:
- Chiều cao chuẩn của một hàng phím chữ là `46dp`, nhân với mật độ điểm ảnh màn hình `density` và nhân với hệ số `keyHeightScale`.
- Chiều cao chuẩn của hàng phím số trên cùng là `40dp * density * keyHeightScale`.
- Tổng chiều cao tấm toan là:
  $$\text{targetH} = (\text{numRowH nếu có hàng số}) + 4 \times \text{letterRowH} + 8\text{dp đệm trên dưới}$$
- Nhiều người mới lập trình Android chỉ sửa chiều cao trong hàm `onMeasure()`, nhưng quên mất rằng khung chứa bên ngoài (`layoutParams.height`) nếu đang giữ giá trị cũ thì hệ thống sẽ không chịu cấp lại không gian mới. Vì vậy, `syncStateAndInvalidate()` cập nhật trực tiếp `lp.height = targetH`, gọi `layoutKeyRects(width, targetH)` để chia lại kích thước 44 phím theo chiều cao mới, rồi mới hô `requestLayout()` và `invalidate()`. Nhờ đó bạn chỉnh bất kỳ nấc nào trong 7 nấc chiều cao, bàn phím đều co giãn chuẩn xác tới từng pixel!

### 12.2. Bí mật của các bàn phím cao cấp: Tại sao mỗi phím lại có tới HAI hình chữ nhựt (`rect` và `hitRect`)?

Khi bạn nhìn lên mặt bàn phím `vnchFlex`, bạn thấy giữa các phím bấm có một khe hở nhỏ màu xám (theo chiều ngang `gapX = 2.5dp`, theo chiều dọc `gapY = 3.5dp`) để các phím tách rời nhau ra, nhìn thoáng đãng và đẹp mắt.
Nhưng bạn có bao giờ tự hỏi: *"Ủa, nếu ngón tay mình lỡ chạm trúng ngay cái khe hở nằm giữa phím `G` và phím `H` thì chuyện gì xảy ra? Hổng lẽ bấm hụt hả?"*

Nếu bộ gõ dùng chung một hình chữ nhựt để vừa vẽ nút bấm vừa bắt cảm ứng, thì khi bạn bấm trúng khe hở, nó sẽ bị hụt thiệt!
Để không bao giờ có chuyện bấm hụt vô khe hở, hàm `layoutKeyRects` ([dòng 309–335](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L309-L335)) cấp cho mỗi phím `KeySpec` tới **hai khung hình chữ nhựt**:

```kotlin
var curX = sideInset
for (key in row) {
    val cellW = availW * (key.weight / totalWeight)
    key.hitRect.set(curX, curY, curX + cellW, curY + rowH)
    key.rect.set(curX + gapX, curY + gapY, curX + cellW - gapX, curY + rowH - gapY)
    allKeys.add(key)
    curX += cellW
}
```

Tụi mình vẽ hình ra là thấy rõ như ban ngày liền:

```
+--------------------------- hitRect (Khung bắt cảm ứng: khít 100%, không có khe hở)
|   +------------------- rect (Khung vẽ hình nút bấm: thụt vô gapX, gapY)
|   |               |   |
|   |       G       |   |       H       |
|   |               |   |
|   +---------------+   |
+-----------------------+---------------+
```

1. **`hitRect` (Khung bắt cảm ứng vô hình)**: Trải dài từ `curX` tới `curX + cellW` và từ `curY` tới `curY + rowH`. Các khung `hitRect` của các phím nằm sát rạt vô nhau, lấp kín 100% diện tích mặt bàn phím mà không chừa một khe hở nào hết! Bất kể ngón tay bạn chạm xuống đâu trên mặt bàn phím, hàm `findKeyAt(x, y)` ([dòng 510–516](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L510-L516)) kiểm tra `k.hitRect.contains(x, y)` đều bắt dính ngay phím gần nhứt.
2. **`rect` (Khung vẽ nút bấm hữu hình)**: Thụt vô trong so với `hitRect` một khoảng lề `gapX = 2.5dp` mỗi bên trái/phải và `gapY = 3.5dp` mỗi bên trên/dưới. Hàm `onDraw` chỉ tô màu nền phím và vẽ bóng đổ bên trong khung `rect` này thôi.

Nhờ tách bạch `hitRect` (cho ngón tay) và `rect` (cho con mắt), bàn phím vừa đẹp thanh thoát, vừa bấm bách phát bách trúng!

### 12.3. Chia độ rộng phím theo Trọng số (`weight`) và Thụt lề hàng thứ hai (`ASDFGHJKL`)

Bạn để ý trên bàn phím QWERTY:
- Hàng chữ đầu tiên (`Q W E R T Y U I O P`) có **10 phím**.
- Hàng chữ thứ hai (`A S D F G H J K L`) chỉ có **9 phím**. Để 9 phím này nằm so le đẹp mắt ở chánh giữa bàn phím (chữ `A` nằm thụt vô một chút so với chữ `Q`), dòng 320–321 tính khoảng thụt lề hai bên `sideInset = 16dp`:
  ```kotlin
  val isIndentedRow2 = (page == Page.LETTERS && ((showNumberRow && rIdx == 2) || (!showNumberRow && rIdx == 1)))
  val sideInset = if (isIndentedRow2) 16f * density else 0f
  ```
- Còn ở hàng thứ ba và thứ tư, có những phím to nhỏ khác nhau: phím chữ cái có trọng số `weight = 1.0f`, phím `Shift`, `Backspace`, `?123`, `Enter` rộng gấp rưỡi nên có `weight = 1.5f`, còn phím dấu cách (`Space`) dài nhứt nên có `weight = 5.2f`.
- Công thức chia chiều rộng cho từng phím trong một hàng đơn giản mà chuẩn xác tuyệt đối:
  $$\text{cellW} = \text{availW} \times \frac{\text{key.weight}}{\sum \text{weight của cả hàng}}$$
  Nhờ công thức tỷ lệ này, dù bạn bật hay tắt phím chuyển ngôn ngữ (`🌐`), dù bạn xoay ngang màn hình hay bật chế độ gõ một tay (`oneHandedMode`), tổng chiều rộng các phím lúc nào cũng vừa khít 100% chiều ngang được cấp!

---

## CHƯƠNG 13: MÁY TRẠNG THÁI ĐA ĐIỂM CHẠM (`SparseArray<PointerTracker>`) — GÕ HAI NGÓN CÁI CÙNG LÚC KHÔNG BAO GIỜ NUỐT CHỮ

### 13.1. Hiện tượng "Gõ lăn hai ngón cái" (`Two-Thumb Rollover`) là gì?

Khi bạn nhắn tin nhanh bằng hai ngón tay cái (tay trái và tay phải), bạn hổng bao giờ đợi ngón cái bên trái nhấc hẳn lên khỏi màn hình rồi ngón cái bên phải mới chạm xuống đâu!
Thực tế diễn ra trong vài chục mili-giây như vầy nè:
1. Tại thời điểm `t = 0 ms`: Ngón cái tay trái chạm xuống phím **`T`** (`ACTION_DOWN`, ngón số 0).
2. Tại thời điểm `t = 25 ms`: Ngón cái tay trái **vẫn còn đang đè trên phím `T`**, thì ngón cái tay phải đã chạm xuống phím **`H`** (`ACTION_POINTER_DOWN`, ngón số 1)!
3. Tại thời điểm `t = 45 ms`: Ngón cái tay trái mới nhấc lên khỏi phím **`T`** (`ACTION_POINTER_UP`, ngón số 0).
4. Tại thời điểm `t = 70 ms`: Ngón cái tay phải nhấc lên khỏi phím **`H`** (`ACTION_UP`, ngón số 1).

Nếu một bộ gõ chỉ dùng một biến đơn lẻ `var pressedKey: KeySpec?` để nhớ phím đang bấm, thì ở thời điểm `t = 25 ms` khi ngón phải chạm vô `H`, nó sẽ ghi đè quên mất ngón trái đang ở `T`, hoặc tưởng bạn đang kéo một ngón tay từ `T` sang `H` (biến thành lướt phím Glide Typing), thế là nuốt mất chữ `T` và chữ `H`!

### 13.2. Cách `vnchFlex` theo dõi từng ngón tay độc lập bằng `SparseArray<PointerTracker>`

Trong [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L47-L60), mỗi ngón tay chạm vô màn hình được hệ điều hành Android cấp một mã số riêng gọi là `pointerId` (ngón thứ nhứt là `0`, ngón thứ hai là `1`...). Tụi mình tạo cho mỗi ngón tay một hồ sơ theo dõi riêng gọi là `PointerTracker` và cất vô bảng `pointers = SparseArray<PointerTracker>(4)`:

```kotlin
private class PointerTracker(
    val pointerId: Int,
    var downKey: KeySpec?,
    var currentKey: KeySpec?,
    val startX: Float,
    val startY: Float,
    var lastX: Float,
    var lastY: Float,
    var committedOnDown: Boolean = false,
    var longPressTriggered: Boolean = false,
    var isSpaceSwiping: Boolean = false,
    var isBackspaceSwiping: Boolean = false,
    var longPressRunnable: Runnable? = null
)
```

Hãy coi cách hàm `onTouchEvent(event: MotionEvent)` ([dòng 518–595](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L518-L595)) điều phối khi hai ngón tay chạm cùng lúc:

```kotlin
MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
    if (action == MotionEvent.ACTION_POINTER_DOWN && isGliding) {
        // Second finger tapped while gliding -> cancel glide immediately (2-thumb typing!)
        isGliding = false
        glideKeys.clear()
        glideTrailPoints.clear()
    }
    // Cancel pending long-press on any previous finger (rollover)
    for (i in 0 until pointers.size()) {
        val prev = pointers.valueAt(i)
        prev.longPressRunnable?.let { handler.removeCallbacks(it) }
    }
    val key = findKeyAt(x, y)
    val tracker = PointerTracker(
        pointerId = pid,
        downKey = key,
        currentKey = key,
        startX = x,
        startY = y,
        lastX = x,
        lastY = y
    )
    pointers.put(pid, tracker)
    if (key != null) {
        handleKeyDown(tracker, key)
    }
    invalidate()
    return true
}
```

Bạn nhìn 3 quyết định cực kỳ dứt khoát khi ngón tay thứ hai chạm xuống (`ACTION_POINTER_DOWN`):
1. **Chốt chữ ngay tại thời điểm chạm xuống (`handleKeyDown`)**: Ngay khi ngón trái chạm phím `T`, chữ `'t'` đã được gởi ngay lập tức vô `VietnameseEngine` (`tracker.committedOnDown = true`). Khi ngón phải chạm phím `H` sau đó 25ms, chữ `'h'` cũng được gởi ngay lập tức nối tiếp vô sau chữ `'t'` thành `"th"`. Không cần phải đợi ngón nào nhấc lên trước, thứ tự chữ cái luôn đúng 100% theo thứ tự chạm xuống!
2. **Hủy bộ đếm nhấn giữ (`longPressRunnable`) của ngón trước**: Khi ngón thứ hai đã gõ xuống, chắc chắn người dùng đang gõ nhanh bằng hai tay chớ hổng phải muốn nhấn giữ ngón thứ nhứt để mở ký tự phụ `@, #, $`. Hủy ngay bộ đếm nhấn giữ của ngón trước giúp người gõ nhanh không bao giờ bị nhảy ký tự phụ ngoài ý muốn.
3. **Ngắt chế độ Lướt phím (`isGliding = false`) nếu có ngón thứ hai chạm vô**: Người ta chỉ lướt phím (Glide Typing) bằng 1 ngón tay duy nhứt (`pointerCount == 1`). Nếu đang di chuyển ngón thứ nhứt mà ngón thứ hai chạm xuống, bộ gõ hiểu ngay đó là gõ nhanh 2 ngón cái và tắt cờ lướt phím liền.

---

## CHƯƠNG 14: BA CỬ CHỈ THÔNG MINH TRÊN MỘT MẶT PHÍM: LƯỚT SPACEBAR DỜI CON TRỎ, LƯỚT BACKSPACE XÓA TỪNG CỤM TỪ, NHẤN GIỮ MỞ DẢI KÝ TỰ PHỤ

Khi ngón tay của bạn di chuyển trên màn hình (`MotionEvent.ACTION_MOVE`), hàm `handlePointerMove` ([dòng 698–789](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L698-L789)) sẽ kiểm tra xem lúc đầu ngón tay đó đặt xuống ở phím nào (`tracker.downKey`) để kích hoạt đúng cử chỉ thông minh tương ứng:

### 14.1. Cử chỉ 1: Trượt trên phím Dấu cách (Spacebar) để điều khiển con trỏ (`Spacebar Gesture Cursor Control`)

Khi bạn viết một câu dài rồi phát hiện chữ ở giữa câu bị sai, việc lấy ngón tay chọt lên màn hình ngay đúng khe giữa hai chữ cái nhỏ xíu trên ô tin nhắn nhiều khi trật lên trật xuống.
Trên `vnchFlex`, bạn chỉ cần đặt ngón tay lên phím dài `Tiếng Việt` (Spacebar) rồi trượt qua trái hoặc qua phải ([dòng 714–734](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L714-L734)):

```kotlin
// 2. Spacebar gesture cursor control
if (downKey.role == KeyRole.SPACE && enableGestureCursor) {
    val thresholdPx = 28f * density
    val stepPx = 14f * density
    if (!tracker.isSpaceSwiping && kotlin.math.abs(mx - tracker.startX) > thresholdPx) {
        tracker.isSpaceSwiping = true
        tracker.longPressRunnable?.let { handler.removeCallbacks(it) }
        if (tracker.committedOnDown) {
            onUndoDownSpace?.invoke()
            tracker.committedOnDown = false
        }
    }
    if (tracker.isSpaceSwiping) {
        val dx = mx - tracker.lastX
        if (dx > stepPx) {
            onSpaceCursorMove?.invoke(1)
        } else if (dx < -stepPx) {
            onSpaceCursorMove?.invoke(-1)
        }
    }
    return
}
```
- Lúc bạn mới chạm xuống phím Spacebar, bộ gõ đã tạm thời chèn một dấu cách (`tracker.committedOnDown = true`).
- Nhưng nếu bạn không nhấc tay lên mà kéo ngang vượt qua ngưỡng `thresholdPx = 28dp`: Bộ gõ biết ngay bạn muốn **lướt để dời con trỏ**!
- Nó lập tức gọi `onUndoDownSpace?.invoke()` để **rút lại dấu cách vừa chèn**, hủy bộ đếm nhấn giữ 750ms, rồi cứ mỗi `stepPx = 14dp` bạn trượt qua trái hay qua phải, nó phát lệnh `onSpaceCursorMove(-1)` hoặc `onSpaceCursorMove(+1)` để đẩy con trỏ văn bản đi từng chữ cái một êm như nhung!

### 14.2. Cử chỉ 2: Trượt phím Xóa lùi (`⌫`) sang trái để tô đen và xóa nguyên cả cụm từ (`Backspace Slide-to-Select Gesture Delete`)

Đây là tính năng mà ai đã quen xài rồi là rất thích: Thay vì phải nhấn giữ phím `⌫` chờ nó xóa rà rà từng chữ cái, bạn chỉ cần chạm vô phím `⌫` rồi **kéo ngón tay sang bên trái** ([dòng 737–757](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L737-L757)):

```kotlin
// 3. Backspace slide-to-select gesture delete
if (downKey.role == KeyRole.BACKSPACE && enableGestureDelete) {
    val swipeStartPx = 24f * density
    val stepPerWordPx = 36f * density
    val dragLeftPx = tracker.startX - mx
    if (!tracker.isBackspaceSwiping && dragLeftPx > swipeStartPx) {
        tracker.isBackspaceSwiping = true
        stopBackspaceRepeat()
        onHaptic?.invoke(true)
        backspaceGestureWords = 1
        onBackspaceGestureSelect?.invoke(1, true)
    }
    if (tracker.isBackspaceSwiping) {
        val extraPx = (dragLeftPx - swipeStartPx).coerceAtLeast(0f)
        val targetWords = if (dragLeftPx <= swipeStartPx * 0.5f) 0 else 1 + (extraPx / stepPerWordPx).toInt()
        if (targetWords != backspaceGestureWords) {
            backspaceGestureWords = targetWords
            onBackspaceGestureSelect?.invoke(targetWords, false)
        }
    }
    return
}
```

Cách hoạt động của nó cực kỳ trực quan:
1. Khi bạn kéo sang trái vượt quá `swipeStartPx = 24dp`, máy rung nhẹ một cái báo hiệu vào chế độ chọn từ (`isBackspaceSwiping = true`), đồng thời tắt bộ lặp xóa từng chữ (`stopBackspaceRepeat()`).
2. Cứ mỗi `stepPerWordPx = 36dp` bạn kéo thêm về bên trái, số từ được chọn `targetWords` tăng lên 1 từ (`1, 2, 3, 4...`). Nhạc trưởng `VietKeyboardService` sẽ gọi `ic.setSelection(selStart, canvasGestureBaseEnd)` để **tô sáng (highlight)** đúng bấy nhiêu từ trên màn hình cho mắt bạn nhìn thấy rõ mình sắp xóa những chữ nào!
3. Nếu bạn lỡ kéo lố tay? Đừng lo, bạn chỉ cần kéo ngón tay ngược về bên phải một chút là vùng tô sáng thu hẹp lại ngay (`targetWords` giảm xuống). Nếu bạn kéo sát về lại phím `⌫` (`targetWords = 0`), không có từ nào bị xóa cả!
4. Khi bạn vừa ý rồi và **nhấc ngón tay lên** (`handlePointerUp`), toàn bộ cụm từ đang tô sáng biến mất trong một nhát cắt duy nhứt (`ic.commitText("", 1)`).

### 14.3. Cử chỉ 3: Nhấn giữ mở Dải ký tự phụ (`Inline Alternates Strip`) và trượt ngón tay để chọn

Khi bạn nhấn giữ một phím chữ cái (ví dụ phím `'a'`) hoặc phím dấu chấm (`'.'`) quá `longPressDelayMs` (mặc định 300ms):
- Bộ đếm `longPressRunnable` nổ! Nó rút lại chữ `'a'` vừa gõ xuống (`onUndoDownLetter?.invoke()`), rung nhẹ một cái, rồi bật danh sách ký tự phụ `activeAltOptions`:
  - Với phím `'a'`: `["á", "à", "ả", "ã", "ạ", "ă", "â", "@"]`
  - Với phím `'.'`: `["?", "!", ",", ":", ";", "-", "(", ")", "/"]`
- Hàm `drawAlternatesStrip` ([dòng 477–508](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L477-L508)) sẽ vẽ một dải bong bóng nổi ngay phía trên phím đó, ô đang chọn được tô nền xanh dương nổi bật (`#FF1A73E8`).
- Bạn không cần nhấc tay lên mà cứ trượt nhẹ ngón tay qua trái/phải để đổi ô chọn (`activeAltSelectedIdx`), rồi thả tay ra là ký tự được chọn nhảy thẳng vô văn bản!

---

# PHẦN IV: NHẠC TRƯỞNG HỆ THỐNG — DỊCH VỤ BÀN PHÍM (`VietKeyboardService`) & QUẢN LÝ BỘ NHỚ TẠM (`ClipboardStore`)

---

## CHƯƠNG 15: VÒNG ĐỜI CỦA MỘT `InputMethodService` VÀ CÂY CẦU `InputConnection`

Nếu `VietnameseEngine` là đầu bếp trưởng chế biến món ăn Tiếng Việt, còn `KeyboardCanvasView` là mặt tiền nhà hàng sang trọng 120FPS, thì [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt) (dài 1.901 dòng) chánh là vị **Nhạc trưởng (Orchestrator)** đứng giữa điều phối tất cả mọi bộ phận.

### 15.1. Năm cột mốc trong vòng đời của `VietKeyboardService`

Vì bàn phím là một dịch vụ chạy ngầm của hệ điều hành Android (`class VietKeyboardService : InputMethodService()`), nó không có nút bấm mở lên như các ứng dụng bình thường mà sống theo **5 cột mốc vòng đời** do hệ thống Android gọi:

```
   [1. onCreate()]  ---> Chạy 1 lần duy nhứt khi dịch vụ bàn phím khởi động
          |              Khởi tạo: VietnameseEngine, MacroEngine, SuggestionEngine,
          |                        OnnxVietnameseCorrector, GlideDecoder, ClipboardStore
          v
[2. onCreateInputView()] ---> Chạy khi bàn phím chuẩn bị hiện lên màn hình lần đầu
          |                   Nạp layout XML, gắn kết các callback của KeyboardCanvasView,
          |                   khởi tạo 3 ô gợi ý từ (suggestionSlots) và thanh công cụ
          v
  [3. onStartInput()]    ---> Chạy MỖI KHI người dùng bấm vô một ô nhập chữ mới
          |                   (kể cả khi bàn phím chưa trồi lên). Kiểm tra xem ô đó thuộc
          |                   app nào (có phải Telegram không?), có đang bôi đen chữ không?
          v
[4. onStartInputView()]  ---> Chạy mỗi khi mặt bàn phím thực sự trồi lên cho người dùng gõ
          |                   Đồng bộ cài đặt mới nhứt (chiều cao, giao diện, biểu tượng VnCharKey,
          |                   bỏ dấu cổ điển), xét viết hoa đầu câu và cập nhật thanh gợi ý
          v
   [5. onDestroy()]      ---> Chạy khi hệ thống tắt dịch vụ bàn phím
                              Đóng phiên ONNX AI, lưu nốt từ điển N-gram xuống bộ nhớ máy
```

### 15.2. Cây cầu `InputConnection` và quy tắc vàng `beginBatchEdit() / endBatchEdit()`

Làm sao `VietKeyboardService` có thể đọc chữ hoặc chèn chữ vô một ứng dụng khác (như Zalo hay Chrome) khi mỗi ứng dụng Android chạy trong một vùng nhớ riêng biệt?
Câu trả lời nằm ở đối tượng **`currentInputConnection`** (kiểu `InputConnection`). Đó là một đường ống liên lạc liên tiến trình (*Inter-Process Communication — IPC*) nối từ bàn phím của tụi mình sang ô nhập chữ của ứng dụng đang mở.

Cây cầu `InputConnection` này cung cấp cho tụi mình 6 câu lệnh quan trọng nhứt:
1. **`ic.setComposingText(text, 1)`**: Đặt chuỗi chữ đang gõ dở (có gạch chân tạm thời) lên ô nhập chữ. Nếu trên ô nhập chữ đã có phần gạch chân trước đó, lệnh này sẽ tự động thay thế phần gạch chân cũ bằng `text` mới.
2. **`ic.finishComposingText()`**: Gỡ bỏ đường gạch chân tạm thời, biến chữ đang gõ thành văn bản chánh thức (nhưng vẫn giữ nguyên chữ đó trên màn hình).
3. **`ic.commitText(text, 1)`**: Chốt thẳng chuỗi `text` vô vị trí con trỏ (hoặc đè lên phần đang gạch chân / phần đang bôi đen).
4. **`ic.deleteSurroundingText(beforeLength, afterLength)`**: Xóa `beforeLength` ký tự đứng ngay trước con trỏ và `afterLength` ký tự đứng ngay sau con trỏ.
5. **`ic.getTextBeforeCursor(n, 0)`**: Đọc tối đa `n` ký tự đứng trước con trỏ để biết ngữ cảnh câu đang viết.
6. **`ic.getSelectedText(0)`**: Lấy chuỗi văn bản mà người dùng đang bôi đen (ví dụ khi người dùng bấm *Chọn tất cả / Select All*).

> **Quy tắc vàng khi qua cầu `InputConnection`**:
> Vì mỗi lệnh gọi lên `ic` phải đi qua đường ống IPC giữa hai ứng dụng, nếu bạn gọi `ic.deleteSurroundingText(...)` rồi mới gọi `ic.commitText(...)` rời rạc nhau, ứng dụng bên kia sẽ vẽ lại màn hình 2 lần: lần 1 người dùng thấy chữ biến mất (trắng xóa), vài mili-giây sau mới thấy chữ mới hiện ra -> gây ra hiện tượng chớp nháy!
> Bởi vậy, trong `vnchFlex`, bất cứ khi nào cần thực hiện từ 2 thao tác trở lên, tụi mình luôn luôn gói chúng vào trong cặp lệnh:
> ```kotlin
> ic.beginBatchEdit()
> try {
>     // Thực hiện xóa và chèn ở đây...
> } finally {
>     ic.endBatchEdit()
> }
> ```
> Lệnh `beginBatchEdit()` bảo với ứng dụng bên kia rằng: *"Khoan vẽ màn hình nha, chờ tui sửa xong hết tới `endBatchEdit()` rồi hãy vẽ một lần duy nhứt!"*

---

## CHƯƠNG 16: TUYỆT CHIÊU "CHỐT NGAY KHI CHẠM XUỐNG, RÚT LẠI KHI ĐỔI Ý" (COMMIT-ON-DOWN & CANCEL-ON-ACTION)

### 16.1. Tại sao các bàn phím thông thường lại có cảm giác "trễ một nhịp"?

Bạn thử để ý một phím có hai chức năng trên bàn phím điện thoại:
- Ví dụ phím chữ **`a`**: Bấm nhanh một cái rồi thả ra (`Tap`) thì ra chữ `'a'`, nhưng nếu nhấn giữ lâu quá `300ms` (`Long-press`) thì ra ký tự phụ `'@'` (hoặc dải chọn dấu `'á, à, ả...'`).
- Hoặc phím **Dấu cách (`Space`)**: Bấm nhanh thì ra khoảng trắng `' '`, trượt ngang thì dời con trỏ, nhấn giữ lâu `750ms` thì mở bảng đổi bàn phím hệ thống.

Nếu người lập trình xử lý theo cách ngây thơ là **"Đợi người dùng nhấc ngón tay lên (`ACTION_UP`) mới dám xuất chữ ra màn hình"** (gọi là *Commit-on-Up*), thì chuyện gì sẽ xảy ra?
- Khi ngón tay của bạn chạm xuống mặt kiếng (`ACTION_DOWN`), màn hình **chưa hiện ra chữ gì hết**!
- Phải mất từ `60ms` đến `120ms` sau, khi ngón tay bạn rời khỏi mặt kiếng (`ACTION_UP`), chữ `'a'` mới chịu hiện lên màn hình.
- Khoảng trễ `100ms` đó làm cho não bộ cảm thấy cái bàn phím này "lì lì, chậm chạp", và nguy hiểm hơn là khi gõ nhanh hai ngón cái sẽ dễ bị đảo lộn thứ tự phím nếu ngón chạm sau lại nhấc lên trước!

### 16.2. Cơ chế Commit-on-Down & Cancel-on-Action của `vnchFlex`

Trong `vnchFlex`, tụi mình đảo ngược hoàn toàn quy trình đó bằng triết lý **Chốt ngay khi chạm xuống — Rút lại khi đổi ý (`Commit-on-Down & Cancel-on-Action`)**:

1. **Ngay khoảnh khắc `ACTION_DOWN` (0ms trễ)**:
   - Vừa chạm ngón tay xuống phím `'a'`, `KeyboardCanvasView` gọi ngay `onLetterDown('a')` và đánh dấu `tracker.committedOnDown = true`. Chữ `'a'` hiện lên màn hình tức khắc ngay dưới đầu ngón tay bạn!
2. **Nếu bạn chỉ gõ bình thường rồi nhấc tay lên trước 300ms**:
   - Mọi chuyện đã xong xuôi từ lúc chạm xuống rồi! Lúc nhấc tay lên (`ACTION_UP`), bộ gõ chỉ cần tắt bộ đếm hẹn giờ `longPressRunnable`, không phải tốn thời gian xuất chữ nữa.
3. **Nếu bạn đổi ý: Nhấn giữ quá 300ms để lấy chữ `'@'`, hoặc bắt đầu kéo ngón tay để Lướt phím (Glide Typing)**:
   - Ngay khoảnh khắc bộ đếm 300ms nổ (hoặc khoảnh khắc ngón tay kéo vượt quá ngưỡng `glideThresholdDp = 24dp`), `KeyboardCanvasView` gọi điện cho nhạc trưởng `VietKeyboardService` qua hàm **`undoDownLetterCommit()`** (hoặc `onGlideStart`, `undoDownSpaceCommit()`, `undoDownPunctuationCommit()`):

Tụi mình coi hàm `undoDownLetterCommit()` trong [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L1185-L1200) rút lại chữ vừa chạm xuống gọn gàng ra sao nha:

```kotlin
private fun undoDownLetterCommit() {
    val ic = currentInputConnection ?: return
    markOwnIcEdit()
    if (isVietnameseMode && vnEngine.isComposing) {
        vnEngine.onBackspace(ic)
    } else {
        if (enComposing.isNotEmpty()) enComposing.deleteCharAt(enComposing.length - 1)
        ic.deleteSurroundingText(1, 0)
    }
    if (currentTokenBuffer.isNotEmpty()) {
        currentTokenBuffer.deleteCharAt(currentTokenBuffer.length - 1)
    }
    justExpandedMacroWithSpace = justExpandedMacroBeforeDown
}
```
- Nếu đang gõ Tiếng Việt (`vnEngine.isComposing`), nó chỉ cần gọi `vnEngine.onBackspace(ic)` để lùi lại đúng 1 bước trước khi chạm ngón tay xuống.
- Rút chữ tạm ra xong, nó chèn ngay ký tự phụ `'@'` vô (hoặc bắt đầu vẽ đường lướt phím Glide Typing).
- Nhờ tuyệt chiêu này, 99% các cú gõ phím bình thường của bạn đều có **độ trễ bằng 0**, nhanh và nhạy y hệt như đang gõ trên bàn phím cơ thiệt!

---

## CHƯƠNG 17: XỬ LÝ TRỌN VẸN PHÍM XÓA LÙI (`onBackspace`) VÀ BÀI HỌC KINH ĐIỂN TỪ LỖI `Select All + Backspace`

Phím Xóa lùi (`Backspace` / `⌫`) nhìn bề ngoài chỉ có một cái hình mũi tên nhỏ xíu, nhưng bên trong `VietKeyboardService.kt`, hàm `onBackspace()` ([dòng 937–1051](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L937-L1051)) lại là một trong những hàm thú vị nhứt vì nó phải xử lý tới **6 tầng ưu tiên khác nhau**.

### 17.1. Bài học xương máu: Tại sao bấm `Select All` (Chọn tất cả) rồi bấm `Backspace` mà chữ hổng chịu xóa?

Tụi mình cùng nhớ lại một con bug thực tế rất hay mà bạn đã phát hiện ra trên `vnchFlex`:
Khi người dùng gõ một đoạn văn bản dài, sau đó nhấn giữ trên ô nhập chữ chọn **"Select All" (Chọn tất cả / Bôi đen toàn bộ văn bản)**, rồi bấm phím **`Backspace` (`⌫`)** trên bàn phím với mong muốn xóa sạch đoạn văn đang bôi đen.
Thế nhưng trong phiên bản cũ, bấm `Backspace` một cái... **chẳng có chuyện gì xảy ra hết**, đoạn chữ đang bôi đen vẫn trơ trơ ra đó!

**Tại sao lại có chuyện kỳ lạ vậy?**
Hãy nhìn lại những gì xảy ra khi người dùng vừa gõ xong một chữ Tiếng Việt (ví dụ chữ `"chào"`):
1. Lúc mới gõ xong chữ `"chào"` (chưa bấm dấu cách), bộ máy `VietnameseEngine` vẫn đang giữ trong bụng nó danh sách phím thô `rawChars = ['c', 'h', 'a', 'o', 'f']` và `renderedOutput = "chào"`, tức là `vnEngine.isComposing` đang bằng **`true`**!
2. Ngay lúc đó, người dùng đưa tay lên ô nhập chữ bấm **"Select All"** để bôi đen cả đoạn văn dài từ đầu tới cuối.
3. Khi người dùng bấm phím `Backspace`, nếu hàm `onBackspace()` không kiểm tra xem trên màn hình có đang bôi đen văn bản hay không, mà nhảy thẳng vô hỏi `vnEngine.onBackspace(ic)`:
   - `vnEngine` thấy trong bụng nó có chữ `"chào"`, nên nó chỉ xóa chữ `'o'` cuối cùng của `"chào"` thành `"chà"`, hoặc tệ hơn là nếu `rawChars` đang rỗng thì hàm cuối cùng gọi `ic.deleteSurroundingText(1, 0)`.
   - Mà bạn biết đặc điểm của lệnh `ic.deleteSurroundingText(1, 0)` trong Android là gì hông? Theo tài liệu chánh thức của Google Android SDK, lệnh `deleteSurroundingText(beforeLength, afterLength)` **chỉ xóa các ký tự nằm ngoài vùng đang bôi đen (surrounding the current selection), và hoàn toàn KHÔNG đụng chạm gì tới phần văn bản đang được bôi đen (`Selected Text`)!**
   - Khi bạn bấm `Select All`, toàn bộ 100% văn bản trên ô nhập chữ đều nằm **bên trong** vùng bôi đen, phía trước vùng bôi đen không còn ký tự nào cả (`0` ký tự)! Nên gọi `ic.deleteSurroundingText(1, 0)` lúc đang `Select All` là xóa 0 ký tự — chẳng có tác dụng gì ráo!

**Cách giải quyết triệt để trong `vnchFlex` ([dòng 944–957](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L944-L957))**:
Ngay ở đầu hàm `onBackspace()`, ưu tiên số 1 tối thượng trước mọi xử lý khác là kiểm tra `ic.getSelectedText(0)`:

```kotlin
// Delete active text selection (e.g. Select All + Backspace)
if (!ic.getSelectedText(0).isNullOrEmpty()) {
    vnEngine.reset()
    enComposing.clear()
    currentTokenBuffer.clear()
    lastGlideCommittedLength = 0
    lastAutoCorrection = null
    ic.commitText("", 1)
    updateSelectionToolbarState(false)
    trackCommittedChar(null)
    updateAutoCapitalization()
    updateSuggestions("")
    return
}
```
Khi phát hiện người dùng đang bôi đen văn bản (`!ic.getSelectedText(0).isNullOrEmpty()`):
1. Bộ gõ dọn sạch toàn bộ bộ đệm `vnEngine.reset()`, `enComposing.clear()`, `currentTokenBuffer.clear()`.
2. Gọi **`ic.commitText("", 1)`**: Trong chuẩn Android `InputConnection`, việc commit một chuỗi rỗng `""` khi đang có vùng chọn sẽ **thay thế toàn bộ vùng đang bôi đen bằng chuỗi rỗng**, tức là xóa sạch toàn bộ đoạn văn bản đã chọn chỉ trong một tích tắc!
3. Cập nhật lại trạng thái viết hoa đầu câu (`updateAutoCapitalization()`) và làm mới thanh gợi ý.

Một bài học quá hay cho bất kỳ ai viết bộ gõ Android: **Không bao giờ dùng `deleteSurroundingText` để xóa vùng chữ đang bôi đen (`Selection`), mà phải kiểm tra `getSelectedText(0)` và dùng `commitText("", 1)`!**

### 17.2. Sáu tầng ưu tiên của hàm `onBackspace()`

Sau khi hiểu tầng ưu tiên số 1, tụi mình điểm qua toàn bộ 6 tầng ưu tiên trong `onBackspace()` để thấy sự chu đáo của `vnchFlex`:

```
                       [ Người dùng nhấn phím Backspace (⌫) ]
                                         |
                                         v
+---------------------------------------------------------------------------------+
| Tầng 1: Có văn bản đang được bôi đen (Select All / Highlight) không?            |
|         -> Nếu CÓ: Gọi ic.commitText("", 1) xóa sạch vùng chọn & Reset bộ đệm.  |
+---------------------------------------------------------------------------------+
                                         | (Nếu không)
                                         v
+---------------------------------------------------------------------------------+
| Tầng 2: Vừa mới Lướt phím (Glide Typing) ra cả từ xong phải không?              |
|         (lastGlideCommittedLength > 0)                                          |
|         -> Nếu CÓ: Xóa trọn vẹn cả từ vừa lướt chỉ bằng 1 nhát Backspace!       |
+---------------------------------------------------------------------------------+
                                         | (Nếu không)
                                         v
+---------------------------------------------------------------------------------+
| Tầng 3: Vừa bị Tự động sửa lỗi lệch phím (Spatial Auto-Correction) phải không?  |
|         (lastAutoCorrection != null)                                            |
|         -> Nếu CÓ: Hoàn tác, trả lại đúng chữ gốc người dùng đã gõ & học từ đó! |
+---------------------------------------------------------------------------------+
                                         | (Nếu không)
                                         v
+---------------------------------------------------------------------------------+
| Tầng 4: Vừa mới bung Gõ tắt (Macro) và bấm Backspace lần 2 trong vòng 600ms?    |
|         -> Nếu CÓ: Thu hồi cụm từ gõ tắt, trả lại từ viết tắt gốc (vd: "ko ").  |
+---------------------------------------------------------------------------------+
                                         | (Nếu không)
                                         v
+---------------------------------------------------------------------------------+
| Tầng 5: Đang gõ dở một chữ trong VietnameseEngine (isComposing == true)?        |
|         -> Nếu CÓ: Gọi vnEngine.onBackspace(ic) xóa lùi 1 chữ cái hiển thị.     |
+---------------------------------------------------------------------------------+
                                         | (Nếu không)
                                         v
+---------------------------------------------------------------------------------+
| Tầng 6: Xóa 1 ký tự thường; nếu lùi đụng trúng một chữ Tiếng Việt đứng trước    |
|         -> Gọi vnEngine.rehydrateWord(prevWord, ic) nạp lại chữ đó để sửa tiếp! |
+---------------------------------------------------------------------------------+
```

---

## CHƯƠNG 18: QUẢN LÝ BỘ NHỚ TẠM (`ClipboardStore`), GHIM VĂN BẢN VÀ TỰ ĐỘNG DỌN RÁC SAU 1 GIỜ

Khi bạn sao chép (Copy) một số điện thoại, một địa chỉ nhà hay một mã đơn hàng trên điện thoại, nếu chỉ dùng bộ nhớ tạm mặc định của hệ thống thì mỗi lần bạn Copy món mới, món cũ sẽ bị ghi đè mất tiêu.
Để giúp người dùng lưu giữ được tới **20 đoạn văn bản sao chép gần nhứt**, `vnchFlex` xây dựng hệ thống [`ClipboardStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardStore.kt) và giao diện bảng quản lý [`ClipboardPanelController.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardPanelController.kt).

### 18.1. Cấu trúc một mảnh nhớ tạm (`ClipboardItem`) và thời hạn tự hủy 1 giờ (`UNPINNED_TTL_MS`)

Tụi mình mở [`ClipboardStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardStore.kt#L7-L56) coi cách tổ chức dữ liệu nha:

```kotlin
data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

class ClipboardStore(context: Context) {
    private val prefs = context.getSharedPreferences("vnchflex_clipboard", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "clipboard_items_json"
        const val UNPINNED_TTL_MS = 60L * 60L * 1000L // 1 hour
        const val MAX_ITEMS = 20
    }
```

Tại sao phải có hằng số **`UNPINNED_TTL_MS = 60 * 60 * 1000` (1 tiếng đồng hồ)**? (`TTL` là viết tắt của *Time-To-Live*, tức là Thời gian sống của dữ liệu):
- Trong thực tế, đôi khi người dùng copy một đường link tạm thời hoặc một đoạn mã xác nhận. Nếu lưu vĩnh viễn mọi thứ người dùng copy từ tháng này qua năm nọ mà người dùng không ghim lại, bộ nhớ tạm sẽ trở thành một bãi rác vừa rối mắt vừa kém an toàn.
- Vì vậy, `ClipboardStore` áp dụng đúng chuẩn bảo mật và tiện lợi của Gboard:
  1. **Mục chưa ghim (`isPinned == false`)**: Tự động hết hạn và được dọn dẹp sạch sẽ sau **1 giờ** (`(now - it.timestamp) <= UNPINNED_TTL_MS`).
  2. **Mục đã ghim (`isPinned == true`)**: Khi bạn bấm nút Ghim (Pin) vào những thông tin cần xài hoài (như số tài khoản ngân hàng, địa chỉ nhà, câu chào khách hàng), mục đó sẽ **được giữ lại vĩnh viễn** và luôn được ưu tiên xếp lên đầu danh sách (`compareByDescending<ClipboardItem> { it.isPinned }.thenByDescending { it.timestamp }`).
  3. **Chống trùng lặp thông minh**: Trong hàm `addClip(text, now)` ([dòng 40–56](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardStore.kt#L40-L56)), nếu bạn copy lại một đoạn chữ đã có sẵn trong danh sách, nó không tạo ra 2 dòng trùng nhau mà chỉ việc bốc dòng đó lên vị trí đầu bảng (`index 0`), làm mới lại đồng hồ `timestamp = now` và giữ nguyên trạng thái ghim `isPinned` của dòng đó!

---

## CHƯƠNG 19: HỆ THỐNG TÙY CHỈNH (`KeyboardPreferences`) — LƯU GIỮ THÓI QUEN CỦA NGƯỜI DÙNG TỪ BIỂU TƯỢNG `VnCharKey` TỚI CÁCH BỎ DẤU CỔ ĐIỂN

### 19.1. `SharedPreferences` là gì và tại sao `KeyboardPreferences` đóng vai trò trung tâm?

Trên hệ điều hành Android, khi bạn bật một công tắc trong màn hình Cài đặt (ví dụ chọn giao diện Tối, chọn chiều cao bàn phím, hay chọn kiểu bỏ dấu cổ điển), làm sao để sau khi bạn tắt máy khởi động lại, ứng dụng vẫn nhớ như in những lựa chọn đó?
Android cung cấp một kho lưu trữ dạng `Khóa - Giá trị` (`Key - Value`) nằm trên bộ nhớ trong của điện thoại gọi là **`SharedPreferences`** (được hệ thống lưu dưới dạng một tập tin XML nhỏ trong thư mục riêng tư của ứng dụng).

Thay vì để các tập tin trong dự án tự ý đọc/ghi tên khóa lung tung dễ bị gõ sai chữ, lớp [`KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt) gom toàn bộ **34 tùy chọn** của bộ gõ về một mối duy nhứt thông qua các thuộc tính Kotlin (`var ... get() ... set(v) ...`).

### 19.2. Hai thiết lập mặc định mang đậm bản sắc của `vnchFlex`

Hãy nhìn vào hai thuộc tính mặc định được thiết lập sẵn ngay từ lần đầu tiên người dùng cài đặt `vnchFlex` trong [`KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt#L24-L28) và [dòng 187–190](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt#L187-L190):

1. **Biểu tượng phím chuyển ngôn ngữ mặc định là `VnCharKey` (`"vncharkey_logo"`)**:
   ```kotlin
   /** Biểu tượng phím chuyển ngôn ngữ: "vncharkey_logo" (mặc định), "logo", "ev", hoặc "globe" */
   var langSwitchKeyIcon: String
       get() = prefs.getString("pref_lang_switch_key_icon", "vncharkey_logo") ?: "vncharkey_logo"
       set(v) = prefs.edit().putString("pref_lang_switch_key_icon", v).apply()
   ```
   Khi `langSwitchKeyIcon` mang giá trị mặc định `"vncharkey_logo"`, hàm `applyPreferencesToView()` trong `VietKeyboardService.kt` ([dòng 315–320](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L315-L320)) sẽ tự động vẽ:
   - Biểu tượng logo **VnCharKey** sắc nét (`R.drawable.ic_lang_vncharkey_logo`) khi bạn đang ở chế độ gõ **Tiếng Việt**.
   - Biểu tượng chữ **E** đồng bộ phong cách VnCharKey (`R.drawable.ic_lang_vncharkey_e`) khi bạn chuyển sang chế độ gõ **Tiếng Anh**.
   Nhờ vậy, chỉ cần liếc mắt nhìn xuống góc trái phím Spacebar là bạn biết ngay mình đang ở chế độ Tiếng Việt hay Tiếng Anh mà hổng bao giờ sợ gõ nhầm!

2. **Cách bỏ dấu cổ điển làm mặc định (`modernOrthography = false`)**:
   ```kotlin
   /** Kiểu đặt dấu mới (oà, oè, uý) vs kiểu cũ/cổ điển (òa, òe, úy) */
   var modernOrthography: Boolean
       get() = prefs.getBoolean("pref_vn_modern_orthography", false)
       set(v) = prefs.edit().putBoolean("pref_vn_modern_orthography", v).apply()
   ```
   Khi `modernOrthography` mặc định là `false`, bộ máy `VietnameseEngine` sẽ tự động bỏ dấu theo chuẩn chánh tả cổ điển (`hòa bình`, `khỏe mạnh`, `thủy chung`, `túy lúy`). Nếu người dùng nào muốn chuyển qua kiểu mới (`hoà`, `khoẻ`, `thuỷ`), họ vẫn có thể bật công tắc này trong phần Cài đặt bất cứ lúc nào.

### 19.3. Cơ chế khóa băm `lastAppliedPrefsKey` — Chống vẽ lại thừa thãi khi mở bàn phím

Trong [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L297-L308), hàm `applyPreferencesToView()` được gọi mỗi khi bàn phím trồi lên (`onStartInputView`). Nhưng hầu hết các lần mở bàn phím để nhắn tin, người dùng đâu có vô Cài đặt đổi cấu hình đâu!
Để không tốn công tính toán và vẽ lại toàn bộ giao diện nếu cài đặt không có gì thay đổi, `vnchFlex` tạo một chuỗi khóa tổng hợp `prefsKey`:

```kotlin
val prefsKey = listOf(
    prefs.showNumberRow, prefs.showSuggestionStrip, prefs.showLanguageSwitchKey,
    prefs.showStripDiacriticsButton, prefs.enableVoiceTyping,
    prefs.themeMode, prefs.keyBorders, prefs.oneHandedMode, prefs.keyboardHeight,
    prefs.longPressForSymbols, prefs.enableGlideTyping, prefs.showGestureTrail,
    prefs.glideSensitivityDp, prefs.langSwitchKeyIcon, isVietnameseMode, shiftState.name
).joinToString("|")
if (prefsKey == lastAppliedPrefsKey) return
lastAppliedPrefsKey = prefsKey
```
Nếu chuỗi `prefsKey` hiện tại giống hệt `lastAppliedPrefsKey` của lần mở trước, hàm `applyPreferencesToView()` thoát ra ngay ở dòng thứ 3 (`return`), tiết kiệm tối đa pin và CPU cho điện thoại!

---
*(Hết Phần III và Phần IV — Tiếp theo là Phần V và Phần VI đi sâu vào Trí tuệ Ngôn ngữ N-gram, Lướt phím Hình học & Mạng thần kinh ONNX)*

# PHẦN V: TRÍ TUỆ NGÔN NGỮ — TIÊN ĐOÁN TỪ, TỰ HỌC N-GRAM VÀ SỬA LỖI LỆCH PHÍM (`SuggestionEngine`)

---

## CHƯƠNG 20: CẤU TRÚC DỮ LIỆU CÂY TIỀN TỐ KHÔNG DẤU (`Stripped Trie`) — TÌM HÀNG NGÀN TỪ TIẾNG VIỆT TRONG CHỚP MẮT

### 20.1. Tại sao tìm kiếm bằng danh sách thường (`List`) lại quá chậm khi gõ phím?

Trong từ điển của `vnchFlex` ([`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt)) có hàng ngàn từ vựng Tiếng Việt phổ thông, từ phương ngữ Nam Bộ (`dzìa, dzách, dzô, dzậy, dzọt, dzui, thiệt, hông, hen, nha...`), từ vựng Tiếng Anh và cả các từ do người dùng tự dạy cho máy.
Giả sử người dùng mới gõ 2 chữ cái không dấu là `"vi"` (hoặc `"dz"`). Làm sao bộ gõ tìm ra ngay trong vòng **chưa tới 1 mili-giây** tất cả các từ Tiếng Việt bắt đầu bằng `"vi"` (như *"việt"*, *"việc"*, *"vì"*, *"viết"*, *"viên"*) hoặc bắt đầu bằng `"dz"` (như *"dzìa"*, *"dzách"*, *"dzô"*, *"dzậy"*) để hiện lên 3 ô gợi ý phía trên bàn phím?

Nếu bạn cất `10.000` từ trong một cái danh sách dài (`List<String>`), thì mỗi lần người dùng gõ 1 phím, máy tính phải đi hỏi từng từ từ số `1` tới số `10.000`, lại còn phải gọi `DiacriticUtils.strip(word)` cho cả `10.000` từ đó để so sánh chữ không dấu. Làm kiểu đó điện thoại sẽ nóng ran và hao pin dữ lắm!

### 20.2. Cây tiền tố không dấu (`Stripped Trie`) là cái gì mà thần kỳ vậy?

Để tìm từ nhanh như chớp mà không cần duyệt qua cả từ điển, các nhà khoa học máy tính phát minh ra một cấu trúc dữ liệu hình cái cây lộn ngược gọi là **Cây tiền tố (`Trie` / `Prefix Tree`)**.
Trong [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt), tụi mình nâng cấp nó thành **Cây tiền tố Không dấu (`strippedTrieRoot`)**:

```
                        [ Gốc Cây (strippedTrieRoot) ]
                             /                  \
                           'v'                  'd'
                           /                      \
                         'i'                      'z'
                        /   \                    / | \
             words:["vì"]   'e'               'i' 'o' 'a'
                            /                 /    |    \
                          't'               'a'  ["dzô"] 'c'
                          /                 /             \
              words:["việt","viết"]    words:["dzìa"]     'h'
                                                           \
                                                      words:["dzách"]
```

Bạn nhìn cái sơ đồ cây ở trên là hiểu liền nguyên lý hoạt động:
1. **Cách trồng cây (`insertIntoTrie(word)`)**:
   - Khi nạp chữ **`"việt"`** hoặc chữ **`"viết"`** vô cây, bộ máy lột sạch dấu của nó ra trước bằng `DiacriticUtils.strip("việt")`, được chuỗi không dấu là **`"viet"`**.
   - Từ gốc cây (`strippedTrieRoot`), nó bước theo nhánh chữ `'v'` -> nhánh chữ `'i'` -> nhánh chữ `'e'` -> nhánh chữ `'t'`.
   - Tại cái nút lá `'t'` cuối cùng của nhánh `v-i-e-t`, nó treo cả hai trái cây có dấu là **`"việt"`** và **`"viết"`** vô cái giỏ `node.words`!
2. **Cách hái quả khi người dùng gõ phím**:
   - Giả sử bạn lười bỏ dấu, bạn chỉ gõ chữ không dấu `"viet"`.
   - Thay vì tìm trong `10.000` từ, máy tính chỉ việc đi đúng **4 bước chân** từ gốc cây: bước qua `'v'`, bước qua `'i'`, bước qua `'e'`, bước qua `'t'`.
   - Tới nút `'t'`, nó mở giỏ `node.words` ra là thấy ngay chữ **`"việt"`** và **`"viết"`** nằm chờ sẵn ở đó! Thời gian tìm kiếm chỉ phụ thuộc vào độ dài của từ bạn đang gõ (`4` bước cho từ 4 chữ cái), hoàn toàn **không phụ thuộc** vào việc từ điển có 10 ngàn từ hay 1 triệu từ! Trong tin học, người ta gọi tốc độ này là $O(L)$ với $L$ là số chữ cái của từ đang gõ.

---

## CHƯƠNG 21: MÔ HÌNH NGÔN NGỮ THỐNG KÊ UNIGRAM, BIGRAM, TRIGRAM VÀ THUẬT TOÁN LÙI BẬC KATZ BACKOFF

### 21.1. Tại sao chỉ tìm theo chữ cái đầu là chưa đủ thông minh?

Giả sử bạn gõ chữ `"nam"` (hoặc mới gõ `"na"`). Trong giỏ từ điển có cả chữ **`"nam"`** (trong *Việt Nam*), chữ **`"năm"`** (trong *năm mới*), chữ **`"nằm"`** (trong *nằm nghỉ*), chữ **`"nấm"`** (trong *ăn nấm*).
Làm sao `vnchFlex` biết phải đưa chữ nào lên ô gợi ý đầu tiên?
- Nếu trước đó bạn vừa gõ chữ **`"việt"`** -> Chắc chắn 99% chữ tiếp theo bạn muốn là **`"nam"`** (*"việt nam"*).
- Nếu trước đó bạn vừa gõ chữ **`"chúc mừng"`** -> Chắc chắn chữ tiếp theo bạn muốn là **`"năm"`** (*"chúc mừng năm mới"*).
- Nếu trước đó bạn vừa gõ chữ **`"đi"`** -> Chữ hợp lý nhứt là **`"nằm"`** (*"đi nằm"*).

Khả năng đoán ý người dùng dựa trên những từ đứng ngay phía trước gọi là **Mô hình Ngôn ngữ Thống kê N-gram (`Statistical N-gram Language Model`)**.

### 21.2. Cắt nghĩa Unigram (1-gram), Bigram (2-gram) và Trigram (3-gram) dễ hiểu nhứt

Chữ **"N-gram"** nghe có vẻ hàn lâm toán học vậy thôi, chớ dịch ra tiếng bình dân Miền Nam mình thì chữ **"gram"** là **"từ"**, còn **"N"** là **"mấy từ đi chung với nhau"**:

1. **Unigram (`1-gram` — Tần suất từ đơn lẻ)**:
   - Đo xem một từ đứng một mình nó xuất hiện thường xuyên cỡ nào trong đời sống hàng ngày.
   - Trong [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L33-L58), mỗi từ có một điểm số gốc (`coreUnigramWeights`): những từ xài hoài mỗi ngày như `"không"` (`100` điểm), `"được"` (`99` điểm), `"việt"` (`98` điểm), `"bạn"` (`97` điểm), `"dzô"` (`89` điểm), `"dzìa"` (`88` điểm) có điểm gốc cao hơn những từ ít xài.
2. **Bigram (`2-gram` — Cặp 2 từ đi đôi với nhau)**:
   - Xét xác suất xuất hiện của từ hiện tại $w_i$ khi biết **1 từ đứng ngay trước nó** là $w_{i-1}$, ký hiệu toán học là:
     $$P(w_i \mid w_{i-1})$$
   - Ví dụ: Khi biết $w_{i-1} = \text{"cảm"}$, bảng `bundledBigrams["cảm"]` và `userBigrams["cảm"]` cho biết từ đi sau hay gặp nhứt là **`"ơn"`**, kế tới là **`"thấy"`**, **`"giác"`**.
3. **Trigram (`3-gram` — Bộ 3 từ đi liền nhau)**:
   - Xét xác suất xuất hiện của từ hiện tại $w_i$ khi biết **cả 2 từ liên tiếp đứng ngay trước nó** là $(w_{i-2}, w_{i-1})$, ký hiệu toán học là:
     $$P(w_i \mid w_{i-2}, w_{i-1})$$
   - Ví dụ: Nếu chỉ nhìn 1 từ trước là `"hồ"` (Bigram), máy chưa chắc bạn muốn nói *"hồ nước"*, *"hồ cá"* hay *"Hồ Chí Minh"*. Nhưng nếu nhìn cả 2 từ trước là `(w_{i-2} = "thành", w_{i-1} = "phố")`, hoặc `(w_{i-2} = "hồ", w_{i-1} = "chí")`, mô hình Trigram biết chắc chắn 100% từ thứ ba phải là **`"minh"`**!

### 21.3. Thuật toán Lùi bậc Katz Backoff và Công thức chấm điểm tái xếp hạng (`Context Re-ranking`)

Khi bạn vừa gõ xong một chữ và bấm dấu cách (chưa gõ chữ cái nào của từ mới), hàm `suggestNextWord(prevWord1, prevWord2, limit = 3)` trong [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L780-L818) sẽ dùng thuật toán **Katz Backoff (Lùi bậc khi thiếu ngữ cảnh)** để lấp đầy 3 ô gợi ý từ tiếp theo:

```
[ Bước 1: Hỏi bậc cao nhứt — TRIGRAM (prevWord2 + " " + prevWord1) ]
  Tra trong userTrigrams (câu người dùng hay nói) & bundledTrigrams (ngữ liệu Tiếng Việt).
  -> Đã đủ 3 từ gợi ý chưa?
         |
         +--- Chưa đủ 3 từ ---> [ Bước 2: Lùi xuống 1 bậc — BIGRAM (prevWord1) ]
                                  Tra trong userBigrams & bundledBigrams theo 1 từ vừa gõ.
                                  -> Đã đủ 3 từ gợi ý chưa?
                                         |
                                         +--- Vẫn chưa đủ ---> [ Bước 3: Lùi về UNIGRAM ]
                                                                 Lấy các hư từ nối câu phổ biến nhứt:
                                                                 "là", "có", "và", "được", "không", "nha"
```

Còn khi bạn đang gõ dở một từ (hoặc khi bộ gõ muốn khôi phục dấu cho một từ không dấu trong hàm `bestAccentedMatch` tại [dòng 1123–1151](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L1123-L1151)), điểm số tổng hợp (`score`) của mỗi ứng viên được tính bằng sự kết hợp của cả 3 nguồn sức mạnh:

$$\text{Score}(w) = \text{BaseUnigram}(w) + 18 \times \text{UserCount}(w) + 35 \times \text{ContextWeight}(w \mid w_{i-2}, w_{i-1})$$

Trong đó hàm `getContextualWeights(prevWord2, prevWord1)` ([dòng 1157–1185](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L1157-L1185)) thưởng điểm cực lớn cho những ứng viên khớp Trigram (`+count * 6` cho Trigram người dùng, `+(10 - idx)` cho Trigram dựng sẵn) và Bigram (`+count * 3` cho Bigram người dùng, `+(5 - idx)` cho Bigram dựng sẵn). Nhờ vậy, càng xài lâu, `vnchFlex` càng thuộc nằm lòng giọng văn riêng của bạn!

---

## CHƯƠNG 22: CƠ CHẾ TỰ HỌC KHI GÕ, BỘ LỌC BẢO MẬT THÔNG TIN NHẠY CẢM (PRIVACY GUARD) VÀ NGƯỠNG TỪ LẠ (OOV THRESHOLD)

Một bộ gõ biết tự học từ mới của người dùng (`Online N-gram Learning`) là rất quý, nhưng nếu học bừa bãi không có bộ lọc thì sẽ gây ra 2 tai hại lớn:
1. **Lộ thông tin nhạy cảm**: Hôm trước bạn gõ mật khẩu ngân hàng `MatKhau123@`, mã OTP `482910`, hoặc email cá nhân. Hôm sau bạn bè mượn điện thoại gõ chữ `Mat`, tự nhiên thanh gợi ý hiện chình ình chữ `MatKhau123@` lên!
2. **Học luôn cả lỗi gõ sai (Typo Pollution)**: Bạn định gõ chữ `"thương"` mà lỡ tay bấm nhầm thành chữ vô nghĩa `"thưongx"`. Nếu mới gõ 1 lần mà bộ gõ đã vội vàng nạp `"thưongx"` vô từ điển, thì những lần sau nó cứ đem cái chữ sai chánh tả đó ra gợi ý hoài!

Hãy mở [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L826-L869) coi hàm `learn(...)` của `vnchFlex` chặn đứng cả 2 nguy cơ này bằng **Bộ lọc Bảo mật (`Privacy Filter`)** và **Ngưỡng Từ lạ 2 lần (`OOV Threshold`)** như thế nào nha:

```kotlin
fun learn(word: String, prevWord1: String = "", prevWord2: String = "", forceImmediate: Boolean = false) {
    val w = word.trim().lowercase()
    if (w.length !in 2..20) return
    if (!w.all { it.isLetter() }) return
    writeLock.lock()
    try {
        val isAlreadyKnown = coreUnigramWeights.containsKey(w) ||
            englishUnigramWeights.containsKey(w) ||
            userUnigramCounts.containsKey(w)

        if (!isAlreadyKnown && !forceImmediate) {
            val seen = (pendingOovCounts[w] ?: 0) + 1
            pendingOovCounts[w] = seen
            if (seen < 2) return
            pendingOovCounts.remove(w)
        } else {
            pendingOovCounts.remove(w)
        }

        // 1. Learn Unigram
        userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 1
        dictStore?.addLearnedWord(w)
        if (trieBuilt) insertIntoTrie(w)

        // 2. Learn Bigram (prevWord1 -> w)
        val p1 = prevWord1.trim().lowercase()
        if (p1.isNotEmpty() && p1.all { it.isLetter() }) {
            val bMap = userBigrams.getOrPut(p1) { mutableMapOf() }
            bMap[w] = (bMap[w] ?: 0) + 1
            dictStore?.saveLearnedBigrams(userBigrams)

            // 3. Learn Trigram (prevWord2 + " " + prevWord1 -> w)
            val p2 = prevWord2.trim().lowercase()
            if (p2.isNotEmpty() && p2.all { it.isLetter() }) {
                val tKey = "$p2 $p1"
                val tMap = userTrigrams.getOrPut(tKey) { mutableMapOf() }
                tMap[w] = (tMap[w] ?: 0) + 1
                dictStore?.saveLearnedTrigrams(userTrigrams)
            }
        }
    } finally {
        writeLock.unlock()
    }
}
```

Bạn nhìn kỹ 3 chốt chặn thông minh trong đoạn code trên:
1. **Chốt chặn 1 (`Privacy Guard` — Dòng 828–829)**:
   - `if (w.length !in 2..20) return`
   - `if (!w.all { it.isLetter() }) return`
   Bất kỳ chuỗi nào có chứa dù chỉ **1 chữ số** (`0..9`) hoặc **1 ký tự đặc biệt** (`@, #, $, _, ., /...`) đều bị loại bỏ ngay ở cửa! Nhờ vậy, mã OTP, số điện thoại, số căn cước, mật khẩu có số/ký hiệu hay địa chỉ email không bao giờ lọt vô bộ nhớ học từ của bàn phím.
2. **Chốt chặn 2 (`OOV Threshold` — Phòng chờ từ lạ `pendingOovCounts`, Dòng 836–843)**:
   - `OOV` là viết tắt của *Out-Of-Vocabulary* (Từ mới chưa từng có trong từ điển).
   - Khi bạn gõ một từ mới lạ lần đầu tiên (`seen == 1`), bộ gõ **chưa tin ngay** đâu! Nó tạm ghi tên từ đó vô phòng chờ `pendingOovCounts[w] = 1` rồi thoát ra (`if (seen < 2) return`). Nếu đó chỉ là một lỗi gõ nhầm ngẫu nhiên, bạn sẽ không gõ lại cái lỗi đó nữa và từ sai không bao giờ làm bẩn từ điển.
   - Ngược lại, nếu đó thiệt sự là tên riêng của bạn bè, biệt danh, hay từ lóng mới mà bạn chủ động xài tới **lần thứ 2** (`seen >= 2`), hoặc khi bạn chủ động bấm chọn từ đó trên thanh gợi ý / bấm Backspace để giữ lại từ đó (`forceImmediate = true`), lúc đó bộ gõ mới chánh thức cấp hộ khẩu cho nó vô `userUnigramCounts` và trồng vô cây `Trie`!
3. **Chốt chặn 3 (`ReentrantReadWriteLock` — Khóa Đọc/Ghi đa luồng an toàn)**:
   - Vì việc học từ (`learn`) chạy trên một luồng chạy ngầm riêng (`learnExecutor` trong `VietKeyboardService`) để không làm khựng tay khi bạn đang gõ, còn việc đọc gợi ý lại chạy trên luồng giao diện chánh, `SuggestionEngine` dùng `readLock` và `writeLock` để nhiều luồng có thể cùng đọc từ điển một lúc, nhưng khi có luồng đang ghi từ mới thì khóa chặt lại không cho đụng độ bộ nhớ.

---

## CHƯƠNG 23: THUẬT TOÁN TỰ SỬA LỖI CHẠM LỆCH PHÍM LÂN CẬN (`findSpatialAutoCorrection`) VÀ LƯU TRỮ BẤT ĐỒNG BỘ (`PersonalDictionaryStore`)

### 23.1. Khi ngón tay cái bấm trật qua phím hàng xóm (`Spatial Touch Typo`)

Trên bàn phím điện thoại nhỏ xíu, phím chữ **`N`** nằm sát vách với phím **`B`**, **`H`**, **`J`**, **`M`**. Phím chữ **`G`** nằm sát vách với **`T`**, **`Y`**, **`H`**, **`B`**, **`V`**, **`F`**.
Đôi khi bạn muốn gõ câu: *"cảm ơn **bạn**"* nhưng ngón cái bấm lệch qua phải một chút trên chữ `'b'`, thành ra bạn gõ chữ **`"nạn"`** hoặc **`"vạn"`**? Không, `"nạn"` và `"vạn"` là từ có nghĩa trong từ điển nên máy không được tự ý sửa. Nhưng giả sử bạn bấm lệch phím chữ `'n'` ở cuối chữ `"bạn"` sang phím hàng xóm `'b'` hoặc `'j'` tạo thành một từ không tồn tại trong từ điển Tiếng Việt (ví dụ gõ `"khônh"` thay vì `"không"`, vì phím `'h'` nằm sát bên phím `'g'`!).

Hàm `findSpatialAutoCorrection` trong [`SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt#L877-L949) sẽ cứu nguy cho bạn ngay khi bạn vừa bấm phím Dấu cách (`Space`):

```kotlin
fun findSpatialAutoCorrection(
    typedWord: String,
    prevWord1: String = "",
    prevWord2: String = "",
    whitelist: Set<String> = emptySet()
): String? {
    val clean = typedWord.trim()
    if (clean.length !in 2..7 || !clean.all { it.isLetter() }) return null
    val lower = clean.lowercase()
    if (whitelist.contains(lower) || isKnownWord(lower)) return null

    val stripped = DiacriticUtils.strip(lower)
    if (!trieBuilt) ensureTrieBuilt()

    val contextWeights = getContextualWeights(prevWord2, prevWord1)
    val variants = generateNeighborVariants(stripped)
```

Quy trình 4 bước của thuật toán sửa lỗi chạm lệch phím:
1. **Tôn trọng từ đã biết**: Nếu từ bạn vừa gõ đã có trong `whitelist` của người dùng hoặc đã là một từ có nghĩa trong từ điển Tiếng Việt / Tiếng Anh (`isKnownWord(lower) == true`), hàm trả về `null` ngay (tuyệt đối không sửa bậy từ đúng thành từ khác!).
2. **Sinh biến thể hàng xóm (`generateNeighborVariants`)**:
   - Dựa vào bản đồ phím lân cận `GlideDecoder.KEY_NEIGHBORS` (ví dụ hàng xóm của `'h'` là `"yujnbg"`), hàm lần lượt thử thay thế từng chữ cái trong từ vừa gõ bằng các phím nằm sát vách với nó.
   - Ví dụ bạn gõ nhầm `"khonh"` (do bấm lệch `'g'` qua `'h'`), một trong các biến thể hàng xóm sinh ra sẽ là **`"khong"`**.
3. **Tra trên Cây tiền tố không dấu (`strippedTrieRoot`) và chấm điểm ngữ cảnh**:
   - Biến thể `"khong"` đi vào cây `Trie` tìm thấy ứng viên có dấu là **`"không"`**.
   - Điểm số được chấm: `score = baseW + userW + ctxW`.
   - Để tránh sửa nhầm những từ hiếm, thuật toán yêu cầu ngưỡng tự tin rất cao: ứng viên phải là từ phổ thông tần suất cao (`baseW >= 88`), hoặc khớp mạnh với ngữ cảnh câu đang viết (`ctxW >= 75`), hoặc là từ quen thuộc của người dùng (`userW >= 40`).
4. **Hoàn tác tức thì nếu người dùng không đồng ý**:
   - Như tụi mình đã học ở Chương 17, khi `VietKeyboardService` tự động sửa `"khonh "` thành `"không "`, nó lưu cặp `lastAutoCorrection = "khonh" to "không "`. Nếu bạn thật sự muốn viết chữ `"khonh"`, bạn chỉ cần bấm `Backspace` một cái là chữ `"khonh "` quay trở lại ngay lập tức và được học luôn vào từ điển (`forceImmediate = true`) để lần sau không sửa nữa!

### 23.2. Gom nhóm lưu trữ trễ 1.5 giây (`Debounced Persistence`) trong `PersonalDictionaryStore.kt`

Mỗi khi bạn gõ xong một từ, `SuggestionEngine.learn(...)` lại cập nhật bảng `userUnigramCounts`, `userBigrams`, `userTrigrams`.
Nếu cứ mỗi một từ bạn gõ, bộ gõ lại chuyển toàn bộ bảng Bigram và Trigram ra chuỗi JSON rồi ghi xuống bộ nhớ Flash của điện thoại, thì chuyện gì xảy ra? Một câu tin nhắn 15 từ sẽ bắt bộ nhớ Flash ghi đĩa 15 lần liên tiếp!

Hãy mở [`PersonalDictionaryStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/PersonalDictionaryStore.kt#L165-L200) để học kỹ thuật **Trì hoãn và Gom nhóm ghi đĩa (`Debounced Disk Flush`)** chuẩn công nghiệp:

```kotlin
private fun scheduleNgramSave() {
    if (ngramSavePending) return
    ngramSavePending = true
    handler.postDelayed(ngramSaveRunnable, 1500L)
}
```
- Khi dữ liệu trên RAM vừa thay đổi, `PersonalDictionaryStore` chỉ bật một lá cờ đánh dấu "dữ liệu đã thay đổi" (`learnedBigramsDirty = true`) và hẹn đồng hồ **1.500 mili-giây (1,5 giây)** sau mới chạy hàm `flushNgrams()`.
- Trong suốt 1,5 giây đó, dù bạn có gõ liên thanh thêm 10 từ nữa, biến `ngramSavePending` đang là `true` nên nó không hề ghi đĩa một lần nào cả — mọi thứ chỉ cập nhật trên bộ nhớ RAM nhanh tức thời.
- Đợi tới khi bạn ngừng tay nghỉ 1,5 giây (hoặc khi bàn phím đóng lại gọi `flushPendingSaves()`), hàm `flushNgrams()` mới gom toàn bộ kết quả học được của cả câu tin nhắn ghi xuống `SharedPreferences` trong **1 lần duy nhứt**!

---

# PHẦN VI: TRÍ TUỆ NHÂN TẠO & HÌNH HỌC — LƯỚT PHÍM (`GlideDecoder`) & SỬA CHÁNH TẢ ONNX (`OnnxVietnameseCorrector`)

---

## CHƯƠNG 24: GIẢI PHẪU THUẬT TOÁN LƯỚT PHÍM (`GlideDecoder`) — TỪ GOM CHỮ TRÙNG (`collapseRepeats`), KHOẢNG CÁCH LEVENSHTEIN TỚI QUY HOẠCH ĐỘNG DTW

Lướt phím (**Glide Typing** / **Swipe Typing**) là cách nhập liệu hiện đại cho phép bạn đặt ngón tay lên chữ cái đầu tiên của từ, lướt một đường liền mạch qua các chữ cái tiếp theo rồi nhấc tay lên ở chữ cái cuối cùng. Ví dụ: bạn đặt tay ở phím **`V`**, kéo qua **`I`**, kéo qua **`E`**, kéo tới **`T`** rồi nhấc tay lên — không cần gõ hai lần chữ `e`, cũng chẳng cần gõ phím dấu nặng `j`, mà chữ **`"việt "` (hoặc `"việc "`)** tự động hiện ra!

Làm sao [`GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt) giải được bài toán đó khi đường ngón tay lướt trên màn hình lúc nào cũng đi ngang qua hàng loạt phím thừa nằm giữa đường đi? Tụi mình cùng tháo từng lớp thuật toán trong `GlideDecoder.kt` ra coi nha!

### 24.1. Tại sao khi lướt chữ `"việt"`, chuỗi phím đi qua lại là `"vbuioert"`?

Bạn nhìn lên mặt bàn phím QWERTY:
- Phím **`V`** nằm ở hàng dưới cùng.
- Phím **`I`** nằm ở góc phải hàng trên cùng.
- Khi bạn kéo ngón tay từ `V` lên `I`, ngón tay của bạn bắt buộc phải đi cắt ngang qua các phím nằm ở giữa đường là **`B`**, **`H`**, **`U`**!
- Khi bạn kéo từ `I` qua `E`, ngón tay của bạn lại quét qua **`O`**, **`U`**, **`Y`**, **`T`**, **`R`**!
- Cuối cùng khi kéo từ `E` về `T`, ngón tay lại đi ngang qua **`R`**.
Như vậy, dù trong đầu bạn muốn lướt chữ **`"viet"`** (4 chữ cái), nhưng chuỗi phím thô mà mặt bàn phím ghi nhận được (`swipedKeys`) lại dài ngoằng: **`"vbhuioytert"`**!

Làm sao máy tính nhìn vào đống hỗn độn `"vbhuioytert"` mà tìm ra được chữ `"việt"`?

### 24.2. Năm vũ khí toán học kết hợp trong hàm `GlideDecoder.decode(...)`

Tụi mình đọc trực tiếp hàm `decode` tại [`GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L162-L216):

```kotlin
fun decode(
    swipedKeys: String,
    suggestionEngine: SuggestionEngine,
    prevWord1: String,
    prevWord2: String,
    limit: Int = 3,
    isVietnameseMode: Boolean = true,
    normalizedPoints: FloatArray? = null
): List<String> {
    val neuralSeq = if (normalizedPoints != null) predictNeuralSequence(normalizedPoints) else null
    val raw = collapseRepeats((neuralSeq ?: swipedKeys).lowercase())
    if (raw.length < 2) return emptyList()

    val swipePath = raw.mapNotNull { KEY_POSITIONS[it] }
    val firstChar = raw.first()
    val lastChar = raw.last()
    val vocab = suggestionEngine.getGlideVocabulary(isVietnameseMode)
    val contextWeights = suggestionEngine.getContextBoosts(prevWord2, prevWord1)

    val scored = mutableListOf<Pair<String, Int>>()
    for ((word, unigramWeight) in vocab) {
        val stripped = DiacriticUtils.strip(word).lowercase()
        if (stripped.length < 2) continue

        val collapsedTarget = collapseRepeats(stripped)
        val exactFirst = collapsedTarget.first() == firstChar
        val exactLast = collapsedTarget.last() == lastChar
        val nearFirst = exactFirst || isNeighbor(collapsedTarget.first(), firstChar)
        val nearLast = exactLast || isNeighbor(collapsedTarget.last(), lastChar)
        if (!nearFirst || !nearLast) continue

        val subseqMatched = isSubsequence(collapsedTarget, raw)
        val dist = levenshtein(collapsedTarget, raw)
        if (!subseqMatched && dist > (raw.length / 2).coerceAtLeast(2)) continue

        var score = unigramWeight
        if (exactFirst) score += 90 else score += 25
        if (exactLast) score += 90 else score += 25
        if (subseqMatched) score += 220
        if (dist == 0) score += 140
        val targetPath = collapsedTarget.mapNotNull { KEY_POSITIONS[it] }
        val dtw = dtwDistance(targetPath, swipePath)
        score -= (dtw * 180f).toInt()
        score -= dist * 45
        score -= kotlin.math.abs(collapsedTarget.length - raw.length) * 18
        score += (contextWeights[word] ?: 0) * 65
        scored.add(word to score)
    }

    return scored
        .sortedByDescending { it.second }
        .map { it.first }
        .distinct()
        .take(limit)
}
```

Tụi mình mổ xẻ 5 vũ khí toán học bên trong vòng lặp trên nha:

#### Vũ khí 1: Gom chữ cái trùng liền kề (`collapseRepeats` — Dòng 186 & 221–232)
Khi bạn lướt một từ Tiếng Anh có 2 chữ cái giống nhau đứng cạnh nhau như `"book"` hay `"letter"`, hoặc từ Tiếng Việt như `"xoong"`, ngón tay của bạn đâu thể nào nhấc lên ở chữ `'o'` thứ nhứt rồi chạm xuống chữ `'o'` thứ hai được (nhấc lên là kết thúc lướt phím mất rồi!). Ngón tay chỉ lướt qua phím `'o'` một lần duy nhứt, tạo ra đường lướt `"bok"` hoặc `"leter"`.
Vì vậy, hàm `collapseRepeats(s)` gom mọi chữ cái giống nhau đứng liền kề trong từ mục tiêu về 1 chữ (`"letter"` -> `"leter"`, `"book"` -> `"bok"`). Nhờ đó các từ có chữ kép không bao giờ bị phạt điểm khi lướt phím!

#### Vũ khí 2: Khoan dung ở điểm đặt tay và điểm nhấc tay (`nearFirst` & `nearLast` — Dòng 187–191)
Khi lướt phím nhanh, điểm quan trọng nhứt là **phím đầu tiên bạn đặt ngón tay xuống** (`firstChar`) và **phím cuối cùng bạn nhấc ngón tay lên** (`lastChar`).
- Tuy nhiên, đôi khi lúc nhấc tay ở chữ `'t'` của `"việt"`, ngón tay bạn hơi trượt nhẹ qua phím hàng xóm `'r'` hoặc `'y'`.
- Nếu bộ gõ cứng nhắc đòi `collapsedTarget.last() == lastChar`, bạn chỉ cần lệch 1 milimét ở cuối đường lướt là mất trắng từ đó!
- Bởi vậy, `vnchFlex` dùng điều kiện `nearFirst` và `nearLast`: Cho phép phím đầu và phím cuối của từ ứng viên **trùng khớp hoàn toàn** (được thưởng nóng `+90` điểm) HOẶC **là phím hàng xóm sát vách** (`isNeighbor`, được thưởng `+25` điểm). Những từ nào mà cả chữ đầu lẫn chữ cuối đều nằm xa lắc xa lơ so với điểm đặt tay và điểm nhấc tay sẽ bị loại ngay ở dòng 191 (`if (!nearFirst || !nearLast) continue`), giúp lọc bỏ 95% từ điển chỉ trong tích tắc!

#### Vũ khí 3: Kiểm tra Chuỗi con theo thứ tự (`isSubsequence` — Dòng 193 & 234–242)
Thế nào là Chuỗi con (`Subsequence`)?
Chuỗi `"viet"` là một **chuỗi con** của đường lướt `"vbhuioytert"` vì toàn bộ các chữ cái `'v' -> 'i' -> 'e' -> 't'` đều xuất hiện đầy đủ và **đúng theo thứ tự trước sau** bên trong `"vbhuioytert"`:
$$\underline{\mathbf{v}}\text{bhu}\underline{\mathbf{i}}\text{oyt}\underline{\mathbf{e}}\text{r}\underline{\mathbf{t}}$$
Hàm `isSubsequence(target, source)` chỉ dùng 2 con trỏ `ti` và `si` chạy một lượt từ trái qua phải. Nếu từ ứng viên chứa đầy đủ các đỉnh chữ cái nằm trên đường lướt (`subseqMatched == true`), nó được thưởng ngay **`+220` điểm**!

#### Vũ khí 4: Khoảng cách chỉnh sửa Levenshtein (`levenshtein` — Dòng 194 & 244–259)
Khoảng cách Levenshtein giữa hai chuỗi chữ `a` và `b` là **số thao tác ít nhứt** (thêm 1 chữ, xóa 1 chữ, hoặc thay 1 chữ) để biến chuỗi `a` thành chuỗi `b`.
Ví dụ giữa `"viet"` và `"vbuirt"` có khoảng cách Levenshtein là `3`. Mỗi đơn vị khoảng cách Levenshtein lớn hơn 0 sẽ bị trừ nhẹ `dist * 45` điểm để ưu tiên những đường lướt gọn gàng, ít đi vòng vèo.

#### Vũ khí 5: Quy hoạch động uốn cong thời gian (`Dynamic Time Warping — DTW` — Dòng 32–47 & 202–204)
Tại sao có `isSubsequence` rồi mà vẫn cần tới thuật toán hình học **DTW (`dtwDistance`)**?
Bạn thử nghĩ coi: Giả sử đường lướt của bạn đi qua `"v-b-h-u-i-o-e-r-t"`.
Trong từ điển có chữ **`"việt"`** (`v-i-e-t`) và chữ **`"vượt"`** (`v-u-o-t`). Cả `"viet"` lẫn `"vuot"` đều là chuỗi con (`isSubsequence == true`) của `"vbhuioert"`, đều bắt đầu bằng `'v'` và kết thúc bằng `'t'`, lại có cùng độ dài 4 chữ cái! Làm sao biết ngón tay bạn thực sự lướt chữ `"việt"` hay chữ `"vượt"`?
Câu trả lời nằm ở **hình dáng hình học 2D của quỹ đạo ngón tay**:
- Mỗi phím từ `'a'` đến `'z'` có một tọa độ tâm chuẩn hóa $(x, y) \in [0, 1] \times [0, 1]$ trong bản đồ `KEY_POSITIONS` ([dòng 23–30](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L23-L30)).
- Hàm `dtwDistance(pathA, pathB)` ([dòng 32–47](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L32-L47)) lập một bảng quy hoạch động hai chiều `dp[n + 1][m + 1]` để đo khoảng cách Euclid $\sqrt{\Delta x^2 + \Delta y^2}$ ngắn nhứt giữa đường gấp khúc lý tưởng của từ ứng viên (`targetPath`) và đường ngón tay thực tế của bạn (`swipePath`):
  $$\text{dp}[i][j] = \sqrt{(x_i^A - x_j^B)^2 + (y_i^A - y_j^B)^2} + \min\big(\text{dp}[i-1][j],\; \text{dp}[i][j-1],\; \text{dp}[i-1][j-1]\big)$$
- Đường gấp khúc nào bám sát quỹ đạo ngón tay của bạn hơn thì có khoảng cách `dtw` nhỏ hơn (bị trừ ít điểm `(dtw * 180f)` hơn) và chiến thắng!

---

## CHƯƠNG 25: MẠNG THẦN KINH LƯỚT PHÍM (`neural_glide.onnx`) — NỘI SUY 48 ĐIỂM ĐẲNG CỰ VÀ TRÍCH XUẤT VÉC-TƠ ĐỘNG HỌC 6 CHIỀU

Bên cạnh thuật toán hình học DTW, `GlideDecoder.kt` còn tích hợp một mô hình Trí tuệ Nhân tạo học sâu (**Deep Learning ONNX Runtime**) nằm trong tập tin `assets/neural_glide.onnx`, được gọi qua hàm `predictNeuralSequence(normalizedPoints: FloatArray)` ([dòng 78–160](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L78-L160)).

Dành cho các bạn mới bắt đầu muốn hiểu cách đưa một đường vẽ ngón tay vào mạng thần kinh nhân tạo (Neural Network):

### 25.1. Bước 1: Chia đều đường lướt thành đúng 48 điểm cách đều nhau (`Arc-Length Equidistant Resampling`)

Khi bạn lướt một từ ngắn hay lướt thật nhanh, màn hình cảm ứng chỉ gởi về khoảng `15` tọa độ $(x, y)$. Nhưng khi bạn lướt một từ dài hoặc lướt chậm, màn hình gởi về tới `90` tọa độ $(x, y)$.
Mạng thần kinh `neural_glide.onnx` thì đòi hỏi đầu vào phải có kích thước cố định là **`targetLen = 48` bước thời gian**.
Bởi vậy, đoạn code từ [dòng 84 đến 107](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L84-L107) thực hiện phép **Nội suy tuyến tính theo chiều dài cung (`Arc-Length Resampling`)**:
1. Tính tổng chiều dài quãng đường mà ngón tay đã đi qua:
   $$L_{\text{total}} = \sum_{i=1}^{N-1} \sqrt{(x_i - x_{i-1})^2 + (y_i - y_{i-1})^2}$$
2. Chia quãng đường $L_{\text{total}}$ đó thành `47` đoạn bằng nhau, tạo ra đúng `48` cột mốc $(rx_t, ry_t)$ với $t \in \{0, 1, \dots, 47\}$. Nhờ chia đều theo quãng đường hình học chớ không phải theo thời gian, dù bạn lướt nhanh hay lướt chậm, hình dáng 48 điểm thu được luôn luôn đồng nhứt!

### 25.2. Bước 2: Trích xuất 6 đặc trưng động học (`6D Kinematic Feature Vector`) cho mỗi điểm

Tại mỗi điểm $t$ trong 48 điểm đó, thay vì chỉ đưa tọa độ $(rx_t, ry_t)$ vào mô hình AI, tụi mình tính thêm vận tốc biến thiên $(\Delta x_t, \Delta y_t)$ và góc hướng đi $\theta_t = \text{atan2}(\Delta y_t, \Delta x_t)$ ([dòng 109–129](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L109-L129)):

$$\mathbf{f}_t = \big[\; rx_t,\; ry_t,\; \Delta x_t,\; \Delta y_t,\; \sin(\theta_t),\; \cos(\theta_t) \;\big] \in \mathbb{R}^6$$

Tại sao phải có $\sin(\theta_t)$ và $\cos(\theta_t)$? Vì nơi ngón tay bạn **đổi hướng đột ngột** (góc $\theta_t$ xoay ngoắt lại) chánh là nơi ngón tay bạn đang dừng ở **tâm của chữ cái mục tiêu** (như chữ `'i'` hay chữ `'e'` trong `"việt"`), còn nơi góc $\theta_t$ giữ nguyên thẳng băng chỉ là những phím vô tình bị lướt ngang qua!
Sau khi đưa khối Tensor kích thước `[1, 48, 6]` vào `session.run(...)`, bộ giải mã **CTC Greedy Decoder** ([dòng 139–157](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L139-L157)) sẽ lọc bỏ nhãn rỗng (`blank = 0`) và các nhãn trùng liên tiếp (`bestId != prevId`) để xuất ra chuỗi chữ cái cốt lõi mà người dùng muốn lướt.

---

## CHƯƠNG 26: LIÊN KẾT DẢI GỢI Ý BIẾN THỂ VÀ TỪ KẾ TIẾP NGAY SAU KHI LƯỚT PHÍM (`renderGlideCandidates`)

Một trải nghiệm cực kỳ tinh tế trên Gboard mà tụi mình đã mang trọn vẹn vào `vnchFlex` nằm ở hàm `renderGlideCandidates(candidates: List<String>)` và `replaceLastGlideWordOrAccept(word: String)` trong [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L175-L245):

### 26.1. Hai nhu cầu của người dùng ngay sau khi nhấc tay kết thúc một cú lướt phím

Giả sử bạn vừa lướt một đường từ `V -> I -> E -> T`.
Bộ giải mã `GlideDecoder` trả về 3 ứng viên sáng giá nhứt:
1. Ứng viên số 1 (`best`): **`"việt"`** (đã được tự động chèn ngay lên màn hình thành `"việt "` kèm theo một dấu cách ở cuối, và lưu `lastGlideCommittedLength = 5`).
2. Ứng viên số 2 & 3 (`variants` — Các biến thể gần giống của đường lướt đó): **`"viết"`**, **`"việc"`**.

Ngay lúc chữ `"việt "` vừa hiện lên màn hình, người dùng chỉ có 1 trong 2 nhu cầu sau đây:
- **Nhu cầu A**: *"Đúng chữ `"việt"` rồi! Giờ gợi ý giùm tui **từ tiếp theo** đi (ví dụ `"nam"` để tui bấm vô thành `"việt nam "` luôn cho lẹ)!"*
- **Nhu cầu B**: *"Ủa hổng phải, hồi nãy tui muốn lướt chữ **`"việc"`** (hoặc **`"viết"`**) cơ! Cho tui bấm vô chữ `"việc"` trên thanh gợi ý để **thay thế** chữ `"việt "` vừa ra đi!"*

### 26.2. Cách `renderGlideCandidates` kết hợp cả Biến thể (`isVariant = true`) lẫn Từ tiếp theo (`isVariant = false`) trên cùng một thanh gợi ý

Hãy coi đoạn code tại [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L178-L227) giải quyết đồng thời cả hai nhu cầu A và B khéo léo tới mức nào:

```kotlin
val best = candidates.firstOrNull().orEmpty()
val variants = candidates.drop(1).take(2)
val nextWords = if (prefs.nextWordSuggestions && best.isNotEmpty()) {
    suggestionEngine.suggestNextWord(prevWord1 = best, prevWord2 = prevCommittedWord2, limit = 3)
        .filter { nw -> !candidates.any { it.equals(nw, ignoreCase = true) } }
} else emptyList()

val displayItems = ArrayList<Pair<String, Boolean>>(3) // word to isVariant
for (v in variants) {
    if (displayItems.size < 2) displayItems.add(v to true)
}
for (nw in nextWords) {
    if (displayItems.size < 3) displayItems.add(nw to false)
}
```

Bạn nhìn cái danh sách `displayItems` gồm 3 ô trên thanh gợi ý kìa:
- Mỗi mục trong `displayItems` là một cặp `(Từ_gợi_ý, Cờ_isVariant)`:
  - Những từ lấy từ `variants` (như `"viết"`, `"việc"`) được gắn nhãn **`isVariant = true`**. Khi bạn chạm tay vô ô này, bộ gõ gọi hàm `replaceLastGlideWordOrAccept(cased)`: nó dùng `lastGlideCommittedLength` để **xóa chữ `"việt "` vừa lướt và thay thế bằng chữ `"việc "`**!
  - Những từ lấy từ `nextWords` (do `suggestionEngine.suggestNextWord(prevWord1 = "việt", ...)` sinh ra, ví dụ chữ **`"nam"`**) được gắn nhãn **`isVariant = false`**. Khi bạn chạm tay vô ô này, bộ gõ giữ nguyên chữ `"việt "` và **nối tiếp luôn chữ `"nam "`** vô sau thành **`"việt nam "`**!

Nhờ sự kết hợp nhịp nhàng đó, sau mỗi cú lướt phím bạn vừa có thể sửa nhanh sang biến thể khác, vừa có thể bấm chọn liên hoàn từ tiếp theo y hệt như trên Gboard!

---

## CHƯƠNG 27: MẠNG THẦN KINH SỬA LỖI DẤU THANH VÀ CHÁNH TẢ TIẾNG VIỆT 100% NGOẠI TUYẾN (`OnnxVietnameseCorrector`)

### 27.1. Khi nào người dùng cần tới AI sửa lỗi dấu thanh & chánh tả cả câu?

Khi bạn gõ nhanh một đoạn văn bản dài hoặc dán một đoạn tin nhắn không dấu như:
`"Hom nay troi dep qua minh di uong ca phe sua da Sai Gon nghen"`
Làm sao bộ gõ có thể tự động đọc hiểu ngữ cảnh của cả câu để điền đầy đủ dấu thanh và dấu mũ/móc thành:
`"Hôm nay trời đẹp quá mình đi uống cà phê sữa đá Sài Gòn nghen"`
mà **không cần gởi văn bản của bạn lên bất kỳ máy chủ Internet nào** (bảo mật tuyệt đối 100% offline ngay trên điện thoại)?

Đó chánh là nhiệm vụ của lớp [`OnnxVietnameseCorrector.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/OnnxVietnameseCorrector.kt) sử dụng mô hình mạng thần kinh hồi quy hai chiều (`BiGRU INT8`, tập tin `vi_corrector.onnx` và từ điển ký tự `vi_corrector_vocab.json`).

### 27.2. Giải phẫu thuật toán Khôi phục Dấu có Ràng buộc Chữ gốc (`Base-Character Constrained Decoding`)

Nhiều mô hình AI tạo sinh trên mạng khi sửa lỗi chánh tả hay bị tật "sáng tác thêm chữ" (ảo giác / Hallucination): câu gốc người ta có 10 chữ, nó tự ý đổi thành 12 chữ khác làm sai lệch ý người viết!
Trong `OnnxVietnameseCorrector.kt`, tụi mình thiết kế thuật toán **Giải mã có Ràng buộc Chữ cái Gốc (`Base-Character Constrained Decoding`)** ở [dòng 80–127](../app/src/main/java/com/vnchflex/vietkeyboard/engine/OnnxVietnameseCorrector.kt#L80-L127) để đảm bảo AI **chỉ được phép thêm/sửa dấu Tiếng Việt trên đúng chữ cái đó**, tuyệt đối không bao giờ làm thay đổi chữ cái gốc hay làm lệch độ dài câu:

```kotlin
for (t in 0 until seqLen) {
    val srcChar = trimmed[t]
    if (!srcChar.isLetter()) {
        sb.append(srcChar)
        continue
    }
    val srcBase = DiacriticUtils.strip(srcChar.toString()).lowercase()
    val scores = logits[0][t]
    var bestId = -1
    var bestScore = Float.NEGATIVE_INFINITY
    for (v in 2 until scores.size) { // Bỏ qua <PAD>=0 và <UNK>=1
        val candChar = id2Char.getOrNull(v) ?: continue
        if (candChar == '\u0000' || !candChar.isLetter()) continue
        val candBase = DiacriticUtils.strip(candChar.toString()).lowercase()
        if (candBase != srcBase) continue
        if (scores[v] > bestScore) {
            bestScore = scores[v]
            bestId = v
        }
    }
    val outChar = if (bestId >= 0) id2Char[bestId] else srcChar
    sb.append(
        if (srcChar.isUpperCase()) outChar.uppercaseChar() else outChar.lowercaseChar()
    )
}
```

Bạn nhìn dòng lệnh then chốt thứ 113 kìa:
```kotlin
val candBase = DiacriticUtils.strip(candChar.toString()).lowercase()
if (candBase != srcBase) continue
```
Giả sử tại vị trí $t$, chữ cái đầu vào của bạn là chữ `'o'` (hoặc `'ò'`, `'ố'`) trong chữ `"troi"`:
- Khi lột dấu ra, chữ gốc `srcBase` là **`"o"`**.
- Mạng thần kinh BiGRU nhìn toàn bộ các chữ đứng trước và đứng sau chữ `"troi"` trong câu (`"Hom nay [troi] dep qua"`), rồi chấm điểm xác suất (`logits[0][t]`) cho toàn bộ bảng chữ cái trong `vi_corrector_vocab.json`.
- Nhờ điều kiện `if (candBase != srcBase) continue`, bộ giải mã chỉ xét những ứng viên nào có cùng gốc `"o"` (tức là tập hợp `{o, ó, ò, ỏ, õ, ọ, ô, ố, ồ, ổ, ỗ, ộ, ơ, ớ, ờ, ở, ỡ, ợ}`).
- Trong ngữ cảnh `"Hôm nay ... đẹp quá"`, ứng viên **`'ờ'`** đạt điểm `scores[v]` cao nhứt -> Chữ `"troi"` được khôi phục chánh xác thành **`"trời"`**, đồng thời giữ nguyên định dạng viết HOA / viết thường của chữ gốc!

Ngoài ra, nếu đoạn văn bản dài hơn `128` ký tự, hàm `correctSentence(input)` ([dòng 60–78](../app/src/main/java/com/vnchflex/vietkeyboard/engine/OnnxVietnameseCorrector.kt#L60-L78)) sẽ tự động cắt đoạn văn thành từng khúc nhỏ tại đúng ranh giới dấu chấm câu hoặc khoảng trắng (`\n, ., !, ?, ' '`) để không bao giờ cắt ngang lưng chừng một từ!

---

## CHƯƠNG 28: DỊCH VỤ KIỂM TRA CHÁNH TẢ HỆ THỐNG (`VnchSpellCheckerService`) VÀ CHUẨN HÓA VĂN BẢN (`VietnameseTextNormalizer`)

Để khép lại Phần VI, tụi mình cùng điểm qua hai mảnh ghép hoàn thiện hệ sinh thái xử lý ngôn ngữ của `vnchFlex`:

1. **`VietnameseTextNormalizer.kt` ([`engine/VietnameseTextNormalizer.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseTextNormalizer.kt))**:
   - Đảm nhận việc dọn dẹp khoảng trắng thừa, chuẩn hóa bảng mã Unicode về chuẩn dựng sẵn NFC, và chuyển đổi qua lại nhất quán giữa hai quy tắc đặt dấu thanh (`òa, òe, úy` cổ điển và `oà, oè, uý` kiểu mới) trên toàn bộ đoạn văn bản khi người dùng sử dụng nút Công cụ AI (`AI Tools`) trên thanh công cụ bàn phím.
2. **`VnchSpellCheckerService.kt` ([`ime/VnchSpellCheckerService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VnchSpellCheckerService.kt))**:
   - Đây là dịch vụ kết nối trực tiếp với trình Kiểm tra chánh tả của hệ điều hành Android (`SpellCheckerService`).
   - Khi một ứng dụng soạn thảo văn bản hỏi hệ điều hành: *"Từ này viết có đúng chánh tả Tiếng Việt không? Nếu sai thì gợi ý từ sửa lại là gì?"*, `VnchSpellCheckerService` sẽ sử dụng `SuggestionEngine` và `OnnxVietnameseCorrector` của `vnchFlex` để kiểm tra và trả về danh sách các từ Tiếng Việt đúng chánh tả tương ứng.

---
*(Hết Phần V và Phần VI — Tiếp theo là Phần VII: Bảng tra cứu 20 kịch bản gõ phím hóc búa, Bài tập thực hành tự tay thêm tính năng mới và Phụ lục toàn tập)*

# PHẦN VII: CẨM NANG THỰC HÀNH, GỠ LỖI (DEBUGGING) VÀ BÀI TẬP TỰ RÈN LUYỆN

---

## CHƯƠNG 29: BẢNG TRA CỨU NHANH 20 KỊCH BẢN GÕ PHÍM HÓC BÚA NHỨT VÀ CÁCH VNCHFLEX XỬ LÝ

Khi bạn tự tay kiểm thử (test) hoặc nâng cấp một bộ gõ Tiếng Việt, có **20 kịch bản kinh điển** mà các bộ gõ thông thường rất dễ bị sập bẫy. Tụi mình gom hết 20 kịch bản này vô một bảng tổng hợp kèm theo vị trí hàm xử lý trong `vnchFlex` để bạn tiện tra cứu bất cứ lúc nào:

| STT | Chuỗi thao tác của người dùng | Kết quả mong đợi chuẩn `vnchKey` / `vnchFlex` | Cơ chế & Tập tin xử lý trong `vnchFlex` |
| :---: | :--- | :---: | :--- |
| **1** | Gõ `v-i-e-e-t-j` (Telex truyền thống) | **`việt`** | Nhánh 4 (`ee -> ê`) + Nhánh 7 (`j -> Tone.DOT`) + `applyTone()` trong [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt). |
| **2** | Gõ `v-i-e-t-e-j` (Bỏ dấu mũ & dấu thanh tự do ở cuối từ) | **`việt`** | Nhánh 5 (`freeTonePlacement`): tìm thấy `'e'` trước đó, phần giữa `between = "t"` thuộc `validCoda` nên đội nón thành `"viêt"`, rồi `'j'` gắn dấu nặng thành `"việt"`. |
| **3** | Gõ `t-r-u-o-n-g-w-r` (1 phím `w` móc cả cụm `uo` ở cuối từ) | **`trưởng`** | Nhánh 6 (`lower == 'w'`): quét ngược gặp `'o'` có `'u'` đứng trước (không phải sau `'q'`), móc đồng thời cả hai thành `"ươ"`, rồi `'r'` đặt dấu hỏi lên `'ơ'`. |
| **4** | Gõ `t-r-u-w-o-n-g-r` (Chỉ gõ `w` sau `u`, không gõ `w` sau `o`) | **`trưởng`** | Bước đồng bộ cuối hàm `transformTelexCore` ([dòng 509–515](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L509-L515)): thấy cụm `"ưo"` có phụ âm cuối `'n', 'g'` theo sau nên tự động đồng bộ thành `"ươ"`. |
| **5** | Gõ `d-z-i-a-f`, `d-z-a-c-h-s`, `d-z-o-o`, `d-z-a-a-y-j` (Phương ngữ Nam Bộ) | **`dzìa`**, **`dzách`**, **`dzô`**, **`dzậy`** | Cờ `isDzOnset = lowerRaw.startsWith("dz")` giữ nguyên cụm phụ âm đầu `"dz"` không bị biến thành `'đ'`, phần vần phía sau ghép dấu bình thường. |
| **6** | Gõ `w-a-s`, `w-a-a-y-j`, `z-u-i`, `f-a-i-r` (Phụ âm mở rộng Miền Nam / Gen-Z) | **`wá`**, **`wậy`**, **`zui`**, **`fải`** | Nhánh `southernDialectAndGenZ` ([dòng 197–217](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L197-L217)) bảo vệ từ Tiếng Anh thông dụng (`water, free, zero, just`) nhưng cho phép bỏ dấu các từ âm tiết Tiếng Việt. |
| **7** | Gõ `t-e-x-t`, `n-e-x-t`, `f-l-e-x`, `m-o-r-e`, `u-s-e-r` trong chế độ Tiếng Việt | **`text`**, **`next`**, **`flex`**, **`more`**, **`user`** | Tập hợp `ENGLISH_AUTO_RESTORE_WORDS` ([dòng 625–647](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L625-L647)) tự động nhận diện từ Tiếng Anh nguyên vẹn mà không biến thành `"tẽt"`, `"flẽ"`, `"mỏe"`. |
| **8** | Gõ `t-e-l-e` hoặc `t-e-l-e-e` | **`tele`** | Cổng gác `validCoda` thấy giữa hai chữ `'e'` là phụ âm `"l"` (không phải âm cuối Tiếng Việt) nên không bỏ dấu mũ bậy; nếu đã đội mũ thì chữ `'e'` lặp lại sẽ hạ mũ và hoàn nguyên. |
| **9** | Gõ `V-i-e-t-n-a-m-e`, `V-i-e-t-n-a-m-e-e`, `V-i-e-t-n-a-m-e-s` | **`Vietname`**, **`Vietnamee`**, **`Vietnames`** | Phần chữ nằm giữa `between = "tnam"` bị cổng `validCoda` chặn lại, và điều kiện `result.none { it == lower }` ngăn hai chữ `'e'` ở đuôi một từ dài biến thành `'ê'`. |
| **10** | Gõ `c-a-n-h-f` (`cành`), rồi gõ bồi thêm `f` lần nữa (`canhff`) | **`canhf`** | Nhánh 7 ([dòng 467–472](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L467-L472)): thấy `tone == newTone` (`GRAVE == GRAVE`), lập tức hủy dấu về `Tone.NONE`, bật `toneEscapeActive = true` và xuất chữ `'f'` thường. |
| **11** | Gõ `h-o-a-f`, `k-h-o-e-r`, `t-h-u-y-r` ở cài đặt mặc định | **`hòa`**, **`khỏe`**, **`thủy`** | Mặc định `modernOrthography = false` trong `KeyboardPreferences.kt` đặt dấu thanh theo chuẩn chánh tả cổ điển ở nguyên âm đầu cho các vần mở `oa, oe, uy`. |
| **12** | Gõ `h-o-a-f-n` (Bỏ dấu huyền trước khi gõ phụ âm cuối `'n'`) | **`hoàn`** | Kiến trúc Pure-Rebuild chạy lại `applyTone()` khi có thêm `'n'`: thấy `hasCoda = true` nên tự động dời dấu huyền từ `'ò'` (`hòa`) sang `'à'` (`hoàn`). |
| **13** | Gõ `v-n` + `Space` so với gõ `g-o-o-g-l-e-.-v-n` + `Space` | **`Việt Nam `** so với **`google.vn `** | `MacroEngine.tryExpand()` kiểm tra ký tự đứng trước token và các dấu phân cách kỹ thuật để không bung gõ tắt trong tên miền, email, đường dẫn. |
| **14** | Gõ `k-o` + `Space` -> bung thành `"không "`, rồi bấm `Backspace` 2 lần nhanh | **`ko `** | Cơ chế `Double-Backspace Undo` trong `VietKeyboardService.onBackspace()` ([dòng 993–1014](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L993-L1014)) kiểm tra khoảng cách 2 lần bấm `< 600ms` và khôi phục lại từ gốc. |
| **15** | Bấm **Select All (Chọn tất cả)** trên đoạn văn bản rồi bấm **`Backspace`** | **Xóa sạch 100% văn bản đang bôi đen** | Tầng ưu tiên 1 của `onBackspace()` kiểm tra `!ic.getSelectedText(0).isNullOrEmpty()`, reset các bộ đệm và gọi `ic.commitText("", 1)` thay vì `deleteSurroundingText`. |
| **16** | Lướt phím (Glide) từ `V -> I -> E -> T` rồi bấm `Backspace` 1 lần | **Xóa trọn cả từ `"việt "` vừa lướt** | Biến `lastGlideCommittedLength` ghi nhớ độ dài từ vừa lướt kèm dấu cách; 1 nhát `Backspace` ngay sau khi lướt sẽ xóa nguyên cụm `lastGlideCommittedLength` ký tự. |
| **17** | Lướt phím ra chữ `"việt "`, nhìn lên thanh gợi ý thấy cả biến thể lẫn từ kế tiếp | Ô biến thể (`"việc"`) thay thế từ vừa lướt; ô từ kế (`"nam"`) nối tiếp thành **`"việt nam "`** | Hàm `renderGlideCandidates()` gắn cờ `isVariant = true/false` cho từng ô trên thanh gợi ý, phân nhánh giữa `replaceLastGlideWordOrAccept()` và `acceptSuggestion()`. |
| **18** | Đổi chiều cao bàn phím trong Cài đặt từ `Bình thường` sang `Rất cao` | **Bàn phím cao lên ngay lập tức** (`1.20x`) | Hàm `KeyboardCanvasView.syncStateAndInvalidate()` cập nhật trực tiếp `layoutParams.height = targetH`, chia lại `layoutKeyRects()` rồi gọi `requestLayout()`. |
| **19** | Gõ nhanh bằng 2 ngón tay cái (ngón trái chưa nhấc khỏi phím `T`, ngón phải đã chạm phím `H`) | **`th`** (Đúng thứ tự, không nuốt chữ, không nhảy lướt phím) | Máy trạng thái `SparseArray<PointerTracker>` chốt chữ ngay tại `ACTION_DOWN` / `ACTION_POINTER_DOWN` và ngắt lướt phím ngay khi có ngón thứ 2 chạm xuống. |
| **20** | Nhắn tin tốc độ cao trong ứng dụng **Telegram** | **Chữ hiện êm ru, không nhấp nháy gạch chân hay nhảy con trỏ** | Hàm `isTelegramPackage()` tự động tắt `useComposingSpan = false` và chuyển qua thuật toán `Common Prefix Diffing` trong `replaceComposingDirect()`. |

---

## CHƯƠNG 30: HƯỚNG DẪN TỰ THÊM TÍNH NĂNG MỚI CHO NGƯỜI MỚI BẮT ĐẦU (KÈM LỜI GIẢI CHI TIẾT)

Đọc sách lý thuyết xong mà được tự tay "độ" thêm tính năng mới vô bộ gõ của mình thì mới thiệt sự là làm chủ cuốn sách!
Trong chương này, tụi mình sẽ cùng nhau giải quyết **3 bài tập thực hành từ dễ tới nâng cao**, có chỉ rõ cần mở tập tin nào, dòng số mấy, và viết thêm đoạn code gì.

---

### 30.1. Bài tập thực hành số 1 (Mức độ Dễ): Thêm biến động `{clipboard}` và `{year}` vô Bộ máy Gõ tắt (`MacroEngine`)

- **Đề bài**: Hiện tại trong [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L83-L88), người dùng có thể tạo từ gõ tắt chứa `{date}` (ngày tháng năm) và `{time}` (giờ phút giây). Bây giờ bạn muốn thêm biến `{year}` để tự điền năm hiện tại (ví dụ `"2026"`), và biến `{day}` để điền thứ trong tuần bằng Tiếng Việt (ví dụ `"Chủ Nhật"`, `"Thứ Hai"`...).
- **Cách làm từng bước**:
  1. Bạn mở tập tin [`app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt) ở dòng 83.
  2. Tìm hàm `resolveVariables(text: String): String`:
     ```kotlin
     private fun resolveVariables(text: String): String {
         val now = java.time.ZonedDateTime.now()
         return text
             .replace("{date}", now.toLocalDate().toString())
             .replace("{time}", now.toLocalTime().withNano(0).toString())
     }
     ```
  3. Bạn sửa lại hàm đó thêm `{year}` và `{day}` như sau:
     ```kotlin
     private fun resolveVariables(text: String): String {
         val now = java.time.ZonedDateTime.now()
         val vietnameseDay = when (now.dayOfWeek) {
             java.time.DayOfWeek.MONDAY -> "Thứ Hai"
             java.time.DayOfWeek.TUESDAY -> "Thứ Ba"
             java.time.DayOfWeek.WEDNESDAY -> "Thứ Tư"
             java.time.DayOfWeek.THURSDAY -> "Thứ Năm"
             java.time.DayOfWeek.FRIDAY -> "Thứ Sáu"
             java.time.DayOfWeek.SATURDAY -> "Thứ Bảy"
             java.time.DayOfWeek.SUNDAY -> "Chủ Nhật"
         }
         return text
             .replace("{date}", now.toLocalDate().toString())
             .replace("{time}", now.toLocalTime().withNano(0).toString())
             .replace("{year}", now.year.toString())
             .replace("{day}", vietnameseDay)
     }
     ```
  4. **Thành quả**: Từ nay trong bảng Gõ tắt, bạn tạo một phím tắt `hnay` -> `"Hôm nay là {day}, ngày {date}"`. Mỗi lần bạn gõ `hnay` rồi bấm dấu cách, bộ gõ sẽ tự động bung ra: **`"Hôm nay là Chủ Nhật, ngày 2026-11-28 "`**. Dễ ợt phải hông bạn!

---

### 30.2. Bài tập thực hành số 2 (Mức độ Trung bình): Thêm các từ phương ngữ Nam Bộ mới vô Từ điển Gợi ý và Lướt phím

- **Đề bài**: Bạn muốn dạy sẵn cho bộ gõ `vnchFlex` biết thêm một loạt từ đặc trưng của Người Miền Nam mình (ví dụ: `"bển"`, `"trển"`, `"ngoải"`, `"trỏng"`, `"hồi nãy"`, `"mơi"`, `"chớ"`, `"nghen"`, `"dễ ợt"`) để khi người dùng lướt phím hoặc mới gõ chữ cái đầu là nó ưu tiên gợi ý ngay lập tức, kèm theo gợi ý từ đi đôi (Bigram) như gõ `"dễ"` thì gợi ý `"ợt"`, gõ `"hồi"` thì gợi ý `"nãy"`.
- **Cách làm từng bước**:
  1. Bạn mở tập tin [`app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt).
  2. Ở dòng 53–57 (trong bảng `highFreq` của `coreUnigramWeights`), bạn thêm điểm tần suất Unigram cho các từ Miền Nam này:
     ```kotlin
     // Từ phương ngữ Nam Bộ & Gen-Z (vnchKey)
     "dzìa" to 88, "dzách" to 87, "dzô" to 89, "dzậy" to 88, "dzọt" to 85, "dzui" to 87,
     "dzữ" to 85, "dza" to 82, "zậy" to 86, "zui" to 86, "wá" to 85, "fải" to 84,
     "bá" to 85, "cháy" to 84, "thiệt" to 88, "hông" to 88, "hen" to 85, "nghen" to 85,
     "chớ" to 89, "bển" to 84, "trển" to 84, "ngoải" to 84, "trỏng" to 84, "ợt" to 85,
     ```
  3. Tiếp theo, bạn kéo xuống bảng `bundledBigrams` trong cùng tập tin `SuggestionEngine.kt`, thêm các cặp từ đi đôi của Người Miền Nam:
     ```kotlin
     "dễ" to listOf("ợt", "thương", "dàng", "chịu"),
     "hồi" to listOf("nãy", "đó", "xưa", "hộp"),
     "bá" to listOf("dzách", "cháy"),
     ```
  4. **Thành quả**: Ngay khi người dùng gõ chữ `"dễ "` rồi bấm dấu cách, ô gợi ý đầu tiên sẽ hiện ngay chữ **`"ợt"`**! Và khi lướt ngón tay qua `C -> H -> O`, chữ **`"chớ"`** sẽ xuất hiện ngay trên thanh gợi ý.

---

### 30.3. Bài tập thực hành số 3 (Mức độ Nâng cao): Thêm tùy chọn Giao diện mới (Ví dụ: Giao diện "Xanh Lá Miền Tây") lên `KeyboardCanvasView`

- **Đề bài**: Hiện tại bàn phím có giao diện Sáng (`gboard_light`) và Tối (`gboard_dark`). Bạn muốn tự thiết kế thêm một bộ màu thứ ba tươi mát đặt tên là `"mien_tay_green"` (nền bàn phím xanh lá nhạt dịu mắt, phím Enter màu xanh ngọc lục bảo).
- **Cách làm từng bước**:
  1. Mở [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt), thêm một biến cấu hình `var themeMode: String = "gboard_light"`.
  2. Trong hàm `onDraw(canvas: Canvas)` ([dòng 337–347](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L337-L347)), bạn chọn màu cọ vẽ theo `themeMode`:
     ```kotlin
     val isMienTay = (themeMode == "mien_tay_green")
     val keyColor = when {
         isMienTay -> Color.parseColor("#FFFFFF")
         isDarkTheme -> context.getColor(R.color.key_bg_dark)
         else -> context.getColor(R.color.key_bg)
     }
     val funcColor = when {
         isMienTay -> Color.parseColor("#C8E6C9") // Xanh lá mạ dịu cho phím chức năng
         isDarkTheme -> context.getColor(R.color.key_func_bg_dark)
         else -> context.getColor(R.color.key_func_bg)
     }
     val enterColor = if (isMienTay) Color.parseColor("#2E7D32") else Color.parseColor("#FF1A73E8")
     ```
  3. Tại [`VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt#L324) trong hàm `applyPreferencesToView()`, bạn truyền `cv.themeMode = prefs.themeMode` và đặt màu nền `root.setBackgroundColor(...)` là `#E8F5E9` khi `prefs.themeMode == "mien_tay_green"`.
  4. Chỉ cần 3 bước ngắn gọn vậy thôi, không phải sửa bất kỳ một tập tin XML phức tạp nào, toàn bộ 44 phím trên tấm toan `KeyboardCanvasView` sẽ khoác lên mình tấm áo mới ở tốc độ 120FPS!

---

## PHỤ LỤC A: SƠ ĐỒ TOÀN BỘ 18 TẬP TIN MÃ NGUỒN TRONG DỰ ÁN VNCHFLEX

Để bạn có cái nhìn toàn cảnh mỗi khi mở dự án trên Android Studio hoặc trình soạn thảo mã nguồn, dưới đây là danh mục đầy đủ từng tập tin Kotlin trong `app/src/main/java/com/vnchflex/vietkeyboard/` cùng quy mô và chức năng cụ thể:

1. **[`engine/VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt)** *(705 dòng)*: Trái tim của bộ gõ Tiếng Việt Telex bỏ dấu tự do theo phong cách `vnchKey`. Chứa kiến trúc Pure-Rebuild, hỗ trợ phương ngữ Nam Bộ (`dz-`, `z-`, `f-`, `j-`, `w-`), tự động khôi phục từ Tiếng Anh, cổng gác âm cuối Tiếng Việt (`validCoda`), hoàn nguyên dấu khi lặp phím (`telee -> tele`), đặt dấu thanh cổ điển/kiểu mới, phục hồi từ đã chốt (`rehydrateWord`) và chế độ tối ưu riêng cho Telegram (`Common Prefix Diffing`).
2. **[`engine/DiacriticUtils.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/DiacriticUtils.kt)** *(25 dòng)*: Hộp công cụ lột sạch dấu Tiếng Việt siêu tốc bằng chuẩn phân rã Unicode NFD kết hợp thay thế `đ/Đ -> d/D`.
3. **[`engine/OnnxVietnameseCorrector.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/OnnxVietnameseCorrector.kt)** *(155 dòng)*: Bộ máy Trí tuệ Nhân tạo sửa lỗi dấu thanh và chánh tả Tiếng Việt 100% ngoại tuyến chạy trên ONNX Runtime Android (`vi_corrector.onnx`), sử dụng thuật toán giải mã có ràng buộc chữ cái gốc (`Base-Character Constrained Decoding`).
4. **[`engine/VietnameseTextNormalizer.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseTextNormalizer.kt)**: Bộ chuẩn hóa bảng mã Unicode NFC, dọn khoảng trắng thừa và đồng bộ quy tắc đặt dấu thanh cho cả đoạn văn.
5. **[`macro/Macro.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/Macro.kt)**: Lớp dữ liệu định nghĩa một mục Gõ tắt (`shortcut`, `expansion`, `enabled`, `caseSensitive`).
6. **[`macro/MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt)** *(95 dòng)*: Bộ máy bung Gõ tắt thông minh với khả năng tự thích ứng chữ HOA/thường (`adaptCase`), biến động ngày giờ (`{date}`, `{time}`), chặn bung nhầm trong tên miền/email (`isTechnicalDelimiter`) và lưu lịch sử cho phép hoàn tác bằng hai lần nhấn phím xóa (`lastExpansion`).
7. **[`macro/MacroStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroStore.kt)**: Lưu trữ, nạp, xuất/nhập (Export/Import JSON) danh sách Gõ tắt của người dùng trên `SharedPreferences`.
8. **[`ime/KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt)** *(912 dòng)*: Bộ dựng đồ họa bàn phím 120FPS trên một tấm toan duy nhứt (`Single-Canvas Renderer`) và bộ điều phối đa điểm chạm (`SparseArray<PointerTracker>`). Xử lý 3 trang bàn phím (`LETTERS`, `SYMBOLS1`, `SYMBOLS2`), co giãn chiều cao linh hoạt (`keyHeightScale`), vẽ vệt lướt phím, bong bóng phóng to chữ, dải ký tự phụ, trượt Spacebar dời con trỏ và trượt Backspace tô đen xóa từng cụm từ.
9. **[`ime/VietKeyboardService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VietKeyboardService.kt)** *(1.901 dòng)*: Dịch vụ bàn phím trung tâm (`InputMethodService`) kết nối toàn bộ các bộ máy với hệ điều hành Android qua `InputConnection`. Điều phối cơ chế `Commit-on-Down & Cancel-on-Action`, 6 tầng ưu tiên của phím `Backspace` (bao gồm xóa vùng chọn `Select All`), gợi ý biến thể và từ kế tiếp sau khi lướt phím (`renderGlideCandidates`), phản hồi âm thanh và rung xúc giác.
10. **[`ime/GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt)** *(260 dòng)*: Bộ giải mã Lướt phím lai ghép giữa Mạng thần kinh học sâu (`neural_glide.onnx`, nội suy 48 điểm đẳng cự + 6 đặc trưng động học + giải mã CTC) và thuật toán Hình học DTW (`dtwDistance`), gom chữ trùng (`collapseRepeats`), chuỗi con (`isSubsequence`), khoảng cách Levenshtein cùng điểm thưởng ngữ cảnh Trigram.
11. **[`ime/ClipboardStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardStore.kt)** *(106 dòng)*: Kho lưu trữ lịch sử bộ nhớ tạm (tối đa 20 mục), hỗ trợ ghim văn bản vĩnh viễn (`isPinned`) và tự động dọn dẹp các mục chưa ghim sau 1 giờ (`UNPINNED_TTL_MS`).
12. **[`ime/ClipboardPanelController.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardPanelController.kt)**: Bộ điều khiển giao diện bảng quản lý Bộ nhớ tạm ngay bên trong khung bàn phím (cho phép dán nhanh, ghim, bỏ ghim, xóa từng mục).
13. **[`ime/VnchSpellCheckerService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VnchSpellCheckerService.kt)**: Dịch vụ kiểm tra chánh tả cấp hệ thống của Android (`SpellCheckerService`).
14. **[`ime/VoiceInputActivity.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VoiceInputActivity.kt)**: Cầu nối kích hoạt tính năng Nhập liệu bằng giọng nói Tiếng Việt.
15. **[`suggestion/SuggestionEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/SuggestionEngine.kt)** *(1.277 dòng)*: Bộ máy tiên đoán từ và tự học mô hình ngôn ngữ N-gram. Chứa Cây tiền tố không dấu (`strippedTrieRoot`), thuật toán lùi bậc Katz Backoff (`Trigram -> Bigram -> Unigram`), bộ lọc bảo mật thông tin nhạy cảm (`Privacy Guard`), ngưỡng từ lạ 2 lần (`OOV Threshold`), sửa lỗi chạm lệch phím hàng xóm (`findSpatialAutoCorrection`) và học nhanh từ văn bản mẫu (`learnFromCorpus`).
16. **[`suggestion/PersonalDictionaryStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/suggestion/PersonalDictionaryStore.kt)** *(336 dòng)*: Bộ lưu trữ đệm trên RAM và gom nhóm ghi đĩa trễ 1,5 giây (`Debounced Persistence`) cho từ điển cá nhân cùng lịch sử học Unigram, Bigram, Trigram; hỗ trợ sao lưu và khôi phục toàn bộ dữ liệu học được ra tập tin JSON.
17. **[`settings/KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt)** *(234 dòng)*: Lớp quản lý tập trung toàn bộ 34 tùy chọn của bộ gõ (mặc định biểu tượng phím chuyển ngôn ngữ là `VnCharKey` `"vncharkey_logo"` và cách bỏ dấu cổ điển `modernOrthography = false`).
18. **[`settings/SettingsActivity.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/SettingsActivity.kt)**: Giao diện màn hình Cài đặt trực quan của ứng dụng `vnchFlex`, nơi người dùng bật bàn phím, tùy chỉnh chiều cao, giao diện, quản lý bảng Gõ tắt, từ điển cá nhân, danh sách từ Tiếng Anh ngoại lệ (Whitelist) và nạp văn bản mẫu cho AI học.

---

## PHỤ LỤC B: BẢNG THUẬT NGỮ KỸ THUẬT ANH – VIỆT GIẢI THÍCH THEO PHONG CÁCH BÌNH DÂN MIỀN NAM

Khi đọc tài liệu kỹ thuật bằng Tiếng Anh của Google Android hay các bài báo về Xử lý Ngôn ngữ Tự nhiên (NLP), bạn sẽ hay đụng mấy từ chuyên môn dưới đây. Tụi mình dịch nghĩa sát sườn theo kiểu bình dân dễ nhớ nhứt để bạn khỏi bỡ ngỡ:

- **IME (`Input Method Editor` / `InputMethodService`)**: Dịch vụ Bộ gõ bàn phím chạy ngầm trong hệ điều hành Android.
- **IPC (`Inter-Process Communication`)**: Đường ống nói chuyện qua lại giữa hai ứng dụng khác nhau (ví dụ giữa bàn phím `vnchFlex` và ứng dụng nhắn tin Zalo).
- **`InputConnection`**: Cây cầu nối cụ thể để bàn phím gởi chữ sang ô nhập liệu của ứng dụng đang mở.
- **`Composing Text` / `Composing Span`**: Chữ đang trong quá trình gõ và ghép dấu dở dang (thường có đường gạch chân mờ ở dưới), chưa chốt hẳn.
- **`Commit Text`**: Hành động "chốt sổ" — gỡ bỏ gạch chân tạm thời và ghi thẳng chữ đó vào ô văn bản chánh thức.
- **`Pure-Rebuild Architecture`**: Kiến trúc "Tái tạo thuần khiết" — giữ nguyên lịch sử các phím thô đã bấm từ đầu âm tiết và tính toán lại từ đầu mỗi khi có phím mới, giúp việc bỏ dấu tự do hay hoàn nguyên dấu không bao giờ bị rối.
- **`Onset` / `Nucleus` / `Coda`**: Ba phần của một tiếng (âm tiết): **Phụ âm đầu** (`Onset`, như *tr-*), **Âm chính / Nguyên âm** (`Nucleus`, như *-ươ-*), và **Phụ âm cuối** (`Coda`, như *-ng* trong chữ *trưởng*).
- **`Orthography` (Chánh tả)**: Quy tắc viết chữ và đặt dấu thanh đúng chuẩn mực của một ngôn ngữ (như chánh tả cổ điển `hòa, khỏe, thủy` hay chánh tả kiểu mới `hoà, khoẻ, thuỷ`).
- **`NFC` và `NFD`**: Hai chuẩn biểu diễn chữ có dấu trong bảng mã Unicode. `NFC` đúc sẵn chữ và dấu thành 1 ký tự duy nhứt để hiển thị; `NFD` tháo rời chữ cái gốc và các dấu thanh ra từng mảnh để dễ bề lột dấu.
- **`Single-Canvas View`**: Kỹ thuật vẽ toàn bộ 44 phím của bàn phím lên một tấm bảng phẳng duy nhứt thay vì tạo 44 nút bấm XML riêng lẻ, giúp bàn phím đạt tốc độ 120 khung hình/giây.
- **`Two-Thumb Rollover`**: Hiện tượng gõ nhanh bằng hai ngón tay cái khiến ngón sau đã chạm xuống phím mới trong khi ngón trước chưa kịp nhấc lên khỏi phím cũ.
- **`Trie` (`Prefix Tree` — Cây tiền tố)**: Cấu trúc dữ liệu hình cái cây phân nhánh theo từng chữ cái từ đầu từ tới cuối từ, giúp tìm kiếm hàng ngàn từ gợi ý chỉ trong vài bước nhảy.
- **`Unigram`, `Bigram`, `Trigram` (`N-gram`)**: Thống kê tần suất xuất hiện của 1 từ đơn lẻ (`Unigram`), cặp 2 từ đi liền nhau (`Bigram`), và bộ 3 từ đi liền nhau (`Trigram`) để đoán từ tiếp theo sát với ngữ cảnh câu văn.
- **`OOV` (`Out-Of-Vocabulary`)**: Từ mới lạ chưa từng có trong từ điển gốc của máy (như tên riêng, biệt danh, từ lóng mới).
- **`DTW` (`Dynamic Time Warping`)**: Thuật toán quy hoạch động đo độ giống nhau về hình dáng hình học giữa hai đường gấp khúc 2D (đường ngón tay bạn lướt trên màn hình và đường nối tâm các phím của một từ trong từ điển).
- **`ONNX` (`Open Neural Network Exchange`)**: Định dạng chuẩn quốc tế để đóng gói các mô hình Trí tuệ Nhân tạo (như mạng thần kinh lướt phím `neural_glide.onnx` và mạng sửa lỗi chánh tả `vi_corrector.onnx`) đem chạy trực tiếp ngay trên con chip điện thoại Android mà không cần mạng Internet.

---

## LỜI KẾT

Vậy là tụi mình đã cùng nhau đi trọn một hành trình dài suốt 30 chương sách — bắt đầu từ con số 0 tròn trĩnh khi tìm hiểu tại sao màn hình hiểu được chữ `'a'`, chữ `'ệ'`, đi qua từng dòng lệnh biến đổi Telex, từng cổng gác âm cuối Tiếng Việt, từng nét cọ 120FPS trên tấm toan `KeyboardCanvasView`, cho tới cây tiền tố `Trie`, mô hình ngôn ngữ `Trigram` và mạng thần kinh nhân tạo `ONNX`!

Hy vọng rằng khi gấp trang cuối của cuốn cẩm nang này lại, cái bàn phím trên chiếc điện thoại hàng ngày không còn là một "hộp đen bí ẩn" đối với bạn nữa. Mỗi lần bạn gõ chữ `"dzìa"`, gõ chữ `"telee"`, bấm `Select All + Backspace`, hay lướt nhẹ ngón tay một đường trên mặt kiếng để hiện ra cả câu Tiếng Việt mượt mà, bạn đều có thể nắm rõ tới từng chi tiết dòng code nào đang chạy ở bên dưới để phục vụ mình.

Chúc bạn luôn giữ được niềm vui khám phá và viết ra thêm nhiều dòng code sạch, chạy ổn định nha!


---

## PHỤ LỤC C: PHÒNG THÍ NGHIỆM THỰC HÀNH (HANDS-ON LABS) — BẢNG CHẠY TỪNG BƯỚC & CÂU HỎI ÔN TẬP TỪNG PHẦN CHO NGƯỜI MỚI BẮT ĐẦU

Để một người mới bắt đầu từ con số 0 có thể tự kiểm tra xem mình đã nắm vững kiến thức của từng phần hay chưa, Phụ lục C này được thiết kế như một cuốn **Vở bài tập thực hành có lời giải chi tiết (Workbook & Lab Manual)**. Bạn hãy thử tự suy nghĩ câu trả lời trước khi đọc phần **Lời giải của tụi mình** ngay bên dưới mỗi bài nha!

---

### LAB 1: THỰC HÀNH BẢNG MÃ UNICODE & PHÂN RÃ DẤU NFD (`DiacriticUtils`)

#### Bài toán 1.1: Đếm số mảnh Unicode khi chuyển từ NFC sang NFD
Giả sử bạn có từ phương ngữ Nam Bộ là **`"dzậy"`** (gồm 4 chữ cái theo chuẩn dựng sẵn NFC: `'d'`, `'z'`, `'ậ'`, `'y'`).
1. Khi chuỗi `"dzậy"` đang ở chuẩn **NFC**, hàm `"dzậy".length` trả về mấy?
2. Khi gọi `Normalizer.normalize("dzậy", Normalizer.Form.NFD)`, chữ `'ậ'` bị tách thành mấy mảnh? Lúc này chuỗi NFD có tổng cộng bao nhiêu ký tự (`length`)?
3. Khi chạy tiếp `COMBINING_MARKS.replace(normalized, "")`, những mảnh nào bị xóa đi và những mảnh nào còn giữ lại?

#### Lời giải chi tiết của tụi mình:
1. Ở chuẩn **NFC (Dựng sẵn)**:
   - `'d'` (`U+0064`) — 1 ký tự
   - `'z'` (`U+007A`) — 1 ký tự
   - `'ậ'` (`U+1EAD` — Latin Small Letter A With Circumflex And Dot Below) — 1 ký tự đúc sẵn cả chữ `a`, dấu mũ `ˆ` và dấu nặng `.`
   - `'y'` (`U+0079`) — 1 ký tự
   -> Tổng cộng `"dzậy".length` bằng **`4`**.
2. Khi chuyển sang chuẩn **NFD (Tổ hợp)**:
   - Chữ `'d'`, `'z'`, `'y'` không có dấu nên giữ nguyên.
   - Riêng chữ `'ậ'` bị tháo rời thành **3 mảnh**:
     - Mảnh 1: Chữ cái gốc `'a'` (`U+0061`)
     - Mảnh 2: Dấu mũ bay lơ lửng `ˆ` (`U+0302`, thuộc nhóm `Mark, Nonspacing` — `\p{Mn}`)
     - Mảnh 3: Dấu chấm nặng bay lơ lửng `̣` (`U+0323`, thuộc nhóm `Mark, Nonspacing` — `\p{Mn}`)
   -> Lúc này chuỗi NFD gồm `['d', 'z', 'a', 'ˆ', '̣', 'y']`, nên `normalized.length` tăng lên thành **`6`**!
3. Khi quét biểu thức chánh quy `COMBINING_MARKS` (`\p{Mn}+`):
   - Hai mảnh dấu bay lơ lửng `ˆ` (`U+0302`) và `̣` (`U+0323`) đều thuộc nhóm `\p{Mn}` nên bị quét sạch.
   - Còn lại đúng 4 chữ cái gốc không dấu: **`"dzay"`**!

---

#### Bài toán 1.2: Tại sao chữ `"Đ"` và `"đ"` phải có lệnh `.replace('đ', 'd')` riêng?
Một bạn mới học lập trình thắc mắc: *"Ủa anh ơi, trên chữ `'đ'` có cái gạch ngang ở giữa, sao khi chuyển sang chuẩn NFD cái gạch ngang đó hổng tự tách ra giống như dấu mũ của chữ `'â'` để lệnh `COMBINING_MARKS` xóa luôn cho khỏe?"*

#### Lời giải chi tiết của tụi mình:
Trong lịch sử thiết kế bảng mã Unicode quốc tế, Ủy ban Unicode phân biệt hai loại biến thể chữ cái Latin:
- **Loại 1 (Chữ cái gắn dấu phụ rời — Diacritical Marks)**: Như `á, à, ả, ã, ạ, â, ă, ê, ô, ơ, ư`. Những chữ này được định nghĩa trong bảng phân rã chuẩn (Canonical Decomposition Mapping) là `Chữ_gốc + Dấu_kết_hợp`. Khi gọi `NFD`, chúng tự động tách đôi.
- **Loại 2 (Chữ cái có nét gạch xuyên thân — Stroked Letters)**: Như chữ `đ/Đ` của Tiếng Việt (`U+0111` / `U+0110`), chữ `ø/Ø` của Bắc Âu, hay chữ `ł/Ł` của Ba Lan. Ủy ban Unicode coi nét gạch ngang xuyên qua thân chữ `'d'` là một phần xương sống cố định của con chữ mới chớ hổng phải là một cái dấu treo bên ngoài! Vì vậy trong bảng Unicode, `U+0111` (`'đ'`) **không có công thức phân rã NFD**. Khi gọi `Normalizer.normalize("đ", Form.NFD)`, kết quả trả về vẫn y nguyên là `"đ"`. Đó là lý do mọi hàm lột dấu Tiếng Việt chuẩn chỉnh đều phải nối thêm `.replace('đ', 'd').replace('Đ', 'D')` ở bước cuối cùng!

---

### LAB 2: THỰC HÀNH CHẠY TAY (MANUAL TRACE) BỘ MÁY `VietnameseEngine`

Để hiểu rõ tới từng hơi thở của vòng lặp `while (i < chars.size)` trong `transformTelexCore`, tụi mình cùng lấy giấy bút ra chạy từng bước cho 4 ca gõ phím thực tế dưới đây.

#### Ca thực hành 2.1: Gõ từ phương ngữ Nam Bộ `"dzaayj"` -> `"dzậy"` và `"dzachs"` -> `"dzách"`

Tụi mình lập bảng chạy khi `rawChars = ['d', 'z', 'a', 'a', 'y', 'j']`:

1. **Bước kiểm tra đầu hàm `transformTelex`**:
   - `lowerRaw = "dzaayj"`
   - `isDzOnset = lowerRaw.startsWith("dz")` -> **`true`**!
   - Gọi `transformTelexCore(chars, isDzOnset = true)`.
2. **Bước khởi tạo trong `transformTelexCore`**:
   - Vì `isDzOnset == true` và `chars.size == 6 >= 2`:
     - `result.add('d')`
     - `result.add('z')`
     - Con trỏ `i` nhảy thẳng tới **`i = 2`**!
3. **Vòng lặp `while (i < 6)`**:
   - **Tại `i = 2` (`ch = 'a'`)**:
     - Kiểm tra Nhánh 4 (`Hai nguyên âm liền nhau tạo dấu mũ`): `lower == 'a'`, ký tự kế tiếp `chars[3]` cũng là `'a'`, `circumflexCancelledKey != 'a'`, và trong `result` (`['d', 'z']`) chưa có chữ `'a'` nào.
     - Thỏa mãn hoàn toàn! Bộ máy biến cặp `'a', 'a'` tại `i = 2, 3` thành **`'â'`**, bỏ vào `result` -> `result = ['d', 'z', 'â']`.
     - Con trỏ `i` tăng 2 bước: `i = 2 + 2 = 4`.
   - **Tại `i = 4` (`ch = 'y'`)**:
     - Chữ `'y'` không rơi vào nhánh đặc biệt nào, đi xuống cuối vòng lặp: `result.add('y')` -> `result = ['d', 'z', 'â', 'y']`, `i = 5`.
   - **Tại `i = 5` (`ch = 'j'`)**:
     - Kiểm tra Nhánh 7 (`Phím dấu thanh Telex`): Trong `result` đã có nguyên âm (`'â'`, `'y'`), `lower == 'j'` tương ứng với `newTone = Tone.DOT` (dấu nặng).
     - Gán `tone = Tone.DOT`, tăng `i = 6` -> Kết thúc vòng lặp `while`.
4. **Bước đặt dấu thanh `applyTone(result, Tone.DOT, modernOrthography)`**:
   - Tìm vị trí các nguyên âm trong `['d', 'z', 'â', 'y']`: `vowelIndices = [2, 3]` (vị trí `2` là `'â'`, vị trí `3` là bán nguyên âm `'y'`).
   - Vì `secondV == 'y'` và `firstV == 'â' != 'u'`, biến `isFallingOffglide = true`, `hasCoda = false`.
   - Cặp nguyên âm `pair = "ây"` rơi vào nhánh `else -> vowelIndices[0]` (tức là vị trí số `2`, chữ `'â'`).
   - Gọi `addToneToVowel('â', Tone.DOT)` -> biến `'â'` thành **`'ậ'`**.
   - Kết quả cuối cùng: **`"dzậy"`**!

---

#### Ca thực hành 2.2: Gõ từ `"teleegram"` và bài toán bấm Backspace xóa `"am"` rồi gõ tiếp

Bạn còn nhớ tình huống thực tế bạn từng hỏi tụi mình hông:
*"Khi gõ `teleegram`, nếu bộ gõ xử lý không khéo sẽ ra `telegảm`, rồi khi bấm Backspace xóa đi chữ `ảm` và gõ thêm chữ `r` lại nhảy loạn xạ thành `tểlg`!"*
Tụi mình cùng kiểm tra xem trên bộ máy `VietnameseEngine.kt` hiện tại của `vnchFlex`, khi bạn gõ từng phím của `t-e-l-e-e-g-r-a-m` và khi bấm Backspace thì chuyện gì diễn ra ở bên trong nha:

1. **Khi gõ từ `t` đến `e` thứ hai (`t-e-l-e`)**:
   - Tại `i = 0` (`'t'`): `result = ['t']`
   - Tại `i = 1` (`'e'`): `result = ['t', 'e']`
   - Tại `i = 2` (`'l'`): `result = ['t', 'e', 'l']`
   - Tại `i = 3` (`'e'`): Nhánh 5 kiểm tra phần chữ nằm giữa chữ `'e'` đầu (`targetIdx = 1`) và cuối từ là `between = "l"`.
   - Vì `"l"` **không nằm trong tập phụ âm cuối Tiếng Việt** (`c, ch, m, n, ng, nh, p, t, i, u, y`), biến `validCoda` bằng **`false`**!
   - Do đó, chữ `'e'` ở `i = 3` không biến chữ `'e'` đầu thành `'ê'`, mà được thêm thẳng vào cuối: `result = ['t', 'e', 'l', 'e']` -> màn hình hiện **`"tele"`**!
2. **Nếu người dùng có thói quen gõ `t-e-l-e-e` (2 chữ `'e'` ở cuối để chắc chắn thoát dấu)**:
   - Khi gõ `t-e-l-e-e`, tại `i = 3`, Nhánh 4 thấy hai chữ `'e'` liền nhau ở `i = 3, 4` nhưng kiểm tra điều kiện:
     ```kotlin
     result.none { it.lowercaseChar() == lower }
     ```
     Trong `result` (`['t', 'e', 'l']`) đã có sẵn một chữ `'e'` ở vị trí `1` rồi! Nên `result.none { it == 'e' }` trả về **`false`**. Nhánh 4 không gom hai chữ `'e'` ở đuôi thành `'ê'` (`"telê"`), mà giữ nguyên từng chữ.
3. **Khi người dùng bấm phím Xóa lùi (`onBackspace`)**:
   - Hãy coi lại hàm `onBackspace` tại [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L60-L81):
     ```kotlin
     fun onBackspace(ic: InputConnection): Boolean {
         if (rawChars.isEmpty()) return false
         val prevText = renderedOutput
         if (renderedOutput.isNotEmpty()) {
             val updated = renderedOutput.dropLast(1)
             if (updated.isEmpty()) {
                 rawChars.clear()
                 renderedOutput = ""
                 replaceComposingDirect(ic, prevText, "")
             } else {
                 renderedOutput = updated
                 rawChars.clear()
                 rawChars.addAll(rawKeysFor(updated).toList())
                 replaceComposingDirect(ic, prevText, renderedOutput)
             }
             return true
         }
         ...
     }
     ```
   - Bạn nhìn kỹ nghệ thuật xử lý `onBackspace` ở dòng 64–74:
     Thay vì chỉ xóa 1 phần tử trong danh sách `rawChars` (vốn có thể còn vướng những phím dấu cũ hoặc phím lặp hoàn nguyên làm cho lần gõ tiếp theo bị nhảy dấu bất ngờ), hàm `onBackspace` cắt trực tiếp 1 ký tự hiển thị cuối cùng trên màn hình (`val updated = renderedOutput.dropLast(1)`), rồi gọi **`rawKeysFor(updated)`** để đồng bộ hóa `rawChars` khớp 100% với những gì mắt người dùng đang nhìn thấy trên màn hình!
   - Nhờ vậy, khi trên màn hình đang có chữ gì và bạn bấm `Backspace` xóa bớt 1 chữ, bộ đệm `rawChars` luôn sạch bóng các phím thừa lịch sử, không bao giờ xảy ra chuyện dấu cũ "sống dậy" nhảy lên đầu chữ trước đó!

---

### LAB 3: THỰC HÀNH TÍNH TOÁN TỌA ĐỘ PHÍM BẤM TRÊN TẤM TOAN `KeyboardCanvasView`

#### Bài toán 3.1: Tự tay tính kích thước điểm ảnh (Pixels) của bàn phím trên màn hình Full HD+
Giả sử chiếc điện thoại Android của bạn có thông số màn hình như sau:
- Chiều ngang màn hình: `totalW = 1080 pixels`.
- Mật độ điểm ảnh: `density = 3.0f` (tức là `1 dp = 3 pixels`).
- Người dùng đang mở trang chữ cái (`Page.LETTERS`), có bật hàng phím số (`showNumberRow = true`), và chọn chiều cao bàn phím là **Cao (`"tall"`, hệ số `keyHeightScale = 1.12f`)**.

Hãy áp dụng công thức trong [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L190-L193) và [dòng 309–335](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L309-L335) để tính:
1. Chiều cao của hàng phím số (`numRowH`) bằng bao nhiêu pixels?
2. Chiều cao của mỗi hàng phím chữ (`letterRowH`) bằng bao nhiêu pixels?
3. Tổng chiều cao của tấm toan bàn phím (`targetH`) bằng bao nhiêu pixels?
4. Trên hàng chữ thứ nhứt (`Q W E R T Y U I O P` gồm 10 phím có cùng `weight = 1.0f`), mỗi phím có khung bắt cảm ứng `hitRect` rộng bao nhiêu pixels và khung vẽ nút bấm `rect` rộng bao nhiêu pixels?

#### Lời giải chi tiết của tụi mình:
1. **Chiều cao hàng phím số (`numRowH`)**:
   $$\text{numRowH} = \lfloor 40 \times \text{density} \times \text{keyHeightScale} \rfloor = \lfloor 40 \times 3.0 \times 1.12 \rfloor = \lfloor 134.4 \rfloor = \mathbf{134\text{ pixels}}$$
2. **Chiều cao mỗi hàng phím chữ (`letterRowH`)**:
   $$\text{letterRowH} = \lfloor 46 \times \text{density} \times \text{keyHeightScale} \rfloor = \lfloor 46 \times 3.0 \times 1.12 \rfloor = \lfloor 154.56 \rfloor = \mathbf{154\text{ pixels}}$$
3. **Tổng chiều cao tấm toan (`targetH`)**:
   - Khoảng đệm trên dưới: $8 \times \text{density} = 8 \times 3.0 = 24\text{ pixels}$.
   - Tổng chiều cao 1 hàng số + 4 hàng chữ + đệm:
     $$\text{targetH} = 134 + (154 \times 4) + 24 = 134 + 616 + 24 = \mathbf{774\text{ pixels}}$$
   (So với mức bình thường `keyHeightScale = 1.0f` là $120 + 138 \times 4 + 24 = 696\text{ pixels}$, bàn phím khi chọn mức `"tall"` đã cao thêm đúng `78 pixels`, giúp các nút bấm thoáng và dễ chạm hơn hẳn!).
4. **Độ rộng của mỗi phím trên hàng `Q W E R T Y U I O P`**:
   - Hàng này không thụt lề (`sideInset = 0`), nên chiều ngang khả dụng là `availW = 1080 pixels`.
   - Tổng trọng số của 10 phím là `totalWeight = 10.0f`.
   - Độ rộng khung bắt cảm ứng `hitRect` của mỗi phím là:
     $$\text{cellW} = 1080 \times \frac{1.0}{10.0} = \mathbf{108\text{ pixels}}$$
   - Khoảng hở ngang mỗi bên của nút bấm là:
     $$\text{gapX} = 2.5 \times \text{density} = 2.5 \times 3.0 = 7.5\text{ pixels}$$
   - Độ rộng khung vẽ nút bấm hữu hình `rect` của mỗi phím là:
     $$\text{rect.width} = \text{cellW} - 2 \times \text{gapX} = 108 - 15 = \mathbf{93\text{ pixels}}$$

---

### LAB 4: THỰC HÀNH CHẤM ĐIỂM LƯỚT PHÍM (`GlideDecoder`) VÀ TIÊN ĐOÁN N-GRAM (`SuggestionEngine`)

#### Bài toán 4.1: Tính điểm so tài giữa hai ứng viên khi người dùng lướt phím
Giả sử người dùng vừa gõ xong hai chữ trước là **`"thành phố"`** (`prevWord2 = "thành"`, `prevWord1 = "phố"`).
Tiếp theo, người dùng đặt ngón tay ở phím **`H`** rồi kéo sang phím **`O`** để lướt từ thứ ba, tạo ra chuỗi phím lướt `raw = "ho"`.
Trong từ điển có 2 từ ứng viên cùng có 2 chữ cái là:
- Ứng viên A: **`"hồ"`** (với tần suất gốc `unigramWeight = 90`, và trong ngữ cảnh `"thành phố"` -> `"hồ"` có điểm ngữ cảnh Trigram/Bigram là `contextWeight = 8`).
- Ứng viên B: **`"họ"`** (với tần suất gốc `unigramWeight = 92`, nhưng không khớp ngữ cảnh `"thành phố"` nên `contextWeight = 0`).

Cả hai ứng viên sau khi lột dấu đều có `collapsedTarget = "ho"`, trùng khớp hoàn toàn với `raw = "ho"` (nên `exactFirst = true`, `exactLast = true`, `subseqMatched = true`, `dist = 0`, `dtw = 0`).
Hãy dùng công thức chấm điểm tại [`GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L197-L208) để tính tổng điểm `score` của `"hồ"` và `"họ"`:

$$\text{score} = \text{unigramWeight} + 90_{\text{first}} + 90_{\text{last}} + 220_{\text{subseq}} + 140_{\text{dist=0}} + 65 \times \text{contextWeight}$$

#### Lời giải chi tiết của tụi mình:
1. **Điểm hình học cơ bản (cả hai ứng viên đều đạt tối đa)**:
   $$\text{GeometricBonus} = 90 + 90 + 220 + 140 = 540\text{ điểm}$$
2. **Tổng điểm của Ứng viên B (`"họ"`)**:
   $$\text{Score}(\text{"họ"}) = 92 + 540 + (65 \times 0) = \mathbf{632\text{ điểm}}$$
3. **Tổng điểm của Ứng viên A (`"hồ"`)**:
   $$\text{Score}(\text{"hồ"}) = 90 + 540 + (65 \times 8) = 630 + 520 = \mathbf{1150\text{ điểm}}$$

**Kết luận**: Dù đứng một mình chữ `"họ"` có điểm tần suất gốc nhỉnh hơn chữ `"hồ"` một chút (`92` so với `90`), nhưng nhờ đứng sau cụm từ `"thành phố"`, điểm thưởng ngữ cảnh Trigram (`+520` điểm) đã giúp chữ **`"hồ"`** (trong *"Thành phố Hồ Chí Minh"*) bứt phá ngoạn mục lên vị trí số 1 với `1150` điểm!

---

## PHỤ LỤC D: CẨM NANG HỎI – ĐÁP & GỠ LỖI NHANH (TROUBLESHOOTING FAQ) DÀNH CHO NGƯỜI MỚI PHÁT TRIỂN BỘ GÕ

Khi bạn tự tay sửa code hoặc biên dịch bộ gõ `vnchFlex` để cài lên điện thoại của mình, đôi khi bạn sẽ gặp một vài thắc mắc thường gặp. Tụi mình ghi sẵn 8 câu hỏi – đáp thực tế nhứt ở đây để bạn tra cứu liền tay:

### Câu hỏi 1: Tại sao mình sửa code trong `KeyboardPreferences.kt` để đổi một giá trị mặc định, nhưng cài đè ứng dụng lên máy lại chưa thấy giá trị mới?
- **Giải đáp**: Hàm `prefs.getBoolean("tên_khóa", giá_trị_mặc_định)` hoặc `prefs.getString("tên_khóa", giá_trị_mặc_định)` của Android chỉ lấy `giá_trị_mặc_định` khi trong tập tin `SharedPreferences` trên điện thoại **chưa từng lưu khóa đó**. Nếu trước đó bạn đã từng mở màn hình Cài đặt và bấm vô công tắc đó rồi, giá trị cũ đã được ghi cứng vào bộ nhớ máy.
- **Cách xử lý**: Sau khi đổi giá trị mặc định trong code, nếu muốn kiểm tra trải nghiệm của người dùng cài mới lần đầu, bạn chỉ cần vô *Cài đặt của điện thoại -> Ứng dụng -> vnchFlex -> Lưu trữ -> Xóa dữ liệu (Clear Data)* (hoặc gỡ app cũ ra cài lại) là bộ gõ sẽ nạp đúng 100% giá trị mặc định mới!

### Câu hỏi 2: Làm sao để thêm một từ Tiếng Anh mới mà mình hay gõ để bộ gõ không bao giờ biến nó thành chữ Tiếng Việt có dấu?
- **Giải đáp**: Bạn có tới **2 cách**:
  - **Cách 1 (Không cần sửa code, làm ngay trên điện thoại)**: Mở ứng dụng `vnchFlex` -> vô mục *Gõ Tiếng Việt & Gõ tắt* -> thêm từ đó vào danh sách **Từ ngoại lệ Tiếng Anh (`English Whitelist`)**. Biến `userWhitelist` trong `VietnameseEngine.kt` sẽ kiểm tra danh sách này ở ngay dòng đầu tiên của `transformTelex()` và giữ nguyên 100% chữ bạn gõ.
  - **Cách 2 (Thêm cứng vào mã nguồn cho mọi người dùng)**: Mở tập tin [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt#L625-L647), thêm từ Tiếng Anh đó (viết thường) vào tập hợp `ENGLISH_AUTO_RESTORE_WORDS`.

### Câu hỏi 3: Tại sao khi vẽ lên `Canvas` trong `KeyboardCanvasView.kt`, mình phải nhân số đo với `density` hoặc `scaledDensity`?
- **Giải đáp**:
  - Điện thoại giá rẻ có màn hình độ phân giải thấp (ví dụ 1 inch chỉ có 160 điểm ảnh, `density = 1.0f`), trong khi điện thoại cao cấp có màn hình siêu nét (1 inch có tới 480 hay 640 điểm ảnh, `density = 3.0f` hoặc `4.0f`).
  - Nếu bạn viết cứng `rowH = 46f` (46 điểm ảnh), thì trên màn hình siêu nét cái nút bấm sẽ teo nhỏ lại bằng 1/3 ngón tay!
  - Khi bạn nhân `46f * density` (đơn vị `dp` — *Density-independent Pixels*), chiếc nút bấm sẽ luôn cao đúng bằng khoảng `7 milimét` ngoài đời thật trên mọi chiếc điện thoại lớn nhỏ khác nhau. Còn khi vẽ cỡ chữ (`textSize`), ta nhân với `scaledDensity` (đơn vị `sp`) để tôn trọng cả tỷ lệ cỡ chữ của hệ thống.

### Câu hỏi 4: Tại sao các mô hình AI (`vi_corrector.onnx` và `neural_glide.onnx`) phải nạp trên một luồng riêng (`Thread { ... }.start()`) trong hàm `initAsync`?
- **Giải đáp**: Tập tin mô hình mạng thần kinh ONNX nặng vài Megabyte. Quá trình đọc vài triệu byte từ bộ nhớ máy và khởi tạo đồ thị tính toán (`ortEnv.createSession(modelBytes, opts)`) mất khoảng `50ms` đến `150ms`. Nếu chạy trực tiếp trên luồng giao diện chánh (`Main UI Thread`) lúc hàm `onCreate()` vừa gọi, bàn phím sẽ bị khựng lại một nhịp trong lần mở đầu tiên. Cho nó chạy ngầm trong `Thread { ... }.start()` rồi bật cờ `@Volatile var isReady = true` giúp bàn phím bật lên tức thì ở `0ms`!

### Câu hỏi 5: Khi mình muốn xuất cuốn sách `docs/VNCHFLEX_BOOK.md` này ra bản `.pdf` mới để in sau khi tự viết thêm ghi chú cá nhân, mình chạy câu lệnh gì trên máy Linux?
- **Giải đáp**: Bạn chỉ cần mở Terminal tại thư mục gốc của dự án `.` và chạy đúng dòng lệnh kết hợp `pandoc` và `libreoffice` sau đây:
  ```bash
  pandoc -f markdown-yaml_metadata_block docs/VNCHFLEX_BOOK.md -o /tmp/VNCHFLEX_BOOK.docx && \
  libreoffice -env:UserInstallation=file:///tmp/lo_profile --headless --convert-to pdf --outdir docs /tmp/VNCHFLEX_BOOK.docx
  ```
  Vài giây sau, tập tin `docs/VNCHFLEX_BOOK.pdf` rõ đẹp, hỗ trợ đầy đủ phông chữ Tiếng Việt và công thức toán học sẽ nằm sẵn trong thư mục `docs/` để bạn đem đi in! Và nhớ quy ước của tụi mình nha: tập tin `.pdf` chỉ giữ trên máy cá nhân để in đọc thôi, đừng `git add` hay `git push` tập tin `.pdf` lên GitHub để kho mã nguồn luôn nhẹ nhàng, sạch sẽ.


---

# PHẦN VIII: MỔ XẺ SÂU TỪNG NGÓC NGÁCH CÒN LẠI — KHÔNG CHỪA MỘT CHI TIẾT NÀO (CHƯƠNG 31–36)

---

## CHƯƠNG 31: TẠI SAO CÀI APP XONG PHẢI BẬT 2 BƯỚC TRONG CÀI ĐẶT ANDROID? (`AndroidManifest.xml`, `method.xml` & `SettingsActivity.kt`)

Nhiều bạn mới cài `vnchFlex` (hoặc bất kỳ bàn phím nào khác) lên điện thoại lần đầu hay thắc mắc:
*"Ủa, sao mấy app như Zalo, Facebook cài xong bấm mở là xài liền, còn app bàn phím cài xong mở lên nó bắt mình phải vô Cài đặt Hệ thống bật công tắc lên, rồi còn bắt chọn 'Đặt làm bàn phím mặc định' nữa?"*

Lý do nằm ở **tính bảo mật tối đa của hệ điều hành Android**:
Bàn phím là nơi tiếp xúc với **mọi thứ bạn gõ**, kể cả mật khẩu ngân hàng, tin nhắn riêng tư và số thẻ tín dụng. Nếu một ứng dụng mới cài vô mà tự động biến thành bàn phím mặc định của máy luôn không cần bạn cho phép, thì kẻ xấu có thể lén cài app đọc trộm hết mật khẩu của bạn!
Bởi vậy, Android bắt buộc phải có 2 lớp khai báo rõ ràng trong mã nguồn và 2 bước xác nhận bằng tay của người dùng:

1. **Tập tin khai báo `AndroidManifest.xml` (Giấy khai sinh của ứng dụng)**:
   - Trong tập tin này, `vnchFlex` phải đăng ký `VietKeyboardService` với quyền đặc biệt:
     `android:permission="android.permission.BIND_INPUT_METHOD"`
     và gắn nhãn `<action android:name="android.view.InputMethod" />`.
   - Dòng khai báo này nói với hệ điều hành Android rằng: *"Tui là một Dịch vụ Bàn phím chánh quy, hãy liệt kê tên tui vô danh sách Bàn phím trong phần Cài đặt Ngôn ngữ & Nhập liệu của máy."*
2. **Tập tin cấu hình `res/xml/method.xml`**:
   - Khai báo cho Android biết bàn phím này hỗ trợ các ngôn ngữ nào (ví dụ `vi` — Tiếng Việt, `en` — Tiếng Anh), và chỉ định màn hình Cài đặt riêng của bàn phím là `SettingsActivity`.
3. **Màn hình `SettingsActivity.kt` ([`settings/SettingsActivity.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/SettingsActivity.kt))**:
   - Đây là màn hình duy nhứt có biểu tượng ngoài màn hình chánh của điện thoại. Khi bạn mở `vnchFlex` lên, `SettingsActivity` kiểm tra xem bàn phím đã được bật chưa (`InputMethodManager.enabledInputMethodList`), nếu chưa thì hiện nút hướng dẫn bạn mở thẳng tới trang bật bàn phím của Android (Bước 1: **Enable**), rồi hiện tiếp hộp thoại chọn bàn phím đang hoạt động (Bước 2: **Select Input Method**).

---

## CHƯƠNG 32: CHUẨN HÓA VĂN BẢN (`VietnameseTextNormalizer.kt`) & KIỂM TRA CHÁNH TẢ HỆ THỐNG (`VnchSpellCheckerService.kt`)

### 32.1. `VietnameseTextNormalizer.kt` làm việc gì ở hậu trường?
Khi bạn copy một đoạn văn bản Tiếng Việt từ trên mạng về (hoặc từ một file PDF cũ), rất có thể đoạn văn đó đang bị trộn lẫn lộn: chỗ thì dùng Unicode dựng sẵn (NFC), chỗ thì dùng Unicode tổ hợp (NFD), chỗ thì gõ kiểu bỏ dấu mới (`hoà, thuỷ`), chỗ thì dư 2–3 khoảng trắng liên tiếp.

Lớp [`VietnameseTextNormalizer.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseTextNormalizer.kt) đóng vai trò như một "máy ủi phẳng văn bản":
1. **Ép toàn bộ về chuẩn Unicode NFC (`Normalizer.Form.NFC`)**: Gom tất cả các mảnh chữ và dấu rời rạc thành ký tự đúc sẵn duy nhứt để không bị lỗi hiển thị hay lỗi đếm ký tự.
2. **Dọn khoảng trắng thừa**: Biến nhiều khoảng trắng liên tiếp thành 1 khoảng trắng duy nhứt.
3. **Đồng bộ quy tắc bỏ dấu**: Chuyển toàn bộ các vần mở `oa, oe, uy` về cùng một chuẩn (cổ điển `hòa, khỏe, thủy` hoặc kiểu mới `hoà, khoẻ, thuỷ`) theo đúng lựa chọn của người dùng.

### 32.2. `VnchSpellCheckerService.kt` khác gì với `SuggestionEngine`?
- **`SuggestionEngine`** là bộ máy gợi ý từ **riêng của bàn phím `vnchFlex`**, chạy trên thanh 3 ô gợi ý phía trên bàn phím khi bạn đang gõ.
- **`VnchSpellCheckerService`** ([`ime/VnchSpellCheckerService.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VnchSpellCheckerService.kt)) là dịch vụ **Kiểm tra chánh tả cấp Hệ điều hành Android (`SpellCheckerService`)**. Khi bạn gõ trong các app như Gmail, Google Docs hay trình duyệt, app đó sẽ nhờ Android gạch chân đỏ dưới những từ nghi ngờ sai chánh tả. Khi bạn chạm vô từ bị gạch đỏ đó, Android gọi tới `VnchSpellCheckerService` của `vnchFlex` để lấy danh sách các từ Tiếng Việt đúng chánh tả hiện ra cho bạn chọn sửa lại.

---

## CHƯƠNG 33: BẢNG BỘ NHỚ TẠM TRỰC QUAN (`ClipboardPanelController.kt`) & NHẬP LIỆU GIỌNG NÓI (`VoiceInputActivity.kt`)

### 33.1. Làm sao bảng Bộ nhớ tạm hiện đè lên mặt bàn phím? (`ClipboardPanelController.kt`)
Khi bạn bấm biểu tượng Clipboard trên thanh công cụ, bạn thấy mặt bàn phím chữ cái tạm thời ẩn đi (hoặc bị che lại) và thay bằng danh sách các đoạn văn bản bạn đã copy.
- Lớp [`ClipboardPanelController.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/ClipboardPanelController.kt) chịu trách nhiệm vẽ danh sách các `ClipboardItem` lấy từ `ClipboardStore`.
- Mỗi dòng trong bảng có 3 nút bấm rõ ràng:
  1. **Chạm vô nội dung chữ**: Gọi `ic.commitText(item.text, 1)` để dán thẳng đoạn chữ đó vô ô chat rồi tự động đóng bảng Clipboard trả lại mặt bàn phím cho bạn gõ tiếp.
  2. **Nút Ghim (Pin)**: Gọi `clipboardStore.togglePin(item.id)` để khóa mục đó lại vĩnh viễn không bị tự xóa sau 1 tiếng.
  3. **Nút Xóa (Trash)**: Gọi `clipboardStore.delete(item.id)` để xóa bỏ mục đó khỏi danh sách.

### 33.2. Tại sao Nhập liệu Giọng nói phải mở một `Activity` riêng (`VoiceInputActivity.kt`)?
Một dịch vụ bàn phím (`InputMethodService`) chạy ngầm không thể tự ý bật hộp thoại Micro của Google Speech Recognizer trực tiếp từ bên trong nó được (do giới hạn kiến trúc của Android).
Bởi vậy, khi bạn bấm nút Micro trên thanh công cụ, `VietKeyboardService` khởi chạy một màn hình trung gian trong suốt nhỏ gọn tên là [`VoiceInputActivity.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/VoiceInputActivity.kt):
1. `VoiceInputActivity` xin quyền Micro và mở trình nhận diện giọng nói Tiếng Việt (`RecognizerIntent.ACTION_RECOGNIZE_SPEECH` với ngôn ngữ `"vi-VN"`).
2. Khi bạn nói xong câu Tiếng Việt, Google trả về chuỗi văn bản kết quả.
3. `VoiceInputActivity` gởi ngược chuỗi văn bản đó về cho `VietKeyboardService` (qua `Broadcast` hoặc bộ nhớ chia sẻ) để chèn thẳng vô ô nhập chữ qua `InputConnection`, rồi tự đóng `VoiceInputActivity` lại trong chớp mắt!

---

## CHƯƠNG 34: JSON LÀ GÌ VÀ CÁCH `MacroStore` / `PersonalDictionaryStore` SAO LƯU DỮ LIỆU RA SAO?

Trong sách tụi mình hay nhắc tới chữ **JSON** (viết tắt của *JavaScript Object Notation*). Dành cho bạn nào chưa từng nghe tới JSON:
Hãy tưởng tượng JSON giống như một **tờ giấy ghi chú có cấu trúc cặp `Tên_Mục : Giá_Trị`** cực kỳ gọn gàng, mà cả con người lẫn máy tính đều đọc hiểu được ngay lập tức.

Ví dụ, một danh sách Gõ tắt lưu trong [`MacroStore.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroStore.kt) khi đổi ra chữ JSON sẽ trông y hệt như vầy:
```json
[
  {
    "shortcut": "vn",
    "expansion": "Việt Nam",
    "enabled": true,
    "caseSensitive": false
  },
  {
    "shortcut": "dc",
    "expansion": "được",
    "enabled": true,
    "caseSensitive": false
  }
]
```
- Dấu ngoặc vuông `[ ... ]` có nghĩa là **một Danh sách nhiều món**.
- Mỗi cặp ngoặc nhọn `{ ... }` là **một Món đồ** (một từ gõ tắt).
- Bên trong là từng thuộc tính đi theo cặp `"tên": giá_trị`.

Nhờ lưu trữ dưới dạng chuỗi JSON chuẩn quốc tế trong `SharedPreferences`, cả `MacroStore.kt` lẫn `PersonalDictionaryStore.kt` đều dễ dàng thực hiện tính năng **Sao lưu (Export)** ra một tập tin `.json` để bạn cất lên Google Drive, và khi đổi điện thoại mới chỉ cần bấm **Khôi phục (Import)** là toàn bộ từ điển cá nhân, Bigram, Trigram và bảng Gõ tắt quay trở lại nguyên vẹn!

---

## CHƯƠNG 35: TỪ MÃ NGUỒN `.kt` BIẾN THÀNH FILE `.apk` CÀI LÊN ĐIỆN THOẠI NHƯ THẾ NÀO? (`build-apk.yml`)

Khi bạn đọc hoặc sửa các tập tin `.kt` (Kotlin) trong dự án `vnchFlex`, điện thoại Android không thể chạy trực tiếp các tập tin chữ viết đó được. Nó cần trải qua một quy trình đóng gói gọi là **Build APK** (được tự động hóa sẵn trong tập tin `.github/workflows/build-apk.yml` của dự án):

```
[ Mã nguồn Kotlin (.kt) + Tài nguyên (icon, màu sắc) + Mô hình AI (.onnx) ]
                                    |
                                    v
             (1) Trình biên dịch Kotlin (kotlinc / Gradle)
                 Dịch chữ người đọc thành mã máy ảo Dalvik/ART (.dex)
                                    |
                                    v
             (2) Đóng gói & Ký tên (Packaging & Signing)
                 Nén .dex + assets + AndroidManifest.xml thành file .apk
                                    |
                                    v
             [ Tập tin vnchflex-release.apk sẵn sàng cài lên điện thoại ]
```

Mỗi khi bạn đẩy code mới lên nhánh `main` trên GitHub, hệ thống **GitHub Actions** sẽ tự động thuê một máy chủ Linux miễn phí trên mây, tải bộ công cụ Android SDK về, chạy lệnh Gradle biên dịch toàn bộ mã nguồn của `vnchFlex` và xuất ra tập tin `.apk` cho bạn tải về cài thẳng lên máy!

---

## CHƯƠNG 36: TỪ ĐIỂN BỎ TÚI — 10 LỖI KINH ĐIỂN NGƯỜI MỚI HAY GẶP & CÁCH BẮT BỆNH

1. **Lỗi 1: Bấm `Backspace` khi đang `Select All` không xóa chữ** -> *Nguyên nhân*: Dùng `ic.deleteSurroundingText(1, 0)` thay vì kiểm tra `ic.getSelectedText(0)` và gọi `ic.commitText("", 1)`.
2. **Lỗi 2: Chữ trong Telegram bị nhảy hoặc nhấp nháy gạch chân** -> *Nguyên nhân*: Gọi `ic.setComposingText()` liên tục trên ô chat của Telegram. *Cách trị*: Tắt `useComposingSpan = false` cho Telegram và dùng `Common Prefix Diffing`.
3. **Lỗi 3: Gõ chữ Tiếng Anh `telee` bị biến thành `telê`** -> *Nguyên nhân*: Không kiểm tra cổng gác âm cuối Tiếng Việt (`validCoda`) và không có cờ `circumflexCancelledKey` để hoàn nguyên khi lặp phím.
4. **Lỗi 4: Chỉnh chiều cao bàn phím trong Cài đặt mà bàn phím không cao lên** -> *Nguyên nhân*: Chỉ tính lại chiều cao trong `onMeasure()`, quên cập nhật `layoutParams.height` và gọi `requestLayout()`.
5. **Lỗi 5: Gõ nhanh 2 ngón cái bị nuốt chữ** -> *Nguyên nhân*: Dùng 1 biến `pressedKey` duy nhứt thay vì dùng `SparseArray<PointerTracker>` theo dõi từng `pointerId` độc lập và chốt chữ ngay lúc chạm xuống (`Commit-on-Down`).
6. **Lỗi 6: Chạm vô khe hở giữa 2 phím bị hụt** -> *Nguyên nhân*: Dùng chung 1 khung chữ nhựt để vừa vẽ vừa bắt cảm ứng thay vì tách riêng `rect` (khung vẽ) và `hitRect` (khung bắt cảm ứng khít 100%).
7. **Lỗi 7: Bàn phím bị khựng khi vừa bật lên lần đầu** -> *Nguyên nhân*: Nạp tập tin AI `.onnx` nặng vài MB trực tiếp trên luồng giao diện chánh (`Main Thread`) thay vì nạp ngầm trong `Thread { ... }.start()`.
8. **Lỗi 8: Lưu mật khẩu và mã OTP vô từ điển gợi ý** -> *Nguyên nhân*: Thiếu bộ lọc `Privacy Guard` (`if (!w.all { it.isLetter() }) return`).
9. **Lỗi 9: Gõ `google.vn` bị bung thành `google.Việt Nam`** -> *Nguyên nhân*: `MacroEngine` không kiểm tra ký tự phân cách kỹ thuật đứng ngay trước từ viết tắt.
10. **Lỗi 10: Đổi giá trị mặc định trong `KeyboardPreferences.kt` nhưng cài đè app lên máy không thấy đổi** -> *Nguyên nhân*: `SharedPreferences` trên máy vẫn còn lưu giá trị cũ của lần cài trước; cần *Xóa dữ liệu ứng dụng (Clear Data)* hoặc gỡ cài lại để kiểm tra cài đặt mặc định.


---

# PHẦN IX: PHÂN TÍCH ĐÁNH ĐỔI THUẬT TOÁN (ADVANTAGES & LIMITATIONS) VÀ KHO BÀI TẬP THỰC HÀNH LUYỆN KỸ NĂNG

---

## CHƯƠNG 37: TẠI SAO CHÚNG TA CHỌN NHỮNG THUẬT TOÁN ĐẮT GIÁ NHỨT? (PHÂN TÍCH ĐÁNH ĐỔI KIẾN TRÚC)

Trong kỹ thuật phần mềm, **không có thuật toán nào là hoàn hảo tuyệt đối**. Mọi quyết định chọn kiến trúc trong `vnchFlex` đều là một sự **đánh đổi có tính toán (Engineering Trade-off)** giữa tốc độ, độ chánh xác, dung lượng bộ nhớ, thời lượng pin và sự phức tạp khi bảo trì. Chương này nói thẳng, nói thật về **Ưu điểm (Advantages)** và cả **Giới hạn chưa có lời giải trọn vẹn (Limitations)** của 6 lựa chọn đắt giá nhứt trong `vnchFlex`.

### 37.1. Kiến trúc Pure-Rebuild trong `VietnameseEngine` so với Sửa chuỗi tại chỗ (In-place Mutation)

- **Tại sao chọn?**
  Khi người dùng gõ bỏ dấu tự do (`vietej -> việt`), đổi dấu (`việt -> viết`), hoặc gõ lặp để hoàn nguyên (`telee -> tele`), việc giữ nguyên chuỗi phím thô `rawChars` và tính lại toàn bộ từ đầu (`transformTelex(rawChars)`) giúp loại bỏ hoàn toàn lỗi "rác trạng thái" và hiện tượng dấu nhảy loạn xạ.
- **Ưu điểm (Advantages)**:
  - Dễ kiểm chứng, dễ viết unit test: cùng một chuỗi `rawChars` luôn cho ra đúng một kết quả duy nhứt (tính thuần khiết / Deterministic).
  - Hỗ trợ `rehydrateWord` (nạp lại từ đã chốt để sửa dấu tiếp) một cách tự nhiên.
- **Giới hạn chưa có lời giải trọn vẹn (Limitations)**:
  - **Độ phức tạp tính toán $O(N)$ trên mỗi phím gõ**: Với từ dài 7–10 chữ cái thì $O(N)$ cực kỳ nhanh (vài micro-giây), nhưng nếu người dùng cố tình giữ phím gõ một chuỗi vô nghĩa dài hàng trăm ký tự không có khoảng trắng, hàm `transformTelex` phải quét lại từ đầu mỗi lần gõ.
  - **Hạn chế của `rawKeysFor()` khi phục hồi từ Tiếng Anh**: Khi dùng `rehydrateWord` nạp lại một từ đã chốt trước đó, `rawKeysFor` dịch ngược từ chữ có dấu ra chuỗi phím Telex chuẩn, nên không thể biết trước đó người dùng đã từng bấm phím hoàn nguyên (như `telee`) hay chưa. Dù vậy, đây vẫn là sự đánh đổi tốt nhứt để đổi lấy sự ổn định tuyệt đối cho 99,9% trường hợp gõ Tiếng Việt hàng ngày.

### 37.2. Đồ họa Single-Canvas 120FPS (`KeyboardCanvasView`) so với XML `ViewGroup` truyền thống

- **Tại sao chọn?**
  Để đạt 120 khung hình/giây không khựng và hỗ trợ lướt phím (Glide Typing) xuyên qua 44 phím mượt mà, toàn bộ bàn phím được vẽ trên một `Canvas` duy nhứt với kỹ thuật `Zero-Allocation onDraw`.
- **Ưu điểm (Advantages)**:
  - Tốc độ vẽ cực nhanh, không tốn chi phí `Measure/Layout` của 44 nút `<Button>` con.
  - Theo dõi đa điểm chạm (`SparseArray<PointerTracker>`) và vẽ vệt lướt phím, bong bóng phóng to chữ cực kỳ linh hoạt.
- **Giới hạn chưa có lời giải trọn vẹn (Limitations)**:
  - **Khó hỗ trợ Trình đọc màn hình cho người khiếm thị (Accessibility / TalkBack)**: Vì chỉ có 1 `View` phẳng duy nhứt trên màn hình, hệ thống Android Accessibility không tự động biết vị trí từng phím `A, B, C` nằm ở đâu nếu không tự viết thêm `ExploreByTouchHelper` phức tạp.
  - **Tự quản lý toàn bộ tương tác**: Mọi hiệu ứng nhấn nút, đổ bóng, bo góc, canh lề chữ đều phải tự tính bằng tọa độ pixel thay vì dùng sẵn thuộc tính XML của Android. Tuy nhiên, để đổi lấy cảm giác gõ 0ms độ trễ và lướt phím mượt như Gboard, đây là lựa chọn bắt buộc.

### 37.3. Thuật toán Lướt phím Lai ghép (`DTW` + `Levenshtein` + `neural_glide.onnx`)

- **Tại sao chọn?**
  Đường lướt ngón tay trên màn hình chứa rất nhiều phím nhiễu nằm dọc đường đi. Kết hợp quy hoạch động hình học **DTW** (`O(N × M)`) với mạng thần kinh **Conv1D + BiGRU + CTC** (`neural_glide.onnx`) cho độ chánh xác vượt trội hơn hẳn việc chỉ so khớp chuỗi con đơn thuần.
- **Ưu điểm (Advantages)**:
  - Chấp nhận được việc ngón tay lướt hơi lệch ở đầu/cuối từ (`isNeighbor`), tự động bỏ qua chữ kép (`collapseRepeats`), và phân biệt được các từ có cùng chuỗi con nhờ hình dáng quỹ đạo 2D (`dtwDistance`).
  - Kết hợp điểm thưởng ngữ cảnh Trigram nên đoán trúng ngay cả những từ ngắn dễ nhầm lẫn.
- **Giới hạn chưa có lời giải trọn vẹn (Limitations)**:
  - **Chi phí CPU tỷ lệ thuận với kích thước từ điển**: Nếu từ điển `vocab` phình to lên 50.000 từ, việc chạy `dtwDistance` và `levenshtein` cho hàng ngàn ứng viên sau khi lọc có thể tốn 30–50ms trên máy cấu hình yếu.
  - **Chưa giải mã được đường lướt 1 nét cho cả cụm 2–3 từ liên tiếp** (Phrase-level Glide) vì mạng CTC hiện tại được huấn luyện ở cấp độ từng từ đơn.

### 37.4. Mạng thần kinh sửa chánh tả `OnnxVietnameseCorrector` (BiGRU INT8 + Constrained Decoding)

- **Tại sao chọn?**
  Chạy 100% ngoại tuyến ngay trên điện thoại (không gởi dữ liệu người dùng lên máy chủ), kích thước nhỏ gọn (`6.92 MB`), và dùng **Base-Character Constrained Decoding** để ép AI chỉ được phép sửa dấu Tiếng Việt trên đúng chữ cái gốc.
- **Ưu điểm (Advantages)**:
  - Tuyệt đối không bị "ảo giác" (Hallucination) — không bao giờ tự ý thêm chữ, bớt chữ hay đổi chữ cái gốc của người dùng.
  - Bảo mật 100% quyền riêng tư.
- **Giới hạn chưa có lời giải trọn vẹn (Limitations)**:
  - **Không sửa được lỗi sai phụ âm đầu hoặc sai vần** (ví dụ người dùng viết nhầm `"tr"` thành `"ch"`, hoặc viết thiếu chữ cái): Vì ràng buộc `candBase == srcBase` khóa chặt chữ cái gốc, mô hình chỉ sửa được lỗi sai dấu thanh và dấu mũ/móc (`a/ă/â`, `e/ê`, `o/ô/ơ`, `u/ư`).
  - **Giới hạn cửa sổ ngữ cảnh 128 ký tự**: Các câu văn quá dài bị cắt khúc mỗi 128 ký tự tại ranh giới dấu câu, nên ngữ cảnh ở đầu câu dài có thể không ảnh hưởng tới cuối câu.

### 37.5. Lưu trữ N-gram trên `SharedPreferences` + JSON (`PersonalDictionaryStore`)

- **Tại sao chọn?**
  Tránh phải dựng cơ sở dữ liệu SQLite phức tạp cho dữ liệu nhỏ, kết hợp đệm trên RAM và gom nhóm ghi đĩa trễ 1,5 giây (`Debounced Disk Flush`).
- **Ưu điểm (Advantages)**:
  - Đọc/ghi cực nhanh trên RAM trong lúc gõ; Export/Import sao lưu ra JSON chỉ tốn vài chục dòng code.
- **Giới hạn chưa có lời giải trọn vẹn (Limitations)**:
  - **Không mở rộng lên hàng triệu cặp từ được**: Nếu người dùng nạp văn bản mẫu (corpus) khổng lồ, việc lưu toàn bộ bảng Bigram/Trigram dưới dạng một chuỗi JSON lớn trong `SharedPreferences` sẽ tốn RAM và thời gian parse JSON khi khởi động. Vì vậy `PersonalDictionaryStore` phải đặt giới hạn trần (`MAX_STORED_CONTEXTS`, `MAX_FOLLOWERS_PER_CONTEXT`) để giữ bộ nhớ luôn nhẹ.

---

## CHƯƠNG 38: KHO BÀI TẬP THỰC HÀNH TỰ LUYỆN KỸ NĂNG (CẤP ĐỘ 1: XỬ LÝ CHUỖI & TELEX)

Hãy tự tay làm các bài tập dưới đây trên máy hoặc viết ra giấy rồi đối chiếu với phần **Đáp án gợi ý** để rèn phản xạ đọc hiểu và sửa code bộ gõ.

### Bài tập 1: Thêm từ Tiếng Anh mới vào danh sách tự động khôi phục
- **Yêu cầu**: Bạn hay gõ các từ kỹ thuật như `"kotlin"`, `"gradle"`, `"github"`, `"docker"` khi đang bật chế độ Tiếng Việt, nhưng chữ `"docker"` có thể bị biến dấu nếu gõ nhanh. Hãy bổ sung 4 từ này vào danh sách bảo vệ tự động trong [`VietnameseEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/engine/VietnameseEngine.kt).
- **Đáp án gợi ý**:
  Mở `VietnameseEngine.kt` tại dòng 625 (`ENGLISH_AUTO_RESTORE_WORDS`), thêm `"kotlin"`, `"gradle"`, `"github"`, `"docker"` vào `setOf(...)`.

### Bài tập 2: Viết hàm kiểm tra một từ có phải toàn chữ HOA hay không
- **Yêu cầu**: Viết một hàm nhỏ bằng Kotlin nhận vào `word: String` và trả về `true` nếu từ đó có ít nhứt 2 chữ cái và tất cả đều viết HOA (ví dụ `"VNCHFLEX"` -> `true`, `"Việt"` -> `false`, `"A"` -> `false`).
- **Đáp án gợi ý**:
  ```kotlin
  fun isAllCapsWord(word: String): Boolean {
      val letters = word.filter { it.isLetter() }
      return letters.length >= 2 && letters.all { it.isUpperCase() }
  }
  ```

### Bài tập 3: Chạy tay (Manual Trace) hàm `DiacriticUtils.strip`
- **Yêu cầu**: Cho chuỗi `"Đặng Thị Ngọc Hân"`. Hãy ghi ra kết quả trung gian sau từng bước của hàm `DiacriticUtils.strip`:
  1. Sau `Normalizer.normalize(input, Normalizer.Form.NFD)`
  2. Sau `COMBINING_MARKS.replace(normalized, "")`
  3. Sau `.replace('đ', 'd').replace('Đ', 'D')`
- **Đáp án gợi ý**:
  1. Chữ `'Đ'` giữ nguyên; `'ặ'` tách thành `'a' + '˘' + '̣'`; `'ị'` tách thành `'i' + '̣'`; `'ọ'` tách thành `'o' + '̣'`; `'â'` tách thành `'a' + 'ˆ'`.
  2. Quét sạch các dấu `\p{Mn}+` -> còn `"Đang Thi Ngoc Han"`.
  3. Thay `'Đ'` -> **`"Dang Thi Ngoc Han"`**.

### Bài tập 4: Thiết kế quy tắc chặn Macro cho dấu hai chấm `:`
- **Yêu cầu**: Hiện tại `MacroEngine.kt` chặn không bung Macro khi gặp các ký tự kỹ thuật `/@_-#\=$~`` ở biến `isTechnicalDelimiter(c: Char)`. Nếu bạn muốn gõ `"http:"` mà không bị bung macro `"http"`, bạn cần sửa dòng nào trong `MacroEngine.kt`?
- **Đáp án gợi ý**:
  Tại dòng 90 của [`MacroEngine.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/macro/MacroEngine.kt#L90), thêm dấu `:` vào chuỗi kiểm tra:
  `private fun isTechnicalDelimiter(c: Char): Boolean = c in "/@_-#\\=$~`:"`

---

## CHƯƠNG 39: KHO BÀI TẬP THỰC HÀNH TỰ LUYỆN KỸ NĂNG (CẤP ĐỘ 2: ĐỒ HỌA CANVAS & TƯƠNG TÁC ANDROID)

### Bài tập 5: Đổi màu đường vệt lướt phím (Glide Trail) theo giao diện Sáng / Tối
- **Yêu cầu**: Trong [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L156-L162), cây cọ `trailPaint` hiện tại dùng cố định màu xanh dương `#CC1A73E8`. Hãy viết đoạn code trong `onDraw` để khi ở giao diện Tối (`isDarkTheme == true`), vệt lướt phím đổi sang màu xanh ngọc sáng `#CC64B5F6` cho nổi bật trên nền đen.
- **Đáp án gợi ý**:
  Ngay trước khối vẽ `// Glide trail` (dòng 423 của `KeyboardCanvasView.kt`), thêm:
  ```kotlin
  trailPaint.color = if (isDarkTheme) {
      Color.parseColor("#CC64B5F6")
  } else {
      Color.parseColor("#CC1A73E8")
  }
  ```

### Bài tập 6: Thêm nấc chiều cao "Siêu thấp" (`ultra_short`) vào `KeyboardPreferences.kt`
- **Yêu cầu**: Người dùng có màn hình nhỏ muốn có thêm tùy chọn chiều cao `"ultra_short"` với tỷ lệ `0.75f`. Bạn cần sửa ở đâu trong [`KeyboardPreferences.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/settings/KeyboardPreferences.kt#L220-L229)?
- **Đáp án gợi ý**:
  Trong hàm `keyHeightScale()`, thêm nhánh `"ultra_short" -> 0.75f` vào khối `when (keyboardHeight)`.

### Bài tập 7: Tùy chỉnh thời gian giữ phím để lặp Backspace
- **Yêu cầu**: Hiện tại trong `KeyboardCanvasView.kt` (dòng 687), khi nhấn giữ phím `Backspace`, phải đợi `400L` (400ms) mới bắt đầu lặp xóa liên tục, và mỗi lần xóa cách nhau `50L` (50ms). Nếu bạn muốn phản hồi nhanh hơn (đợi `280ms` và lặp mỗi `35ms`), bạn sửa ở 2 dòng nào?
- **Đáp án gợi ý**:
  - Dòng 687 trong `KeyboardCanvasView.kt`: đổi `400L` thành `280L`.
  - Dòng 142 trong `backspaceRepeatRunnable`: đổi `50L` thành `35L`.

---

## CHƯƠNG 40: KHO BÀI TẬP THỰC HÀNH TỰ LUYỆN KỸ NĂNG (CẤP ĐỘ 3: TRÍ TUỆ NGÔN NGỮ & THUẬT TOÁN)

### Bài tập 8: Tính bằng tay khoảng cách Levenshtein
- **Yêu cầu**: Hãy tự vẽ bảng quy hoạch động hoặc đếm số bước ít nhứt (thêm, xóa, thay) để tính `levenshtein("viet", "vbuirt")` trong `GlideDecoder.kt`.
- **Đáp án gợi ý**:
  Biến `"viet"` (4 chữ) thành `"vbuirt"` (6 chữ):
  1. Giữ `'v'`.
  2. Thêm `'b'` và `'u'` trước `'i'` (+2 thao tác).
  3. Giữ `'i'`.
  4. Thay `'e'` thành `'r'` (+1 thao tác).
  5. Giữ `'t'`.
  -> Khoảng cách Levenshtein = **`3`** (thêm `b`, thêm `u`, thay `e -> r`).

### Bài tập 9: Giải mã ràng buộc chữ cái gốc trong `OnnxVietnameseCorrector`
- **Yêu cầu**: Giả sử đầu vào là từ `"duong"` (không dấu) và tại vị trí chữ `'o'` (`srcChar = 'o'`), mạng neural trả về điểm cao nhứt cho ký tự `'ờ'` (`score = 9.5`), cao nhì cho `'ơ'` (`score = 8.1`), và `'à'` (`score = 9.9`). Hỏi hàm `correctChunk` sẽ chọn chữ nào cho vị trí đó? Tại sao?
- **Đáp án gợi ý**:
  Hàm sẽ chọn **`'ờ'`** (`score = 9.5`), và bỏ qua `'à'` dù `'à'` có điểm `9.9` cao hơn. Lý do: `srcBase = "o"`, còn `'à'` có `candBase = "a" != "o"` nên bị lệnh `if (candBase != srcBase) continue` loại bỏ ngay!

### Bài tập 10: Viết bộ lọc Privacy Guard cho `SuggestionEngine`
- **Yêu cầu**: Nếu bạn muốn `SuggestionEngine.learn()` **không bao giờ học** các từ viết tắt toàn chữ HOA có độ dài từ 5 ký tự trở lên (thường là mã đơn hàng hoặc mã vé máy bay như `VNXAB`, `QH123`), bạn sẽ thêm điều kiện gì ở đầu hàm `learn()`?
- **Đáp án gợi ý**:
  Ngay sau dòng `val w = word.trim().lowercase()`, kiểm tra trên biến gốc `word` trước khi chuyển về chữ thường:
  ```kotlin
  val rawTrimmed = word.trim()
  if (rawTrimmed.length >= 5 && rawTrimmed.all { it.isUpperCase() }) return
  ```


---

# PHẦN X: TOÁN HỌC CỐT LÕI ĐẰNG SAU MỌI TÍNH NĂNG CỦA VNCHFLEX (CHƯƠNG 41–43)

Nhiều bạn mới học lập trình hay sợ toán, nhưng thiệt ra toán học trong `vnchFlex` chính là **ngôn ngữ ngắn gọn và chánh xác nhứt** để diễn đạt ý tưởng trước khi viết thành code. Chương này sẽ tháo rời từng phương trình toán học đang chạy bên trong `vnchFlex`, giải thích cặn kẽ từng ký hiệu một để bạn thấy mỗi công thức đều có lý do sống còn của nó.

---

## CHƯƠNG 41: TOÁN HỌC HÌNH HỌC & CẢM ỨNG TRONG `KeyboardCanvasView`

### 41.1. Phương trình chia chiều rộng phím theo Trọng số (`weight`)
$$\text{cellW}_k = \text{availW} \times \frac{w_k}{\sum_{j=1}^{M} w_j}$$
- **Giải thích từng ký hiệu**:
  - $\text{availW}$: Tổng chiều ngang còn lại của hàng phím (đã trừ lề thụt hai bên `sideInset`).
  - $M$: Tổng số phím trên hàng đó (ví dụ hàng `Q..P` có $M=10$, hàng dưới cùng có $M=6$).
  - $w_k$: Trọng số của phím thứ $k$ (`key.weight`). Phím chữ bình thường có $w = 1.0$, phím `Space` có $w = 5.2$.
  - $\sum_{j=1}^{M} w_j$: Tổng trọng số của cả hàng.
- **Ý nghĩa thực tế**: Chia tỷ lệ như vậy bảo đảm tổng chiều rộng của tất cả các phím trên một hàng **luôn luôn bằng đúng 100% $\text{availW}$**, bất kể người dùng bật/tắt phím chuyển ngôn ngữ hay xoay ngang màn hình.

### 41.2. Khoảng cách Euclid 2D kích hoạt Lướt phím & Chuẩn hóa tọa độ về $[0, 1]^2$
1. **Khoảng cách di chuyển ngón tay**:
   $$d = \sqrt{(m_x - s_x)^2 + (m_y - s_y)^2}$$
   - $(s_x, s_y)$ là tọa độ điểm đặt ngón tay ban đầu (`startX, startY`); $(m_x, m_y)$ là tọa độ hiện tại khi ngón tay đang kéo (`mx, my`).
   - Khi $d > \text{glideThresholdDp} \times \text{density}$, bộ gõ biết chắc người dùng đang **lướt phím** chớ không phải bấm nhầm rung tay tại chỗ.
2. **Chuẩn hóa tọa độ về hình vuông đơn vị $[0, 1]^2$**:
   $$\hat{x} = \frac{p_x - x_{\min}}{x_{\max} - x_{\min}}, \qquad \hat{y} = \frac{p_y - y_{\min}}{y_{\max} - y_{\min}}$$
   - $(x_{\min}, y_{\min})$ và $(x_{\max}, y_{\max})$ là khung bao quanh toàn bộ vùng phím chữ cái.
   - **Tại sao phải chia tỷ lệ này?** Vì điện thoại màn hình nhỏ (720px) và điện thoại màn hình lớn (1440px) có kích thước điểm ảnh khác nhau. Khi đưa về $[0, 1]^2$, phím `Q` luôn nằm ở góc $(0.05, 0.16)$ và phím `M` luôn nằm ở $(0.80, 0.84)$ trên **mọi chiếc điện thoại**, giúp mô hình AI `neural_glide.onnx` nhận diện chuẩn xác không phụ thuộc kích cỡ màn hình!

---

## CHƯƠNG 42: TOÁN HỌC NGÔN NGỮ & XÁC SUẤT TRONG `SuggestionEngine`

### 42.1. Xác suất có điều kiện $P(w_i \mid w_{i-2}, w_{i-1})$
$$P(w_i \mid w_{i-2}, w_{i-1}) = \frac{\text{Count}(w_{i-2}, w_{i-1}, w_i)}{\text{Count}(w_{i-2}, w_{i-1})}$$
- Ký hiệu dấu gạch đứng `|` đọc là **"khi đã biết trước"**.
- Công thức này trả lời câu hỏi: *"Khi đã biết 2 từ đứng ngay trước là $(w_{i-2}, w_{i-1})$, khả năng từ thứ ba là $w_i$ cao tới mức nào?"*
- Ví dụ: Khi $(w_{i-2}, w_{i-1}) = (\text{"chúc"}, \text{"mừng"})$, số lần cụm `"chúc mừng năm"` xuất hiện áp đảo hoàn toàn các cụm khác, nên xác suất $P(\text{"năm"} \mid \text{"chúc"}, \text{"mừng"})$ cao nhứt.

### 42.2. Phương trình chấm điểm tổng hợp ứng viên (`bestAccentedMatch`)
$$\text{Score}(w) = \text{BaseUnigram}(w) + 18 \times \text{UserCount}(w) + 35 \times \text{ContextWeight}(w)$$
- $\text{BaseUnigram}(w)$: Độ phổ biến gốc của từ $w$ trong Tiếng Việt (từ `0` tới `100`).
- $18 \times \text{UserCount}(w)$: Hệ số `18` nhân với số lần chính bạn đã gõ từ $w$ trong quá khứ — giúp thói quen cá nhân của bạn thắng được từ điển chung nếu bạn hay dùng từ đó.
- $35 \times \text{ContextWeight}(w)$: Hệ số `35` (lớn nhứt trong 3 thành phần) dành cho ngữ cảnh Bigram/Trigram. Tại sao nhân `35`? Vì **ngữ cảnh câu văn quan trọng hơn tần suất đứng một mình**: chữ `"nam"` đứng riêng có thể thua chữ `"năm"`, nhưng đứng sau `"việt"` thì điểm ngữ cảnh nhân `35` sẽ kéo `"nam"` lên hạng 1 ngay lập tức.

---

## CHƯƠNG 43: TOÁN HỌC LƯỚT PHÍM & TRÍ TUỆ NHÂN TẠO (`GlideDecoder` & `OnnxVietnameseCorrector`)

### 43.1. Phương trình Quy hoạch động Uốn cong Thời gian (`DTW`)
$$\text{dp}[i][j] = \underbrace{\sqrt{(x_i^A - x_j^B)^2 + (y_i^A - y_j^B)^2}}_{\text{Khoảng cách tại bước }(i,j)} + \min\Big(\underbrace{\text{dp}[i-1][j]}_{\text{Đi tiếp trên }A},\; \underbrace{\text{dp}[i][j-1]}_{\text{Đi tiếp trên }B},\; \underbrace{\text{dp}[i-1][j-1]}_{\text{Cả hai cùng bước}}\Big)$$
- **Ý nghĩa**: Giả sử đường lướt thực tế của ngón tay có $m = 40$ điểm, còn đường gấp khúc lý tưởng của từ `"việt"` chỉ có $n = 4$ điểm (`v, i, e, t`). Hai đường có số điểm khác nhau nên không thể so sánh 1-chọi-1 được.
- Bảng $\text{dp}[i][j]$ cho phép nhiều điểm liên tiếp trên đường ngón tay cùng ghép vào 1 đỉnh chữ cái (uốn cong thời gian) sao cho tổng khoảng cách hình học là nhỏ nhứt. Cuối cùng chia cho $\max(n, m)$ để chuẩn hóa theo độ dài đường đi.

### 43.2. Nội suy Đẳng cự & Véc-tơ Động học 6 chiều cho Mạng Thần kinh
1. **Tổng chiều dài cung**:
   $$L_{\text{total}} = \sum_{i=1}^{N-1} \sqrt{(x_i - x_{i-1})^2 + (y_i - y_{i-1})^2}$$
   Dùng để chia đều đường lướt thành đúng `48` điểm cách đều nhau về mặt không gian, loại bỏ hoàn toàn sự khác biệt giữa người lướt nhanh và người lướt chậm.
2. **Véc-tơ 6 chiều tại bước $t$**:
   $$\mathbf{f}_t = \big[\; rx_t,\; ry_t,\; \Delta x_t,\; \Delta y_t,\; \sin(\theta_t),\; \cos(\theta_t) \;\big]$$
   Trong đó $\theta_t = \text{atan2}(\Delta y_t, \Delta x_t)$ là góc hướng di chuyển của ngón tay. Dùng cặp $(\sin\theta_t, \cos\theta_t)$ thay vì dùng trực tiếp góc $\theta_t$ giúp mạng thần kinh không bị "nhảy số" đột ngột ở ranh giới $-\pi$ và $+\pi$ (ví dụ khi ngón tay quay đầu $180^\circ$).

### 43.3. Giải mã có Ràng buộc Chữ cái Gốc (`OnnxVietnameseCorrector`)
$$\hat{c}_t = \arg\max_{v \in \mathcal{V},\; \text{strip}(v) = \text{strip}(x_t)} \text{logits}[t][v]$$
- $\mathcal{V}$ là toàn bộ tập ký tự trong `vi_corrector_vocab.json`.
- Điều kiện lọc $\text{strip}(v) = \text{strip}(x_t)$ thu hẹp không gian tìm kiếm từ hàng trăm ký tự xuống chỉ còn đúng nhóm chữ cái có cùng gốc với ký tự đầu vào $x_t$, giúp AI chỉ sửa dấu thanh/mũ/móc và tuyệt đối không bao giờ làm biến dạng chữ cái gốc.


---

## CHƯƠNG 44: CÁC PHƯƠNG TRÌNH ĐỒ HỌA BÉZIER, KHOẢNG CÁCH LEVENSHTEIN & TỰ ĐỘNG SỬA LỖI THIẾU DẤU

### 44.1. Đường cong Bézier bậc 2 (`quadTo`) làm mượt vệt lướt phím 120FPS
Khi bạn lướt ngón tay trên màn hình, cảm ứng trả về một chuỗi các điểm rời rạc $\mathbf{P}_0, \mathbf{P}_1, \mathbf{P}_2, \dots$ Nếu nối thẳng các điểm đó bằng đoạn thẳng, đường vẽ sẽ bị gãy khúc răng cưa.
Trong [`KeyboardCanvasView.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/KeyboardCanvasView.kt#L423-L432), tụi mình dùng **Đường cong Bézier bậc 2 (`Quadratic Bézier`)** đi qua trung điểm của hai điểm liên tiếp:
$$\mathbf{B}(t) = (1 - t)^2 \mathbf{P}_{\text{prev}} + 2(1 - t)t \,\mathbf{P}_{\text{ctrl}} + t^2 \mathbf{P}_{\text{mid}}, \qquad t \in [0, 1]$$
Trong đó $\mathbf{P}_{\text{mid}} = \frac{\mathbf{P}_{\text{prev}} + \mathbf{P}_{\text{cur}}}{2}$. Nhờ vậy vệt mực xanh lướt theo ngón tay lúc nào cũng cong tròn mượt mà ở 120FPS!

### 44.2. Phương trình Quy hoạch động Khoảng cách Chỉnh sửa (`Levenshtein Distance`)
Trong [`GlideDecoder.kt`](../app/src/main/java/com/vnchflex/vietkeyboard/ime/GlideDecoder.kt#L244-L259), số thao tác ít nhứt để biến chuỗi $a$ thành chuỗi $b$ được tính bằng:
$$\text{dp}[i][j] = \begin{cases} \text{dp}[i-1][j-1] & \text{nếu } a_{i-1} = b_{j-1} \\ 1 + \min\big(\text{dp}[i-1][j],\; \text{dp}[i][j-1],\; \text{dp}[i-1][j-1]\big) & \text{ngược lại} \end{cases}$$
- $\text{dp}[i-1][j] + 1$: Phép **Xóa** 1 ký tự ở chuỗi $a$.
- $\text{dp}[i][j-1] + 1$: Phép **Chèn** 1 ký tự vào chuỗi $a$.
- $\text{dp}[i-1][j-1] + 1$: Phép **Thay thế** 1 ký tự.
Nhờ phương trình này, `GlideDecoder` đo chính xác mức độ sai lệch giữa đường lướt thực tế và từ mục tiêu.
