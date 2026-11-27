"""Huấn luyện mô hình BiGRU sửa lỗi chính tả & dấu tiếng Việt siêu nhẹ (~8 MB)
từ dataset `coung21/vi-spelling-correction` và xuất ra định dạng ONNX INT8.
"""

import json
from pathlib import Path
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from datasets import load_dataset
from onnxruntime.quantization import quantize_dynamic, QuantType

# ==================== 1. BẢNG KÝ TỰ TIẾNG VIỆT & ASCII ====================
SPECIAL_TOKENS = ["<PAD>", "<UNK>"]
BASE_CHARS = (
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "0123456789 .,!?;:'\"()-_/\\@#&*+%=<>[]{}~`^|\n"
    "áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ"
    "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸỴĐ"
)
VOCAB = SPECIAL_TOKENS + list(dict.fromkeys(BASE_CHARS))
CHAR2ID = {c: i for i, c in enumerate(VOCAB)}
ID2CHAR = {i: c for i, c in enumerate(VOCAB)}
MAX_LEN = 128


def encode_text(text: str, max_len: int = MAX_LEN) -> list[int]:
    ids = [CHAR2ID.get(c, 1) for c in text[:max_len]]
    if len(ids) < max_len:
        ids += [0] * (max_len - len(ids))
    return ids


# ==================== 2. DATASET & MODEL ====================
class SpellingDataset(Dataset):
    def __init__(self, pairs: list[tuple[str, str]]):
        self.pairs = pairs

    def __len__(self):
        return len(self.pairs)

    def __getitem__(self, idx):
        src, tgt = self.pairs[idx]
        return (
            torch.tensor(encode_text(src), dtype=torch.long),
            torch.tensor(encode_text(tgt), dtype=torch.long),
        )


class MiniBiGRUCorrector(nn.Module):
    def __init__(self, vocab_size: int, emb_dim: int = 128, hidden_dim: int = 256):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size, emb_dim, padding_idx=0)
        self.gru = nn.GRU(
            emb_dim,
            hidden_dim,
            num_layers=2,
            batch_first=True,
            bidirectional=True,
            dropout=0.15,
        )
        self.fc = nn.Linear(hidden_dim * 2, vocab_size)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        emb = self.embedding(x)
        out, _ = self.gru(emb)
        return self.fc(out)  # [B, T, Vocab]


# ==================== 3. HUẤN LUYỆN & XUẤT ONNX ====================
def train_and_export(
    max_samples: int = 120_000,
    epochs: int = 6,
    batch_size: int = 256,
    out_dir: str = "app/src/main/assets",
):
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Đang tải dataset coung21/vi-spelling-correction trên {device}...")
    ds = load_dataset("coung21/vi-spelling-correction", split="train")
    cols = ds.column_names
    print(f"Các cột trong dataset: {cols}")
    src_col = next((c for c in ["text", "input", "source", "src", "wrong", "corrupted", "noisy"] if c in cols), cols[0])
    tgt_col = next((c for c in ["label", "output", "target", "tgt", "correct", "clean", "ground_truth"] if c in cols and c != src_col), cols[1] if len(cols) > 1 else cols[0])
    print(f"Sử dụng cột nguồn='{src_col}', cột đích='{tgt_col}'")

    # Chỉ giữ các cặp câu có cùng độ dài ký tự (sửa dấu thanh, dấu mũ, phụ âm ch/tr, s/x, l/n...)
    pairs = []
    for row in ds:
        src = str(row.get(src_col, "")).strip()
        tgt = str(row.get(tgt_col, "")).strip()
        if 2 <= len(src) <= MAX_LEN and len(src) == len(tgt):
            pairs.append((src, tgt))
            if len(pairs) >= max_samples:
                break

    print(f"Đã lọc {len(pairs)} mẫu huấn luyện chuẩn độ dài.")
    loader = DataLoader(SpellingDataset(pairs), batch_size=batch_size, shuffle=True)

    model = MiniBiGRUCorrector(len(VOCAB)).to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=2e-3)
    criterion = nn.CrossEntropyLoss(ignore_index=0)

    for epoch in range(1, epochs + 1):
        model.train()
        total_loss = 0.0
        for x, y in loader:
            x, y = x.to(device), y.to(device)
            optimizer.zero_grad()
            logits = model(x)
            loss = criterion(logits.view(-1, len(VOCAB)), y.view(-1))
            loss.backward()
            optimizer.step()
            total_loss += loss.item()
        print(f"Epoch {epoch}/{epochs} - Loss: {total_loss / len(loader):.4f}")

    # Xuất ra ONNX FP32 rồi lượng tử hóa INT8 để giảm 4x dung lượng (~6-9 MB)
    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)
    fp32_onnx = out_path / "vi_corrector_fp32.onnx"
    int8_onnx = out_path / "vi_corrector.onnx"

    model.eval().cpu()
    dummy_input = torch.zeros((1, MAX_LEN), dtype=torch.long)
    torch.onnx.export(
        model,
        dummy_input,
        str(fp32_onnx),
        input_names=["input_ids"],
        output_names=["logits"],
        dynamic_axes={"input_ids": {0: "batch", 1: "seq"}, "logits": {0: "batch", 1: "seq"}},
        opset_version=14,
    )

    quantize_dynamic(
        model_input=str(fp32_onnx),
        model_output=str(int8_onnx),
        weight_type=QuantType.QInt8,
    )
    fp32_onnx.unlink(missing_ok=True)

    # Xuất bảng từ vựng ký tự sang JSON để Kotlin đọc khớp 100%
    vocab_file = out_path / "vi_corrector_vocab.json"
    vocab_file.write_text(json.dumps(VOCAB, ensure_ascii=False), encoding="utf-8")

    size_mb = int8_onnx.stat().st_size / (1024 * 1024)
    print(f"Hoàn tất! Đã xuất {int8_onnx} ({size_mb:.2f} MB) và {vocab_file}")


if __name__ == "__main__":
    train_and_export()
