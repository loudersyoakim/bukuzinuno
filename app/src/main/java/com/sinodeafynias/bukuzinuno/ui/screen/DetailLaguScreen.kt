package com.sinodeafynias.bukuzinuno.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection

@Composable
fun DetailLaguScreen(
    viewModel: LaguViewModel,
    laguId: String,
    onNavigate: (String) -> Unit
) {
    val daftarLagu by viewModel.semuaLagu.collectAsState()
    val context = LocalContext.current

    val lagu = remember(daftarLagu, laguId) { daftarLagu.find { it.id == laguId } }
    val currentIndex = remember(daftarLagu, laguId) { daftarLagu.indexOfFirst { it.id == laguId } }
    val prevLagu = remember(daftarLagu, currentIndex) { daftarLagu.getOrNull(currentIndex - 1) }
    val nextLagu = remember(daftarLagu, currentIndex) { daftarLagu.getOrNull(currentIndex + 1) }

    val prefs = remember { context.getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE) }
    var fontScale by remember { mutableStateOf(prefs.getFloat("font_scale", 19f)) }

    LaunchedEffect(fontScale) {
        delay(1000)
        prefs.edit().putFloat("font_scale", fontScale).apply()
    }

    if (lagu == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var isMenuExpanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 180f else 0f,
        label = "iconRotation"
    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No. ${lagu.nomor}", fontSize = 14.sp, color = colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    text = lagu.judul.uppercase(),
                    fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── AREA NADA / AUDIO PLAYER ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart // Align left sesuai permintaan
            ) {
                if (lagu.audio_id.isNotEmpty()) {
                    // Jika ada audio, gunakan Expandable Player (menggantikan Pill Nada statis)
                    key(lagu.id) {
                        ExpandableAudioPlayer(
                            audioId = lagu.audio_id,
                            nadaText = lagu.nada,
                            context = context,
                            colorScheme = colorScheme,
                            audioBaseUrl = viewModel.audioBaseUrl
                        )
                    }
                } else if (lagu.nada.isNotEmpty()) {
                    // Jika TIDAK ADA audio, barulah tampilkan Pill Nada Statis biasa
                    Surface(
                        color = colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(16.dp), tint = colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = lagu.nada, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Lirik
            Column(modifier = Modifier.fillMaxWidth()) {
                lagu.lirik.forEachIndexed { idx, bait ->
                    val cleanText = bait.replaceFirst("^\\d+\\.\\s*".toRegex(), "")
                    Row(modifier = Modifier.padding(bottom = (fontScale * 0.7f).dp)) {
                        Text(
                            text = "${idx + 1}.",
                            fontSize = fontScale.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary,
                            modifier = Modifier.width((fontScale * 1.4f).dp)
                        )
                        Text(
                            text = cleanText,
                            fontSize = fontScale.sp, lineHeight = (fontScale * 1.45f).sp, color = colorScheme.onBackground
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(200.dp))
        }

        // ── 2. FAB MENU LIDAH (MENGAMBANG BEBAS) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 60.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isMenuExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp, bottom = 38.dp, start = 4.dp, end = 4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("📖 BUKU ZINUNO AFY\n")
                                    append("No. ${lagu.nomor} - ${lagu.judul.uppercase()}\n")
                                    if (lagu.nada.isNotEmpty()) append("Nada: ${lagu.nada}\n")
                                    append("\n")
                                    lagu.lirik.forEachIndexed { index, bait ->
                                        val lirikBersih = bait.replaceFirst("^\\d+\\.\\s*".toRegex(), "")
                                        append("${index + 1}. $lirikBersih\n\n")
                                    }
                                    append("Dapatkan aplikasi Buku Zinuno AFY di Google Play Store:\nhttps://play.google.com/store/apps/details?id=${context.packageName}")
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
                            Icon(Icons.Rounded.Share, "Bagikan Lagu", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
                        }

                        HorizontalDivider(modifier = Modifier.width(32.dp).padding(vertical = 4.dp), color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                        IconButton(
                            onClick = { viewModel.updateFavorit(lagu.id, !lagu.isFavorit) },
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

            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Rounded.KeyboardArrowUp, "Menu Opsi", modifier = Modifier.size(32.dp).rotate(iconRotation))
            }
        }

        // ── 3. BILAH NAVIGASI PREV / NEXT (MELEKAT DI BAWAH) ──
        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = colorScheme.surface,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable(enabled = prevLagu != null) { prevLagu?.let { onNavigate(it.id) } }.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prevLagu != null) {
                        Icon(Icons.Rounded.ArrowBackIosNew, "Sebelumnya", modifier = Modifier.size(16.dp), tint = colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(text = prevLagu.judul, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Black, color = colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = prevLagu.kategori, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Column(modifier = Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = "${currentIndex + 1}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = colorScheme.onSurface, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(11.dp))
                }

                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable(enabled = nextLagu != null) { nextLagu?.let { onNavigate(it.id) } }.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (nextLagu != null) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(text = nextLagu.judul, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Black, color = colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                            Text(text = nextLagu.kategori, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Medium, color = colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Rounded.ArrowForwardIos, "Selanjutnya", modifier = Modifier.size(16.dp), tint = colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ExpandableAudioPlayer(
    audioId: String,
    nadaText: String,
    context: Context,
    colorScheme: ColorScheme,
    audioBaseUrl: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isExpanded    by remember { mutableStateOf(false) }
    var isPlaying     by remember { mutableStateOf(false) }
    var isBuffering   by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) } // Untuk unduhan awal
    var isSyncing     by remember { mutableStateOf(false) } // Khusus untuk fitur Update
    var isRepeat      by remember { mutableStateOf(false) }
    var duration      by remember { mutableLongStateOf(0L) }
    var currentPos    by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = false }
    }

    fun playLocal(file: File) {
        exoPlayer.setMediaItem(MediaItem.fromUri(file.toUri()))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING

                // Ambil durasi awal jika sudah siap
                if (state == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0) duration = d
                }

                if (state == Player.STATE_ENDED) {
                    if (isRepeat) {
                        coroutineScope.launch {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                            delay(1000L) // Jeda 1 detik
                            exoPlayer.play()
                        }
                    } else {
                        exoPlayer.seekTo(0)
                        exoPlayer.pause()
                        isPlaying = false
                    }
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(context, "Gagal memutar audio", Toast.LENGTH_SHORT).show()
                isExpanded = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Ticker Slider diperbarui agar terus memantau durasi
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            currentPos = exoPlayer.currentPosition

            // FIX BUG TITIK DIAM: Terus update durasi jika sebelumnya gagal terbaca
            val d = exoPlayer.duration
            if (d > 0 && duration <= 0L) {
                duration = d
            }

            delay(150L) // Refresh rate dipercepat sedikit agar titik berjalan lebih mulus
        }
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) exoPlayer.pause()
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val primary = colorScheme.primary
    val localFile = File(context.filesDir, "$audioId.mp3")

    Surface(
        color = primary.copy(alpha = 0.10f),
        shape = CircleShape,
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(32.dp).animateContentSize(animationSpec = tween(300))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 8.dp)
            ) {
                Icon(Icons.Rounded.MusicNote, null, tint = primary, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = nadaText.ifEmpty { "Putar" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    maxLines = 1,
                    letterSpacing = 0.2.sp
                )
            }

            Box(Modifier.width(1.dp).height(15.dp).background(primary.copy(alpha = 0.20f)))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isDownloading && !isSyncing) {
                        if (!isExpanded) {
                            isExpanded = true
                            if (localFile.exists()) {
                                playLocal(localFile)
                            } else {
                                coroutineScope.launch {
                                    isDownloading = true
                                    val url = "${audioBaseUrl}$audioId.mp3"

                                    val ok = withContext(Dispatchers.IO) {
                                        try {
                                            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                                            conn.connectTimeout = 4000
                                            conn.connect()

                                            if (conn.responseCode != 200) {
                                                withContext(Dispatchers.Main) { Toast.makeText(context, "Lagu belum tersedia di server", Toast.LENGTH_LONG).show() }
                                                return@withContext false
                                            }

                                            conn.inputStream.use { i ->
                                                localFile.outputStream().use { o -> i.copyTo(o) }
                                            }
                                            true
                                        } catch (e: java.net.UnknownHostException) {
                                            withContext(Dispatchers.Main) { Toast.makeText(context, "Tidak ada internet. Nyalakan data seluler.", Toast.LENGTH_LONG).show() }
                                            localFile.delete()
                                            false
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { Toast.makeText(context, "Terjadi gangguan jaringan", Toast.LENGTH_SHORT).show() }
                                            localFile.delete()
                                            false
                                        }
                                    }

                                    isDownloading = false
                                    if (ok) {
                                        playLocal(localFile)
                                    } else {
                                        isExpanded = false
                                    }
                                }
                            }
                        } else {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }
                    }
            ) {
                when {
                    isBuffering || isDownloading -> CircularProgressIndicator(modifier = Modifier.size(14.dp), color = primary, strokeWidth = 1.5.dp)
                    isPlaying -> Icon(Icons.Rounded.Pause, "Jeda", tint = primary, modifier = Modifier.size(16.dp))
                    else -> Icon(Icons.Rounded.PlayArrow, "Putar", tint = primary, modifier = Modifier.size(16.dp))
                }
            }

            AnimatedVisibility(
                visible = isExpanded && !isDownloading,
                enter = expandHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Start
                ) + fadeIn(tween(240, delayMillis = 60)),
                exit = shrinkHorizontally(
                    animationSpec = tween(220),
                    shrinkTowards = Alignment.Start
                ) + fadeOut(tween(160))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                ) {
                    val sliderProgress = if (duration > 0) {
                        (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .pointerInput(duration) {
                                detectHorizontalDragGestures { change, _ ->
                                    if (isSyncing) return@detectHorizontalDragGestures // Cegah drag saat sync
                                    change.consume()
                                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val seekTo = (fraction * duration).toLong()
                                    currentPos = seekTo
                                    exoPlayer.seekTo(seekTo)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val trackWidthPx = constraints.maxWidth

                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape).background(primary.copy(alpha = 0.18f)))
                        Box(modifier = Modifier.fillMaxWidth(sliderProgress).height(2.dp).clip(CircleShape).background(primary).align(Alignment.CenterStart))

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.CenterStart)
                                .offset {
                                    val thumbOffset = (sliderProgress * trackWidthPx - 4.dp.toPx()).toInt()
                                    androidx.compose.ui.unit.IntOffset(x = thumbOffset, y = 0)
                                }
                                .clip(CircleShape)
                                .background(primary)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // ── TOMBOL SYNC / UPDATE ──
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isSyncing) {
                                coroutineScope.launch {
                                    isSyncing = true
                                    exoPlayer.stop() // Hentikan pemutaran

                                    val url = "${audioBaseUrl}$audioId.mp3"

                                    val ok = withContext(Dispatchers.IO) {
                                        try {
                                            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                                            conn.connectTimeout = 4000
                                            conn.connect()

                                            // Jika file ditemukan, timpa yang lama
                                            if (conn.responseCode == 200) {
                                                // Buat file sementara agar jika gagal di tengah jalan, file asli tidak rusak
                                                val tempFile = File(context.filesDir, "${audioId}_temp.mp3")
                                                conn.inputStream.use { i -> tempFile.outputStream().use { o -> i.copyTo(o) } }

                                                // Jika berhasil terunduh penuh, hapus yang lama dan rename temp ke asli
                                                if (localFile.exists()) localFile.delete()
                                                tempFile.renameTo(localFile)
                                                true
                                            } else {
                                                withContext(Dispatchers.Main) { Toast.makeText(context, "Lagu di server tidak ditemukan", Toast.LENGTH_SHORT).show() }
                                                false
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal memperbarui lagu. Periksa internet Anda.", Toast.LENGTH_SHORT).show() }
                                            File(context.filesDir, "${audioId}_temp.mp3").delete() // Bersihkan temp file
                                            false
                                        }
                                    }

                                    isSyncing = false
                                    if (ok) {
                                        Toast.makeText(context, "Lagu diperbarui!", Toast.LENGTH_SHORT).show()
                                        playLocal(localFile) // Putar ulang versi terbaru
                                    } else {
                                        // Jika gagal update (misal internet putus), putar ulang yang lama jika masih ada
                                        if (localFile.exists()) playLocal(localFile)
                                    }
                                }
                            }
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = primary, strokeWidth = 1.5.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = "Perbarui",
                                tint = primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // ── TOMBOL REPEAT ──
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isSyncing) {
                                isRepeat = !isRepeat
                                exoPlayer.repeatMode = if (isRepeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                            }
                    ) {
                        Icon(
                            imageVector = if (isRepeat) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            contentDescription = "Ulangi",
                            tint = primary.copy(alpha = if (isRepeat) 1f else 0.35f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}