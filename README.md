# Buku Zinuno AFY

Aplikasi **Buku Zinuno Angowuloa Fa'awösa Khö Yesu (AFY)** berbasis Android. Dibangun untuk memudahkan jemaat dalam memuji Tuhan kapan saja dan di mana saja, lengkap dengan lirik dan melodi lagu.

## Fitur Utama

- **Audio & Melodi Offline**: Putar nada lagu langsung dari dalam aplikasi. Mendukung pengunduhan penuh untuk penggunaan tanpa koneksi internet saat ibadah.
- **Smart Sync Audio (Delta Update)**: Sistem pembaruan audio cerdas. Saat menekan tombol unduh, sistem membandingkan versi lokal dan server, lalu **hanya mengunduh** file MP3 yang belum ada atau versi yang diperbarui. Sangat menghemat kuota dan memori perangkat.
- **Sinkronisasi Cloud *Real-time***: Pembaruan data lirik, kategori, dan informasi aplikasi dilakukan secara otomatis di latar belakang melalui Firebase, tanpa mewajibkan pengguna memperbarui aplikasi via Play Store.
- **Pencarian Cepat**: Mesin pencari instan untuk menemukan lagu berdasarkan Nomor Urut atau Judul.
- **Daftar Favorit**: Simpan lagu-lagu yang sering dinyanyikan agar mudah diakses kembali.
- **Kenyamanan Pengguna (UI/UX)**: 
  - Dukungan **Mode Gelap (Dark Mode)** penuh.
  - Fitur **Layar Tetap Menyala (Keep Screen On)** agar HP tidak otomatis terkunci saat sedang bernyanyi.

## Teknologi & Arsitektur

Aplikasi ini dibangun menggunakan pendekatan *Modern Android Development* (MAD):

- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchronous**: Coroutines & StateFlow / SharedFlow
- **Penyimpanan Lokal**: Room Database (SQLite) & SharedPreferences
- **Backend/Cloud**: Firebase Realtime Database
- **Analytics**: Firebase Analytics

## Repositori Terkait

Untuk menjaga efisiensi ukuran aplikasi, file audio MP3 tidak di-bundling secara langsung di dalam APK, melainkan di-hosting secara terpisah:
- **Repositori Audio MP3**: [bkz_afy_audio](https://github.com/loudersyoakim/bkz_afy_audio)

## Unduh Aplikasi

Versi stabil dari aplikasi ini dapat diunduh secara resmi melalui Google Play Store:
> [Buku Zinuno AFY di Google Play Store](https://play.google.com/store/apps/details?id=com.sinodeafynias.bukuzinuno)

## Lisensi & Penggunaan

- Konten lirik dan lagu di dalam aplikasi ini merupakan himne rohani Buku Zinuno Niha Keriso yang digunakan secara umum.
- Kode sumber aplikasi ini bersifat terbuka (*open-source*) untuk tujuan transparansi, referensi, dan pembelajaran.

---
*Dikembangkan oleh [Louders Yoakim Telaumbanua](https://github.com/loudersyoakim).*
