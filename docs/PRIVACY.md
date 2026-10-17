# Privasi VoxPanel

VoxPanel memilih efek audio bawaan perangkat. Aplikasi tidak merekam, menyimpan, mengirim, atau menganalisis suara pengguna. Tidak ada akun, analytics, iklan, backend, maupun izin internet atau mikrofon di manifest aplikasi.

Yang disimpan lokal: aplikasi target, perintah efek terakhir untuk membantu reset, posisi kontrol mengambang per orientasi, serta status permintaan izin notifikasi. Data tersebut berada di penyimpanan privat aplikasi; pencadangan aplikasi dinonaktifkan. Status perintah bukan rekaman audio.

## Izin

| Izin | Penggunaan |
|---|---|
| MODIFY_AUDIO_SETTINGS | Mengirim dan membaca parameter efek vendor |
| SYSTEM_ALERT_WINDOW | Kontrol mengambang setelah pengguna mengizinkannya |
| FOREGROUND_SERVICE | Menjaga kontrol terlihat saat aplikasi target aktif |
| POST_NOTIFICATIONS | Notifikasi kontrol dan tindakan Normal & tutup |

Hanya dua aplikasi target yang dideklarasikan untuk visibilitas paket. Tidak ada izin aksesibilitas atau pembacaan seluruh daftar aplikasi.

Menutup overlay menghentikan foreground service dan mencoba mengirim Normal. Force stop atau proses yang dibunuh sistem bisa melewati callback cleanup; gunakan tombol Normal jika perlu. Menghapus VoxPanel tidak menjamin nilai efek sistem langsung direset: gunakan Normal sebelum uninstall.

Halaman ini menjelaskan kode proyek, bukan kebijakan WhatsApp, Mobile Legends, Android, atau vendor perangkat.
