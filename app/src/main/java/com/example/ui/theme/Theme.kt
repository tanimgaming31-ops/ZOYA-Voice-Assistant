package com.example.ui.theme

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

private val ZoyaDarkColorScheme =
  darkColorScheme(
    primary = ZoyaPrimary,
    onPrimary = Color.White,
    secondary = ZoyaSecondary,
    onSecondary = Color.Black,
    tertiary = ZoyaTertiary,
    background = ZoyaDarkBackground,
    onBackground = ZoyaOnDark,
    surface = ZoyaDarkSurface,
    onSurface = ZoyaOnDark,
    surfaceVariant = ZoyaDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8)
  )

private val ZoyaLightColorScheme =
  lightColorScheme(
    primary = ZoyaPrimary,
    onPrimary = Color.White,
    secondary = ZoyaSecondary,
    onSecondary = Color.Black,
    tertiary = ZoyaTertiary,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF64748B)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> ZoyaDarkColorScheme
      else -> ZoyaLightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
