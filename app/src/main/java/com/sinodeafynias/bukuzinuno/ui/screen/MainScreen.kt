package com.sinodeafynias.bukuzinuno.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sinodeafynias.bukuzinuno.ui.components.CustomBottomBar
import com.sinodeafynias.bukuzinuno.ui.components.CustomTopBar
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. FUNGSI HELPER NORMALISASI TEKS
 */
fun String.menormalisasiTeks(): String {
    return this.lowercase()
        .replace("ö", "o")
        .replace("ŵ", "w")
        .replace("Ö", "o")
        .replace("Ŵ", "w")
        .replace("'", "")
        .replace("[^a-z0-9 ]".toRegex(), "")
        .trim()
}

/**
 * 2. MAIN SCREEN (NAVIGASI PUSAT)
 */
@OptIn(ExperimentalMaterial3Api::class) // Ditambahkan agar bisa pakai PullToRefreshBox
@Composable
fun MainScreen(
    viewModel: LaguViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isKeepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit
) {
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope() // Scope untuk menjalankan animasi coroutine

    // --- STATE PULL TO REFRESH ---
    var isRefreshing by remember { mutableStateOf(false) }

    // --- STATE BACKSTACK (MENGINGAT RIWAYAT MENU) ---
    val navigationHistory = remember { mutableStateListOf(0) }
    var selectedItem by remember { mutableStateOf(navigationHistory.last()) }

    // State untuk Popup Keluar
    var showExitDialog by remember { mutableStateOf(false) }

    // --- STATE MANAGEMENT INTERNAL ---
    var openedKategori by remember { mutableStateOf<String?>(null) }
    var selectedLaguId by remember { mutableStateOf<String?>(null) }

    // --- LOGIKA TOMBOL BACK FISIK (HIERARKI PINTAR) ---
    BackHandler {
        if (showExitDialog) {
            // 1. Jika dialog keluar terbuka, batalkan
            showExitDialog = false
        } else if (selectedLaguId != null) {
            // 2. Jika sedang baca lirik, tutup liriknya
            selectedLaguId = null
        } else if (openedKategori != null) {
            // 3. Jika sedang lihat detail kategori, kembali ke daftar kategori utama
            openedKategori = null
        } else if (navigationHistory.size > 1) {
            // 4. Mundur ke menu sebelumnya secara berurutan
            navigationHistory.removeAt(navigationHistory.lastIndex)
            selectedItem = navigationHistory.last()
        } else {
            // 5. Jika mentok di menu paling awal (Daftar Lagu), munculkan popup keluar
            showExitDialog = true
        }
    }

    // --- DIALOG KONFIRMASI KELUAR ---
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text("Keluar Aplikasi", fontWeight = FontWeight.Black, color = colorScheme.onSurface)
            },
            text = {
                Text("Apakah Anda yakin ingin keluar dari aplikasi Buku Zinuno AFY?", color = colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        activity?.finish() // Menutup aplikasi seutuhnya
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    Text("Ya, Keluar", fontWeight = FontWeight.Bold, color = colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- LOGIKA HEADER (TOP BAR) DINAMIS ---
    val (headerTitle, headerSubtitle, showBack) = when {
        selectedLaguId != null -> {
            val lagu = viewModel.semuaLagu.collectAsState().value.find { it.id == selectedLaguId }
            Triple(lagu?.kategori ?: "Detail Lagu", "No. ${lagu?.nomor}. ${lagu?.judul}", true)
        }
        selectedItem == 0 -> Triple("Buku Zinuno AFY", "Daftar Semua Lagu", false)
        selectedItem == 1 -> {
            if (openedKategori != null) Triple("Kategori Lagu", openedKategori!!, true)
            else Triple("Kategori Lagu", "Pilih daftar pujian", false)
        }
        selectedItem == 2 -> Triple("Pencarian", "Cari judul atau nomor", false)
        selectedItem == 3 -> {
            val jumlahFavorit = viewModel.semuaLagu.collectAsState().value.count { it.isFavorit }
            Triple("Lagu Favorit", "$jumlahFavorit lagu disimpan", false)
        }
        selectedItem == 4 -> Triple("Menu Aplikasi", "Pengaturan & Informasi", false)
        else -> Triple("Buku Zinuno AFY", "Pelayanan Sinode AFY", false)
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = headerTitle,
                subtitle = headerSubtitle,
                showBackButton = showBack,
                onBackClick = {
                    if (selectedLaguId != null) selectedLaguId = null
                    else if (openedKategori != null) openedKategori = null
                }
            )
        },
        bottomBar = {
            CustomBottomBar(
                selectedItem = selectedItem,
                onItemSelected = { index ->
                    if (selectedItem != index) {
                        // LOGIKA ANTI-LOOP / ANTI-DUPLIKASI RIWAYAT
                        if (index == 0) {
                            navigationHistory.clear()
                            navigationHistory.add(0)
                        } else {
                            navigationHistory.remove(index)
                            navigationHistory.add(index)
                        }
                        selectedItem = index
                    } else if (index == 1) {
                        openedKategori = null
                    }

                    selectedLaguId = null
                    if (index != 1) openedKategori = null
                }
            )
        }
    ) { innerPadding ->
        // --- FITUR TARIK UNTUK REFRESH ---
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    // Panggil fungsi sinkronisasi dari ViewModel
                    viewModel.sinkronisasiCerdas(context)
                    // Beri jeda animasi sedikit agar terlihat responsif
                    delay(1200)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Padding Scaffold pindah ke sini
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // LAYER 1: TAMPILAN DETAIL (OVERLAY)
                if (selectedLaguId != null) {
                    DetailLaguScreen(viewModel = viewModel, laguId = selectedLaguId!!)
                }
                // LAYER 2: TAMPILAN MENU UTAMA
                else {
                    when (selectedItem) {
                        0 -> DaftarLaguScreen(
                            viewModel = viewModel,
                            onLaguClick = { selectedLaguId = it }
                        )
                        1 -> {
                            if (openedKategori != null) {
                                KategoriDetailScreen(viewModel, openedKategori!!, { selectedLaguId = it })
                            } else {
                                KategoriScreen(viewModel) { openedKategori = it }
                            }
                        }
                        2 -> SearchScreen(viewModel, onLaguClick = { selectedLaguId = it })
                        3 -> FavoritScreen(viewModel, onLaguClick = { selectedLaguId = it })
                        4 -> MenuScreen(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = onDarkModeChange,
                            isKeepScreenOn = isKeepScreenOn,
                            onKeepScreenOnChange = onKeepScreenOnChange
                        )
                    }
                }
            }
        }
    }
}