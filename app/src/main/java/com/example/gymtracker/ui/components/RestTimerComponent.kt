package com.example.gymtracker.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Triggers native sporty vibration and short notification beep when rest timer reaches 00:00:
 * - Native Vibrator / VibratorManager with VibrationEffect.createWaveform(longArrayOf(0, 250, 100, 250), -1)
 * - Short sporty beep via android.media.ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) (TONE_PROP_BEEP2)
 */
fun triggerRestHaptic(context: Context, haptic: HapticFeedback? = null) {
    // 1. Native Vibration
    try {
        haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        val waveformPattern = longArrayOf(0, 250, 100, 250)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.let { vibrator ->
                val effect = VibrationEffect.createWaveform(waveformPattern, -1)
                vibrator.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(waveformPattern, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(waveformPattern, -1)
            }
        }
    } catch (_: Exception) {
        // Fallback safely if device vibrator service is restricted
    }

    // 2. Native Sporty Audio Beep (without external audio files)
    try {
        val toneGenerator = android.media.ToneGenerator(
            android.media.AudioManager.STREAM_NOTIFICATION,
            100
        )
        toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 350)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                toneGenerator.release()
            } catch (_: Exception) {
            }
        }, 450L)
    } catch (_: Exception) {
        // Fallback safely if audio hardware is unavailable in test/headless environment
    }
}

/**
 * Rest timer component for ActiveWorkoutScreen.
 * Supports:
 * - Floating bottom bar when minimized.
 * - Expanded bottom card modal.
 * - Quick time adjustments (-30s, +30s, saltar).
 * - Circular and linear progress indicators.
 * - Haptic feedback at zero seconds.
 * - Minimizing/expanding without interrupting active workout flow.
 */
@Composable
fun RestTimerOverlay(
    isActive: Boolean,
    isExpanded: Boolean,
    totalSeconds: Int,
    remainingSeconds: Int,
    onAddSeconds: (Int) -> Unit,
    onSkip: () -> Unit,
    onToggleExpand: () -> Unit,
    onSetExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Trigger haptic feedback when remaining reaches 0 while active
    LaunchedEffect(remainingSeconds, isActive) {
        if (isActive && remainingSeconds == 0) {
            triggerRestHaptic(context, haptic)
        }
    }

    AnimatedVisibility(
        visible = isActive,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (isExpanded) {
            RestTimerExpandedSheet(
                totalSeconds = totalSeconds,
                remainingSeconds = remainingSeconds,
                onAddSeconds = onAddSeconds,
                onSkip = onSkip,
                onMinimize = { onSetExpanded(false) }
            )
        } else {
            RestTimerFloatingBar(
                totalSeconds = totalSeconds,
                remainingSeconds = remainingSeconds,
                onAddSeconds = onAddSeconds,
                onSkip = onSkip,
                onExpand = { onSetExpanded(true) }
            )
        }
    }
}

/**
 * Minimized floating bar pinned above the bottom of the screen.
 */
@Composable
fun RestTimerFloatingBar(
    totalSeconds: Int,
    remainingSeconds: Int,
    onAddSeconds: (Int) -> Unit,
    onSkip: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "mini_progress")
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val isFinished = remainingSeconds == 0
    val timerColor by animateColorAsState(
        targetValue = if (isFinished) {
            MaterialTheme.colorScheme.tertiary
        } else if (remainingSeconds <= 10) {
            Color(0xFFF59E0B) // Amber alert
        } else {
            MaterialTheme.colorScheme.primary
        },
        label = "mini_color"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, timerColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("rest_timer_floating_bar")
    ) {
        Column {
            // Linear progress line at the very top of the bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = timerColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Mini circular countdown + Title (clickable to expand)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onExpand)
                        .padding(vertical = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(34.dp),
                            strokeWidth = 3.5.dp,
                            color = timerColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = timerColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isFinished) "¡Listo! $timeFormatted" else timeFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFinished) timerColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFinished) "Toca para ver" else "Descanso activo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Quick actions (-30s, +30s, Expand, Skip)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // -30s button
                    OutlinedButton(
                        onClick = { onAddSeconds(-30) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("rest_timer_sub_30"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(text = "-30s", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // +30s button
                    OutlinedButton(
                        onClick = { onAddSeconds(30) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("rest_timer_add_30"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "+30s", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // Expand button
                    IconButton(
                        onClick = onExpand,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rest_timer_toggle_expand")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Expandir temporizador",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Skip / Close button
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rest_timer_skip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Saltar descanso",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full expanded bottom sheet / modal card for comprehensive timer controls and presets.
 */
@Composable
fun RestTimerExpandedSheet(
    totalSeconds: Int,
    remainingSeconds: Int,
    onAddSeconds: (Int) -> Unit,
    onSkip: () -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "expanded_progress")
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val isFinished = remainingSeconds == 0
    val accentColor by animateColorAsState(
        targetValue = if (isFinished) {
            MaterialTheme.colorScheme.tertiary
        } else if (remainingSeconds <= 10) {
            Color(0xFFF59E0B)
        } else {
            MaterialTheme.colorScheme.primary
        },
        label = "expanded_color"
    )

    // Full screen overlay with semi-transparent scrim allowing click to minimize
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onMinimize),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Card container (stopPropagation via non-clickable on child)
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .clickable(enabled = false) {} // Prevent click-through to scrim
                .testTag("rest_timer_expanded_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle / Minimize Pill
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header with Minimize, Title and Saltar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.testTag("rest_timer_minimize")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Minimizar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Temporizador de Descanso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Entrenamiento sigue activo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("rest_timer_skip_button")
                    ) {
                        Text(
                            text = "Saltar",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Circular Progress Indicator with countdown in center
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(190.dp),
                        strokeWidth = 12.dp,
                        color = accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isFinished) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFinished) "¡A por la siguiente serie! 🔥" else "Descansando",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFinished) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Linear Progress Indicator
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quick -30s, +30s, Saltar action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onAddSeconds(-30) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("rest_timer_sub_30_expanded")
                    ) {
                        Text(text = "-30s", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onAddSeconds(30) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("rest_timer_add_30_expanded")
                    ) {
                        Text(text = "+30s", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(text = "Saltar", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick preset pills: 30s, 60s, 90s, 120s, 180s
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(30, 60, 90, 120, 180).forEach { sec ->
                        val isSelected = totalSeconds == sec
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    val delta = sec - remainingSeconds
                                    onAddSeconds(delta)
                                }
                        ) {
                            Text(
                                text = "${sec}s",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to minimize and keep training
                Button(
                    onClick = onMinimize,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Minimizar (anotar o ver series)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
