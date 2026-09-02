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

private val BentoLightColorScheme =
  lightColorScheme(
    primary = BentoBluePrimary,
    onPrimary = BentoBlueOnPrimary,
    primaryContainer = BentoBlueContainer,
    onPrimaryContainer = BentoBlueOnContainer,
    secondary = BentoLilacText,
    onSecondary = Color.White,
    secondaryContainer = BentoLilac,
    onSecondaryContainer = BentoLilacText,
    tertiary = BentoLilacText,
    onTertiary = Color.White,
    tertiaryContainer = BentoLilacContainer,
    onTertiaryContainer = BentoLilacText,
    background = BentoBackground,
    onBackground = BentoOnSurface,
    surface = BentoSurface,
    onSurface = BentoOnSurface,
    surfaceVariant = BentoTileGray,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoOutline,
    outlineVariant = BentoTileGray,
    error = BentoError,
    onError = Color.White,
    errorContainer = BentoErrorContainer,
    onErrorContainer = BentoError,
  )

private val BentoDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF004690),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1B1C1F),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF282B33),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep bespoke Bento Grid styling crisp and consistent
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> BentoDarkColorScheme
      else -> BentoLightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

