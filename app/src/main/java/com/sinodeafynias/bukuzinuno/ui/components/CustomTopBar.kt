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

    // RAHASIA ESTETIK 1: Gradient Vertikal Halus
    // Dari warna primary ke warna yang sedikit lebih gelap agar punya "kedalaman"
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.primary,
            colorScheme.primary.copy(alpha = 0.9f)
        )
    )

    Surface(
        // RAHASIA ESTETIK 2: Berikan lengkungan halus di bagian bawah (Bottom Corners)
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        shadowElevation = 12.dp,
        color = Color.Transparent // Kita pakai background gradient di bawahnya
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .statusBarsPadding() // Menangani area notch/batere di HP modern
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
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp,
                            color = colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                // RAHASIA ESTETIK 3: Subtitle jangan terlalu terang
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
                            // Pakai Ikon IosNew agar terlihat lebih modern/premium
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Kembali",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                // Tambahkan aksi kosong di kanan agar judul benar-benar di tengah secara visual
                actions = {
                    if (showBackButton) {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // Biar gradient-nya kelihatan
                )
            )
        }
    }
}