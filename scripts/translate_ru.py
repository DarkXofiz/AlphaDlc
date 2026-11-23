#!/usr/bin/env python3
"""Decompile edilmis kaynaktaki Rusca string literallerini Ingilizceye cevirir.

Kullanim: translate_ru.py <kaynak_dizini> [rapor_dosyasi]

- Kelime sozlugu: glossary_ru.txt
- Cumle/ifade duzeltmeleri: phrases_ru.txt  ("rusca=ingilizce", tam eslesme)
- Sunucu mesajlarini eslestiren islevsel stringler KORUNUR (KEEP kurallari).
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CYR = re.compile(r"[\u0400-\u04FF]")
WORD = re.compile(r"[А-Яа-яЁёғ]+")
LITERAL = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
UESC = re.compile(r"\\u([0-9a-fA-F]{4})")

# Rusca sunucu metnini/ogeleri eslestirdigi icin cevrilmeyen dosyalar (kucuk harfle baslayanlar korunur)
LOWER_KEEP_FILES = {"ServerJoiner.java", "AutoDuel.java", "ServerHelper.java",
                    "SunriseHelper.java", "UseTracker.java", "WellHelper.java"}
AUTODUEL_KITS = {"Классик", "Лук", "Шары", "Щиты", "Тотемы", "Шипы 3", "НоДебафф", "Читерский рай"}
KEEP_EXACT = {"Invsee элитры"}
FORCE_TRANSLATE = {" имеет ЭЛИТРЫ!"}

ELEMENTS = {
    "ServerHelper": "ServerHelper", "notif": "Notifications", "бафов": "Buffs", "броня": "Armor",
    "брони": "Armor", "вм": "Watermark", "инфо": "Info", "кд": "Cooldowns", "кейбиндов": "Keybinds",
    "кб": "Keybinds", "стафф": "Staff", "тотем-бар": "Totem Bar", "тх": "Target HUD",
    "хотбар": "Hotbar", "хотбара": "Hotbar", "ватермарки": "Watermark", "кулдаунов": "Cooldowns",
    "полосы тотемов": "Totem Bar", "списка модераторов": "Staff List", "таргет худа": "Target HUD",
    "уведомлений": "Notifications",
}
ATTRS = {"Блик": "Glint", "Блюр": "Blur", "Прозрачность блика": "Glint Opacity", "Прозрачность": "Opacity",
         "Размер": "Size", "Толщина блика": "Glint Thickness", "Уголки": "Corners"}
TEMPLATE = re.compile("^(%s) (%s)$" % ("|".join(sorted(map(re.escape, ATTRS), key=len, reverse=True)),
                                          "|".join(sorted(map(re.escape, ELEMENTS), key=len, reverse=True))))


def load_pairs(path):
    d = {}
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.rstrip("\n")
                if not line or line.startswith("#") or "=" not in line:
                    continue
                k, v = line.split("=", 1)
                d[k] = v
    return d


GLOSS = load_pairs(os.path.join(HERE, "glossary_ru.txt"))
sys.path.insert(0, HERE)
from phrases_ru import PHRASES  # noqa: E402
UNKNOWN = {}


def match_case(src, dst):
    if len(src) > 1 and src.isupper():
        return dst.upper()
    if src[:1].isupper():
        return dst[:1].upper() + dst[1:]
    return dst



ATTR_NOUNS = {"цвет": "Color", "размер": "Size", "скорость": "Speed", "сила": "Strength", "толщина": "Thickness",
              "прозрачность": "Opacity", "яркость": "Brightness", "радиус": "Radius", "длина": "Length",
              "количество": "Amount", "задержка": "Delay", "дистанция": "Distance", "плотность": "Density",
              "интенсивность": "Intensity", "ширина": "Width", "насыщенность": "Saturation",
              "длительность": "Duration", "масштаб": "Scale", "блик": "Glint", "блюр": "Blur", "уголки": "Corners"}
PREPS = {"от", "по", "на", "в", "при", "для", "до", "с", "со", "из", "за", "к", "и", "или", "без"}


def reorder(text):
    """'Цвет глова' -> 'Glow Color' (ad tamlamasi ters cevrilir)."""
    parts = text.split(" ")
    if len(parts) < 2 or len(parts) > 4:
        return None
    head = parts[0].lower()
    if head not in ATTR_NOUNS:
        return None
    rest = parts[1:]
    suffix = ""
    if rest and rest[-1].isdigit():
        suffix = " " + rest.pop()
    if not rest or any(w.lower() in PREPS for w in rest) or not all(WORD.fullmatch(w) or re.fullmatch(r"[A-Za-z]+", w) for w in rest):
        return None
    tr = " ".join(translate(w) if CYR.search(w) else w for w in rest)
    return "%s %s%s" % (tr[:1].upper() + tr[1:], ATTR_NOUNS[head], suffix)


def translate(text):
    if text in PHRASES:
        return PHRASES[text]
    m = TEMPLATE.match(text)
    if m:
        return "%s %s" % (ELEMENTS[m.group(2)], ATTRS[m.group(1)])
    r = reorder(text)
    if r is not None:
        return r

    def word(m):
        w = m.group(0)
        t = GLOSS.get(w.lower())
        if t is None:
            UNKNOWN[w.lower()] = UNKNOWN.get(w.lower(), 0) + 1
            return w
        return match_case(w, t)

    return WORD.sub(word, text)


ESC = {"n": "\n", "t": "\t", "r": "\r", "b": "\b", "f": "\f", '"': '"', "'": "'", "\\": "\\", "0": "\0", "s": " "}


def decode(lit):
    out = []
    i = 0
    n = len(lit)
    while i < n:
        c = lit[i]
        if c == "\\" and i + 1 < n:
            d = lit[i + 1]
            if d == "u":
                j = i + 1
                while j < n and lit[j] == "u":
                    j += 1
                out.append(chr(int(lit[j:j + 4], 16)))
                i = j + 4
                continue
            out.append(ESC.get(d, "\\" + d))
            i += 2
            continue
        out.append(c)
        i += 1
    return "".join(out)


def encode(s):
    out = []
    for ch in s:
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif ch == "\n":
            out.append("\\n")
        elif ch == "\t":
            out.append("\\t")
        elif ch == "\r":
            out.append("\\r")
        elif ord(ch) < 32 or ord(ch) >= 127:
            out.append("\\u%04x" % ord(ch))
        else:
            out.append(ch)
    return "".join(out)


def should_keep(fname, text):
    if text in FORCE_TRANSLATE:
        return False
    if len(text.strip()) <= 1:
        return True
    if text in KEEP_EXACT:
        return True
    if "(?:" in text or ("|" in text and ".*" in text):
        return True
    first = next((c for c in text if CYR.match(c)), None)
    if fname in LOWER_KEEP_FILES and first is not None and first.islower():
        return True
    if fname == "AutoDuel.java" and text in AUTODUEL_KITS:
        return True
    if fname == "RWHelper.java" and "»" in text:
        return True
    return False


def process_file(path, stats, report):
    with open(path, encoding="utf-8") as f:
        src = f.read()
    fname = os.path.basename(path)

    def repl(m):
        lit = m.group(1)
        text = decode(lit)
        if not CYR.search(text):
            return m.group(0)
        if should_keep(fname, text):
            stats["kept"] += 1
            report.setdefault("kept", set()).add((fname, text))
            return m.group(0)
        new = translate(text)
        stats["translated"] += 1
        return '"' + encode(new) + '"'

    new_src = LITERAL.sub(repl, src)
    if new_src != src:
        with open(path, "w", encoding="utf-8") as f:
            f.write(new_src)
        stats["files"] += 1


def main():
    root = sys.argv[1]
    report_path = sys.argv[2] if len(sys.argv) > 2 else None
    stats = {"files": 0, "translated": 0, "kept": 0}
    report = {}
    for d, _, fs in os.walk(root):
        for fn in fs:
            if fn.endswith(".java"):
                process_file(os.path.join(d, fn), stats, report)
    print("Degisen dosya: %d, cevrilen string: %d, korunan string: %d" % (stats["files"], stats["translated"], stats["kept"]))
    if UNKNOWN:
        print("Sozlukte olmayan kelimeler:", ", ".join(sorted(UNKNOWN)))
    if report_path:
        with open(report_path, "w", encoding="utf-8") as f:
            f.write("# Translation notes\n\nStrings intentionally left in Russian because they match text sent by Russian servers "
                    "(chat messages, item names, menu titles). Translating them would break the related features.\n\n")
            for fname, text in sorted(report.get("kept", [])):
                f.write("- `%s`: `%s`\n" % (fname, text.replace("`", "'")))


if __name__ == "__main__":
    main()
