package com.sinodeafynias.bukuzinuno.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star // DIUBAH: Import ikon Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import java.util.Locale
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

@Composable
fun FavoritScreen(viewModel: LaguViewModel, onLaguClick: (String) -> Unit) {
    val listFavorit by viewModel.laguFavorit.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val analytics = Firebase.analytics
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Favorit Screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
    }
    val laguDitampilkan = remember(listFavorit, searchQuery) {
        listFavorit.filter { lagu ->
            if (searchQuery.isEmpty()) return@filter true
            val queryBersih = searchQuery.menormalisasiTeks().lowercase(Locale.getDefault())
            val judulBersih = lagu.judul.menormalisasiTeks().lowercase(Locale.getDefault())
            val nomorBersih = lagu.nomor.menormalisasiTeks().lowercase(Locale.getDefault())
            judulBersih.contains(queryBersih) || nomorBersih.contains(queryBersih)
        }
    }

    // Mengambil skema warna dari tema
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background) // ADAPTIF
    ) {

        // 1. HEADER SEARCH (Hanya muncul jika ada lagu favorit)
        if (listFavorit.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = colorScheme.surface, // ADAPTIF
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            "Search",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface // ADAPTIF
                            ),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Cari di favorit...",
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 16.sp
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            }
        }

        // 2. KONTEN UTAMA
        Box(modifier = Modifier.weight(1f)) {
            if (listFavorit.isEmpty()) {
                // EMPTY STATE (Warna adaptif agar teks tidak hilang saat gelap)
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star, // DIUBAH: Menjadi Star
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Belum ada lagu favorit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface // ADAPTIF
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tekan ikon bintang pada lagu yang Anda sukai untuk menemukannya kembali di sini dengan mudah.", // DIUBAH: Teks hati menjadi bintang
                        fontSize = 15.sp,
                        color = colorScheme.onSurfaceVariant, // ADAPTIF
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // DAFTAR FAVORIT
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(laguDitampilkan, key = { it.id }) { lagu ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onLaguClick(lagu.id) },
                            color = colorScheme.surface, // ADAPTIF
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lagu.nomor.padStart(3, '0'),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = colorScheme.primary // ADAPTIF
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = lagu.judul,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colorScheme.onSurface, // ADAPTIF
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { viewModel.updateFavorit(lagu.id, false) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star, // DIUBAH: Menjadi Star
                                        contentDescription = "Hapus dari Favorit",
                                        tint = Color(0xFFFFC107), // DIUBAH: Menjadi warna Kuning Emas (Amber)
                                        modifier = Modifier.size(28.dp) // Sedikit diperbesar agar bintangnya lebih mantap
                                    )
                                }
                            }
                        }
                    }
                }

                // Fade Effect Adaptif
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(colorScheme.background, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}