package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val GymDarkColorScheme = darkColorScheme(
    primary = GymGreenPrimary,
    onPrimary = Color(0xFF04130D),
    primaryContainer = Color(0xFF0D2E24),
    onPrimaryContainer = GymGreenLight,
    secondary = GymCyanSecondary,
    onSecondary = Color(0xFF04141C),
    secondaryContainer = GymDarkSurfaceVariant,
    onSecondaryContainer = GymTextPrimary,
    tertiary = GymAccentOrange,
    onTertiary = Color.Black,
    background = GymDarkBg,
    onBackground = GymTextPrimary,
    surface = GymDarkSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymDarkSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    surfaceContainer = GymDarkSurface,
    surfaceContainerHigh = GymDarkSurfaceVariant,
    surfaceContainerHighest = Color(0xFF222A3A),
    outline = GymDarkBorder,
    outlineVariant = GymGlassBorder,
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

val GymShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Carbon OLED Dark Mode by default
    dynamicColor: Boolean = false,
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
        shapes = GymShapes,
        content = content
    )
}
