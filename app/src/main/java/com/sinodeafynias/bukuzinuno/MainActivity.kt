package com.sinodeafynias.bukuzinuno

import android.content.Context
import android.os.Bundle

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sinodeafynias.bukuzinuno.ui.screen.MainScreen
import com.sinodeafynias.bukuzinuno.ui.theme.BukuZinunoTheme
import com.sinodeafynias.bukuzinuno.ui.viewmodel.LaguViewModel
import com.sinodeafynias.bukuzinuno.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val laguViewModel: LaguViewModel by viewModels {
        val app = application as BukuZinunoApp
        ViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        laguViewModel.sinkronisasiCerdas(this)

        setContent {
            // 1. Ambil instance SharedPreferences yang sama dengan ViewModel (ZinunoPrefs)
            val prefs = remember { getSharedPreferences("ZinunoPrefs", Context.MODE_PRIVATE) }

            // 2. Inisialisasi State dengan membaca data yang tersimpan.
            // Kalau data belum ada (baru install), kita kasih default (false untuk DarkMode, true untuk KeepScreen)
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("is_dark_mode", false))
            }
            var isKeepScreenOn by remember {
                mutableStateOf(prefs.getBoolean("is_keep_screen_on", true))
            }

            // 3. LOGIKA: KEEP SCREEN ON (Langsung jalan saat startup & saat state berubah)
            LaunchedEffect(isKeepScreenOn) {
                if (isKeepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // 4. Bungkus aplikasi dengan Tema
            BukuZinunoTheme(darkTheme = isDarkMode) {
                MainScreen(
                    viewModel = laguViewModel,
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { statusBaru ->
                        // Simpan ke State (biar UI berubah)
                        isDarkMode = statusBaru
                        // Simpan ke SharedPreferences (biar permanen)
                        prefs.edit().putBoolean("is_dark_mode", statusBaru).apply()
                    },
                    isKeepScreenOn = isKeepScreenOn,
                    onKeepScreenOnChange = { statusBaru ->
                        // Simpan ke State
                        isKeepScreenOn = statusBaru
                        // Simpan ke SharedPreferences
                        prefs.edit().putBoolean("is_keep_screen_on", statusBaru).apply()
                    }
                )
            }
        }
    }
}