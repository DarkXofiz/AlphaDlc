# AlphaDlc

Fabric client mod. Kaynak kodlar `input/alphadlc-0_0_9.jar` dosyasından
GitHub Actions (Vineflower) ile otomatik üretilir.

## Akış
1. `input/` altına jar konur ve push edilir.
2. `Decompile Jar` workflow'u çalışır.
3. Çıktı `src/main/java`, `src/main/resources` ve `libs/` altına commit edilir.

## Elle çalıştırma
Actions sekmesi > Decompile Jar > Run workflow
