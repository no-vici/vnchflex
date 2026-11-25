#!/usr/bin/env python3
"""
Quét toàn bộ codebase và gộp tất cả mã nguồn thành 1 file text duy nhất.

Cách dùng:
    python3 generate_codebase.py [output_file]

Mặc định output_file là: codebase_dump.txt
"""
import base64
import os
import sys
from pathlib import Path

IGNORE_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    "build",
    "__pycache__",
    ".kotlin",
}

IGNORE_FILES = {
    "generate_codebase.py",
    "codebase_dump.txt",
    ".DS_Store",
}

BINARY_EXTENSIONS = {
    ".png", ".jpg", ".jpeg", ".webp", ".gif", ".ico",
    ".jar", ".apk", ".aab", ".dex", ".class", ".so", ".keystore", ".jks",
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
        out.write(f"# CODEBASE DUMP: {root_dir.name}\n")
        out.write(f"# Total files: {len(collected_files)}\n")
        out.write("=" * 80 + "\n\n")

        # Mục lục cấu trúc file
        out.write("## PROJECT STRUCTURE\n")
        for fpath in collected_files:
            rel = fpath.relative_to(root_dir).as_posix()
            out.write(f"- {rel}\n")
        out.write("\n" + "=" * 80 + "\n\n")

        # Nội dung từng file
        for fpath in collected_files:
            rel = fpath.relative_to(root_dir).as_posix()
            out.write(f"===== FILE: {rel} =====\n")
            ext = fpath.suffix.lower()

            if ext in BINARY_EXTENSIONS:
                b64 = base64.b64encode(fpath.read_bytes()).decode("ascii")
                out.write(f"[BINARY FILE - BASE64 ({fpath.stat().st_size} bytes)]\n")
                out.write(b64 + "\n")
            else:
                try:
                    content = fpath.read_text(encoding="utf-8")
                    out.write(content)
                    if not content.endswith("\n"):
                        out.write("\n")
                except UnicodeDecodeError:
                    b64 = base64.b64encode(fpath.read_bytes()).decode("ascii")
                    out.write(f"[BINARY FILE - BASE64 ({fpath.stat().st_size} bytes)]\n")
                    out.write(b64 + "\n")

            out.write(f"===== END FILE: {rel} =====\n\n")

    print(f"Đã xuất {len(collected_files)} files vào: {output_file}")


if __name__ == "__main__":
    project_root = Path(__file__).resolve().parent
    out_path = Path(sys.argv[1]) if len(sys.argv) > 1 else project_root / "codebase_dump.txt"
    dump_codebase(project_root, out_path)
