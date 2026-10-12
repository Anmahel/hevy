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

private val GymDarkColorScheme = darkColorScheme(
    primary = GymGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = GymGreenDark,
    onPrimaryContainer = Color.White,
    secondary = GymCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = GymDarkSurfaceVariant,
    onSecondaryContainer = GymTextPrimary,
    tertiary = GymAccentOrange,
    background = GymDarkBg,
    onBackground = GymTextPrimary,
    surface = GymDarkSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymDarkSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    outline = GymDarkBorder,
    error = GymError,
    onError = Color.White
)

private val GymLightColorScheme = lightColorScheme(
    primary = GymGreenDark,
    onPrimary = Color.White,
    primaryContainer = GymGreenLight,
    onPrimaryContainer = Color.Black,
    secondary = GymCyanSecondary,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = GymError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark gym mode
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GymDarkColorScheme
        else -> GymLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
