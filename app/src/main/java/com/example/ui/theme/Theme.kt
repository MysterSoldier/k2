package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberBlue,
    onPrimary = Color.Black,
    secondary = CyberPurple,
    onSecondary = Color.White,
    tertiary = CyberGreen,
    onTertiary = Color.Black,
    background = CyberDeepBg,
    onBackground = OnCyberSurface,
    surface = CyberSurface,
    onSurface = OnCyberSurface,
    error = Color(0xFFFF5252),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the custom retro-futuristic theme
    content: @Composable () -> Unit
) {
    // We enforce CyberColorScheme for a consistent tech gaming experience
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
