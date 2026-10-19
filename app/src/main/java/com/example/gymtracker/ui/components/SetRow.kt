package com.example.gymtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.ui.theme.CompletedSetRowBg
import com.example.ui.theme.CompletedSetRowBorder
import com.example.ui.theme.GymAmberFlame
import com.example.ui.theme.GymEmeraldGlow
import com.example.ui.theme.GymGreenLight
import com.example.ui.theme.GymGreenPrimary
import com.example.ui.theme.GymInputBg
import com.example.ui.theme.GymTealAccent
import com.example.ui.theme.GymTextSecondary
import com.example.ui.theme.VoltEmeraldGradient

@Composable
fun SetRow(
    set: WorkoutSet,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onToggleCompleted: () -> Unit,
    onDeleteSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weightText by remember(set.weightKg) {
        mutableStateOf(if (set.weightKg == 0.0) "" else set.weightKg.toString().removeSuffix(".0"))
    }
    var repsText by remember(set.reps) {
        mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
    }

    val animatedRowBg by animateColorAsState(
        targetValue = if (set.isCompleted) CompletedSetRowBg else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "row_bg_animation"
    )
    val animatedRowBorder by animateColorAsState(
        targetValue = if (set.isCompleted) CompletedSetRowBorder else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "row_border_animation"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (set.isCompleted) 1.03f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "check_btn_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedRowBg)
            .border(1.dp, animatedRowBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 5.dp, horizontal = 6.dp)
            .testTag("set_row_${set.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. SET (# o 'W' para calentamiento)
        val isWarmup = set.setType == com.example.gymtracker.domain.model.SetType.WARMUP
        val setLabel = if (isWarmup) "W" else "${set.setNumber}"
        val badgeBg = when {
            set.isCompleted -> GymGreenPrimary.copy(alpha = 0.25f)
            isWarmup -> GymAmberFlame.copy(alpha = 0.20f)
            else -> GymInputBg
        }
        val badgeColor = when {
            set.isCompleted -> GymGreenLight
            isWarmup -> GymAmberFlame
            else -> MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(badgeBg)
                .border(
                    width = 1.dp,
                    color = if (set.isCompleted) GymGreenLight.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.07f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = setLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.3).sp
                ),
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
        }

        // 2. ANTERIOR (kg x reps del último entrenamiento - tabular-nums)
        Box(
            modifier = Modifier
                .width(76.dp)
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = set.previousRecord.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.3).sp
                ),
                color = if (set.isCompleted) GymGreenLight.copy(alpha = 0.9f) else GymTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. KG (campo numérico táctil Pro Gym #11151F con borde verde esmeralda al tocarlo)
        OutlinedTextField(
            value = weightText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d{0,4}(\\.\\d{0,2})?$"))) {
                    val parsed = input.toDoubleOrNull() ?: 0.0
                    if (parsed <= 999.0) {
                        weightText = input
                        onWeightChanged(parsed)
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("weight_input_${set.id}"),
            singleLine = true,
            placeholder = {
                Text(
                    text = "0",
                    textAlign = TextAlign.Center,
                    color = GymTextSecondary.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                fontFeatureSettings = "tnum"
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GymInputBg,
                unfocusedContainerColor = if (set.isCompleted) GymInputBg.copy(alpha = 0.75f) else GymInputBg,
                focusedBorderColor = GymGreenLight,
                unfocusedBorderColor = if (set.isCompleted) GymGreenPrimary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                cursorColor = GymGreenLight
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // 4. REPS (campo numérico táctil Pro Gym #11151F)
        val repPlaceholder = remember(set.targetRepRange) {
            set.targetRepRange
                .replace("reps", "", ignoreCase = true)
                .replace("a", "-")
                .replace(" ", "")
                .ifBlank { "0" }
        }
        OutlinedTextField(
            value = repsText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d{0,3}$"))) {
                    val parsed = input.toIntOrNull() ?: 0
                    if (parsed <= 500) {
                        repsText = input
                        onRepsChanged(parsed)
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("reps_input_${set.id}"),
            singleLine = true,
            placeholder = {
                Text(
                    text = repPlaceholder,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    color = GymTextSecondary.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                fontFeatureSettings = "tnum"
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GymInputBg,
                unfocusedContainerColor = if (set.isCompleted) GymInputBg.copy(alpha = 0.75f) else GymInputBg,
                focusedBorderColor = GymGreenLight,
                unfocusedBorderColor = if (set.isCompleted) GymGreenPrimary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                cursorColor = GymGreenLight
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // 5. CHECK (botón para marcar serie completada que brilla en verde sólido/degradado)
        val checkShape = RoundedCornerShape(10.dp)
        IconButton(
            onClick = onToggleCompleted,
            modifier = Modifier
                .size(38.dp)
                .graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                }
                .then(
                    if (set.isCompleted) {
                        Modifier
                            .shadow(
                                elevation = 8.dp,
                                shape = checkShape,
                                ambientColor = GymEmeraldGlow,
                                spotColor = GymGreenPrimary
                            )
                            .clip(checkShape)
                            .background(VoltEmeraldGradient)
                    } else {
                        Modifier
                            .clip(checkShape)
                            .background(GymInputBg)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), checkShape)
                    }
                )
                .testTag("toggle_complete_${set.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (set.isCompleted) "Completada" else "Completar serie",
                tint = if (set.isCompleted) Color(0xFF04130D) else GymTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Eliminar serie
        IconButton(
            onClick = onDeleteSet,
            modifier = Modifier
                .size(32.dp)
                .testTag("delete_set_${set.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar serie",
                tint = GymTextSecondary.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
