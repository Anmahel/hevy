package com.example.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.gymtracker.domain.util.PlateCalculator

@Composable
fun PlateCalculatorDialog(
    exerciseName: String,
    initialTargetWeight: Double,
    onDismiss: () -> Unit,
    onAddWarmupSets: (List<Pair<Double, Int>>) -> Unit
) {
    var targetWeightText by remember {
        mutableStateOf(if (initialTargetWeight <= 0.0) "60.0" else "%.1f".format(java.util.Locale.US, initialTargetWeight))
    }
    var barWeightKg by remember { mutableDoubleStateOf(20.0) }

    val currentTarget = targetWeightText.toDoubleOrNull() ?: 0.0
    val result = remember(currentTarget, barWeightKg) {
        PlateCalculator.calculate(currentTarget, barWeightKg)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("plate_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera con título y botón de cierre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calculadora de Discos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = exerciseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Entradas de Peso Objetivo y Peso de Barra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetWeightText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d{0,4}(\\.\\d{0,2})?$"))) {
                                targetWeightText = input
                            }
                        },
                        label = { Text("Peso total (kg)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plate_calc_target_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = if (barWeightKg % 1.0 == 0.0) barWeightKg.toInt().toString() else barWeightKg.toString(),
                        onValueChange = { input ->
                            val parsed = input.toDoubleOrNull()
                            if (parsed != null && parsed >= 0.0) {
                                barWeightKg = parsed
                            }
                        },
                        label = { Text("Barra (kg)") },
                        modifier = Modifier
                            .width(105.dp)
                            .testTag("plate_calc_bar_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen numérico por lado
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Peso en cada lado:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.2f kg".format(java.util.Locale.US, result.weightPerSide),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // REPRESENTACIÓN GRÁFICA DE LA BARRA Y LOS DISCOS
                Text(
                    text = "DISCOS POR LADO (DE ADENTRO HACIA AFUERA)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (result.platesPerSide.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF19191C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentTarget <= barWeightKg) "Solo barra vacía (${barWeightKg} kg)" else "Añade peso para calcular discos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Barra gráfica interactiva con discos visuales montados
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Representación horizontal de la barra olímpica
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Collarín / Eje central de la barra
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                        .background(Color(0xFF9E9E9E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "||",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // Discos colocados secuencialmente
                                result.platesPerSide.forEach { plate ->
                                    repeat(plate.countPerSide) {
                                        val heightRatio = when (plate.weightKg) {
                                            25.0 -> 64.dp
                                            20.0 -> 58.dp
                                            15.0 -> 52.dp
                                            10.0 -> 46.dp
                                            5.0 -> 40.dp
                                            2.5 -> 34.dp
                                            else -> 28.dp
                                        }

                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height(heightRatio)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(plate.color)
                                                .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(3.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (plate.weightKg >= 10.0) plate.weightKg.toInt().toString() else "•",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (plate.weightKg == 5.0) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }

                                // Manga metálica sobrante
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                        .background(Color(0xFF757575))
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF282830))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Lista desglosada de discos con su respectivo chip de color
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                result.platesPerSide.forEach { plate ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(plate.color)
                                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Disco de ${plate.label}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "${plate.countPerSide} por lado (Total: ${plate.countPerSide * 2})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // BOTÓN GENERADOR DE SERIES DE CALENTAMIENTO (WARMUP)
                Button(
                    onClick = {
                        val warmupSets = PlateCalculator.generateWarmupSets(currentTarget, barWeightKg)
                        onAddWarmupSets(warmupSets)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_warmup_sets_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB74D),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Generar Series de Calentamiento",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Listo")
                }
            }
        }
    }
}
