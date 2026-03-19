from pathlib import Path
import argparse


EXCLUDED_DIRS = {".git", "gradle", ".gradle"}


def build_tree(root: Path, prefix: str = "", ignore_hidden: bool = False) -> list[str]:
    lines = []

    try:
        items = sorted(
            [
                item for item in root.iterdir()
                if item.name not in EXCLUDED_DIRS
            ],
            key=lambda p: (p.is_file(), p.name.lower())
        )
    except PermissionError:
        return [prefix + "[Нет доступа]"]

    if ignore_hidden:
        items = [item for item in items if not item.name.startswith(".")]

    for index, item in enumerate(items):
        is_last = index == len(items) - 1
        connector = "└── " if is_last else "├── "
        lines.append(prefix + connector + item.name)

        if item.is_dir():
            extension = "    " if is_last else "│   "
            lines.extend(build_tree(item, prefix + extension, ignore_hidden))

    return lines


def save_tree_to_file(start_path: str, output_file: str, ignore_hidden: bool = False):
    root = Path(start_path).resolve()

    if not root.exists():
        print(f"Ошибка: путь не существует: {root}")
        return

    lines = [str(root)]
    lines.extend(build_tree(root, ignore_hidden=ignore_hidden))

    with open(output_file, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"Иерархия сохранена в файл: {output_file}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Сохранить иерархию файлов и папок в txt-файл")
    parser.add_argument("path", nargs="?", default=".", help="Папка, для которой строить дерево")
    parser.add_argument("-o", "--output", default="file_tree.txt", help="Имя выходного txt-файла")
    parser.add_argument("--ignore-hidden", action="store_true", help="Игнорировать скрытые файлы и папки")

    args = parser.parse_args()

    save_tree_to_file(args.path, args.output, args.ignore_hidden)