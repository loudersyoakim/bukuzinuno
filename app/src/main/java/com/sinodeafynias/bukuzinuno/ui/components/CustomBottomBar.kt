package com.sinodeafynias.bukuzinuno.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // MANDATORY for 'by' delegation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color // MANDATORY for Color.White
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

// ─────────────────────────────────────────────
// Shape Kustom: Bar dengan Lekukan Bulat Sempurna (Deep Notch)
// ─────────────────────────────────────────────
class DeepNotchBarShape(
    private val cornerRadius: Float,
    private val notchRadius: Float,
    private val notchDepth: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(buildPath(size))

    private fun buildPath(size: Size): Path {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val r = cornerRadius
        val nr = notchRadius // Radius lekukan (lebar)
        val nd = notchDepth  // Kedalaman lekukan

        return Path().apply {
            moveTo(0f, r)
            // Pojok kiri atas
            quadraticBezierTo(0f, 0f, r, 0f)

            // --- MULAI LEKUKAN NOTCH (SMOOTH TRANSITION) ---
            // Garis datar sebelum notch
            lineTo(cx - (nr * 1.5f), 0f)

            // Kurva masuk ke dalam notch (Ujung Notch Halus Kiri)
            cubicTo(
                x1 = cx - nr, y1 = 0f,
                x2 = cx - nr, y2 = nd,
                x3 = cx, y3 = nd
            )

            // Kurva keluar dari notch (Ujung Notch Halus Kanan)
            cubicTo(
                x1 = cx + nr, y1 = nd,
                x2 = cx + nr, y2 = 0f,
                x3 = cx + (nr * 1.5f), y3 = 0f
            )
            // --- SELESAI LEKUKAN NOTCH ---

            // Garis ke pojok kanan atas
            lineTo(w - r, 0f)
            quadraticBezierTo(w, 0f, w, r)

            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
    }
}

@Composable
fun CustomBottomBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val density = LocalDensity.current

    // Dimensi Tombol & Bar
    val fabSize = 64.dp         // Ukuran FAB standar (lebih rapi)
    val fabLift = 60.dp         // Setengah dari fabSize agar center di garis bar
    val barHeight = 100.dp

    // Konfigurasi Notch agar "Membungkus"
    // notchRadius harus sedikit lebih besar dari (fabSize / 2) agar ada celah (clearance)
    val cornerPx = with(density) { 20.dp.toPx() }
    val notchPx  = with(density) { 40.dp.toPx() } // Jarak radius lekukan
    val depthPx  = with(density) { 45.dp.toPx() } // Kedalaman lekukan

    val isSearchSelected = selectedItem == 2

    // Animasi Ukuran: Membesar sedikit saat dipilih
    val fabScale by animateFloatAsState(
        targetValue = if (isSearchSelected) 1.15f else 1f,
        label = "scale"
    )

    // Animasi Elevasi: Naik sedikit saat dipilih
    val fabElevation by animateDpAsState(
        targetValue = if (isSearchSelected) 18.dp else 12.dp,
        label = "elevation"
    )

    val barShape = DeepNotchBarShape(
        cornerRadius = cornerPx,
        notchRadius  = notchPx,
        notchDepth   = depthPx
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── 1. BODY NAVBAR DENGAN "PARIT" TEMBUS ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            shape = barShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 15.dp // Shadow mengikuti lekukan notch
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem("Daftar", Icons.Rounded.QueueMusic, selectedItem == 0) { onItemSelected(0) }
                BottomBarItem("Kategori", Icons.Rounded.GridView, selectedItem == 1) { onItemSelected(1) }

                // Spacer mengikuti lebar parit (notch)
                Spacer(Modifier.width(90.dp))
                BottomBarItem("Favorit", Icons.Rounded.Star, selectedItem == 3) { onItemSelected(3) }
                BottomBarItem("Menu", Icons.Rounded.Menu, selectedItem == 4) { onItemSelected(4) }
            }
        }

        // ── 2. TOMBOL SEARCH RAKSASA (ANIMATED & GLOW) ──
        Surface(
            modifier = Modifier
                .size(fabSize * fabScale) // Ukuran mengikuti animasi scale
                .offset(y = -fabLift),
            shape = CircleShape,
            // Jika aktif, warnanya bisa sedikit lebih terang atau tetap primary
            color = if (isSearchSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            shadowElevation = fabElevation,
            // Tambahkan Border Glow tipis jika sedang aktif
            border = if (isSearchSelected) BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)) else null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onItemSelected(2) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Cari",
                        // Warna icon berubah menyesuaikan container
                        tint = if (isSearchSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBarItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) onPrimary else onPrimary.copy(alpha = 0.4f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = text,
            color = if (isSelected) onPrimary else onPrimary.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}