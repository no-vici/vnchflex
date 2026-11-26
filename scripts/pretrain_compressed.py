"""Huấn luyện đa nguồn có cân bằng trọng số và xuất file nén GZIP cho vnchFlex.

Đọc dữ liệu từ các thư mục con trong `corpus_data/` với hệ số nhân riêng biệt
để tránh văn phong Wikipedia/báo chí lấn át phương ngữ Nam Bộ và thuật ngữ lập trình.
Xuất ra `vi_unigrams.txt.gz`, `en_unigrams.txt.gz` và `vi_ngrams.tsv.gz`.
"""

import gzip
import re
from collections import Counter, defaultdict
from pathlib import Path

try:
    from pypdf import PdfReader
except ImportError:
    PdfReader = None

VI_CHARS = "a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
SENTENCE_SPLIT_RE = re.compile(r"[.!?\n;:\(\)\[\]\"“”]+")
VI_SYLLABLE_RE = re.compile(rf"^[{VI_CHARS}]{{2,7}}$")
EN_WORD_RE = re.compile(r"^[a-z0-9_-]{2,24}$")

DOMAIN_WEIGHTS = {
    "wiki": 1.0,
    "southern_chat": 12.0,
    "tech_dev": 8.0,
}


def extract_clean_text(path: Path) -> str:
    ext = path.suffix.lower()
    if ext == ".pdf" and PdfReader is not None:
        reader = PdfReader(str(path))
        raw = "\n".join(page.extract_text() or "" for page in reader.pages)
        return re.sub(r"-\s*\n\s*", "", raw)
    if ext in {".md", ".txt", ".csv"}:
        raw = path.read_text(encoding="utf-8", errors="ignore")
        if ext == ".md":
            raw = re.sub(r"```[\s\S]*?```", " ", raw)
            raw = re.sub(r"`[^`]*`", " ", raw)
            raw = re.sub(r"\[([^\]]+)\]\([^\)]+\)", r"\1", raw)
            raw = re.sub(r"https?://\S+", " ", raw)
            raw = re.sub(r"[#*_~>|\\-]+", " ", raw)
        return raw
    return ""


def get_domain_multiplier(file_path: Path, root_dir: Path) -> float:
    rel_parts = file_path.relative_to(root_dir).parts
    if len(rel_parts) > 1:
        folder_name = rel_parts[0].lower()
        return DOMAIN_WEIGHTS.get(folder_name, 1.0)
    return 1.0


def build_compressed_assets(
    corpus_dir: str = "./corpus_data",
    assets_dir: str = "app/src/main/assets",
    max_vi_unigrams: int = 15000,
    max_en_unigrams: int = 8000,
    max_bigrams: int = 10000,
    max_trigrams: int = 50000,
    top_followers: int = 5,
    min_weighted_score: float = 2.0,
) -> None:
    src_root = Path(corpus_dir)
    if not src_root.exists():
        for sub in DOMAIN_WEIGHTS:
            (src_root / sub).mkdir(parents=True, exist_ok=True)
        print(f"Đã tạo cấu trúc thư mục '{corpus_dir}/{{wiki,southern_chat,tech_dev}}'. Hãy bỏ tài liệu vào và chạy lại!")
        return

    vi_uni = defaultdict(float)
    en_uni = defaultdict(float)
    ngrams = defaultdict(lambda: defaultdict(float))

    for f in sorted(src_root.rglob("*")):
        if not f.is_file():
            continue
        text = extract_clean_text(f)
        if not text.strip():
            continue

        mult = get_domain_multiplier(f, src_root)
        print(f"[x{mult:g}] Đang học: {f.relative_to(src_root)}")

        for sentence in SENTENCE_SPLIT_RE.split(text.lower()):
            vi_tokens = []
            for t in sentence.split():
                if VI_SYLLABLE_RE.match(t):
                    vi_uni[t] += mult
                    vi_tokens.append(t)
                    if EN_WORD_RE.match(t) and len(t) >= 3:
                        en_uni[t] += mult
                elif EN_WORD_RE.match(t):
                    en_uni[t] += mult

            for i, w in enumerate(vi_tokens):
                if i >= 1:
                    ngrams[vi_tokens[i - 1]][w] += mult
                if i >= 2:
                    ngrams[f"{vi_tokens[i - 2]} {vi_tokens[i - 1]}"][w] += mult

    out_dir = Path(assets_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    sorted_vi = [w for w, s in sorted(vi_uni.items(), key=lambda x: -x[1]) if s >= min_weighted_score][:max_vi_unigrams]
    vi_lines = [
        f"{w}\t{max(60, 99 - int((idx / max(1, len(sorted_vi))) * 39))}"
        for idx, w in enumerate(sorted_vi)
    ]
    with gzip.open(out_dir / "vi_unigrams.txt.gz", "wt", encoding="utf-8", compresslevel=9) as gz:
        gz.write("\n".join(vi_lines))

    sorted_en = [w for w, s in sorted(en_uni.items(), key=lambda x: -x[1]) if s >= min_weighted_score][:max_en_unigrams]
    en_lines = [
        f"{w}\t{max(62, 98 - int((idx / max(1, len(sorted_en))) * 36))}"
        for idx, w in enumerate(sorted_en)
    ]
    with gzip.open(out_dir / "en_unigrams.txt.gz", "wt", encoding="utf-8", compresslevel=9) as gz:
        gz.write("\n".join(en_lines))

    bi_items = [(k, v) for k, v in ngrams.items() if " " not in k]
    tri_items = [(k, v) for k, v in ngrams.items() if " " in k]
    bi_sorted = sorted(bi_items, key=lambda x: sum(x[1].values()), reverse=True)[:max_bigrams]
    tri_sorted = sorted(tri_items, key=lambda x: sum(x[1].values()), reverse=True)[:max_trigrams]

    ngram_lines = []
    for ctx, followers_dict in bi_sorted + tri_sorted:
        top_w = [
            w for w, s in sorted(followers_dict.items(), key=lambda x: -x[1])
            if s >= min_weighted_score
        ][:top_followers]
        if top_w:
            ngram_lines.append(f"{ctx}\t{','.join(top_w)}")

    gz_path = out_dir / "vi_ngrams.tsv.gz"
    with gzip.open(gz_path, "wt", encoding="utf-8", compresslevel=9) as gz:
        gz.write("\n".join(ngram_lines))

    size_kb = gz_path.stat().st_size / 1024
    print(f"\nHoàn tất! Đã nén {len(ngram_lines)} N-grams vào {gz_path} ({size_kb:.1f} KB).")


if __name__ == "__main__":
    build_compressed_assets()
