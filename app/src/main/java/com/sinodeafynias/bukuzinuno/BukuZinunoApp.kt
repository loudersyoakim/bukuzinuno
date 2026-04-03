package com.sinodeafynias.bukuzinuno

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.sinodeafynias.bukuzinuno.data.local.AppDatabase
import com.sinodeafynias.bukuzinuno.data.repository.LaguRepository

class BukuZinunoApp : Application() {

    // Menggunakan 'lazy' agar database & repository baru diciptakan
    // SAAT pertama kali dipanggil saja (hemat memori di awal).
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { LaguRepository(database.laguDao()) }

    override fun onCreate() {
        super.onCreate()

        // --- FIREBASE OFFLINE CAPABILITY ---
        // 1. Mengaktifkan fitur penyimpanan cache offline (Wajib dipanggil pertama kali)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // 2. Memaksa data 'app_info' agar selalu di-download & disimpan di HP
        // Sehingga jemaat tetap bisa lihat info & kontak walau tidak ada internet
        FirebaseDatabase.getInstance().getReference("app_info").keepSynced(true)
    }
}