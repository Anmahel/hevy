package com.example.gymtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gymtracker.ui.screens.workout.WorkoutSummaryData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 3. TARJETA RESUMEN DE FIN DE ENTRENO (Workout Share Card).
 * Modal de celebración estilo tarjeta coleccionable en modo oscuro que incluye:
 * - Título de la rutina y fecha.
 * - Duración total, Volumen total en kg y Series completadas.
 * - Mini mapa muscular con los músculos trabajados hoy iluminados.
 * - Lista de nuevos Récords Personales (PRs) con icono de trofeo dorado 🏆.
 * - Recompensa total de XP ganada en la sesión.
 */
@Composable
fun WorkoutSummaryDialog(
    summary: WorkoutSummaryData,
    onSaveAndFinish: (notes: String) -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean = false,
    readOnlyShareMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var notesText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val formattedDuration = remember(summary.durationSeconds) {
        val hours = summary.durationSeconds / 3600
        val minutes = (summary.durationSeconds % 3600) / 60
        val seconds = summary.durationSeconds % 60
        if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %02dm %02ds", hours, minutes, seconds)
        } else if (minutes > 0) {
            String.format(Locale.getDefault(), "%d min %02d s", minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d s", seconds)
        }
    }

    val formattedDate = remember(summary.endTime) {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("es", "ES"))
        sdf.format(Date(if (summary.endTime > 0L) summary.endTime else System.currentTimeMillis()))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0B0F17),
            tonalElevation = 10.dp,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E676),
                        Color(0xFFFFD700),
                        Color(0xFF7C4DFF)
                    )
                )
            ),
            modifier = modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp)
                .testTag("workout_summary_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera superior de Tarjeta Coleccionable + XP ganado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF161F30),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "WORKOUT SHARE CARD",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.16f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "+${summary.xpEarnedTotal} XP GANADOS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.testTag("workout_share_card_xp_badge")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Título de Celebración y Nombre de la Rutina + Fecha
                Text(
                    text = "¡Entrenamiento Completado!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E676),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("workout_share_card_title")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("workout_share_card_date")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fila de 3 Métricas Clave (Duración, Volumen Total en kg, Series completadas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetricCard(
                        title = "DURACIÓN",
                        value = formattedDuration,
                        icon = Icons.Default.Timer,
                        iconTint = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMetricCard(
                        title = "VOLUMEN",
                        value = String.format(Locale.getDefault(), "%,.0f kg", summary.totalVolumeKg),
                        icon = Icons.Default.FitnessCenter,
                        iconTint = Color(0xFF00E676),
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMetricCard(
                        title = "SERIES",
                        value = "${summary.completedSetsCount}",
                        icon = Icons.Default.Check,
                        iconTint = Color(0xFFB388FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mini Mapa Muscular Anatómico con los músculos trabajados hoy iluminados
                MiniWorkoutMuscleMap(
                    workedMuscles = summary.workedMuscles
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de Nuevos Récords Personales (PRs) con icono de trofeo dorado 🏆
                Surface(
                    color = Color(0xFF131926),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (summary.newPersonalRecords.isNotEmpty()) {
                            Color(0xFFFFD700).copy(alpha = 0.65f)
                        } else {
                            Color(0xFF253047)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_share_card_prs_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🏆",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "NUEVOS RÉCORDS PERSONALES (PRs)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${summary.newPersonalRecords.size} PRs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (summary.newPersonalRecords.isNotEmpty()) {
                            summary.newPersonalRecords.forEach { pr ->
                                Surface(
                                    color = Color(0xFF1B2333),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = "Trofeo PR",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "🏆 ${pr.exerciseName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = String.format(
                                                        Locale.US,
                                                        "Mejor serie: %.1f kg × %d reps (1RM: %.1f kg)",
                                                        pr.bestSetWeightKg,
                                                        pr.bestSetReps,
                                                        pr.newBest1RmKg
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFD1D5DB)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "+${pr.xpReward} XP",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "🏆 Sin nuevos récords de 1RM hoy. ¡Trabajo sólido acumulando volumen para romper tu marca en la próxima sesión!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Desglose de ejercicios completados
                Text(
                    text = "Resumen de Ejercicios (${summary.exercisesSummary.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    summary.exercisesSummary.forEach { exerciseItem ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF161D2B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exerciseItem.exerciseName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${exerciseItem.muscleGroup} • ${exerciseItem.completedSetsCount} series completadas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%,.0f kg", exerciseItem.volumeKg),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }
                    }
                }

                if (!readOnlyShareMode) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo opcional de notas
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Notas de la sesión (opcional)...", color = Color(0xFF9CA3AF)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF283246),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_summary_notes_input"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón principal para Guardar en Room
                    Button(
                        onClick = { onSaveAndFinish(notesText) },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.5f),
                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_workout_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardando...",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Entrenamiento (+${summary.xpEarnedTotal} XP)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón secundario para volver a editar
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF374151)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("cancel_summary_button")
                    ) {
                        Text(
                            text = "Seguir Entrenando",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("close_share_card_button")
                    ) {
                        Text(
                            text = "Cerrar Tarjeta Coleccionable",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151C28)
        ),
        border = BorderStroke(1.dp, Color(0xFF242F44)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
