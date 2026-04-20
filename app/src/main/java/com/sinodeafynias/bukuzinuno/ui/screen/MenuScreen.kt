package com.sinodeafynias.bukuzinuno.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ContactSupport
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sinodeafynias.bukuzinuno.R
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

@Composable
fun MenuScreen(
    viewModel: LaguViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isKeepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uriHandler = LocalUriHandler.current

    val prefs = context.getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE)
    val versiAppInfo = prefs.getInt("versi_app_info", 1)
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName

    DisposableEffect(isKeepScreenOn) {
        if (isKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.calculateLocalAudioStats(context)
        val analytics = Firebase.analytics
        val fullVersionString = "Versi $versionName.$versiAppInfo"
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Menu Screen")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            param("app_version_full", fullVersionString)
        }
    }

    val appDescription = viewModel.appDescription
    val churchEmail = viewModel.churchEmail
    val devContact = viewModel.devContact
    val devName = viewModel.devName
    val thankYouNote = viewModel.thankYouNote

    // State untuk Dialog Sinkronisasi Audio
    val showSyncDialog by viewModel.showSyncDialog.collectAsState()
    val pendingAudioCount by viewModel.pendingAudioCount.collectAsState()

    // --- STATE UNTUK AUDIO OFFLINE ---
    val isDownloadingAll by viewModel.isDownloadingAll.collectAsState()
    val downloadedCount by viewModel.downloadedCount.collectAsState()
    val totalUniqueAudioCount by viewModel.totalUniqueAudioCount.collectAsState()
    val totalAudioSize by viewModel.totalAudioSize.collectAsState()
    val downloadStatusMessage by viewModel.downloadStatusMessage.collectAsState()

    // Jika ada pesan status baru (setelah download selesai), tampilkan Toast
    LaunchedEffect(downloadStatusMessage) {
        if (downloadStatusMessage.isNotEmpty()) {
            Toast.makeText(context, downloadStatusMessage, Toast.LENGTH_LONG).show()
            viewModel.clearDownloadStatusMessage() // Reset agar tidak muncul terus
        }
    }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var devClickCount by remember { mutableIntStateOf(0) }
    var showDeveloperSecret by remember { mutableStateOf(false) }
    var showAudioInfoDialog by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val textPrimary = colorScheme.onBackground
    val textSecondary = colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- HEADER LOGO ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_sinode),
                        contentDescription = "Logo Sinode AFY",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buku Zinuno AFY",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = textPrimary
                )
                Text(
                    text = "Sinode AFY",
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // --- SECTION PENYIMPANAN AUDIO ---
        item {
            MenuSectionTitle(title = "Penyimpanan")
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isDownloadingAll) {
                                // Mencegah download jika semua data sudah lengkap, memanggil Smart Sync
                                if (downloadedCount < totalUniqueAudioCount) {
                                    viewModel.checkAudioUpdates(context)
                                } else if (totalUniqueAudioCount > 0) {
                                    Toast.makeText(context, "Semua audio sudah tersimpan secara offline", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Belum ada data audio", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.LibraryMusic, null, tint = colorScheme.primary, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // --- PERUBAHAN DI SINI: Teks dan Ikon Info Menyatu ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Audio Offline",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Ikon Info
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "Info Audio Offline",
                                    tint = colorScheme.outline,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .clickable { showAudioInfoDialog = true } // Membuka Dialog
                                )
                            }
                            // ---------------------------------------------------

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "($downloadedCount/$totalUniqueAudioCount - $totalAudioSize)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.outline
                            )
                        }

                        // Icon Kanan Dinamis (Loading / Centang / Unduh)
                        if (isDownloadingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else if (downloadedCount >= totalUniqueAudioCount && totalUniqueAudioCount > 0) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF25D366), modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Rounded.Download, null, tint = colorScheme.outline, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        // --- SECTION PENGATURAN ---
        item {
            MenuSectionTitle(title = "Pengaturan")
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    MenuSwitchItem(
                        icon = Icons.Rounded.DarkMode,
                        title = "Mode Gelap",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = colorScheme.outlineVariant)
                    MenuSwitchItem(
                        icon = Icons.Rounded.LightMode,
                        title = "Layar Tetap Menyala",
                        checked = isKeepScreenOn,
                        onCheckedChange = onKeepScreenOnChange
                    )
                }
            }
        }

        // --- SECTION INFORMASI ---
        item {
            MenuSectionTitle(title = "Informasi & Dukungan")
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    MenuClickableItem(
                        icon = Icons.Rounded.Info,
                        title = "Tentang Aplikasi",
                        onClick = { showAboutDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = colorScheme.outlineVariant)
                    MenuClickableItem(
                        icon = Icons.AutoMirrored.Rounded.ContactSupport,
                        title = "Hubungi Kami",
                        onClick = { showContactDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = colorScheme.outlineVariant)
                    MenuClickableItem(
                        icon = Icons.Rounded.PrivacyTip,
                        title = "Kebijakan Privasi",
                        onClick = { showPrivacyDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = colorScheme.outlineVariant)
                    MenuClickableItem(
                        icon = Icons.Rounded.Share,
                        title = "Bagikan Aplikasi",
                        onClick = {
                            val appPackageName = context.packageName
                            val playStoreLink = "https://play.google.com/store/apps/details?id=$appPackageName"

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Aplikasi Buku Zinuno AFY")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Mari memuji Tuhan bersama menggunakan aplikasi Buku Zinuno Angowuloa Fa'awösa Khö Yesu (AFY).\n\nUnduh sekarang di Google Play Store:\n$playStoreLink"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan melalui"))
                        }
                    )
                }
            }
        }

        // --- FOOTER ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Versi $versionName.${versiAppInfo % 10}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.outline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 Sinode AFY. Hak Cipta Dilindungi.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.outlineVariant
                )
            }
        }
    }

    // --- DIALOG: SMART SYNC CONFIRMATION ---
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncDialog() },
            confirmButton = {
                Button(
                    onClick = { viewModel.startSmartSync(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Unduh Sekarang", fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSyncDialog() }) {
                    Text("Nanti Saja", fontWeight = FontWeight.Bold, color = colorScheme.outline)
                }
            },
            title = { Text("Pembaruan Audio", fontWeight = FontWeight.Black, color = textPrimary) },
            text = {
                Text(
                    text = "Ditemukan $pendingAudioCount file audio yang perlu diunduh atau diperbarui. Apakah Anda ingin menyinkronkan audio sekarang?",
                    fontSize = 14.sp, color = textSecondary, lineHeight = 22.sp
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- DIALOG: INFO AUDIO OFFLINE ---
    if (showAudioInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAudioInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showAudioInfoDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            title = {
                Text("Informasi Audio Offline", fontWeight = FontWeight.Black, color = textPrimary)
            },
            text = {
                Text(
                    text = "Fitur ini akan mengunduh seluruh nada lagu ke dalam perangkat Anda.\n\nDengan menyimpannya secara offline, Anda dapat memutar nada lagu kapan saja saat ibadah tanpa memerlukan koneksi internet, serta dapat menghemat kuota data seluler Anda.",
                    fontSize = 14.sp,
                    color = textSecondary,
                    lineHeight = 22.sp
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- DIALOG: TENTANG APLIKASI ---
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(painter = painterResource(id = R.drawable.logo_sinode), contentDescription = null, modifier = Modifier.size(70.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Buku Zinuno AFY", fontWeight = FontWeight.Black, fontSize = 22.sp, color = textPrimary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(appDescription, fontSize = 15.sp, color = textSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp)

                    if (thankYouNote.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ucapan Terima Kasih:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(thankYouNote, fontSize = 15.sp, color = textSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                devClickCount++
                                if (devClickCount >= 10) {
                                    showDeveloperSecret = true
                                    devClickCount = 0
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text("Dikembangkan Oleh:", fontSize = 12.sp, color = colorScheme.outline, fontWeight = FontWeight.Bold)
                        Text(devName, fontWeight = FontWeight.Black, fontSize = 15.sp, color = textPrimary)
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- DIALOG: HUBUNGI KAMI ---
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            title = { Text("Hubungi Kami", fontWeight = FontWeight.Black, color = textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hubungi kami langsung melalui tombol di bawah:", fontSize = 14.sp, color = textSecondary)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cleanContact = devContact
                                    .replace("+", "")
                                    .replace(" ", "")
                                context.startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$cleanContact".toUri()))
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = Color(0xFF075E54), modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("WhatsApp Resmi", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF075E54))
                                Text(devContact, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = "mailto:$churchEmail".toUri() })
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4285F4).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AlternateEmail, null, tint = Color(0xFF1967D2), modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Email Admin", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1967D2))
                                Text(churchEmail, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- DIALOG: KEBIJAKAN PRIVASI & OPEN SOURCE ---
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            title = { Text("Kebijakan Privasi", fontWeight = FontWeight.Black, color = textPrimary) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pembaruan Terakhir: ${viewModel.privacyLastUpdate}\n\n${viewModel.privacyIntro}",
                        fontSize = 14.sp, color = textSecondary, lineHeight = 22.sp
                    )

                    viewModel.privacyRules.forEach { rule ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(rule.head, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            Text(rule.content, fontSize = 14.sp, color = textSecondary, lineHeight = 20.sp)
                        }
                    }

                    // --- OPEN SOURCE (Diperbarui dengan 2 Link) ---
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Transparansi Kode (Open Source)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = textPrimary)
                        Text("Aplikasi ini bersifat terbuka. Anda dapat melihat kode sumber dan direktori audio melalui tautan berikut:", fontSize = 14.sp, color = textSecondary, lineHeight = 20.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Link 1: Repo Utama (Dinamis dari Firebase)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { uriHandler.openUri(viewModel.repoAppUrl) }.padding(vertical = 6.dp)) {
                            Icon(Icons.Rounded.Code, null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Repositori Utama (Kode)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary, textDecoration = TextDecoration.Underline)
                        }

                        // Link 2: Repo Audio (Dinamis dari Firebase)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { uriHandler.openUri(viewModel.repoAudioUrl) }.padding(vertical = 6.dp)) {
                            Icon(Icons.Rounded.LibraryMusic, null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Repositori Audio (MP3)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary, textDecoration = TextDecoration.Underline)
                        }
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = colorScheme.surface
        )
    }

    // --- DIALOG: DEVELOPER SECRET ---
    if (showDeveloperSecret) {
        AlertDialog(
            onDismissRequest = { showDeveloperSecret = false },
            confirmButton = {
                TextButton(onClick = { showDeveloperSecret = false }) {
                    Text("Tutup", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.foto_louders),
                            contentDescription = "Foto Profil Louders Yoakim",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "LOUDERS YOAKIM TELAUMBANUA",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = colorScheme.outlineVariant)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "louders260704@gmail.com", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text(text = "WA: +62 852-6026-9861", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text(text = "IG: @louders_yoakim", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = colorScheme.surface
        )
    }
}

@Composable
fun MenuSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 16.dp)
    )
}
@Composable
fun MenuSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp), // Disamakan jadi 20.dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp)) // Ikon 26.dp
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp, // Teks 16.sp
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun MenuClickableItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 24.dp), // Disamakan jadi 20.dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp)) // Ikon dikembalikan ke 26.dp
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp, // Teks dikembalikan ke 16.sp
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
    }
}