# Hasil bedah konfigurasi suara TECNO SPARK 20

Pemeriksaan statis, 17 Oktober 2026. Binary berasal dari perangkat TECNO KJ5n Android 13 milik pengguna. Tidak mengubah file sistem, parameter aktif, atau memasang APK saat penelitian ini.

## Hasil utama

Mesin bawaan mendukung pitch numerik, tetapi jalur aplikasi yang ditemukan hanya menerima nama paket dan nomor preset. Nilai pitch dimuat dari file vendor ke memori ketika objek pengolah suara dibuat. Tidak ditemukan perintah reload konfigurasi atau setter pitch bebas pada jalur parameter yang dianalisis.

## Format dan skala

File `/vendor/etc/tranMagiqueSonMode.dat`:

```text
MODE_PARAM 4
1 100 -25 100
2 100 -70 100
3 100 60 100
4 100 100 100
```

Urutan kolom: mode, tempo mentah, pitch mentah, rate mentah. Fungsi `magiqueSonSetup` pada `libmagiqueson.so` alamat 0x2450 menjalankan:

```text
tempo_arg = (tempo_raw - 100) / 100.0
pitch_semitones = pitch_raw / 10.0
rate_arg = (rate_raw - 100) / 100.0
SoundTouch.setTempoChange(tempo_arg)
SoundTouch.setPitchSemiTones(pitch_semitones)
SoundTouch.setRateChange(rate_arg)
```

Ini menggambarkan argumen aktual ke library, bukan asumsi persentase UI.

| Preset | Pitch mentah | Semitone ke SoundTouch | Laporan pengguna |
|---|---:|---:|---|
| 1 | -25 | -2,5 | Agak berat |
| 2 | -70 | -7 | Berat |
| 3 | 60 | +6 | Agak tinggi |
| 4 | 100 | +10 | Tinggi / seperti anak perempuan |

Contoh perhitungan: target pitch -4 semitone akan direpresentasikan sebagai nilai mentah -40 pada file ini. Ini hanya perhitungan offline; file HP tidak diubah dan penerapan custom belum diuji.

## Kapan konfigurasi dibaca

- Constructor `TranMagiqueSon` (0x2060) memanggil `magiqueSonInit` pada 0x20fc.
- `magiqueSonInit` (0x2130) membuka path tetap file vendor dengan `fopen`, membaca header dan empat integer per mode dengan `fscanf`, lalu menutup file.
- Di `libaudioclient.so`, `AudioRecord::set` membuat objek `TranMagiqueSon` pada call site 0x63a20.
- `AudioRecord::read` dan `AudioRecord::processAudioBuffer` mengambil parameter `TranMagiqueMode`, mengatur mode, memanggil setup bila mode berubah, lalu memproses buffer.
- `magiqueSonPackageModeSet` (0x23b0) mem-parsing `%[^,],%d`, membandingkan nama paket dengan objek perekaman, dan memperbarui nomor mode. Fungsi ini tidak memanggil pembaca konfigurasi.
- `magiqueSonSetup` memakai tabel yang sudah ada di objek; tidak membuka ulang file.

Implikasi: sesudah mengganti file melalui modifikasi sistem, memilih preset ulang saja belum membuktikan nilainya dimuat ulang. Perlu membuat ulang objek AudioRecord; memulai ulang aplikasi target adalah kandidat langkah pengujian. Mematikan/menghidupkan mikrofon bisa saja memakai objek lama dan belum tentu cukup. Belum ada pengujian dinamis atas reload atau modifikasi file.

## Batas akses dan rencana realistis

File vendor adalah root:root, mode 0644; /vendor merupakan EROFS yang dipasang read-only. Aplikasi biasa dan akses ADB saat ini tidak dapat menggantinya. Memuat library yang sama di aplikasi kita hanya memengaruhi proses/buffer aplikasi kita sendiri, tidak objek AudioRecord milik WA/ML.

Kode ini mengalokasikan empat entri preset. Jangan mencoba menambahkan mode 5 atau mengirim payload tambahan sebagai pengganti slider; itu bukan antarmuka custom yang didukung.

Eksperimen custom paling kecil, jika kelak tersedia perangkat uji dengan akses sistem yang sesuai: ganti nilai pitch salah satu dari empat slot melalui konfigurasi pengganti, buat ulang aplikasi target, kemudian bandingkan audio yang didengar penerima. Ini tetap hipotesis integrasi, bukan keberhasilan teruji. Slider pitch langsung saat percakapan memerlukan mekanisme pengaturan ulang dalam proses atau dukungan vendor tambahan; penggantian file saja tidak menyediakannya.

## Bukti lokal

Salinan binary, skrip Python ELF/Capstone dan hasil disassembly disimpan secara lokal di luar repositori dan tidak didistribusikan. Analisis simbol dan instruksi ARM64 dilakukan pada `libmagiqueson.so` dan call site relevan `libaudioclient.so`; bukan audit lengkap semua framework, firmware, atau jalur tersembunyi lain.
