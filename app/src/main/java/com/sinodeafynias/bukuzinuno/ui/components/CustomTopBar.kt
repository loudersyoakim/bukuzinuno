package com.sinodeafynias.bukuzinuno.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String = "Buku Zinuno",
    subtitle: String = "Angowuloa Fa'awösa Khö Yesu",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // --- DETEKSI OTOMATIS BARIS TEKS ---
    // Jika title mengandung karakter "\n", berarti ini adalah menu "Daftar Semua Lagu"
    val isTitleDuaBaris = title.contains("\n")

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.primary,
            colorScheme.primary //.copy(alpha = 0.9f)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        shadowElevation = 12.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .statusBarsPadding()
                // Tambah padding vertikal agar lebih lega ke bawah
                .padding(vertical = 8.dp)
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Black,
                            // UBAH UKURAN FONT SECARA DINAMIS DI SINI:
                            // Jika 2 baris jadi 16.sp, jika 1 baris tetap besar (20.sp)
                            fontSize = if (isTitleDuaBaris) 16.sp else 20.sp,
                            lineHeight = if (isTitleDuaBaris) 20.sp else 28.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center, // Wajib agar rata tengah
                            color = colorScheme.onPrimary,
                            maxLines = 2, // Wajib 2 agar \n bisa terbaca
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Kembali",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (showBackButton) {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}