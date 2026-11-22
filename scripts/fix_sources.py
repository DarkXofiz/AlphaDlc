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
    (MIXIN + "HeldItemRendererMixin.java", 124, "net.minecraft.client.render.item.HeldItemRenderer"),
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

BARE_THIS = re.compile(r"\bthis\b(?!\s*\.)")


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

    for p, lines in cache.items():
        write_lines(p, lines)

    print("Uygulanan duzeltme: %d, atlanan: %d" % (applied, len(skipped)))
    for s in skipped:
        print("  atlandi:", s)


if __name__ == "__main__":
    main()
