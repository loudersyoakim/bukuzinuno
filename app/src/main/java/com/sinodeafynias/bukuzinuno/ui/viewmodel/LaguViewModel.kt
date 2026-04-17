package com.sinodeafynias.bukuzinuno.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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

    // --- STATE UNTUK SMART SYNC DIALOG ---
    private val _showSyncDialog = MutableStateFlow(false)
    val showSyncDialog = _showSyncDialog.asStateFlow()

    private val _pendingAudioCount = MutableStateFlow(0)
    val pendingAudioCount = _pendingAudioCount.asStateFlow()

    private var pendingAudioIdsToDownload: List<String> = emptyList()

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

        // 1. CEK VERSI APLIKASI UNTUK UPDATE OTOMATIS
        cekUpdateAplikasi(context, prefs)

        viewModelScope.launch {
            // 2. TAHAP INFO APP
            muatAppInfoLokalAtauPrefs(context, prefs)
            syncAppInfoRemote(prefs)

            // 3. TAHAP LAGU: Cek via SharedPreferences (Bukan via Database agar INSTAN)
            val isFirstInstallLagu = prefs.getInt("versi_lagu", 0) == 0
            if (isFirstInstallLagu) {
                muatLaguDariJsonLokal(context, prefs)
            }

            // 4. CEK UPDATE KE FIREBASE
            syncNodeLaguRemote(prefs, nodeName = "lagu", prefKey = "versi_lagu")
            syncNodeLaguRemote(prefs, nodeName = "dll", prefKey = "versi_dll")
        }
    }

    private fun cekUpdateAplikasi(context: Context, prefs: SharedPreferences) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // Gunakan longVersionCode untuk API level tinggi, namun fallback ke versionCode untuk kompabilitas
            val currentAppVersion = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }

            val savedAppVersion = prefs.getInt("saved_app_version", 0)

            // Jika versi aplikasi saat ini LEBIH BESAR dari yang tersimpan, berarti aplikasi baru di-update dari PlayStore
            if (savedAppVersion < currentAppVersion) {
                Log.d("Sync", "Aplikasi terupdate ke versi $currentAppVersion. Mereset versi_lagu ke 0.")

                prefs.edit()
                    .putInt("versi_lagu", 0) // Paksa baca ulang lirik_lagu.json
                    .putInt("versi_dll", 0) // Jika ada file ononota.json yang diupdate juga
                    .putInt("saved_app_version", currentAppVersion)
                    .apply()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Sync", "Gagal mendapatkan versi aplikasi: ${e.message}")
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
                    } catch (e: Exception) {
                        Log.e("Sync", "Gagal membaca file $namaFile: ${e.message}")
                    }
                }

                if (semuaDataDigabung.isNotEmpty()) {
                    repository.insertSemuaLagu(semuaDataDigabung)
                    prefs.edit().putInt("versi_lagu", 1).putInt("versi_dll", 1).apply()
                    Log.d("Sync", "Berhasil memuat ${semuaDataDigabung.size} lagu dari JSON lokal.")
                }
            } catch (e: Exception) {
                Log.e("Sync", "Gagal memproses JSON lokal: ${e.message}")
            }
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
                            val audioId = laguSnap.child("audio_id").value?.toString() ?: "" // Tambahkan ini

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
                                isFavorit = statusFavorit,
                                audio_id = audioId,
                            ))
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

    // --- FITUR UNDUH AUDIO & SMART SYNC ---
    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _isDownloadingAll = MutableStateFlow(false)
    val isDownloadingAll: StateFlow<Boolean> = _isDownloadingAll.asStateFlow()

    private val _downloadedCount = MutableStateFlow(0)
    val downloadedCount: StateFlow<Int> = _downloadedCount.asStateFlow()

    private val _totalUniqueAudioCount = MutableStateFlow(0)
    val totalUniqueAudioCount: StateFlow<Int> = _totalUniqueAudioCount.asStateFlow()

    private val _totalAudioSize = MutableStateFlow("0 MB")
    val totalAudioSize: StateFlow<String> = _totalAudioSize.asStateFlow()

    private val _downloadStatusMessage = MutableStateFlow("")
    val downloadStatusMessage: StateFlow<String> = _downloadStatusMessage.asStateFlow()

    fun calculateLocalAudioStats(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val listLagu = semuaLagu.value.ifEmpty { repository.getSemuaLaguList() }

            val uniqueAudioIds = listLagu
                .filter { lagu ->
                    // Filter ketat: Hanya rentang Zinuno, audio_id ada, bukan null, bukan 0
                    val isLaguZinuno = lagu.nomor_urut in 1..366
                    val audioIdBersih = lagu.audio_id.trim()
                    val hasAudio = audioIdBersih.isNotEmpty() && audioIdBersih.lowercase() != "null" && audioIdBersih != "0"

                    isLaguZinuno && hasAudio
                }
                .map { it.audio_id.trim() }
                .distinct()
                .sortedBy { it.toIntOrNull() ?: 999 } // Urut agar rapi

            _totalUniqueAudioCount.value = uniqueAudioIds.size

            // Hitung file fisik
            val expectedFiles = uniqueAudioIds.map { "$it.mp3" }
            val audioFiles = context.filesDir.listFiles { file ->
                file.name in expectedFiles && !file.name.contains("_temp")
            } ?: emptyArray()

            _downloadedCount.value = audioFiles.size
            val totalBytes = audioFiles.sumOf { it.length() }
            _totalAudioSize.value = String.format("%.1f MB", totalBytes / (1024.0 * 1024.0))
        }
    }

    // FUNGSI 1: MENGHITUNG KEKURANGAN FILE & UPDATE VERSI
    fun checkAudioUpdates(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val listLagu = semuaLagu.value.ifEmpty { repository.getSemuaLaguList() }

            // BUKU CATATAN UNTUK MENYIMPAN VERSI AUDIO YANG SUDAH DIDOWNLOAD
            val prefs = context.getSharedPreferences("AudioOfflinePrefs", Context.MODE_PRIVATE)

            // Kita kelompokkan lagu berdasarkan audio_id nya
            val groupedAudios = listLagu
                .filter { it.nomor_urut in 1..366 && it.audio_id.trim().isNotEmpty() && it.audio_id.trim().lowercase() != "null" && it.audio_id.trim() != "0" }
                .groupBy { it.audio_id.trim() }

            val idsToDownload = mutableListOf<String>()

            for ((audioId, laguList) in groupedAudios) {
                val localFile = File(context.filesDir, "$audioId.mp3")

                // Cek versi yang tersimpan di HP. Kalau belum pernah download, dianggap versi 0
                val downloadedVersion = prefs.getInt("version_$audioId", 0)

                // Ambil versi terbaru dari Firebase (Database Room)
                val serverVersion = laguList.maxOf { it.version }

                // KONDISI PINTAR (SMART SYNC):
                // 1. Jika file fisiknya belum ada (Missing File)
                // 2. ATAU file ada, tapi versi yang dulu didownload lebih rendah dari versi server (Update)
                if (!localFile.exists() || downloadedVersion < serverVersion) {
                    idsToDownload.add(audioId)
                }
            }

            if (idsToDownload.isEmpty()) {
                _downloadStatusMessage.value = "Semua audio sudah tersimpan secara offline dan up-to-date."
            } else {
                pendingAudioIdsToDownload = idsToDownload
                _pendingAudioCount.value = idsToDownload.size
                _showSyncDialog.value = true // Munculkan popup ke user
            }
        }
    }

    fun dismissSyncDialog() {
        _showSyncDialog.value = false
        pendingAudioIdsToDownload = emptyList()
    }

    // FUNGSI 2: JALANKAN UNDUHAN HANYA UNTUK FILE YANG KURANG/USANG
    fun startSmartSync(context: Context) {
        if (_isDownloadingAll.value) return
        _showSyncDialog.value = false

        if (pendingAudioIdsToDownload.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isDownloadingAll.value = true
            _downloadStatusMessage.value = ""

            val listLagu = semuaLagu.value.ifEmpty { repository.getSemuaLaguList() }
            val prefs = context.getSharedPreferences("AudioOfflinePrefs", Context.MODE_PRIVATE)

            val idsToDownload = pendingAudioIdsToDownload
            val totalToDownload = idsToDownload.size
            var currentProgress = 0
            var successfulDownloads = 0

            Log.i("DownloadAudio", "Memulai Smart Sync untuk $totalToDownload audio...")

            for (audioId in idsToDownload) {
                val localFile = File(context.filesDir, "$audioId.mp3")

                try {
                    val url = "https://raw.githubusercontent.com/loudersyoakim/bkz_afy_audio/main/music_compressed/$audioId.mp3"
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.connect()

                    if (conn.responseCode == 200) {
                        val tempFile = File(context.filesDir, "${audioId}_temp.mp3")
                        conn.inputStream.use { i -> tempFile.outputStream().use { o -> i.copyTo(o) } }

                        // Timpa file lama dengan file baru
                        if (localFile.exists()) localFile.delete()
                        tempFile.renameTo(localFile)

                        successfulDownloads++

                        // PENTING: CATAT VERSI TERBARUNYA KE BUKU CATATAN!
                        val serverVersion = listLagu.filter { it.audio_id.trim() == audioId }.maxOfOrNull { it.version } ?: 1
                        prefs.edit().putInt("version_$audioId", serverVersion).apply()
                    }
                } catch (e: Exception) {
                    Log.e("DownloadAudio", "Gagal unduh ID: $audioId | Error: ${e.message}")
                    File(context.filesDir, "${audioId}_temp.mp3").delete() // Bersihkan file korup
                }

                currentProgress++
                _downloadProgress.value = (currentProgress * 100) / totalToDownload
                calculateLocalAudioStats(context)
            }

            // Bersihkan Status
            _isDownloadingAll.value = false
            _downloadProgress.value = 0
            pendingAudioIdsToDownload = emptyList()
            _pendingAudioCount.value = 0

            calculateLocalAudioStats(context) // Hitung final

            if (successfulDownloads < totalToDownload) {
                _downloadStatusMessage.value = "Koneksi terputus. Berhasil mengunduh $successfulDownloads dari $totalToDownload audio."
            } else {
                _downloadStatusMessage.value = "Smart Sync berhasil! Semua audio sudah up-to-date."
            }
        }
    }

    fun clearDownloadStatusMessage() {
        _downloadStatusMessage.value = ""
    }
}