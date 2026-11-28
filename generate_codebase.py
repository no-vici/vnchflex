#!/usr/bin/env python3
"""
Quét toàn bộ codebase và gộp mã nguồn thành 1 file text gọn nhẹ để AI review.

Cách dùng:
    python3 generate_codebase.py [output_file]
"""
import os
import re
import sys
from pathlib import Path

IGNORE_DIRS = {
    ".git", ".gradle", ".idea", "build", "__pycache__", ".kotlin", "corpus_data",
}

IGNORE_FILES = {
    "generate_codebase.py",
    "codebase_dump.txt",
    ".DS_Store",
    "LICENSE",
    "local.properties",
    "gradlew",
    "gradlew.bat",
}

BINARY_EXTENSIONS = {
    ".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico",
    ".jar", ".apk", ".aab", ".dex", ".class", ".so", ".keystore", ".jks", ".gz",
}

# Các file dữ liệu lớn chỉ trích 25 dòng đầu để giữ ngữ cảnh mà không phình file dump
DATA_PREVIEW_FILES = {
    "vnchflex-trained-data.json",
    "en_unigrams.txt",
    "vi_bigrams.json",
    "vi_trigrams.json",
    "vi_unigrams.txt",
}


def dump_codebase(root_dir: Path, output_file: Path) -> None:
    root_dir = root_dir.resolve()
    output_file = output_file.resolve()

    collected_files: list[Path] = []
    for dirpath, dirnames, filenames in os.walk(root_dir):
        dirnames[:] = sorted([d for d in dirnames if d not in IGNORE_DIRS])
        for fname in sorted(filenames):
            if fname in IGNORE_FILES:
                continue
            full_path = Path(dirpath) / fname
            if full_path.resolve() == output_file:
                continue
            collected_files.append(full_path)

    with output_file.open("w", encoding="utf-8") as out:
        out.write(f"# CODEBASE DUMP: {root_dir.name} ({len(collected_files)} files)\n")
        out.write("=" * 60 + "\n\n## PROJECT STRUCTURE\n")
        for fpath in collected_files:
            out.write(f"- {fpath.relative_to(root_dir).as_posix()}\n")
        out.write("\n" + "=" * 60 + "\n\n")

        for fpath in collected_files:
            rel = fpath.relative_to(root_dir).as_posix()
            ext = fpath.suffix.lower()
            size = fpath.stat().st_size
            out.write(f"===== FILE: {rel} =====\n")

            if ext in BINARY_EXTENSIONS:
                out.write(f"[BINARY OMITTED: {size} bytes]\n")
            elif fpath.name in DATA_PREVIEW_FILES:
                lines = fpath.read_text(encoding="utf-8", errors="ignore").splitlines()
                preview = "\n".join(lines[:25])
                out.write(preview + f"\n... [TRUNCATED: {len(lines)} lines, {size} bytes] ...\n")
            else:
                try:
                    content = fpath.read_text(encoding="utf-8")
                    # Gộp nhiều dòng trống liên tiếp thành 1 dòng trống cho gọn
                    content = re.sub(r"\n{3,}", "\n\n", content)
                    out.write(content if content.endswith("\n") else content + "\n")
                except UnicodeDecodeError:
                    out.write(f"[BINARY OMITTED: {size} bytes]\n")

            out.write(f"===== END FILE: {rel} =====\n\n")

    kb = output_file.stat().st_size / 1024
    print(f"Đã xuất {len(collected_files)} files vào {output_file.name} ({kb:.1f} KB)")


if __name__ == "__main__":
    project_root = Path(__file__).resolve().parent
    out_path = Path(sys.argv[1]) if len(sys.argv) > 1 else project_root / "vnchflex_codebase.txt"
    dump_codebase(project_root, out_path)
