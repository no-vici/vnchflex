"""Huấn luyện trước (pre-train) bộ dữ liệu từ vựng và mô hình N-gram cho vnchFlex.

Script này quét đệ quy các tài liệu văn bản (.pdf, .md, .txt, .csv) trong thư mục
nguồn, làm sạch định dạng và trích xuất tần suất từ đơn (Unigram) cùng ngữ cảnh
2-3 từ liên tiếp (Bigram, Trigram) cho cả tiếng Việt và tiếng Anh.

Các file đầu ra được ghi thẳng vào thư mục `app/src/main/assets/` để đóng gói
vào APK khi build, giúp bàn phím gợi ý từ và giải mã Glide Typing thông minh
ngay từ lần mở đầu tiên:
    - vi_unigrams.txt : Danh sách âm tiết/từ tiếng Việt kèm trọng số (60..99).
    - en_unigrams.txt : Danh sách từ tiếng Anh (gộp và giữ lại từ vựng cũ).
    - vi_bigrams.json : Bản đồ ngữ cảnh 1 từ -> danh sách từ kế tiếp phổ biến.
    - vi_trigrams.json: Bản đồ ngữ cảnh 2 từ -> danh sách từ kế tiếp phổ biến.
"""

import json
import re
from collections import Counter, defaultdict
from pathlib import Path

try:
    from pypdf import PdfReader
except ImportError:
    PdfReader = None

VI_CHARS = "a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
EN_CHARS = "a-z"
SENTENCE_SPLIT_RE = re.compile(r"[.!?\n;:\(\)\[\]\"“”]+")
VI_SYLLABLE_RE = re.compile(rf"^[{VI_CHARS}]{{2,7}}$")
EN_WORD_RE = re.compile(rf"^[{EN_CHARS}]{{2,20}}$")


def clean_markdown(raw: str) -> str:
    """Loại bỏ cú pháp định dạng Markdown, mã nguồn, thẻ HTML và đường dẫn URL.

    Giúp trích xuất phần văn bản tự nhiên thuần túy trong tài liệu Markdown mà
    không làm dính các ký tự đặc biệt (`#`, `*`, `` ` ``, `_`) vào từ vựng.

    Args:
        raw: Chuỗi nội dung thô đọc từ file Markdown (.md).

    Returns:
        Chuỗi văn bản sạch đã loại bỏ cú pháp đánh dấu và liên kết.
    """
    raw = re.sub(r"```[\s\S]*?```", " ", raw)
    raw = re.sub(r"`[^`]*`", " ", raw)
    raw = re.sub(r"\[([^\]]+)\]\([^\)]+\)", r"\1", raw)
    raw = re.sub(r"https?://\S+", " ", raw)
    raw = re.sub(r"<[^>]+>", " ", raw)
    raw = re.sub(r"[#*_~>|\\-]+", " ", raw)
    return raw


def extract_text(path: Path) -> str:
    """Đọc và trích xuất văn bản thuần túy từ một file tài liệu theo phần mở rộng.

    Hỗ trợ các định dạng `.pdf` (qua thư viện `pypdf`, có nối từ bị ngắt dòng
    cuối trang), `.md` (làm sạch cú pháp Markdown), `.txt` và `.csv`.

    Args:
        path: Đối tượng `Path` trỏ tới file cần trích xuất nội dung.

    Returns:
        Chuỗi văn bản đã trích xuất, hoặc chuỗi rỗng `""` nếu định dạng không
        được hỗ trợ hoặc gặp lỗi khi đọc file.
    """
    ext = path.suffix.lower()
    if ext == ".pdf":
        if PdfReader is None:
            print(f"[Bỏ qua PDF] Chưa cài pypdf (chạy: pip install pypdf): {path.name}")
            return ""
        try:
            reader = PdfReader(str(path))
            raw = "\n".join(page.extract_text() or "" for page in reader.pages)
            return re.sub(r"-\s*\n\s*", "", raw)
        except Exception as e:
            print(f"[Lỗi đọc PDF] {path.name}: {e}")
            return ""
    if ext == ".md":
        raw = path.read_text(encoding="utf-8", errors="ignore")
        return clean_markdown(raw)
    if ext in {".txt", ".csv"}:
        return path.read_text(encoding="utf-8", errors="ignore")
    return ""


def build_pretrained_assets(
    corpus_dir: str = "./corpus_data",
    assets_dir: str = "app/src/main/assets",
    max_vi_unigrams: int = 12000,
    max_en_unigrams: int = 5000,
    max_bigrams: int = 4000,
    max_trigrams: int = 6000,
    top_followers: int = 6,
    min_freq: int = 2,
) -> None:
    """Quét kho tài liệu huấn luyện và xuất các file từ điển/N-gram vào thư mục assets.

    Tự động tạo thư mục `corpus_dir` nếu chưa tồn tại. Với các file hợp lệ tìm thấy,
    hàm sẽ tách câu theo dấu ngắt câu để tránh ghép nhầm ngữ cảnh giữa hai câu,
    đếm tần suất xuất hiện và ghi kết quả ra 4 file trong `assets_dir`.

    Args:
        corpus_dir: Đường dẫn thư mục chứa tài liệu đầu vào (.pdf, .md, .txt, .csv).
        assets_dir: Đường dẫn thư mục `assets` của ứng dụng Android để lưu kết quả.
        max_vi_unigrams: Số lượng từ/âm tiết tiếng Việt tối đa xuất ra `vi_unigrams.txt`.
        max_en_unigrams: Số lượng từ tiếng Anh tối đa học thêm để gộp vào `en_unigrams.txt`.
        max_bigrams: Số lượng ngữ cảnh 1 từ tối đa lưu trong `vi_bigrams.json`.
        max_trigrams: Số lượng ngữ cảnh 2 từ tối đa lưu trong `vi_trigrams.json`.
        top_followers: Số lượng từ gợi ý kế tiếp tối đa giữ lại cho mỗi ngữ cảnh.
        min_freq: Ngưỡng tần suất xuất hiện tối thiểu để một từ/cụm từ được giữ lại.
    """
    src_folder = Path(corpus_dir)
    if not src_folder.exists():
        src_folder.mkdir(parents=True, exist_ok=True)
        print(f"Đã tạo thư mục '{corpus_dir}'. Hãy chép các file .pdf, .md, .txt vào đây rồi chạy lại!")
        return

    vi_unigrams = Counter()
    en_unigrams = Counter()
    bigrams = defaultdict(Counter)
    trigrams = defaultdict(Counter)

    file_count = 0
    for f in sorted(src_folder.rglob("*")):
        if not f.is_file():
            continue
        text = extract_text(f)
        if not text.strip():
            continue

        file_count += 1
        print(f"[{file_count}] Đang học: {f.relative_to(src_folder)}")

        for sentence in SENTENCE_SPLIT_RE.split(text.lower()):
            raw_tokens = sentence.split()
            vi_tokens = []

            for t in raw_tokens:
                if VI_SYLLABLE_RE.match(t):
                    vi_unigrams[t] += 1
                    vi_tokens.append(t)
                    if EN_WORD_RE.match(t) and len(t) >= 3:
                        en_unigrams[t] += 1
                elif EN_WORD_RE.match(t):
                    en_unigrams[t] += 1

            for i, w in enumerate(vi_tokens):
                if i >= 1:
                    bigrams[vi_tokens[i - 1]][w] += 1
                if i >= 2:
                    trigrams[f"{vi_tokens[i - 2]} {vi_tokens[i - 1]}"][w] += 1

    if file_count == 0:
        print(f"Không tìm thấy file .pdf, .md, .txt hay .csv nào trong '{corpus_dir}'.")
        return

    out_dir = Path(assets_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    # 1. Xuất vi_unigrams.txt (từ<tab>trọng_số 60..99)
    top_vi = [w for w, c in vi_unigrams.most_common(max_vi_unigrams) if c >= min_freq]
    vi_lines = []
    for rank, w in enumerate(top_vi):
        weight = max(60, 99 - int((rank / max(1, len(top_vi))) * 39))
        vi_lines.append(f"{w}\t{weight}")
    (out_dir / "vi_unigrams.txt").write_text("\n".join(vi_lines), encoding="utf-8")

    # 2. Gộp thêm từ tiếng Anh mới học được vào en_unigrams.txt hiện có (giữ nguyên các từ cũ)
    en_file = out_dir / "en_unigrams.txt"
    existing_en = {}
    if en_file.exists():
        for line in en_file.read_text(encoding="utf-8").splitlines():
            parts = line.strip().split("\t")
            if parts and parts[0]:
                existing_en[parts[0]] = int(parts[1]) if len(parts) > 1 and parts[1].isdigit() else 75

    top_en = [w for w, c in en_unigrams.most_common(max_en_unigrams) if c >= min_freq]
    for rank, w in enumerate(top_en):
        weight = max(62, 95 - int((rank / max(1, len(top_en))) * 33))
        if w not in existing_en or weight > existing_en[w]:
            existing_en[w] = weight

    sorted_en = sorted(existing_en.items(), key=lambda x: (-x[1], x[0]))
    en_file.write_text("\n".join(f"{w}\t{wt}" for w, wt in sorted_en), encoding="utf-8")

    # 3. Xuất vi_bigrams.json
    bi_out = {}
    for ctx, cnt in sorted(bigrams.items(), key=lambda x: sum(x[1].values()), reverse=True)[:max_bigrams]:
        f_list = [w for w, c in cnt.most_common(top_followers) if c >= min_freq]
        if f_list:
            bi_out[ctx] = f_list
    (out_dir / "vi_bigrams.json").write_text(
        json.dumps(bi_out, ensure_ascii=False, separators=(",", ":")), encoding="utf-8"
    )

    # 4. Xuất vi_trigrams.json
    tri_out = {}
    for ctx, cnt in sorted(trigrams.items(), key=lambda x: sum(x[1].values()), reverse=True)[:max_trigrams]:
        f_list = [w for w, c in cnt.most_common(top_followers) if c >= min_freq]
        if f_list:
            tri_out[ctx] = f_list
    (out_dir / "vi_trigrams.json").write_text(
        json.dumps(tri_out, ensure_ascii=False, separators=(",", ":")), encoding="utf-8"
    )

    print(
        f"\nHoàn tất huấn luyện từ {file_count} file -> {out_dir}/\n"
        f"- vi_unigrams.txt : {len(vi_lines)} từ tiếng Việt\n"
        f"- en_unigrams.txt : {len(sorted_en)} từ tiếng Anh\n"
        f"- vi_bigrams.json : {len(bi_out)} ngữ cảnh Bigram\n"
        f"- vi_trigrams.json: {len(tri_out)} ngữ cảnh Trigram"
    )


if __name__ == "__main__":
    build_pretrained_assets(corpus_dir="./corpus_data", assets_dir="app/src/main/assets")
