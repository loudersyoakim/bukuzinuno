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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.foundation.background


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
        val nr = notchRadius
        val nd = notchDepth

        return Path().apply {
            moveTo(0f, r)
            quadraticBezierTo(0f, 0f, r, 0f)

            lineTo(cx - (nr * 1.5f), 0f)

            cubicTo(
                x1 = cx - nr, y1 = 0f,
                x2 = cx - nr, y2 = nd,
                x3 = cx, y3 = nd
            )

            cubicTo(
                x1 = cx + nr, y1 = nd,
                x2 = cx + nr, y2 = 0f,
                x3 = cx + (nr * 1.5f), y3 = 0f
            )

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
    val colorScheme = MaterialTheme.colorScheme
    val onPrimary = colorScheme.onPrimary // Ambil warna onPrimary untuk digunakan di tombol tengah

    val fabSize = 64.dp
    val fabLift = 60.dp
    val barHeight = 100.dp

    val cornerPx = with(density) { 20.dp.toPx() }
    val notchPx  = with(density) { 40.dp.toPx() }
    val depthPx  = with(density) { 45.dp.toPx() }

    val isSearchSelected = selectedItem == 2

    val fabScale by animateFloatAsState(
        targetValue = if (isSearchSelected) 1.15f else 1f,
        label = "scale"
    )

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
            .background(MaterialTheme.colorScheme.surface)
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── 1. BODY NAVBAR DENGAN "PARIT" TEMBUS ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            shape = barShape,
            color = colorScheme.primary,
            shadowElevation = 15.dp
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

                Spacer(Modifier.width(90.dp))
                BottomBarItem("Favorit", Icons.Rounded.Star, selectedItem == 3) { onItemSelected(3) }
                BottomBarItem("Menu", Icons.Rounded.Menu, selectedItem == 4) { onItemSelected(4) }
            }
        }

        // ── 2. TOMBOL SEARCH RAKSASA (ANIMATED & GLOW) ──
        Surface(
            modifier = Modifier
                .size(fabSize * fabScale)
                .offset(y = -fabLift),
            shape = CircleShape,
            color = colorScheme.primary,
            shadowElevation = fabElevation,
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
                        // PERUBAHAN DI SINI: Menyamakan logika warna dengan BottomBarItem
                        tint = if (isSearchSelected) onPrimary else onPrimary.copy(alpha = 0.4f),
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