# Menyiapkan repositori GitHub

Nama repositori yang disarankan: `voxpanel`.

Deskripsi:

> Native Android floating panel for TECNO voice presets, user-tested with WhatsApp and Mobile Legends on SPARK 20.

Topics: `android`, `java`, `voice-changer`, `floating-window`, `foreground-service`, `tecno`, `portfolio`.

## Sebelum publikasi

- Baca README, lisensi MIT, dan batas dukungan perangkat.
- Jalankan build/test/lint dan catat hasil aktual; jangan mengklaim checklist manual yang belum dilakukan.
- Gunakan gambar dari layar VoxPanel sendiri. Jangan unggah screenshot kontak, panggilan, percakapan, atau kode pairing ADB.
- `.gitignore` mengecualikan APK, cache, local.properties, dan keystore. Binary vendor, hasil dekompilasi, serta alat audit berada di luar folder proyek.

Repositori lokal dapat disiapkan tanpa menerbitkan apa pun:

```bash
git status --short
git add .
git commit -m "Prepare VoxPanel portfolio project"
```

Setelah membuat repositori kosong di akun GitHub, gunakan URL repositori milikmu:

```bash
git remote add origin https://github.com/USERNAME/voxpanel.git
git push -u origin main
```

Ganti USERNAME dengan akun sendiri. Jangan menyalin remote contoh sebagai URL final. Workflow Android checks akan membangun debug APK saat ada push atau pull request; artefak workflow bukan release publik otomatis.

APK untuk dibagikan dapat dilampirkan secara manual ke GitHub Release setelah pengujian. Untuk distribusi stabil, siapkan kunci signing sendiri dan simpan di luar repositori; debug APK pada pengembangan/CI bukan pengganti kunci rilis permanen.

Untuk portofolio, gunakan ringkasan di [PORTFOLIO.md](PORTFOLIO.md), screenshot layar utama dan overlay, serta video singkat perbandingan Normal/efek dengan izin peserta. Bedakan hasil manual pengguna dari benchmark terukur.
