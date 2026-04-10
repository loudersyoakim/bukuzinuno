package com.sinodeafynias.bukuzinuno.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel

@Composable
fun KategoriScreen(
    viewModel: LaguViewModel,
    onKategoriClick: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()

    val listKategori = remember(daftarLagu) {
        daftarLagu.map { it.kategori }.distinct().filter { it.isNotEmpty() }.sorted()
    }

    LaunchedEffect(Unit) {
        val analytics = Firebase.analytics
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Kategori Screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
    }

    // Mengambil skema warna adaptif dari sistem
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background), // ADAPTIF: Putih/Navy Gelap
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 110.dp, // Gap sedikit lebih besar agar aman dari BottomBar
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp) // Jarak antar card sedikit lebih lega
    ) {
        items(listKategori) { kategori ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)) // Radius disamakan dengan DaftarLagu (16.dp)
                    .clickable { onKategoriClick(kategori) },
                color = colorScheme.surface, // ADAPTIF: Putih/Abu-Navy
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wadah Ikon Folder (Aksen warna primer yang adaptif)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = colorScheme.primary, // ADAPTIF: Biru Gelap/Terang
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Teks Nama Kategori (Pekat & Jelas)
                    Text(
                        text = kategori,
                        fontWeight = FontWeight.Bold, // Dibuat Bold agar lebih jelas
                        fontSize = 16.sp,
                        color = colorScheme.onSurface, // ADAPTIF: Hitam/Putih Terang
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Ikon Panah Kanan
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Buka",
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f), // ADAPTIF
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}