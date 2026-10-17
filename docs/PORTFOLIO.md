# VoxPanel — studi kasus portofolio

## Ringkasan singkat

VoxPanel adalah aplikasi Android native untuk memilih empat karakter suara bawaan TECNO melalui panel mengambang. Prototipe diuji pengguna pada TECNO SPARK 20 dengan Android 13 untuk panggilan WhatsApp dan voice chat Mobile Legends, termasuk saat pertandingan. Pengujian pengguna melaporkan perubahan suara dan penggunaan yang lancar.

## Masalah dan keputusan desain

Pengguna ingin mengganti karakter suara tanpa meninggalkan permainan. Tantangan utamanya adalah akses ke jalur input audio aplikasi lain. Pemeriksaan komponen vendor menemukan parameter preset yang dapat diakses dari aplikasi biasa pada perangkat uji. Proyek kemudian difokuskan pada pengontrol efek dan pengalaman penggunaan, bukan melatih model AI atau mengklaim kompatibilitas Android universal.

## Implementasi

- Java dan API native Android, tanpa dependency UI runtime.
- Pemisahan pembacaan/validasi protokol vendor, kontrol audio, tampilan, dan foreground service.
- TYPE_APPLICATION_OVERLAY dengan handle yang dapat digeser, panel yang dapat digulir, dan posisi tersimpan per orientasi.
- Status membaca parameter vendor, bukan menganggap pemanggilan API selalu berhasil.
- Pemulihan Normal saat target diganti atau service ditutup secara normal.
- Tidak memerlukan akses mikrofon, internet, aksesibilitas, atau root.
- Gradle wrapper, pengujian protokol, lint, dan workflow CI untuk build debug.

## Hasil dan batas validasi

Hasil suara diverifikasi secara manual oleh pengguna/penerima pada perangkat tersebut. Pengguna juga mengonfirmasi versi 0.4.0 sudah diuji dan lancar. Belum ada angka latensi end-to-end, benchmark baterai, sertifikasi kompatibilitas lintas perangkat, atau uji laboratorium jangka panjang.

Kustomisasi pitch bebas diteliti melalui analisis statis lokal. Hasilnya menunjukkan nilai pitch disimpan dalam konfigurasi vendor dan tidak ditemukan jalur setter bebas untuk aplikasi biasa. Karena pengguna menginginkan penggunaan tanpa root, fitur final tetap empat preset vendor.

## Kalimat untuk kartu proyek

> VoxPanel — aplikasi Android native dengan panel mengambang untuk mengontrol efek suara bawaan TECNO. Diuji pengguna pada WhatsApp dan Mobile Legends di TECNO SPARK 20, dengan pemisahan kontrol audio, foreground service, dan validasi parameter vendor.

## English summary

VoxPanel is a native Android floating control panel for vendor-provided voice presets. User testing confirmed audible voice changes in WhatsApp calls and Mobile Legends voice chat on a TECNO SPARK 20 running Android 13. The project focuses on device-specific integration, overlay lifecycle management, and honest status reporting; it does not provide custom AI voice conversion or claim universal Android compatibility.
