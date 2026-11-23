#!/usr/bin/env python3
"""Access widener dosyasini intermediary namespace'inden named (Yarn) namespace'ine cevirir.

Kullanim: convert_aw.py <yarn.tiny> <dosya.accesswidener>
"""
import re
import sys


def load_tiny(path):
    classes = {}
    fields = {}
    methods = {}
    cur = None
    with open(path, encoding="utf-8") as f:
        header = f.readline().rstrip("\n").split("\t")
        if header[0] != "tiny" or header[1] != "2":
            raise SystemExit("tiny v2 bekleniyor")
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            parts = line.split("\t")
            if parts[0] == "c":
                cur = parts[1]
                classes[cur] = parts[2] if len(parts) > 2 and parts[2] else cur
            elif parts[0] == "" and len(parts) >= 4 and cur is not None:
                kind = parts[1]
                if kind == "f":
                    named = parts[4] if len(parts) > 4 and parts[4] else parts[3]
                    fields[(cur, parts[3], parts[2])] = named
                elif kind == "m":
                    named = parts[4] if len(parts) > 4 and parts[4] else parts[3]
                    methods[(cur, parts[3], parts[2])] = named
    return classes, fields, methods


def main():
    tiny_path, aw_path = sys.argv[1], sys.argv[2]
    classes, fields, methods = load_tiny(tiny_path)

    desc_class = re.compile(r"L([^;]+);")

    def map_desc(desc):
        return desc_class.sub(lambda m: "L" + classes.get(m.group(1), m.group(1)) + ";", desc)

    out = []
    missing = 0
    with open(aw_path, encoding="utf-8") as f:
        lines = f.read().splitlines()

    for line in lines:
        parts = line.split("\t")
        if line.startswith("accessWidener"):
            out.append("accessWidener\tv2\tnamed")
            continue
        if len(parts) < 3 or line.startswith("#"):
            out.append(line)
            continue
        access, kind = parts[0], parts[1]
        if kind == "class":
            owner = parts[2]
            if owner not in classes:
                missing += 1
            out.append("\t".join([access, kind, classes.get(owner, owner)]))
        elif kind in ("field", "method") and len(parts) >= 5:
            owner, name, desc = parts[2], parts[3], parts[4]
            table = fields if kind == "field" else methods
            named = table.get((owner, name, desc))
            if named is None:
                missing += 1
                named = name
            out.append("\t".join([access, kind, classes.get(owner, owner), named, map_desc(desc)]))
        else:
            out.append(line)

    with open(aw_path, "w", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")
    print("AW donusturuldu, eslesmeyen satir: %d" % missing)


if __name__ == "__main__":
    main()
