"""Huấn luyện mô hình BiGRU sửa dấu & lỗi gõ tiếng Việt (~7–8 MB)
từ kho văn bản tự chọn (.txt, .md, .pdf) và xuất ra ONNX INT8.
Hỗ trợ bảo toàn phụ âm kép `dz-` (dzìa, dzách, dzô...).
"""

import json
import random
import re
import unicodedata
from pathlib import Path
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from onnxruntime.quantization import quantize_dynamic, QuantType

try:
    from pypdf import PdfReader
except ImportError:
    PdfReader = None

SPECIAL_TOKENS = ["<PAD>", "<UNK>"]
BASE_CHARS = (
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "0123456789 .,!?;:'\"()-_/\\@#&*+%=<>[]{}~`^|\n"
    "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
    "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ"
)
VOCAB = SPECIAL_TOKENS + list(dict.fromkeys(BASE_CHARS))
CHAR2ID = {c: i for i, c in enumerate(VOCAB)}
MAX_LEN = 128

KEY_NEIGHBORS = {
    "a": "qwsz", "s": "weadzx", "d": "ersfxc", "f": "rtgvcd", "g": "tyhbvf",
    "h": "yujnbg", "j": "uikmnh", "k": "iojlm", "l": "opk", "q": "wa",
    "w": "qeas", "e": "wrsd", "r": "etdf", "t": "ryfg", "y": "tugh",
    "u": "yihj", "i": "uojk", "o": "ipkl", "p": "ol", "z": "asx",
    "x": "zsdc", "c": "xdfv", "v": "cfgb", "b": "vghn", "n": "bhjm", "m": "njk",
}

VOWEL_GROUPS = [
    "aáàảãạ", "ăắằẳẵặ", "âấầẩẫậ",
    "eéèẻẽẹ", "êếềểễệ",
    "iíìỉĩị",
    "oóòỏõọ", "ôốồổỗộ", "ơớờởỡợ",
    "uúùủũụ", "ưứừửữự",
    "yýỳỷỹỵ",
]
CHAR_TO_GROUP = {ch: grp for grp in VOWEL_GROUPS for ch in grp}


def strip_diacritics_char(ch: str) -> str:
    if ch == "đ":
        return "d"
    if ch == "Đ":
        return "D"
    nfd = unicodedata.normalize("NFD", ch)
    return "".join(c for c in nfd if unicodedata.category(c) != "Mn")


def corrupt_vietnamese_sentence(clean: str, rng: random.Random) -> str:
    """Tạo nhiễu gõ phím thực tế (mất dấu, nhầm dấu thanh, lệch phím) giữ nguyên độ dài."""
    chars = list(clean)
    strip_whole_word = rng.random() < 0.35
    in_strip_word = False

    for i, ch in enumerate(chars):
        if ch.isspace():
            in_strip_word = strip_whole_word and (rng.random() < 0.5)
            continue

        lower = ch.lower()
        # Bảo toàn phụ âm kép dz- (dzìa, dzách, dzô...)
        if lower == "d" and i + 1 < len(chars) and chars[i + 1].lower() == "z":
            continue
        if lower == "z" and i > 0 and chars[i - 1].lower() == "d":
            continue

        # 1. Gõ không dấu cả từ hoặc từng chữ
        if in_strip_word or (lower in CHAR_TO_GROUP and rng.random() < 0.25):
            chars[i] = strip_diacritics_char(ch)
            continue

        # 2. Nhầm dấu thanh / dấu mũ trên nguyên âm
        if lower in CHAR_TO_GROUP and rng.random() < 0.12:
            grp = CHAR_TO_GROUP[lower]
            alt = rng.choice(grp)
            chars[i] = alt.upper() if ch.isupper() else alt
            continue

        # 3. Chạm lệch phím hàng xóm QWERTY
        if lower in KEY_NEIGHBORS and rng.random() < 0.04:
            nb = rng.choice(KEY_NEIGHBORS[lower])
            chars[i] = nb.upper() if ch.isupper() else nb

    return "".join(chars)


def load_sentences_from_folder(corpus_dir: str) -> list[str]:
    """Đọc các câu sạch từ thư mục chứa .txt, .md, .pdf tự chọn."""
    sentences = []
    split_re = re.compile(r"[.!?\n;]+")
    for path in sorted(Path(corpus_dir).rglob("*")):
        if not path.is_file():
            continue
        ext = path.suffix.lower()
        raw = ""
        if ext in {".txt", ".md"}:
            raw = path.read_text(encoding="utf-8", errors="ignore")
            if ext == ".md":
                raw = re.sub(r"```[\s\S]*?```|`[^`]*`|[#*_~>|\\-]+", " ", raw)
        elif ext == ".pdf" and PdfReader is not None:
            reader = PdfReader(str(path))
            raw = "\n".join(p.extract_text() or "" for p in reader.pages)

        for seg in split_re.split(raw):
            clean = re.sub(r"\s+", " ", seg).strip()
            if 8 <= len(clean) <= MAX_LEN:
                sentences.append(clean)
    return sentences


def encode_text(text: str, max_len: int = MAX_LEN) -> list[int]:
    ids = [CHAR2ID.get(c, 1) for c in text[:max_len]]
    return ids + [0] * (max_len - len(ids))


class DynamicNoisyDataset(Dataset):
    def __init__(self, sentences: list[str], seed: int = 42):
        self.sentences = sentences
        self.rng = random.Random(seed)

    def __len__(self):
        return len(self.sentences)

    def __getitem__(self, idx):
        tgt = self.sentences[idx]
        src = corrupt_vietnamese_sentence(tgt, self.rng)
        return (
            torch.tensor(encode_text(src), dtype=torch.long),
            torch.tensor(encode_text(tgt), dtype=torch.long),
        )


class MiniBiGRUCorrector(nn.Module):
    def __init__(self, vocab_size: int, emb_dim: int = 128, hidden_dim: int = 256):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size, emb_dim, padding_idx=0)
        self.gru = nn.GRU(
            emb_dim, hidden_dim, num_layers=2, batch_first=True, bidirectional=True, dropout=0.15
        )
        self.fc = nn.Linear(hidden_dim * 2, vocab_size)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.fc(self.gru(self.embedding(x))[0])


def train_custom_onnx(
    corpus_dir: str = "./corpus_data",
    out_dir: str = "app/src/main/assets",
    epochs: int = 8,
    batch_size: int = 256,
):
    sentences = load_sentences_from_folder(corpus_dir)
    if not sentences:
        Path(corpus_dir).mkdir(parents=True, exist_ok=True)
        print(f"Hãy chép các file .txt, .md, .pdf nguồn của bạn vào '{corpus_dir}' rồi chạy lại!")
        return

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Đã nạp {len(sentences)} câu từ '{corpus_dir}'. Bắt đầu train trên {device}...")

    loader = DataLoader(DynamicNoisyDataset(sentences), batch_size=batch_size, shuffle=True)
    model = MiniBiGRUCorrector(len(VOCAB)).to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=2e-3)
    criterion = nn.CrossEntropyLoss(ignore_index=0)

    for epoch in range(1, epochs + 1):
        model.train()
        total_loss = 0.0
        for x, y in loader:
            x, y = x.to(device), y.to(device)
            optimizer.zero_grad()
            loss = criterion(model(x).view(-1, len(VOCAB)), y.view(-1))
            loss.backward()
            optimizer.step()
            total_loss += loss.item()
        print(f"Epoch {epoch}/{epochs} - Loss: {total_loss / len(loader):.4f}")

    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)
    fp32_onnx = out_path / "vi_corrector_fp32.onnx"
    int8_onnx = out_path / "vi_corrector.onnx"

    model.eval().cpu()
    torch.onnx.export(
        model,
        torch.zeros((1, MAX_LEN), dtype=torch.long),
        str(fp32_onnx),
        input_names=["input_ids"],
        output_names=["logits"],
        dynamic_axes={"input_ids": {0: "batch", 1: "seq"}, "logits": {0: "batch", 1: "seq"}},
        opset_version=14,
    )
    quantize_dynamic(str(fp32_onnx), str(int8_onnx), weight_type=QuantType.QInt8)
    fp32_onnx.unlink(missing_ok=True)

    (out_path / "vi_corrector_vocab.json").write_text(
        json.dumps(VOCAB, ensure_ascii=False), encoding="utf-8"
    )
    print(f"Hoàn tất! File model nặng {int8_onnx.stat().st_size / (1024 * 1024):.2f} MB tại {int8_onnx}")


if __name__ == "__main__":
    train_custom_onnx()
