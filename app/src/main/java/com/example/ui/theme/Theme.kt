package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = AquaCyan,
  onPrimary = Color.White,
  primaryContainer = AquaElectricBlue,
  onPrimaryContainer = Color.White,
  secondary = AquaOrange,
  onSecondary = Color.White,
  tertiary = AquaGold,
  background = AquaBlack,
  surface = AquaSurface,
  surfaceVariant = AquaDarkCard,
  onBackground = AquaTextPrimary,
  onSurface = AquaTextPrimary,
  outline = AquaCardBorder
)

@Composable
fun AquaFightersTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
