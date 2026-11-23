#!/usr/bin/env python3
"""Vineflower'in uretemedigi bilinen derleme sorunlarini duzeltir.

Kullanim: fix_sources.py <kaynak_dizini>
Her duzeltme dosya + satir + beklenen metin ile eslestirilir. Eslesmezse atlanir.
Iki kez calistirmak guvenlidir.
"""
import os
import re
import sys

MIXIN = "zov/alphadlc/mixin/"

# (dosya, satir, hedef sinif FQN). Mixin icinde bos 'this' hedef sinifa cast edilir.
THIS_CASTS = [
    (MIXIN + "EnderPearlEntityMixin.java", 19, "net.minecraft.entity.projectile.thrown.EnderPearlEntity"),
    (MIXIN + "EntityHitBoxMixin.java", 18, "net.minecraft.entity.Entity"),
    (MIXIN + "FireworkRocketEntityMixin.java", 45, "net.minecraft.entity.projectile.FireworkRocketEntity"),
    (MIXIN + "ScoreboardMixin.java", 18, "net.minecraft.scoreboard.Scoreboard"),
    (MIXIN + "AbstractClientPlayerEntityMixin.java", 23, "net.minecraft.client.network.AbstractClientPlayerEntity"),
    (MIXIN + "ChatInputSuggestorMixin.java", 73, "net.minecraft.client.gui.screen.ChatInputSuggestor"),
    (MIXIN + "LivingEntityMixin.java", 35, "net.minecraft.client.network.ClientPlayerEntity"),
    (MIXIN + "KeyboardInputMixin.java", 21, "net.minecraft.client.input.KeyboardInput"),
    (MIXIN + "KeyboardInputMixin.java", 22, "net.minecraft.client.input.KeyboardInput"),
    (MIXIN + "KeyboardInputMixin.java", 31, "net.minecraft.client.input.KeyboardInput"),
    (MIXIN + "KeyboardInputMixin.java", 38, "net.minecraft.client.input.KeyboardInput"),
    (MIXIN + "ClientPlayerEntityMixin.java", 112, "net.minecraft.entity.player.PlayerEntity"),
    (MIXIN + "HeldItemRendererMixin.java", 124, "zov.alphadlc.module.list.render.HeldItemRendererAccessor"),
]

# (dosya, satir, eski metin, yeni metin)
REPLACES = [
    (MIXIN + "EntityMixin.java", 19, "this == MinecraftClient", "(Object) this == MinecraftClient"),
    ("zov/alphadlc/util/render/esp/GlowEspRenderer.java", 232,
     "mc.getEntityRenderDispatcher().getRenderer(player) instanceof",
     "(Object) mc.getEntityRenderDispatcher().getRenderer(player) instanceof"),
    ("zov/alphadlc/module/list/render/Chams.java", 58,
     "new ChamsRenderer(entityRenderer)",
     "new ChamsRenderer((net.minecraft.client.render.entity.feature.FeatureRendererContext<"
     "net.minecraft.client.render.entity.state.PlayerEntityRenderState, "
     "net.minecraft.client.render.entity.model.PlayerEntityModel>) (Object) entityRenderer)"),
]


# Decompiler'in ayni isimle urettigi ve derlenmeyen degiskenler icin yeniden adlandirma.
# (dosya, [(regex, yeni metin)])
RENAMES = [
    ("zov/alphadlc/util/commands/defaults/AICommand.java", [
        (r"(String datasetName = args\.getString\(\);\s*)String modelName = args\.getString\(\);",
         r"\1String trainModelName = args.getString();"),
        (r"trainModel\(datasetName, modelName\)", "trainModel(datasetName, trainModelName)"),
    ]),
    ("zov/alphadlc/util/render/ambience/SkyShaderRenderer.java", [
        (r"String shader = c\.mode;", "String shaderName = c.mode;"),
        (r"switch \(shader\) \{(\s*case \"Aurora\")", r"switch (shaderName) {\1"),
    ]),
    ("zov/alphadlc/module/list/render/hud/Interface.java", [
        (r"(?<!final String lambdaName = name;\n)([ \t]*)(Optional<Interface\.Staff> existingStaff = this\.staffPlayers\.stream\(\)\.filter\(s -> s\.name\.equals\()name(\)\)\.findFirst\(\);)",
         r"\1final String lambdaName = name;\n\1\2lambdaName\3"),
    ]),
    ("zov/alphadlc/ui/ModuleComponent.java", [
        (r"([ \t]*)(case ColorSetting \w+:\s*this\.components\.add\(new ColorPickerComponent\(\w+\)\);\s*break;)(?!\s*default:)",
         r"\1\2\n\1default:\n\1   break;"),
    ]),
    ("zov/alphadlc/module/list/render/KillEffect.java", [
        (r"String player = this\.totemEffect\.getValue\(\);", "String totemMode = this.totemEffect.getValue();"),
        (r"String name = switch \(player\) \{", "String name = switch (totemMode) {"),
        (r"String size = this\.effect\.getValue\(\);", "String effectName = this.effect.getValue();"),
        (r"(?<![A-Za-z_] )(?<!float )\bsize = this\.effect\.getValue\(\);", "effectName = this.effect.getValue();"),
        (r"switch \(size\) \{", "switch (effectName) {"),
    ]),
]

BARE_THIS = re.compile(r"\bthis\b(?!\s*\.)")


# ---------------------------------------------------------------------------
# Pattern-matching switch ifadelerinde eksik 'default' dali icin genel duzeltme
# ---------------------------------------------------------------------------
SWITCH_START = re.compile(r"^[ \t]*switch\s*\(", re.M)
PATTERN_CASE = re.compile(r"\bcase\s+(?:final\s+)?[A-Z][\w.<>?, \[\]]*\s+[a-z_]\w*\s*(?:when\b[^:>]*)?(:|->)")


def find_matching_brace(text, open_idx):
    """open_idx konumundaki '{' icin eslesen '}' konumunu dondurur (string/yorum atlanir)."""
    depth = 0
    i = open_idx
    n = len(text)
    while i < n:
        c = text[i]
        if text.startswith("//", i):
            j = text.find("\n", i)
            i = n if j == -1 else j
            continue
        if text.startswith("/*", i):
            j = text.find("*/", i + 2)
            i = n if j == -1 else j + 2
            continue
        if text.startswith('"""', i):
            j = text.find('"""', i + 3)
            i = n if j == -1 else j + 3
            continue
        if c == '"':
            i += 1
            while i < n and text[i] != '"':
                i += 2 if text[i] == "\\" else 1
            i += 1
            continue
        if c == "'":
            i += 1
            while i < n and text[i] != "'":
                i += 2 if text[i] == "\\" else 1
            i += 1
            continue
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def add_switch_defaults(root):
    total = 0
    files = 0
    for d, _, fs in os.walk(root):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            path = os.path.join(d, fn)
            with open(path, encoding="utf-8") as f:
                text = f.read()
            pos = 0
            changed = False
            while True:
                m = SWITCH_START.search(text, pos)
                if not m:
                    break
                brace = text.find("{", m.end())
                if brace == -1:
                    break
                end = find_matching_brace(text, brace)
                if end == -1:
                    break
                body = text[brace + 1:end]
                pos = brace + 1
                if not PATTERN_CASE.search(body):
                    continue
                if re.search(r"\bdefault\s*(:|->)", body):
                    continue
                line_start = text.rfind("\n", 0, end) + 1
                indent = re.match(r"[ \t]*", text[line_start:end]).group(0)
                inner = indent + "   "
                if "->" in re.sub(r"\([^)]*\)", "", PATTERN_CASE.search(body).group(0)):
                    add = inner + "default -> {\n" + inner + "}\n"
                else:
                    add = inner + "default:\n" + inner + "   break;\n"
                # kapanis parantezinin oldugu satirin basina ekle
                if text[line_start:end].strip() == "":
                    text = text[:line_start] + add + text[line_start:]
                else:
                    text = text[:end] + "\n" + add + indent + text[end:]
                total += 1
                changed = True
                pos = brace + 1
            if changed:
                with open(path, "w", encoding="utf-8") as f:
                    f.write(text)
                files += 1
    return total, files


def read_lines(path):
    with open(path, encoding="utf-8") as f:
        return f.read().split("\n")


def write_lines(path, lines):
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))


def main():
    root = sys.argv[1]
    cache = {}
    applied = 0
    skipped = []

    def get(rel):
        p = os.path.join(root, rel)
        if p not in cache:
            if not os.path.exists(p):
                return None
            cache[p] = read_lines(p)
        return cache[p]

    for rel, line_no, fqn in THIS_CASTS:
        lines = get(rel)
        if lines is None or line_no > len(lines):
            skipped.append((rel, line_no, "dosya/satir yok"))
            continue
        line = lines[line_no - 1]
        if "(Object) this" in line or "(Object)this" in line:
            continue
        if not BARE_THIS.search(line):
            skipped.append((rel, line_no, "this bulunamadi"))
            continue
        lines[line_no - 1] = BARE_THIS.sub("((%s) (Object) this)" % fqn, line)
        applied += 1

    for rel, line_no, old, new in REPLACES:
        lines = get(rel)
        if lines is None or line_no > len(lines):
            skipped.append((rel, line_no, "dosya/satir yok"))
            continue
        line = lines[line_no - 1]
        if new in line:
            continue
        if old not in line:
            skipped.append((rel, line_no, "eslesme yok"))
            continue
        lines[line_no - 1] = line.replace(old, new, 1)
        applied += 1


    for rel, rules in RENAMES:
        p = os.path.join(root, rel)
        if not os.path.exists(p):
            skipped.append((rel, 0, "dosya yok"))
            continue
        with open(p, encoding="utf-8") as f:
            text = f.read()
        for pattern, repl in rules:
            new_text, n = re.subn(pattern, repl, text)
            if n:
                text = new_text
                applied += n
        with open(p, "w", encoding="utf-8") as f:
            f.write(text)

    for p, lines in cache.items():
        write_lines(p, lines)


    sw_total, sw_files = add_switch_defaults(root)
    print("Pattern switch default eklenen: %d (dosya: %d)" % (sw_total, sw_files))
    print("Uygulanan duzeltme: %d, atlanan: %d" % (applied, len(skipped)))
    for s in skipped:
        print("  atlandi:", s)


if __name__ == "__main__":
    main()
