package com.example.gymtracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.domain.model.WorkoutSet

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
        targetValue = if (set.isCompleted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            Color.Transparent
        },
        label = "row_bg_animation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(animatedRowBg)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag("set_row_${set.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. SET (#)
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (set.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        // 2. ANTERIOR (kg x reps del último entrenamiento)
        Box(
            modifier = Modifier
                .width(76.dp)
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = set.previousRecord.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }

        // 3. KG (campo numérico editable)
        OutlinedTextField(
            value = weightText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                    weightText = input
                    val parsed = input.toDoubleOrNull() ?: 0.0
                    onWeightChanged(parsed)
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("weight_input_${set.id}"),
            singleLine = true,
            placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // 4. REPS (campo numérico editable)
        OutlinedTextField(
            value = repsText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*$"))) {
                    repsText = input
                    val parsed = input.toIntOrNull() ?: 0
                    onRepsChanged(parsed)
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("reps_input_${set.id}"),
            singleLine = true,
            placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // 5. CHECK (botón para marcar serie completada con verde suave y acción)
        IconButton(
            onClick = onToggleCompleted,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (set.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .testTag("toggle_complete_${set.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (set.isCompleted) "Completada" else "Completar serie",
                tint = if (set.isCompleted) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
