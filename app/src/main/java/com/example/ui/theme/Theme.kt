package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FFDarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianBg,
    secondary = TacticalOrange,
    onSecondary = ObsidianBg,
    tertiary = NeonGreen,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextMuted,
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = FFDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
