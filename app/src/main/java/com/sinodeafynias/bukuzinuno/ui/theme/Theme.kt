package com.sinodeafynias.bukuzinuno.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SacredBlueLight,
    onPrimary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary, // TULISAN OTOMATIS JADI PUTIH
    surface = DarkSurface,
    onSurface = DarkTextPrimary,    // TULISAN DI DALAM KARTU JADI PUTIH
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = SacredBlue,
    onPrimary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary, // TULISAN OTOMATIS JADI HITAM
    surface = LightSurface,
    onSurface = LightTextPrimary,    // TULISAN DI DALAM KARTU JADI HITAM
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun BukuZinunoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Typography kamu juga pakai LocalTextStyle
        content = content
    )
}