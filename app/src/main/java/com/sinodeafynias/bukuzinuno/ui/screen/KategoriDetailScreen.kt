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
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import java.util.Locale

@Composable
fun KategoriDetailScreen(
    viewModel: LaguViewModel,
    namaKategori: String,
    onLaguClick: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val laguDitampilkan = remember(daftarLagu, searchQuery) {
        val queryBersih = searchQuery.menormalisasiTeks().lowercase(Locale.getDefault())
        daftarLagu.filter { lagu ->
            val cocokKategori = lagu.kategori == namaKategori
            val judulBersih = lagu.judul.menormalisasiTeks().lowercase(Locale.getDefault())
            val nomorBersih = lagu.nomor.menormalisasiTeks().lowercase(Locale.getDefault())

            (judulBersih.contains(queryBersih) || nomorBersih.contains(queryBersih)) && cocokKategori
        }
    }

    // Mengambil skema warna adaptif
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background) // ADAPTIF
    ) {
        // 1. HEADER PENCARIAN
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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
                        contentDescription = null,
                        tint = colorScheme.primary, // ADAPTIF
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
                            color = colorScheme.onSurface // ADAPTIF: Putih/Hitam
                        ),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Cari di $namaKategori...",
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 15.sp
                                )
                            }
                            inner()
                        }
                    )
                }
            }
        }

        // 2. LIST / EMPTY STATE
        Box(modifier = Modifier.weight(1f)) {
            if (laguDitampilkan.isEmpty()) {
                // Tampilan saat pencarian tidak ditemukan (Empty State)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SearchOff,
                        contentDescription = null,
                        tint = colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Lagu tidak ditemukan",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = colorScheme.onSurface // ADAPTIF
                    )
                    Text(
                        text = "Kami tidak menemukan lagu '${searchQuery}' di kategori ini.",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant, // ADAPTIF
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .padding(horizontal = 20.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(laguDitampilkan, key = { it.id }) { lagu ->
                        // Menggunakan ResultCard yang aslinya, namun pastikan warnanya juga adaptif
                        ResultCard(
                            lagu = lagu,
                            onClick = { onLaguClick(lagu.id) }
                        )
                    }
                }
            }
        }
    }
}