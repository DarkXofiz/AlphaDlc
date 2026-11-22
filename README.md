# AlphaDlc

Minecraft 1.21.4 Fabric client mod (`zov.alphadlc`).

Kaynak kodlar `input/alphadlc-0_0_9.jar` dosyasından GitHub Actions ile
üretilir: jar önce intermediary isimlerinden Yarn isimlerine remap edilir,
sonra Vineflower ile decompile edilir.

## Yapı
- `input/` orijinal jar
- `src/main/java` decompile edilmiş kaynak
- `src/main/resources` asset, mixin, accesswidener, fabric.mod.json
- `libs/` jar içine gömülü bağımlılıklar (DJL api, pytorch-engine)

## Akış
1. `Decompile Jar` workflow'u kaynakları üretir ve commit eder.
2. `Build` workflow'u `gradle build` çalıştırır.
3. Derleme logu `build-log`, jar `alphadlc-jar` artifact'ı olarak yüklenir.

## Yerel derleme
Java 21 ve Gradle 8.12 ile:

    gradle build

Çıktı: `build/libs/`

## Lisans
CC0 1.0 Universal, bkz. `LICENSE`.
