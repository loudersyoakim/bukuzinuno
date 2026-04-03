package com.sinodeafynias.bukuzinuno.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel

@Composable
fun SearchScreen(
    viewModel: LaguViewModel,
    onLaguClick: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val hasilPencarian = remember(searchQuery, daftarLagu) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else {
            val queryBersih = searchQuery.menormalisasiTeks()
            daftarLagu.filter { lagu ->
                lagu.judul.menormalisasiTeks().contains(queryBersih) ||
                        lagu.nomor.contains(queryBersih)
            }
        }
    }

    // Mengambil skema warna adaptif
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background) // ADAPTIF: Latar belakang utama
    ) {
        // --- SEARCH BAR (HEADER) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.surface, // ADAPTIF: Putih/Navy
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.background, // Sedikit lebih gelap/kontras dari header
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            tint = colorScheme.primary // Biru Adaptif
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                color = colorScheme.onSurface // ADAPTIF: Hitam/Putih
                            ),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Cari nomor atau judul lagu...",
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // --- CONTENT AREA ---
        Box(modifier = Modifier.weight(1f)) {
            when {
                searchQuery.isEmpty() -> {
                    // TAMPILAN AWAL (EMPTY QUERY)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(80.dp).graphicsLayer(alpha = 0.2f),
                            tint = colorScheme.onSurfaceVariant // ADAPTIF
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Mau memuji Tuhan dengan lagu apa?",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                hasilPencarian.isEmpty() -> {
                    // HASIL TIDAK DITEMUKAN
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.SearchOff,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Mohon maaf,",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Lagu '${searchQuery}' tidak ditemukan. Coba periksa ejaan atau gunakan nomor lagu.",
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    // DAFTAR HASIL
                    Column {
                        Text(
                            text = "Ditemukan ${hasilPencarian.size} lagu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(hasilPencarian, key = { it.id }) { lagu ->
                                ResultCard(lagu = lagu, onClick = { onLaguClick(lagu.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(lagu: com.sinodeafynias.bukuzinuno.data.local.Lagu, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = colorScheme.surface, // ADAPTIF
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = colorScheme.primary.copy(alpha = 0.1f), // Aksen Biru
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lagu.nomor,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary // ADAPTIF: Biru
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lagu.judul,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface, // ADAPTIF: Hitam/Putih
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lagu.kategori,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant // ADAPTIF: Abu-abu
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}