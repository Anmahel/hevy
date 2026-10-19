package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// ============================================================================
// CARBON OLED & ATHLETIC NEON PALETTE (Whoop / Hevy Pro / Apple Fitness+ Vibe)
// ============================================================================

// 1. Carbon OLED Backgrounds & Dark Glass Surfaces
val GymDarkBg = Color(0xFF07090E)              // Deep Carbon OLED (#07090E)
val GymDarkSurface = Color(0xFF12161F)         // Dark Glass Card Base (rgba(18, 22, 31))
val GymDarkSurfaceVariant = Color(0xFF191F2C)  // Elevated Slate Container
val GymInputBg = Color(0xFF11151F)             // Pro Gym Tactile Input Box (#11151F)
val GymGlassSurface = Color(0xCC12161F)        // Translucent Glass Card (75-80% alpha)
val GymStickyHeaderBg = Color(0xEB07090E)      // Translucent Sticky Top Bar
val GymBottomBarGlass = Color(0xD905070B)      // Frosted Bottom Nav (bg-black/85)

// 2. Subtle Illuminated Borders
val GymDarkBorder = Color(0xFF263042)
val GymGlassBorder = Color(0x14FFFFFF)         // 1px border-white/[0.07] (~8% white)
val GymTopHairlineBorder = Color(0x1AFFFFFF)   // 1px border-white/10

val CarbonGlassBorderStroke = BorderStroke(1.dp, GymGlassBorder)

// 3. Athletic Neon Energy Accents
// Primary: Volt / Emerald ("from-emerald-400 to-teal-500")
val GymGreenPrimary = Color(0xFF10B981)        // Emerald 500
val GymGreenDark = Color(0xFF059669)           // Emerald 600
val GymGreenLight = Color(0xFF34D399)          // Emerald 400 (Volt highlight)
val GymTealAccent = Color(0xFF14B8A6)          // Teal 500
val GymEmeraldGlow = Color(0x3810B981)         // shadow-emerald-500/20

// Recovery & Hydration: Electric Cyan ("from-cyan-400 to-blue-500")
val GymCyanSecondary = Color(0xFF22D3EE)       // Cyan 400
val GymElectricBlue = Color(0xFF3B82F6)        // Blue 500

// Calories, Streak & PRs: Fire Orange / Amber ("from-orange-500 to-amber-400")
val GymAccentOrange = Color(0xFFF97316)        // Orange 500
val GymAmberFlame = Color(0xFFFBBF24)          // Amber 400

// 4. Typography & Biometric Readout Colors
val GymTextPrimary = Color(0xFFF8FAFC)         // Crisp Alabaster White
val GymTextSecondary = Color(0xFF94A3B8)       // Technical Slate 400
val GymTextMuted = Color(0xFF64748B)           // Muted Slate 500

// 5. Status Colors
val GymSuccess = Color(0xFF10B981)
val GymWarning = Color(0xFFF59E0B)
val GymError = Color(0xFFEF4444)

// 6. Athletic Gradient Brushes
val VoltEmeraldGradient = Brush.horizontalGradient(
    colors = listOf(GymGreenLight, GymTealAccent)
)

val FireAmberGradient = Brush.horizontalGradient(
    colors = listOf(GymAccentOrange, GymAmberFlame)
)

val ElectricCyanGradient = Brush.horizontalGradient(
    colors = listOf(GymCyanSecondary, GymElectricBlue)
)

val CompletedSetRowBg = Color(0x2610B981)      // bg-emerald-500/15
val CompletedSetRowBorder = Color(0x4D10B981)  // border-emerald-500/30

/**
 * Smooth micro-interaction scale effect (active:scale-[0.98] duration-200)
 */
fun Modifier.athleticPressScale(
    interactionSource: MutableInteractionSource? = null,
    pressedScale: Float = 0.98f
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "athletic_press_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Safe status bar header padding that guarantees at least 40.dp top clearance
 * (or the system statusBars / displayCutout inset if larger) so header titles never
 * collide with the system clock or status bar icons.
 */
fun Modifier.safeHeaderTopPadding(): Modifier = composed {
    this.windowInsetsPadding(
        WindowInsets.statusBars
            .union(WindowInsets.displayCutout)
            .union(WindowInsets(top = 40.dp))
    )
}
