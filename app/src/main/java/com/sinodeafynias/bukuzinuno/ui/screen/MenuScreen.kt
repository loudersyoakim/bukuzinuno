package com.sinodeafynias.bukuzinuno.ui.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sinodeafynias.bukuzinuno.R
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource

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

    // --- FITUR: LAYAR TETAP MENYALA ---
    DisposableEffect(isKeepScreenOn) {
        if (isKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { }
    }

    // --- DATA DINAMIS ---
    val appDescription = viewModel.appDescription
    val churchEmail = viewModel.churchEmail
    val devContact = viewModel.devContact
    val devName = viewModel.devName
    val thankYouNote = viewModel.thankYouNote

    var showAboutDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var devClickCount by remember { mutableStateOf(0) }
    var showDeveloperSecret by remember { mutableStateOf(false) }

    // --- SKEMA WARNA ADAPTIF ---
    val colorScheme = MaterialTheme.colorScheme
    val textPrimary = colorScheme.onBackground
    val textSecondary = colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {// --- HEADER LOGO ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(28.dp),
                    // DIUBAH: Pakai Color.White langsung agar tidak ikut jadi gelap
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
                    text = "Buku Zinuno",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = textPrimary // Ini tetap adaptif (Putih di Dark Mode, Hitam di Light Mode)
                )
                Text(
                    text = "Sinode AFY Nias",
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
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
                        icon = Icons.Rounded.ContactSupport,
                        title = "Hubungi Kami",
                        onClick = { showContactDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 1.dp, color = colorScheme.outlineVariant)
                    MenuClickableItem(
                        icon = Icons.Rounded.Share,
                        title = "Bagikan Aplikasi",
                        onClick = {
                            // Mengambil package name secara otomatis (com.sinodeafynias.bukuzinuno)
                            val appPackageName = context.packageName
                            val playStoreLink = "https://play.google.com/store/apps/details?id=$appPackageName"

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Aplikasi Buku Zinuno Sinode AFY")
                                // Menggabungkan teks promosi dengan Link Play Store
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Mari memuji Tuhan bersama menggunakan aplikasi Buku Zinuno Sinode AFY Nias. Dapatkan lirik lagu terlengkap di genggamanmu!\n\nUnduh sekarang di Google Play Store:\n$playStoreLink"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan melalui"))
                        }
                    )
                }
            }
        }
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
                    Text("Buku Zinuno", fontWeight = FontWeight.Black, fontSize = 22.sp, color = textPrimary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Agar semua konten rata tengah
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Deskripsi Aplikasi
                    Text(appDescription, fontSize = 15.sp, color = textSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp)

                    // Ucapan Terima Kasih (Tanpa kotak warna, dibuat simpel dan menyatu)
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
                                indication = null // Menghilangkan efek abu-abu saat di-tap cepat
                            ) {
                                devClickCount++
                                if (devClickCount >= 10) {
                                    showDeveloperSecret = true
                                    devClickCount = 0 // Reset hitungan
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
                    Text("Selesai", fontWeight = FontWeight.Black, color = colorScheme.primary)
                }
            },
            title = { Text("Hubungi Kami", fontWeight = FontWeight.Black, color = textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hubungi kami langsung melalui tombol di bawah:", fontSize = 14.sp, color = textSecondary)

                    // Card WhatsApp (Warna Hijau WhatsApp)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val cleanContact = devContact.replace("+", "").replace(" ", "")
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanContact")))
                        },
                        // Container menggunakan Hijau WhatsApp dengan sedikit transparansi atau Solid
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF25D366).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Ikon Hijau Pekat
                            Icon(Icons.Rounded.Chat, null, tint = Color(0xFF075E54), modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("WhatsApp Resmi", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF075E54))
                                Text(devContact, fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                            }
                        }
                    }

                    // Card Email (Warna Merah/Biru Email)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$churchEmail") })
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4285F4).copy(alpha = 0.15f) // Biru Google
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Ikon Biru Pekat
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
                    // --- FOTO PROFIL (MEMAKAI GAMBAR ASLI) ---
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape, // Tetap bulat sempurna
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Image(
                            // Memanggil foto dari drawable
                            painter = painterResource(id = R.drawable.foto_louders),
                            contentDescription = "Foto Profil Louders Yoakim",
                            // RAHASIA AGAR FOTO PAS: Melakukan Crop agar foto mengisi penuh lingkaran
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

                    // --- DAFTAR KONTAK (RATA TENGAH, MINIMALIS, TANPA IKON) ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, // Bikin semua teks ke tengah
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "louders260704@gmail.com",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                        Text(
                            text = "WA: +62 852-6026-9861", // Diberi spasi agar lebih enak dibaca
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                        Text(
                            text = "IG: @louders_yoakim",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
    }
}
@Composable
fun DeveloperContactItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            // BACKGROUND LINGKARAN:
            // Terang -> Hitam | Gelap -> Putih
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                // WARNA IKON:
                // Terang -> Putih | Gelap -> Hitam
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            // WARNA TEKS: Otomatis Hitam/Putih menyesuaikan tema
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}