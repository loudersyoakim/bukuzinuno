package com.sinodeafynias.bukuzinuno.ui.screen
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import java.util.Locale

@Composable
fun DaftarLaguScreen(
    viewModel: LaguViewModel,
    onLaguClick: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val laguDitampilkan = remember(daftarLagu, searchQuery) {
        daftarLagu.filter { lagu ->
            if (searchQuery.isEmpty()) return@filter true
            val queryBersih = searchQuery.menormalisasiTeks().lowercase(Locale.getDefault())
            val judulBersih = lagu.judul.menormalisasiTeks().lowercase(Locale.getDefault())
            val nomorBersih = lagu.nomor.menormalisasiTeks().lowercase(Locale.getDefault())
            judulBersih.contains(queryBersih) || nomorBersih.contains(queryBersih)
        }
    }

    // Menggunakan skema warna dari Theme (Otomatis berubah Terang/Gelap)
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background) // ADAPTIF: Putih/Navy Gelap
    ) {

        // 1. HEADER (Pencarian)
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
                color = colorScheme.surface, // ADAPTIF: Putih/Abu-Navy
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = colorScheme.primary, // ADAPTIF: Biru Gelap/Biru Terang
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
                            color = colorScheme.onSurface // ADAPTIF: Hitam/Putih
                        ),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Cari judul atau nomor lagu...",
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

        // 2. LIST LAGU
        Box(modifier = Modifier.weight(1f)) {
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
                        color = colorScheme.surface, // ADAPTIF: Putih/Abu-Navy
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nomor Lagu
                            Text(
                                text = lagu.nomor.padStart(3, '0'),
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = colorScheme.primary, // ADAPTIF
                                modifier = Modifier.width(45.dp)
                            )

                            // Judul Lagu
                            Text(
                                text = lagu.judul,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colorScheme.onSurface, // ADAPTIF: Hitam/Putih
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(
                                onClick = { viewModel.updateFavorit(lagu.id, !lagu.isFavorit) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    // Jika favorit pakai Bintang penuh, jika tidak pakai Bintang garis luar
                                    imageVector = if (lagu.isFavorit) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = null,
                                    // Warna Kuning jika aktif, Abu-abu jika tidak
                                    tint = if (lagu.isFavorit) Color(0xFFFFC107) else colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp) // Ukuran sedikit dibesarkan agar mantap
                                )
                            }
                        }
                    }
                }
            }

            // Fade Effect di bagian atas list (Sekarang memakai warna background adaptif)
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