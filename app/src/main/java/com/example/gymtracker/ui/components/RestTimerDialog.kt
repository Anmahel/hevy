package com.example.gymtracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Backward compatibility wrapper delegating to [RestTimerOverlay] or [RestTimerExpandedSheet].
 */
@Composable
fun RestTimerDialog(
    initialSeconds: Int = 90,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    RestTimerExpandedSheet(
        totalSeconds = initialSeconds,
        remainingSeconds = initialSeconds,
        onAddSeconds = {},
        onSkip = onDismiss,
        onMinimize = onDismiss,
        modifier = modifier
    )
}
