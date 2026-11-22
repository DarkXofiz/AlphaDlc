#!/usr/bin/env python3
"""Decompile sonrasi kaynakta kalan intermediary method_/field_ isimlerini Yarn isimlerine cevirir.

Kullanim: fix_names.py <yarn.tiny> <kaynak_dizini>
"""
import os
import re
import sys


def load_names(path):
    methods = {}
    fields = {}
    with open(path, encoding="utf-8") as f:
        f.readline()
        for line in f:
            parts = line.rstrip("\n").split("\t")
            if len(parts) >= 5 and parts[0] == "":
                kind, inter, named = parts[1], parts[3], parts[4]
                if not named or named == inter:
                    continue
                if kind == "m":
                    methods.setdefault(inter, named)
                elif kind == "f":
                    fields.setdefault(inter, named)
    return methods, fields


def main():
    tiny_path, src_dir = sys.argv[1], sys.argv[2]
    methods, fields = load_names(tiny_path)
    pattern = re.compile(r"\b(method_\d+|field_\d+)\b")

    def repl(m):
        name = m.group(1)
        table = methods if name.startswith("method_") else fields
        return table.get(name, name)

    changed_files = 0
    replaced = 0
    for root, _, files in os.walk(src_dir):
        for fn in files:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(root, fn)
            with open(p, encoding="utf-8") as f:
                text = f.read()
            new_text, n = pattern.subn(repl, text)
            if new_text != text:
                with open(p, "w", encoding="utf-8") as f:
                    f.write(new_text)
                changed_files += 1
                replaced += n
    print("Degisen dosya: %d, degisen isim: %d" % (changed_files, replaced))


if __name__ == "__main__":
    main()
