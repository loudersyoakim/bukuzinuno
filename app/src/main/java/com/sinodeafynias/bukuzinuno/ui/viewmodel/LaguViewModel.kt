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
            // 1. LOAD DATA AWAL DARI ASSETS (Pencegahan Layar Kosong)
            muatAppInfoLokal(context)

            val laguSaatIni = repository.semuaLagu.first()
            if (laguSaatIni.isEmpty()) {
                muatLaguDariJsonLokal(context, prefs)
            }

            // 2. CEK UPDATE KE FIREBASE (Hanya download jika versi lebih tinggi)
            syncAppInfoRemote(prefs)
            syncNodeLaguRemote(prefs, nodeName = "lagu", prefKey = "versi_lagu")
            syncNodeLaguRemote(prefs, nodeName = "dll", prefKey = "versi_dll")
        }
    }

    // --- BACA APP_INFO.JSON DARI ASSETS ---
    private fun muatAppInfoLokal(context: Context) {
        try {
            val inputStream = context.assets.open("app_info.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = Gson().fromJson(reader, type)

            appDescription = data["description"]?.toString() ?: ""
            churchEmail = data["email"]?.toString() ?: ""
            devName = data["developer"]?.toString() ?: ""
            thankYouNote = data["thanks"]?.toString() ?: ""
            devContact = data["whatsapp"]?.toString() ?: ""

            reader.close()
            Log.d("Sync", "App Info lokal berhasil dimuat")
        } catch (e: Exception) {
            Log.e("Sync", "Gagal muat app_info.json: ${e.message}")
        }
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

                        prefs.edit().putInt("versi_app_info", versiServer).apply()
                        Log.d("Sync", "App Info diperbarui dari Firebase ke versi $versiServer")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Sync", "Firebase Error: ${error.message}")
            }
        })
    }

    // --- SYNC LAGU/DLL DARI FIREBASE (DELTA SYNC) ---
    private fun syncNodeLaguRemote(prefs: SharedPreferences, nodeName: String, prefKey: String) {
        val versiLokal = prefs.getInt(prefKey, 1)
        val ref = FirebaseDatabase.getInstance().getReference(nodeName)

        // Query: Ambil data yang versinya > versi di HP
        ref.orderByChild("version").startAfter(versiLokal.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d("Sync", "Tabel '$nodeName' sudah versi terbaru")
                        return
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        val daftarLaguBaru = mutableListOf<Lagu>()
                        var versiTertinggi = versiLokal

                        for (laguSnap in snapshot.children) {
                            val v = laguSnap.child("version").getValue(Int::class.java) ?: 1
                            if (v > versiTertinggi) versiTertinggi = v

                            val lirikList = mutableListOf<String>()
                            laguSnap.child("lirik").children.forEach {
                                it.value?.toString()?.let { baris -> lirikList.add(baris) }
                            }

                            val lagu = Lagu(
                                id = laguSnap.child("id").value?.toString() ?: "",
                                nomor_urut = laguSnap.child("nomor_urut").getValue(Int::class.java) ?: 0,
                                nomor = laguSnap.child("nomor").value?.toString() ?: "",
                                judul = laguSnap.child("judul").value?.toString() ?: "",
                                kategori = laguSnap.child("kategori").value?.toString() ?: "",
                                nada = laguSnap.child("nada").value?.toString() ?: "",
                                lirik = lirikList,
                                version = v
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