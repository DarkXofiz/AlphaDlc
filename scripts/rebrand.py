#!/usr/bin/env python3
"""AlphaDLC -> MiranWare yeniden adlandirma.

Kullanim: rebrand.py <proje_koku> [--add-mixin SinifAdi]

- Paket: zov.alphadlc -> miranware (dizin tasinir)
- Sinif/yazi: AlphaDLC -> MiranWare, alphadlc -> miranware, ALPHADLC -> MIRANWARE
- Kaynak dosyalari: alphadlc.mixins.json / alphadlc.accesswidener yeniden adlandirilir
- fabric.mod.json: id, name, description, authors guncellenir
"""
import json
import os
import re
import shutil
import sys

REPL = [
    ("zov.alphadlc", "miranware"),
    ("zov/alphadlc", "miranware"),
    ("AlphaDLC", "MiranWare"),
    ("AlphaDlc", "MiranWare"),
    ("ALPHADLC", "MIRANWARE"),
    ("alphadlc", "miranware"),
]
TEXT_EXT = {".java", ".json", ".accesswidener", ".mcmeta", ".txt", ".properties", ".md", ".gradle"}


def rebrand_text(text):
    for old, new in REPL:
        text = text.replace(old, new)
    return text


def process_tree(root, count):
    for d, _, fs in os.walk(root):
        for fn in fs:
            if os.path.splitext(fn)[1].lower() not in TEXT_EXT:
                continue
            p = os.path.join(d, fn)
            try:
                with open(p, encoding="utf-8") as f:
                    text = f.read()
            except UnicodeDecodeError:
                continue
            new = rebrand_text(text)
            if new != text:
                with open(p, "w", encoding="utf-8") as f:
                    f.write(new)
                count[0] += 1


def rename_files(root):
    for d, dirs, fs in os.walk(root, topdown=False):
        for fn in fs:
            new = rebrand_text(fn)
            if new != fn:
                os.replace(os.path.join(d, fn), os.path.join(d, new))


def move_package(java_root):
    old = os.path.join(java_root, "zov", "alphadlc")
    new = os.path.join(java_root, "miranware")
    if os.path.isdir(old):
        os.makedirs(new, exist_ok=True)
        for item in os.listdir(old):
            shutil.move(os.path.join(old, item), os.path.join(new, item))
        shutil.rmtree(os.path.join(java_root, "zov"), ignore_errors=True)


def patch_mod_json(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    data["id"] = "miranware"
    data["name"] = "MiranWare"
    data["description"] = "MiranWare Client"
    data["authors"] = ["MiranWare Team"]
    data.pop("contact", None)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def add_mixin(path, name):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    lst = data.setdefault("client", [])
    if name not in lst:
        lst.append(name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def main():
    root = sys.argv[1]
    mixin = None
    if "--add-mixin" in sys.argv:
        mixin = sys.argv[sys.argv.index("--add-mixin") + 1]
    java_root = os.path.join(root, "src", "main", "java")
    res_root = os.path.join(root, "src", "main", "resources")
    count = [0]
    process_tree(java_root, count)
    process_tree(res_root, count)
    move_package(java_root)
    rename_files(java_root)
    rename_files(res_root)
    mod_json = os.path.join(res_root, "fabric.mod.json")
    if os.path.exists(mod_json):
        patch_mod_json(mod_json)
    if mixin:
        for mixin_name in mixin.split(","):
            add_mixin(os.path.join(res_root, "miranware.mixins.json"), mixin_name.strip())
    left = 0
    for base in (java_root, res_root):
        for d, _, fs in os.walk(base):
            for fn in fs:
                if os.path.splitext(fn)[1].lower() in TEXT_EXT:
                    try:
                        t = open(os.path.join(d, fn), encoding="utf-8").read()
                    except UnicodeDecodeError:
                        continue
                    if re.search(r"alphadlc|zov\.", t, re.I):
                        left += 1
    print("Yeniden adlandirilan dosya: %d, kalan eski ad iceren dosya: %d" % (count[0], left))


if __name__ == "__main__":
    main()
