package com.sinodeafynias.bukuzinuno.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sinodeafynias.bukuzinuno.data.local.Lagu
import com.sinodeafynias.bukuzinuno.data.repository.LaguRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class LaguViewModel(private val repository: LaguRepository) : ViewModel() {

    // --- DATA LOKAL (ROOM) ---
    val semuaLagu: StateFlow<List<Lagu>> = repository.semuaLagu
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val laguFavorit: StateFlow<List<Lagu>> = repository.laguFavorit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFavorit(id: String, isFavorit: Boolean) {
        viewModelScope.launch { repository.updateFavorit(id, isFavorit) }
    }

    // --- STATE APP INFO (Dinamis dari JSON & Firebase) ---
    var appDescription by mutableStateOf("")
        private set
    var churchEmail by mutableStateOf("")
        private set
    var devContact by mutableStateOf("")
        private set
    var devName by mutableStateOf("")
        private set
    var thankYouNote by mutableStateOf("")
        private set

    // ==========================================
    // SISTEM SMART SYNC (DELTA SYNC)
    // ==========================================
    fun sinkronisasiCerdas(context: Context) {
        val prefs = context.getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE)

        viewModelScope.launch {
            // 1. TAHAP INFO APP: Jangan baca JSON terus-terusan! Cek dulu versinya.
            muatAppInfoLokalAtauPrefs(context, prefs)
            syncAppInfoRemote(prefs)

            // 2. TAHAP LAGU: Cek Room, HANYA baca JSON jika database HP benar-benar kosong
            val laguSaatIni = repository.semuaLagu.first()
            if (laguSaatIni.isEmpty()) {
                muatLaguDariJsonLokal(context, prefs)
            }

            // 3. CEK UPDATE KE FIREBASE
            syncNodeLaguRemote(prefs, nodeName = "lagu", prefKey = "versi_lagu")
            syncNodeLaguRemote(prefs, nodeName = "dll", prefKey = "versi_dll")
        }
    }

    // --- BACA APP INFO (ANTI AMNESIA) ---
    private fun muatAppInfoLokalAtauPrefs(context: Context, prefs: SharedPreferences) {
        val versiLokalInfo = prefs.getInt("versi_app_info", 0)

        if (versiLokalInfo == 0) {
            // JIKA BARU INSTALL: Baca dari JSON
            try {
                val inputStream = context.assets.open("app_info.json")
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val data: Map<String, Any> = Gson().fromJson(InputStreamReader(inputStream), type)

                appDescription = data["description"]?.toString() ?: ""
                churchEmail = data["email"]?.toString() ?: ""
                devName = data["developer"]?.toString() ?: ""
                thankYouNote = data["thanks"]?.toString() ?: ""
                devContact = data["whatsapp"]?.toString() ?: ""
            } catch (e: Exception) {
                Log.e("Sync", "Gagal muat app_info.json: ${e.message}")
            }
        } else {
            // JIKA SUDAH PERNAH UPDATE FIREBASE: Baca dari SharedPreferences
            appDescription = prefs.getString("app_desc", "") ?: ""
            churchEmail = prefs.getString("app_email", "") ?: ""
            devName = prefs.getString("app_dev", "") ?: ""
            thankYouNote = prefs.getString("app_thanks", "") ?: ""
            devContact = prefs.getString("app_wa", "") ?: ""
        }
    }

    // --- SYNC APP INFO DARI FIREBASE ---
    private fun syncAppInfoRemote(prefs: SharedPreferences) {
        val versiLokalInfo = prefs.getInt("versi_app_info", 0)
        val infoRef = FirebaseDatabase.getInstance().getReference("app_info")

        infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val versiServer = snapshot.child("version").getValue(Int::class.java) ?: 1

                    if (versiServer > versiLokalInfo || versiLokalInfo == 0) {
                        appDescription = snapshot.child("description").value?.toString() ?: appDescription
                        churchEmail = snapshot.child("email").value?.toString() ?: churchEmail
                        devContact = snapshot.child("whatsapp").value?.toString() ?: devContact
                        devName = snapshot.child("developer").value?.toString() ?: devName
                        thankYouNote = snapshot.child("thanks").value?.toString() ?: thankYouNote

                        // SIMPAN KE PREFS SECARA PERMANEN AGAR TIDAK HILANG SAAT REFRESH
                        prefs.edit()
                            .putInt("versi_app_info", versiServer)
                            .putString("app_desc", appDescription)
                            .putString("app_email", churchEmail)
                            .putString("app_wa", devContact)
                            .putString("app_dev", devName)
                            .putString("app_thanks", thankYouNote)
                            .apply()

                        Log.d("Sync", "App Info diperbarui dari Firebase ke versi $versiServer")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- BACA LAGU & ONONOTA DARI ASSETS (Hanya 1x saat install) ---
    private suspend fun muatLaguDariJsonLokal(context: Context, prefs: SharedPreferences) {
        withContext(Dispatchers.IO) {
            try {
                val gson = Gson()
                val tipeDaftar = object : TypeToken<List<Lagu>>() {}.type
                val semuaDataDigabung = mutableListOf<Lagu>()
                val daftarFileJson = listOf("lirik_lagu.json", "ononota.json")

                for (namaFile in daftarFileJson) {
                    try {
                        val inputStream = context.assets.open(namaFile)
                        val daftarLagu: List<Lagu> = gson.fromJson(InputStreamReader(inputStream), tipeDaftar)
                        semuaDataDigabung.addAll(daftarLagu)
                    } catch (e: Exception) {
                        Log.e("Sync", "Gagal membaca $namaFile: ${e.message}")
                    }
                }

                if (semuaDataDigabung.isNotEmpty()) {
                    repository.insertSemuaLagu(semuaDataDigabung)
                    prefs.edit().putInt("versi_lagu", 1).putInt("versi_dll", 1).apply()
                    Log.d("Sync", "Lagu lokal berhasil diimport")
                }
            } catch (e: Exception) {
                Log.e("Sync", "Gagal import lagu: ${e.message}")
            }
        }
    }

    // --- SYNC LAGU/DLL DARI FIREBASE (ANTI HILANG FAVORIT) ---
    private fun syncNodeLaguRemote(prefs: SharedPreferences, nodeName: String, prefKey: String) {
        val versiLokal = prefs.getInt(prefKey, 1)
        val ref = FirebaseDatabase.getInstance().getReference(nodeName)

        ref.orderByChild("version").startAfter(versiLokal.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    viewModelScope.launch(Dispatchers.IO) {
                        val daftarLaguBaru = mutableListOf<Lagu>()
                        var versiTertinggi = versiLokal

                        // AMBIL DAFTAR LAGU LOKAL UNTUK MENGECEK STATUS BINTANG
                        val daftarLaguLokal = repository.semuaLagu.first()

                        for (laguSnap in snapshot.children) {
                            val v = laguSnap.child("version").getValue(Int::class.java) ?: 1
                            if (v > versiTertinggi) versiTertinggi = v

                            val lirikList = mutableListOf<String>()
                            laguSnap.child("lirik").children.forEach {
                                it.value?.toString()?.let { baris -> lirikList.add(baris) }
                            }

                            val idLagu = laguSnap.child("id").value?.toString() ?: ""

                            // CARI LAGU INI DI HP, KALAU ADA BINTANGNYA, PERTAHANKAN!
                            val laguLama = daftarLaguLokal.find { it.id == idLagu }
                            val statusFavorit = laguLama?.isFavorit ?: false

                            val lagu = Lagu(
                                id = idLagu,
                                nomor_urut = laguSnap.child("nomor_urut").getValue(Int::class.java) ?: 0,
                                nomor = laguSnap.child("nomor").value?.toString() ?: "",
                                judul = laguSnap.child("judul").value?.toString() ?: "",
                                kategori = laguSnap.child("kategori").value?.toString() ?: "",
                                nada = laguSnap.child("nada").value?.toString() ?: "",
                                lirik = lirikList,
                                version = v,
                                isFavorit = statusFavorit // <--- KUNCI ANTI AMNESIA
                            )
                            daftarLaguBaru.add(lagu)
                        }

                        if (daftarLaguBaru.isNotEmpty()) {
                            repository.insertSemuaLagu(daftarLaguBaru)
                            prefs.edit().putInt(prefKey, versiTertinggi).apply()
                            Log.d("Sync", "Sukses update ${daftarLaguBaru.size} data di '$nodeName'")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}