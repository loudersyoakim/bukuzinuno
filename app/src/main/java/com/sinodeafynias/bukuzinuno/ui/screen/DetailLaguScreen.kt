package com.sinodeafynias.bukuzinuno.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import kotlinx.coroutines.delay

@Composable
fun DetailLaguScreen(
    viewModel: LaguViewModel,
    laguId: String,
    onNavigate: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    val context = LocalContext.current
    val analytics = Firebase.analytics

    val currentIndex = daftarLagu.indexOfFirst { it.id == laguId }
    val lagu = daftarLagu.getOrNull(currentIndex)
    val prevLagu = daftarLagu.getOrNull(currentIndex - 1)
    val nextLagu = daftarLagu.getOrNull(currentIndex + 1)

    // --- FITUR SIMPAN UKURAN FONT ---
    val prefs = context.getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE)

    // Baca memori saat halaman dibuka (Default 19f jika belum pernah dicubit)
    var fontScale by remember { mutableStateOf(prefs.getFloat("font_scale", 19f)) }

    // Simpan ke memori tiap kali fontScale berubah (dengan teknik Debounce/Delay 500ms agar tidak lag)
    LaunchedEffect(fontScale) {
        delay(1000)
        prefs.edit().putFloat("font_scale", fontScale).apply()    }
    // --------------------------------

    var isMenuExpanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 180f else 0f,
        label = "fab_rotation"
    )

    if (lagu == null) return

    LaunchedEffect(lagu.id) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Detail Lagu: ${lagu.judul}")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    fontScale = (fontScale * zoom).coerceIn(15f, 45f)
                }
            }
    ) {
        // ── 1. KONTEN LIRIK ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
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
            // Ruang ekstra agar konten tidak tertutup elemen bawah
            Spacer(modifier = Modifier.height(200.dp))
        }

        // ── 2. FAB MENU LIDAH (MENGAMBANG BEBAS) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Memberikan jarak dari bawah agar tepat berada di atas Bilah Navigasi
                .padding(end = 12.dp, bottom = 60.dp),
            contentAlignment = Alignment.BottomCenter // Kunci di tengah-bawah agar lidah keluar dari dalam
        ) {
            // --- ANAK MENU (LIDAH KAPSUL) ---
            AnimatedVisibility(
                visible = isMenuExpanded,
                // RAHASIA LIDAH ASLI: Mengembang dari bawah ke atas
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                // Padding ini memposisikan dasar lidah tepat di belakang FAB
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = colorScheme.surface,
                    shadowElevation = 4.dp // Lebih rendah dari FAB utama
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp, bottom = 38.dp, start = 4.dp, end = 4.dp)
                    ) {
                        // Tombol Share
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("📖 BUKU ZINUNO AFY\n")
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
                                    append("Dapatkan aplikasi Buku Zinuno AFY di Google Play Store:\n")
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

        // ── 3. BILAH NAVIGASI PREV / NEXT (MELEKAT DI BAWAH) ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = colorScheme.surface,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 10.dp),                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // ── KIRI: Lagu Sebelumnya ──
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = prevLagu != null) {
                            prevLagu?.let { onNavigate(it.id) }
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prevLagu != null) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Sebelumnya",
                            modifier = Modifier.size(16.dp),
                            tint = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                text = prevLagu.judul,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = prevLagu.kategori,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // ── TENGAH: Area Kosong ──
                Column(
                    modifier = Modifier.width(72.dp), // Menggunakan ukuran yang sama dengan Spacer sebelumnya
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${currentIndex + 1}",
                        fontSize = 11.sp, // Disamakan dengan fontSize judul agar sejajar
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(11.dp))
                }

                // ── KANAN: Lagu Selanjutnya ──
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = nextLagu != null) {
                            nextLagu?.let { onNavigate(it.id) }
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (nextLagu != null) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                text = nextLagu.judul,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = nextLagu.kategori,
                                fontSize = 9.sp,
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = "Selanjutnya",
                            modifier = Modifier.size(16.dp),
                            tint = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}