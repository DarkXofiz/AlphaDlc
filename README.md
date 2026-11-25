# MiranWare

Minecraft 1.21.4 Fabric client mod (package `miranware`).

Sources are generated from `input/alphadlc-0_0_9.jar` by GitHub Actions:
the jar is remapped (intermediary to Yarn), decompiled with Vineflower, patched,
translated to English and rebranded to MiranWare.

## Layout
- `input/` original jar
- `overrides/` MiranWare assets and sources applied after decompiling (menu background, logo, icon, main menu screen)
- `scripts/` pipeline scripts (name fixes, source fixes, translation, rebrand)
- `src/main` generated sources and resources
- `libs/` libraries bundled inside the original jar
- `TRANSLATION_NOTES.md` strings that were intentionally left in Russian

## Pipeline
1. `Decompile Jar` generates and commits the sources.
2. `Build` runs `gradle build`; errors are shown in the run summary and committed to `logs/`.
3. The jar is uploaded as the `miranware-0_0_9` artifact.

## Local build
Java 21 and Gradle 8.12:

    gradle build

Output: `build/libs/miranware-0_0_9.jar`

## License
CC0 1.0 Universal, see `LICENSE`.
