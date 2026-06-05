# Buku Zinuno AFY

Aplikasi **Buku Zinuno Angowuloa Fa'awösa Khö Yesu (AFY)** berbasis Android. Dibangun untuk memudahkan jemaat dalam memuji Tuhan kapan saja dan di mana saja, lengkap dengan lirik dan melodi lagu.

## Fitur Utama

*   **Audio & Melodi 100% Offline**: Putar nada lagu langsung dari dalam aplikasi tanpa perlu koneksi internet. Mulai dari versi 1.3, seluruh file audio telah tertanam (*bundled*) langsung di dalam aplikasi sejak pertama kali diinstal.
*   **Sinkronisasi Cloud *Real-time***: Pembaruan data lirik, kategori, dan informasi aplikasi dilakukan secara otomatis di latar belakang melalui Firebase, tanpa mewajibkan pengguna memperbarui aplikasi via Play Store.
*   **Pencarian Cepat**: Mesin pencari instan untuk menemukan lagu berdasarkan Nomor Urut atau Judul.
*   **Daftar Favorit**: Simpan lagu-lagu yang sering dinyanyikan agar mudah diakses kembali.

## Kenyamanan Pengguna (UI/UX)

*   **Pinch to Zoom**: Sesuaikan ukuran teks lirik dengan mudah hanya dengan gerakan mencubit layar—sangat membantu untuk keterbacaan yang lebih baik bagi semua kalangan usia.
*   **Fitur Berbagi (Sharing)**: Bagikan lirik lagu favorit ke media sosial atau aplikasi pesan (WhatsApp, dll) hanya dengan satu klik.
*   **Layar Tetap Menyala (Keep Screen On)**: Mencegah layar HP terkunci atau redup secara otomatis saat sedang bernyanyi atau beribadah.
*   **Dukungan Mode Gelap (Dark Mode)**: Tampilan antarmuka yang penuh kenyamanan di mata, baik dalam kondisi cahaya terang maupun gelap.

## Teknologi & Arsitektur

Aplikasi ini dibangun menggunakan pendekatan *Modern Android Development* (MAD):

- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) 
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchronous**: Coroutines & StateFlow / SharedFlow
- **Penyimpanan Lokal**: Room Database (SQLite) & SharedPreferences
- **Backend/Cloud**: Firebase Realtime Database
- **Analytics**: Firebase Analytics

## Repositori Terkait

Meskipun saat ini seluruh file audio sudah tertanam (*bundled*) di dalam APK untuk kemudahan akses *offline*, direktori mentah file MP3 tetap di-hosting secara terpisah untuk tujuan transparansi dan pengembangan (*open-source*):
- **Repositori Audio MP3**: [bkz_afy_audio](https://github.com/loudersyoakim/bkz_afy_audio)

## Catatan Rilis: Versi 1.3 (Pembaruan dari v1.1)

Versi 1.3 membawa perombakan besar pada cara aplikasi menangani data audio untuk meningkatkan kenyamanan jemaat:
*   **Migrasi ke Bundled Audio**: Menghapus sistem unduh mandiri (Smart Sync). Seluruh 366 file audio kini ditanam langsung ke dalam APK (direktori `assets`). Pengguna tidak perlu lagi mendownload lagu satu per satu.
*   **Perbaikan Kualitas Audio (ExoPlayer)**: Menerapkan mekanisme *volume fade-in* (300ms) dan *millisecond-skip* untuk menghilangkan bunyi gangguan (*noise/chss*) di awal rekaman MP3.
*   **Penyederhanaan UI**: Membersihkan menu "Penyimpanan (Audio Offline)" dan menghapus seluruh status pelacakan unduhan dari menu utama, membuat tampilan aplikasi jauh lebih ringan, rapi, dan mudah digunakan.

## Unduh Aplikasi

Versi stabil dari aplikasi ini dapat diunduh secara resmi melalui Google Play Store:
> [Buku Zinuno AFY di Google Play Store](https://play.google.com/store/apps/details?id=com.sinodeafynias.bukuzinuno)

## Lisensi & Penggunaan

- Konten lirik dan lagu di dalam aplikasi ini merupakan himne rohani Buku Zinuno Niha Keriso yang digunakan secara umum.
- Kode sumber aplikasi ini bersifat terbuka (*open-source*) untuk tujuan transparansi, referensi, dan pembelajaran.

---
*Dikembangkan oleh [Louders Yoakim Telaumbanua](https://github.com/loudersyoakim).*
