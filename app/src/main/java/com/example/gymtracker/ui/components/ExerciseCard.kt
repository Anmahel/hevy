package com.example.gymtracker.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymtracker.data.preset.DefaultRoutinesData
import com.example.gymtracker.domain.model.ExerciseSetsGroup
import com.example.gymtracker.domain.model.WorkoutSet
import com.example.gymtracker.ui.components.media.ExerciseThumbnail

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ExerciseCard(
    group: ExerciseSetsGroup,
    onAddSet: (exerciseId: Long) -> Unit,
    onWeightChanged: (WorkoutSet, Double) -> Unit,
    onRepsChanged: (WorkoutSet, Int) -> Unit,
    onToggleCompleted: (WorkoutSet) -> Unit,
    onDeleteSet: (setId: Long) -> Unit,
    onDeleteExercise: (exerciseId: Long) -> Unit,
    onOpenTimer: () -> Unit,
    onOpenPlateCalculator: () -> Unit = {},
    onUpdatePinnedNote: (exerciseId: Long, note: String) -> Unit = { _, _ -> },
    onOpenSupersetLink: () -> Unit = {},
    onUnlinkSuperset: () -> Unit = {},
    isFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFB388FF))
        } else {
            com.example.ui.theme.CarbonGlassBorderStroke
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("exercise_card_${group.exercise.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isFocused) {
                Surface(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ ¡Tu turno en esta superserie! Completa tu serie para alternar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEDE7F6)
                        )
                    }
                }
            }

            // Fila superior: Miniatura circular + Nombre del ejercicio ocupando todo el ancho disponible (weight(1f)) + botón de eliminar a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseThumbnail(
                    imageSource = group.exercise.thumbnailUrl.ifEmpty { group.exercise.gifPathOrUrl },
                    size = 44.dp
                )

                Text(
                    text = group.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onDeleteExercise(group.exercise.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar ejercicio",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Fila secundaria debajo del título: Chips de Músculo y Equipamiento (sin cortarse) a la izquierda + herramientas a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = group.exercise.muscleGroup.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = group.exercise.equipment,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    val targetRepText = group.targetRepRange.ifBlank {
                        DefaultRoutinesData.findExercisePreset("", group.exercise.name)?.targetRepRange ?: ""
                    }
                    if (targetRepText.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Meta: $targetRepText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Iconos de herramientas (Calculadora de discos, Superserie, Temporizador) agrupados limpiamente a la derecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenPlateCalculator,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("plate_calc_button_${group.exercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Calculadora de discos",
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = if (group.supersetLabel != null) onUnlinkSuperset else onOpenSupersetLink,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("superset_link_button_${group.exercise.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = if (group.supersetLabel != null) "Desvincular superserie" else "Vincular superserie",
                            tint = if (group.supersetLabel != null) Color(0xFFB388FF) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenTimer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Descanso",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 1. INSIGNIA DE SUPERSERIE (si está emparejado en A1 / A2)
            if (group.supersetLabel != null) {
                Surface(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB388FF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = group.supersetLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD1C4E9)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• Al completar serie pasa a su par",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0BEC5),
                                maxLines = 1
                            )
                        }
                        androidx.compose.material3.TextButton(
                            onClick = onUnlinkSuperset,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "Desvincular",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF8A80)
                            )
                        }
                    }
                }
            }

            // 2. COACH DE SOBRECARGA PROGRESIVA AUTOMÁTICA
            if (group.progressiveOverloadSuggestion != null) {
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.14f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("progressive_overload_badge_${group.exercise.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = group.progressiveOverloadSuggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFE082),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. NOTAS FIJAS POR EJERCICIO (Pinned Note permanente)
            var isEditingNote by remember { mutableStateOf(false) }
            var noteInputText by remember(group.exercise.pinnedNote) { mutableStateOf(group.exercise.pinnedNote) }

            if (isEditingNote) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nota fija (ej. Asiento nivel 4, agarre neutro)...") },
                        singleLine = false,
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(onClick = { isEditingNote = false }) {
                            Text("Cancelar")
                        }
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onUpdatePinnedNote(group.exercise.id, noteInputText.trim())
                                isEditingNote = false
                            }
                        ) {
                            Text("Guardar nota", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Surface(
                    color = if (group.exercise.pinnedNote.isNotBlank()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    onClick = { isEditingNote = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Nota fija",
                            tint = if (group.exercise.pinnedNote.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = group.exercise.pinnedNote.ifBlank { "+ Añadir nota fija permanente..." },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (group.exercise.pinnedNote.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = if (group.exercise.pinnedNote.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            // Técnica recomendada si existe para este ejercicio
            val technique = remember(group.exercise.name) {
                DefaultRoutinesData.findTechnique("", group.exercise.name)
            }
            if (technique != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = technique,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table headers (Technical uppercase tracking-widest text-slate-400, vertically centered)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET",
                    style = com.example.ui.theme.TechnicalLabelStyle,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "ANTERIOR",
                    style = com.example.ui.theme.TechnicalLabelStyle,
                    modifier = Modifier.width(76.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "KG",
                    style = com.example.ui.theme.TechnicalLabelStyle,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "REPS",
                    style = com.example.ui.theme.TechnicalLabelStyle,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "✓",
                    style = com.example.ui.theme.TechnicalLabelStyle,
                    modifier = Modifier.width(38.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(32.dp))
            }

            // Sets list
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                group.sets.forEach { set ->
                    SetRow(
                        set = set,
                        onWeightChanged = { w -> onWeightChanged(set, w) },
                        onRepsChanged = { r -> onRepsChanged(set, r) },
                        onToggleCompleted = { onToggleCompleted(set) },
                        onDeleteSet = { onDeleteSet(set.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add set button (único "+ Agregar Serie" sin icono "+" duplicado)
            OutlinedButton(
                onClick = { onAddSet(group.exercise.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_set_button_${group.exercise.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "+ Agregar Serie",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
