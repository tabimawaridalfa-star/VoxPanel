# Validasi dan uji perangkat

## Hasil yang dilaporkan pengguna

Perangkat: TECNO SPARK 20 (KJ5n), Android 13. Pengguna membandingkan empat preset dan Normal melalui WhatsApp. Suara berubah sesuai pilihan. Pengguna juga melaporkan keberhasilan voice chat Mobile Legends selama pertandingan serta penggunaan panel mengambang versi 0.3 yang lancar.

Ini laporan pengujian manual, bukan pengukuran latensi atau jaminan untuk perangkat lain.

## Pemeriksaan versi 0.4.0

Sesudah pemasangan versi 0.4.0, pengguna mengonfirmasi sudah menguji aplikasi dan hasilnya lancar. Ini konfirmasi penggunaan manual; tidak mengubah status checklist spesifik atau menjadi pengukuran latensi/baterai.

- Gradle 8.9 / AGP 8.7.3 / JDK 17: `testDebugUnitTest assembleDebug lintDebug` selesai BUILD SUCCESSFUL.
- Lima unit test, nol failure, nol error.
- Lint: nol error, 22 warning. Peringatan mencakup target API 33 yang dipertahankan untuk perangkat uji, rekomendasi versi compileSdk, deklarasi SDK ganda untuk script legacy, konfigurasi backup, aksesibilitas touch handle, dan teks UI yang belum dilokalkan melalui resource. Ini bukan hasil lint bebas warning.
- Build script lokal menghasilkan APK dengan tanda tangan v2/v3 yang terverifikasi; update dan cold launch 0.4.0 berhasil pada KJ5n.
- Panel dibuka pada perangkat, target ML dipilih dan efek Berat terbaca kembali dari vendor. Saat target diganti ke WhatsApp, parameter target ML kembali Normal.
- Normal & tutup dari layar utama berhasil mengirim Normal tanpa pesan error, menghapus overlay, dan menghentikan service. Jalur penutupan panel/notifikasi serta pemulihan setelah force stop belum diperiksa ulang pada versi ini.
- Screenshot README diambil dari layar VoxPanel sendiri dan diperiksa secara visual; tidak memakai screenshot panggilan/kontak.
- Workflow CI sudah disiapkan tetapi belum dijalankan di GitHub karena belum ada remote/push.
- Pengguna sudah mengonfirmasi pemakaian 0.4.0 lancar. Benchmark baterai, semua rute headset, dan checklist izin/rotasi/pemulihan lengkap belum diverifikasi satu per satu.

## Pemeriksaan otomatis

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

VoiceProtocolTest memeriksa respons perangkat, format dengan prefix/whitespace, respons rusak/kosong, preset di luar rentang, dan penolakan target/payload yang tidak didukung. Ini tidak meniru audio vendor dan tidak membuktikan suara berubah.

## Checklist sesudah memperbarui versi

- Buka VoxPanel; tidak ada efek yang diterapkan otomatis.
- Pilih WA atau ML, nyalakan mikrofon target, bandingkan Normal dan empat efek bersama penerima.
- Pastikan indikator mengikuti respons vendor dan tidak mengubah status kosong menjadi Normal.
- Ganti target saat efek aktif; target lama harus kembali Normal atau aplikasi memberi pesan bahwa reset belum terkonfirmasi.
- Buka overlay, ubah target dan efek dari panel.
- Geser VP, kecilkan, tutup, buka kembali, lalu cek posisi portrait dan landscape.
- Uji panel dalam landscape: semua kontrol dapat dicapai dengan menggulir.
- Tutup lewat panel, aplikasi, dan tindakan notifikasi. Periksa respons vendor kembali Normal.
- Tolak izin overlay/notifikasi, lalu izinkan kembali melalui pengaturan; aplikasi tidak boleh crash.
- Setelah force stop/reboot, periksa ulang Normal secara manual.
- Uji speaker, headset kabel/USB, dan Bluetooth secara terpisah; jangan menganggap satu hasil berlaku untuk semua rute audio.

Jangan menelepon atau mengirim pesan otomatis untuk pengujian. Minta bantuan penerima yang bersedia dan hapus data pribadi dari materi demo publik.
