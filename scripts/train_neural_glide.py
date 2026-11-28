"""Huấn luyện mạng nơ-ron giải mã Glide Typing (Conv1D + BiGRU + CTC) cho vnchFlex.

Tự động sinh dữ liệu vuốt giả lập (Chaikin corner-cutting + nhiễu tay người)
từ kho từ vựng tiếng Việt (bỏ dấu) & tiếng Anh trong `app/src/main/assets/`,
sau đó huấn luyện mô hình CTC và nén INT8 (~2.5 MB) tại `app/src/main/assets/neural_glide.onnx`.
"""

import gzip
import random
import unicodedata
from pathlib import Path

import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from onnxruntime.quantization import quantize_dynamic, QuantType

# Tọa độ chuẩn hóa [0..1] khớp 100% với layout QWERTY của KeyboardCanvasView
KEY_POSITIONS: dict[str, tuple[float, float]] = {
    "q": (0.05, 0.16), "w": (0.15, 0.16), "e": (0.25, 0.16), "r": (0.35, 0.16), "t": (0.45, 0.16),
    "y": (0.55, 0.16), "u": (0.65, 0.16), "i": (0.75, 0.16), "o": (0.85, 0.16), "p": (0.95, 0.16),
    "a": (0.10, 0.50), "s": (0.20, 0.50), "d": (0.30, 0.50), "f": (0.40, 0.50), "g": (0.50, 0.50),
    "h": (0.60, 0.50), "j": (0.70, 0.50), "k": (0.80, 0.50), "l": (0.90, 0.50),
    "z": (0.20, 0.84), "x": (0.30, 0.84), "c": (0.40, 0.84), "v": (0.50, 0.84), "b": (0.60, 0.84),
    "n": (0.70, 0.84), "m": (0.80, 0.84),
}

# CTC Blank = 0, 'a'..'z' = 1..26
CHARS = "abcdefghijklmnopqrstuvwxyz"
CHAR2ID = {c: i + 1 for i, c in enumerate(CHARS)}
NUM_CLASSES = len(CHARS) + 1  # 27
SEQ_LEN = 48


def strip_diacritics(s: str) -> str:
    s = s.replace("đ", "d").replace("Đ", "D")
    nfd = unicodedata.normalize("NFD", s)
    return "".join(c for c in nfd if unicodedata.category(c) != "Mn").lower()


def load_training_words(assets_dir: str = "app/src/main/assets") -> list[tuple[str, float]]:
    """Nạp danh sách từ không dấu (2..12 ký tự) kèm trọng số lấy mẫu."""
    weights: dict[str, float] = {}
    root = Path(assets_dir)

    vi_gz = root / "vi_trained_words.txt.gz"
    if vi_gz.exists():
        with gzip.open(vi_gz, "rt", encoding="utf-8", errors="ignore") as f:
            for rank, line in enumerate(f):
                w = strip_diacritics(line.strip())
                if 2 <= len(w) <= 10 and all(c in CHAR2ID for c in w):
                    weights[w] = max(weights.get(w, 0.0), 3.0 / (1.0 + rank / 800.0))

    en_txt = root / "en_unigrams.txt"
    if en_txt.exists():
        for line in en_txt.read_text(encoding="utf-8", errors="ignore").splitlines():
            parts = line.strip().split("\t")
            w = strip_diacritics(parts[0]) if parts else ""
            wt = float(parts[1]) / 50.0 if len(parts) > 1 and parts[1].isdigit() else 1.0
            if 2 <= len(w) <= 12 and all(c in CHAR2ID for c in w):
                weights[w] = max(weights.get(w, 0.0), wt)

    if not weights:
        for w in ["chao", "xin", "viet", "nam", "ban", "phim", "khong", "duoc", "hello", "thanks", "love"]:
            weights[w] = 2.0

    return list(weights.items())


def extract_6d_features(points: np.ndarray, target_len: int = SEQ_LEN) -> np.ndarray:
    """Lấy mẫu đều đường vuốt về `target_len` điểm và trích xuất 6 đặc trưng [x, y, dx, dy, sin, cos]."""
    pts = np.repeat(points, 2, axis=0) if len(points) < 2 else points
    diffs = np.diff(pts, axis=0)
    seg_lens = np.hypot(diffs[:, 0], diffs[:, 1])
    cum_len = np.concatenate([[0.0], np.cumsum(seg_lens)])
    total_len = cum_len[-1]

    if total_len < 1e-5:
        resampled = np.repeat(pts[:1], target_len, axis=0)
    else:
        sample_dists = np.linspace(0.0, total_len, target_len)
        rx = np.interp(sample_dists, cum_len, pts[:, 0])
        ry = np.interp(sample_dists, cum_len, pts[:, 1])
        resampled = np.stack([rx, ry], axis=-1)

    dx = np.gradient(resampled[:, 0])
    dy = np.gradient(resampled[:, 1])
    angles = np.arctan2(dy, dx)
    feats = np.stack([resampled[:, 0], resampled[:, 1], dx, dy, np.sin(angles), np.cos(angles)], axis=-1)
    return feats.astype(np.float32)


def synthesize_swipe(word: str, rng: random.Random) -> np.ndarray:
    """Mô phỏng quỹ đạo vuốt tay người cho một từ (có bo tròn góc và nhiễu điểm chạm)."""
    collapsed = []
    for c in word:
        if not collapsed or collapsed[-1] != c:
            collapsed.append(c)

    waypoints = []
    for idx, ch in enumerate(collapsed):
        bx, by = KEY_POSITIONS[ch]
        jitter_x = rng.gauss(0.0, 0.018 if idx in (0, len(collapsed) - 1) else 0.032)
        jitter_y = rng.gauss(0.0, 0.035 if idx in (0, len(collapsed) - 1) else 0.055)
        waypoints.append((np.clip(bx + jitter_x, 0.0, 1.0), np.clip(by + jitter_y, 0.0, 1.0)))

    pts = np.array(waypoints, dtype=np.float32)
    if len(pts) >= 3:
        for _ in range(rng.choice([1, 2])):
            new_pts = [pts[0]]
            for i in range(len(pts) - 1):
                p0, p1 = pts[i], pts[i + 1]
                new_pts.append(0.75 * p0 + 0.25 * p1)
                new_pts.append(0.25 * p0 + 0.75 * p1)
            new_pts.append(pts[-1])
            pts = np.array(new_pts, dtype=np.float32)

    return extract_6d_features(pts, SEQ_LEN)


class SyntheticSwipeDataset(Dataset):
    def __init__(self, word_weights: list[tuple[str, float]], num_samples: int = 200_000, seed: int = 42):
        self.words = [w for w, _ in word_weights]
        probs = np.array([wt for _, wt in word_weights], dtype=np.float64)
        self.probs = probs / probs.sum()
        self.num_samples = num_samples
        self.rng = random.Random(seed)
        self.np_rng = np.random.default_rng(seed)

    def __len__(self) -> int:
        return self.num_samples

    def __getitem__(self, idx: int):
        word = self.words[self.np_rng.choice(len(self.words), p=self.probs)]
        collapsed = []
        for c in word:
            if not collapsed or collapsed[-1] != c:
                collapsed.append(c)
        target_ids = [CHAR2ID[c] for c in collapsed]
        feats = synthesize_swipe(word, self.rng)
        return torch.from_numpy(feats), torch.tensor(target_ids, dtype=torch.long)


def collate_ctc(batch):
    xs, ys = zip(*batch)
    return torch.stack(xs, dim=0), torch.cat(ys, dim=0), torch.tensor([len(y) for y in ys], dtype=torch.long)


class NeuralGlideNet(nn.Module):
    """Mạng Conv1D + 2-layer BiGRU + Linear CTC siêu nhẹ (~2.5 MB INT8)."""

    def __init__(self, in_features: int = 6, hidden_dim: int = 160, num_classes: int = NUM_CLASSES):
        super().__init__()
        self.conv = nn.Sequential(
            nn.Conv1d(in_features, 64, kernel_size=3, padding=1),
            nn.GELU(),
            nn.Conv1d(64, 96, kernel_size=3, padding=1),
            nn.GELU(),
        )
        self.gru = nn.GRU(96, hidden_dim, num_layers=2, batch_first=True, bidirectional=True, dropout=0.1)
        self.head = nn.Linear(hidden_dim * 2, num_classes)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        c = self.conv(x.transpose(1, 2)).transpose(1, 2)
        out, _ = self.gru(c)
        return self.head(out)


def train_and_export_glide(
    epochs: int = 8,
    batch_size: int = 256,
    samples_per_epoch: int = 200_000,
    out_dir: str = "app/src/main/assets",
) -> None:
    word_weights = load_training_words(out_dir)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Đã nạp {len(word_weights)} từ huấn luyện Glide trên {device}...")

    loader = DataLoader(SyntheticSwipeDataset(word_weights, num_samples=samples_per_epoch), batch_size=batch_size, shuffle=True, collate_fn=collate_ctc)
    model = NeuralGlideNet().to(device)
    optimizer = torch.optim.AdamW(model.parameters(), lr=2.5e-3, weight_decay=1e-4)
    ctc_loss = nn.CTCLoss(blank=0, zero_infinity=True)

    for epoch in range(1, epochs + 1):
        model.train()
        total_loss = 0.0
        for x, y_concat, y_lens in loader:
            x, y_concat, y_lens = x.to(device), y_concat.to(device), y_lens.to(device)
            optimizer.zero_grad()
            log_probs = model(x).log_softmax(dim=-1).transpose(0, 1)
            input_lens = torch.full((x.size(0),), SEQ_LEN, dtype=torch.long, device=device)
            loss = ctc_loss(log_probs, y_concat, input_lens, y_lens)
            loss.backward()
            torch.nn.utils.clip_grad_norm_(model.parameters(), 2.0)
            optimizer.step()
            total_loss += loss.item()
        print(f"Epoch {epoch}/{epochs} - CTC Loss: {total_loss / len(loader):.4f}")

    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)
    fp32_onnx = out_path / "neural_glide_fp32.onnx"
    int8_onnx = out_path / "neural_glide.onnx"
    model.eval().cpu()
    torch.onnx.export(
        model,
        torch.zeros((1, SEQ_LEN, 6), dtype=torch.float32),
        str(fp32_onnx),
        input_names=["swipe_features"],
        output_names=["logits"],
        opset_version=14,
    )
    quantize_dynamic(str(fp32_onnx), str(int8_onnx), weight_type=QuantType.QInt8)
    fp32_onnx.unlink(missing_ok=True)
    print(f"Hoàn tất! Đã xuất {int8_onnx} ({int8_onnx.stat().st_size / (1024 * 1024):.2f} MB).")


if __name__ == "__main__":
    train_and_export_glide()
