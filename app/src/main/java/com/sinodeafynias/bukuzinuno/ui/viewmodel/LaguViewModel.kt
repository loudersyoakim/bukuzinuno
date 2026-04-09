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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

// Data Class untuk menampung struktur Aturan Privasi
data class PrivacyRule(
    val head: String,
    val content: String
)

class LaguViewModel(private val repository: LaguRepository) : ViewModel() {

    // --- DATA LOKAL (ROOM) ---
    val semuaLagu: StateFlow<List<Lagu>> = repository.semuaLagu
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val laguFavorit: StateFlow<List<Lagu>> = repository.laguFavorit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFavorit(id: String, isFavorit: Boolean) {
        viewModelScope.launch { repository.updateFavorit(id, isFavorit) }
    }

    // --- STATE APP INFO ---
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

    // STATE KHUSUS PRIVACY POLICY BERSARANG
    var privacyIntro by mutableStateOf("")
        private set
    var privacyLastUpdate by mutableStateOf("")
        private set
    var privacyRules by mutableStateOf<List<PrivacyRule>>(emptyList())
        private set

    fun sinkronisasiCerdas(context: Context) {
        val prefs = context.getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE)

        viewModelScope.launch {
            // 1. TAHAP INFO APP
            muatAppInfoLokalAtauPrefs(context, prefs)
            syncAppInfoRemote(prefs)

            // 2. TAHAP LAGU: Cek via SharedPreferences (Bukan via Database agar INSTAN)
            val isFirstInstallLagu = prefs.getInt("versi_lagu", 0) == 0
            if (isFirstInstallLagu) {
                muatLaguDariJsonLokal(context, prefs)
            }

            // 3. CEK UPDATE KE FIREBASE
            syncNodeLaguRemote(prefs, nodeName = "lagu", prefKey = "versi_lagu")
            syncNodeLaguRemote(prefs, nodeName = "dll", prefKey = "versi_dll")
        }
    }

    private fun muatAppInfoLokalAtauPrefs(context: Context, prefs: SharedPreferences) {
        val versiLokalInfo = prefs.getInt("versi_app_info", 0)

        // BACA DULU DARI PREFERENCES
        appDescription = prefs.getString("app_desc", "") ?: ""
        churchEmail = prefs.getString("app_email", "") ?: ""
        devName = prefs.getString("app_dev", "") ?: ""
        thankYouNote = prefs.getString("app_thanks", "") ?: ""
        devContact = prefs.getString("app_wa", "") ?: ""

        privacyIntro = prefs.getString("privacy_intro", "") ?: ""
        privacyLastUpdate = prefs.getString("privacy_update", "") ?: ""

        // Baca list Rules menggunakan Gson
        val rulesJson = prefs.getString("privacy_rules", "[]") ?: "[]"
        try {
            val typeList = object : TypeToken<List<PrivacyRule>>() {}.type
            privacyRules = Gson().fromJson(rulesJson, typeList) ?: emptyList()
        } catch (e: Exception) {
            privacyRules = emptyList()
        }

        // JIKA MASIH KOSONG, BACA DARI JSON
        if (versiLokalInfo == 0 || appDescription.isEmpty()) {
            try {
                val inputStream = context.assets.open("app_info.json")
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val data: Map<String, Any> = Gson().fromJson(InputStreamReader(inputStream), type)

                appDescription = data["description"]?.toString() ?: appDescription
                churchEmail = data["email"]?.toString() ?: churchEmail
                devName = data["developer"]?.toString() ?: devName
                thankYouNote = data["thanks"]?.toString() ?: thankYouNote
                devContact = data["whatsapp"]?.toString() ?: devContact

                // Parsing Objek Bersarang (Nested) Privacy Policy
                val privacyMap = data["privacy_policy"] as? Map<String, Any>
                if (privacyMap != null) {
                    privacyIntro = privacyMap["intro"]?.toString() ?: ""
                    privacyLastUpdate = privacyMap["last_update"]?.toString() ?: ""

                    val tempRules = mutableListOf<PrivacyRule>()
                    for (i in 1..10) {
                        val rule = privacyMap["rule_$i"] as? Map<String, Any>
                        if (rule != null) {
                            tempRules.add(
                                PrivacyRule(
                                    head = rule["head"]?.toString() ?: "",
                                    content = rule["content"]?.toString() ?: ""
                                )
                            )
                        }
                    }
                    privacyRules = tempRules
                }

                // SIMPAN KE PREFERENCES
                prefs.edit()
                    .putString("app_desc", appDescription)
                    .putString("app_email", churchEmail)
                    .putString("app_wa", devContact)
                    .putString("app_dev", devName)
                    .putString("app_thanks", thankYouNote)
                    .putString("privacy_intro", privacyIntro)
                    .putString("privacy_update", privacyLastUpdate)
                    .putString("privacy_rules", Gson().toJson(privacyRules))
                    .putInt("versi_app_info", 1)
                    .apply()

                Log.d("Sync", "Berhasil muat app_info.json ke Prefs dengan struktur baru")
            } catch (e: Exception) {
                Log.e("Sync", "Gagal muat app_info.json: ${e.message}")
            }
        }
    }

    private fun syncAppInfoRemote(prefs: SharedPreferences) {
        val versiLokalInfo = prefs.getInt("versi_app_info", 0)
        val infoRef = FirebaseDatabase.getInstance().getReference("app_info")

        infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val versiServer = snapshot.child("version").getValue(Int::class.java) ?: 1

                    if (versiServer > versiLokalInfo || versiLokalInfo == 0) {

                        // TOMBOL NUKLIR: Jika Admin menaikkan versi app_info, paksa sinkronisasi ulang lirik lagu
                        if (versiLokalInfo != 0) {
                            prefs.edit().putInt("versi_lagu", 0).putInt("versi_dll", 0).apply()
                        }

                        appDescription = snapshot.child("description").value?.toString() ?: appDescription
                        churchEmail = snapshot.child("email").value?.toString() ?: churchEmail
                        devContact = snapshot.child("whatsapp").value?.toString() ?: devContact
                        devName = snapshot.child("developer").value?.toString() ?: devName
                        thankYouNote = snapshot.child("thanks").value?.toString() ?: thankYouNote

                        // Parsing Privacy Policy dari Firebase
                        val privacySnap = snapshot.child("privacy_policy")
                        if (privacySnap.exists()) {
                            privacyIntro = privacySnap.child("intro").value?.toString() ?: privacyIntro
                            privacyLastUpdate = privacySnap.child("last_update").value?.toString() ?: privacyLastUpdate

                            val tempRules = mutableListOf<PrivacyRule>()
                            for (i in 1..10) {
                                val ruleSnap = privacySnap.child("rule_$i")
                                if (ruleSnap.exists()) {
                                    tempRules.add(
                                        PrivacyRule(
                                            head = ruleSnap.child("head").value?.toString() ?: "",
                                            content = ruleSnap.child("content").value?.toString() ?: ""
                                        )
                                    )
                                }
                            }
                            if (tempRules.isNotEmpty()) {
                                privacyRules = tempRules
                            }
                        }

                        prefs.edit()
                            .putInt("versi_app_info", versiServer)
                            .putString("app_desc", appDescription)
                            .putString("app_email", churchEmail)
                            .putString("app_wa", devContact)
                            .putString("app_dev", devName)
                            .putString("app_thanks", thankYouNote)
                            .putString("privacy_intro", privacyIntro)
                            .putString("privacy_update", privacyLastUpdate)
                            .putString("privacy_rules", Gson().toJson(privacyRules))
                            .apply()

                        Log.d("Sync", "App Info diperbarui dari Firebase ke versi $versiServer")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

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
                    } catch (e: Exception) { }
                }

                if (semuaDataDigabung.isNotEmpty()) {
                    repository.insertSemuaLagu(semuaDataDigabung)
                    prefs.edit().putInt("versi_lagu", 1).putInt("versi_dll", 1).apply()
                }
            } catch (e: Exception) { }
        }
    }

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

                        // GUNAKAN QUERY INSTAN AGAR BINTANG FAVORIT TIDAK HILANG
                        val daftarLaguLokal = repository.getSemuaLaguList()

                        for (laguSnap in snapshot.children) {
                            val v = laguSnap.child("version").getValue(Int::class.java) ?: 1
                            if (v > versiTertinggi) versiTertinggi = v

                            val lirikList = mutableListOf<String>()
                            laguSnap.child("lirik").children.forEach {
                                it.value?.toString()?.let { baris -> lirikList.add(baris) }
                            }

                            val idLagu = laguSnap.child("id").value?.toString() ?: ""
                            val laguLama = daftarLaguLokal.find { it.id == idLagu }
                            val statusFavorit = laguLama?.isFavorit ?: false

                            daftarLaguBaru.add(Lagu(
                                id = idLagu,
                                nomor_urut = laguSnap.child("nomor_urut").getValue(Int::class.java) ?: 0,
                                nomor = laguSnap.child("nomor").value?.toString() ?: "",
                                judul = laguSnap.child("judul").value?.toString() ?: "",
                                kategori = laguSnap.child("kategori").value?.toString() ?: "",
                                nada = laguSnap.child("nada").value?.toString() ?: "",
                                lirik = lirikList,
                                version = v,
                                isFavorit = statusFavorit
                            ))
                        }

                        if (daftarLaguBaru.isNotEmpty()) {
                            repository.insertSemuaLagu(daftarLaguBaru)
                            prefs.edit().putInt(prefKey, versiTertinggi).apply()
                            Log.d("Sync", "Sukses update data di '$nodeName'")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}