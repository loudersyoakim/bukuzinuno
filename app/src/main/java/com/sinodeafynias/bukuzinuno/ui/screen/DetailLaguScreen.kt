package com.sinodeafynias.bukuzinuno.ui.screen

import android.content.Intent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel

@Composable
fun DetailLaguScreen(viewModel: LaguViewModel, laguId: String) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    val lagu = daftarLagu.find { it.id == laguId }
    val context = LocalContext.current

    // STATE ZOOM (Default 19sp)
    var fontScale by remember { mutableStateOf(19f) }

    // STATE UNTUK MENU MELAYANG (Expandable FAB)
    var isMenuExpanded by remember { mutableStateOf(false) }
    // Animasi putaran panah (0 derajat saat nutup, 180 derajat / nunjuk bawah saat buka)
    val iconRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 180f else 0f,
        label = "fab_rotation"
    )

    if (lagu == null) return

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newSize = fontScale * zoom
                    fontScale = newSize.coerceIn(15f, 45f)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {

            // 1. HEADER
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No. ${lagu.nomor}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = lagu.judul.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // INFO NADA
            if (lagu.nada.isNotEmpty()) {
                Surface(
                    color = colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nada: ${lagu.nada}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. LIRIK
            Column(modifier = Modifier.fillMaxWidth()) {
                lagu.lirik.forEachIndexed { index, bait ->
                    val lirikBersih = bait.replaceFirst("^\\d+\\.\\s*".toRegex(), "")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = (fontScale * 0.8f).dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontSize = fontScale.sp,
                            lineHeight = (fontScale * 1.5f).sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            modifier = Modifier.width((fontScale * 1.6f).dp)
                        )

                        Text(
                            text = lirikBersih,
                            fontSize = fontScale.sp,
                            lineHeight = (fontScale * 1.5f).sp,
                            color = colorScheme.onBackground,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Left
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Cubit layar untuk mengubah ukuran tulisan",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(140.dp))
        }

        // 3. EXPANDABLE FAB (MENU MELAYANG BUKA-TUTUP SEPERTI LIDAH)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter // Kunci di tengah-bawah
        ) {

            // --- ANAK MENU (LIDAH KAPSUL) ---
            AnimatedVisibility(
                visible = isMenuExpanded,
                // RAHASIA LIDAH ASLI: Mengembang (expand) dari bawah ke atas, menyusut (shrink) ke bawah
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                // Padding ini untuk memposisikan dasar lidah tepat di belakang tengah-tengah tombol utama
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                Surface(
                    // Bentuk lidah
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = colorScheme.surface,
                    shadowElevation = 4.dp // Lebih rendah dari tombol utama
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp, bottom = 38.dp, start = 4.dp, end = 4.dp)
                    ) {
                        // Tombol Share
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("📖 BUKU ZINUNO SINODE AFY\n")
                                    append("No. ${lagu.nomor} - ${lagu.judul.uppercase()}\n")
                                    if (lagu.nada.isNotEmpty()) {
                                        append("Nada: ${lagu.nada}\n")
                                    }
                                    append("\n")
                                    lagu.lirik.forEachIndexed { index, bait ->
                                        val lirikBersih = bait.replaceFirst("^\\d+\\.\\s*".toRegex(), "")
                                        append("${index + 1}. $lirikBersih\n\n")
                                    }
                                    append("Mari memuji Tuhan bersama!\n")
                                    append("Dapatkan aplikasi Buku Zinuno di Google Play Store:\n")
                                    append("https://play.google.com/store/apps/details?id=${context.packageName}")
                                }

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Lirik Lagu: ${lagu.judul}")
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Lirik Melalui"))
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Bagikan Lagu",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .width(32.dp)
                                .padding(vertical = 4.dp),
                            color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Tombol Favorit
                        IconButton(
                            onClick = {
                                viewModel.updateFavorit(lagu.id, !lagu.isFavorit)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (lagu.isFavorit) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (lagu.isFavorit) Color(0xFFFFC107) else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // --- INDUK MENU (TOMBOL UTAMA PANAH) ---
            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Menu Opsi",
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(iconRotation)
                )
            }
        }
    }
}