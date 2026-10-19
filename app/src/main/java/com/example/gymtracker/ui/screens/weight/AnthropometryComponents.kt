package com.example.gymtracker.ui.screens.weight

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import kotlin.math.abs

private val CleanGainGreen = Color(0xFF10B981)
private val WaistAlertAmber = Color(0xFFF59E0B)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentCyan = Color(0xFF06B6D4)

val AVAILABLE_POSES = listOf(
    "Doble Bíceps Frontal",
    "Frente Relajado",
    "Expansión Dorsal",
    "Perfil / Abdomen",
    "Piernas / Cuádriceps"
)

/**
 * Selector de sub-pestañas dentro del módulo de Control de Peso:
 * 1) Peso & Macros
 * 2) Medidas (Cinta Métrica)
 * 3) Comparador (Antes vs. Después)
 */
@Composable
fun WeightSubTabSelector(
    selectedTab: WeightSubTab,
    onSelectTab: (WeightSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("weight_subtab_selector")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WeightSubTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                        .testTag("weight_subtab_${tab.name}")
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Contenido de la sub-pestaña "Medidas" (Cinta Métrica Antropométrica):
 * - Brazo contraído (cm), Pecho (cm), Cintura (cm), Muslo/Cuádriceps (cm), Gemelos (cm).
 * - Diferencia (+cm / -cm) respecto al registro anterior.
 * - Diagnóstico para verificar que la ganancia de peso sea masa muscular limpia y no cintura.
 */
@Composable
fun BodyMeasurementsTabContent(
    items: List<WeightUiItem>,
    onAddMeasurementsClick: () -> Unit,
    onEditLogClick: (WeightUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestWithMeasurements = items.firstOrNull { it.hasMeasurements }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("measurements_tab_list")
    ) {
        // 1. Tarjeta Resumen de Cinta Métrica y Verificación de Volumen Limpio
        item(key = "measurements_summary_header") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("measurements_summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AccentPurple.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Antropometría (Cinta Métrica)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = latestWithMeasurements?.let { "Último control: ${it.shortDate} (${it.formattedWeight})" }
                                        ?: "Sin medidas registradas aún",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onAddMeasurementsClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_measurements_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Medir",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (latestWithMeasurements != null) {
                        // Diagnóstico de Masa Muscular Limpia vs Cintura
                        CleanGainDiagnosticBanner(item = latestWithMeasurements)

                        // Cuadrícula limpia de 2 columnas con los 8 perímetros corporales y su diferencia (+cm / -cm)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MeasurementMetricTile(
                                    label = "Bíceps relajado",
                                    valueCm = latestWithMeasurements.entity.bicepsRelaxedCm,
                                    diffCm = latestWithMeasurements.bicepsRelaxedDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                                MeasurementMetricTile(
                                    label = "Bíceps contraído",
                                    valueCm = latestWithMeasurements.entity.resolvedBicepsContractedCm,
                                    diffCm = latestWithMeasurements.bicepsContractedDiffCm ?: latestWithMeasurements.armDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MeasurementMetricTile(
                                    label = "Antebrazo",
                                    valueCm = latestWithMeasurements.entity.forearmCm,
                                    diffCm = latestWithMeasurements.forearmDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                                MeasurementMetricTile(
                                    label = "Pecho / Torso",
                                    valueCm = latestWithMeasurements.entity.chestCm,
                                    diffCm = latestWithMeasurements.chestDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MeasurementMetricTile(
                                    label = "Cintura (Control)",
                                    valueCm = latestWithMeasurements.entity.waistCm,
                                    diffCm = latestWithMeasurements.waistDiffCm,
                                    isWaist = true,
                                    modifier = Modifier.weight(1f)
                                )
                                MeasurementMetricTile(
                                    label = "Glúteos / Cadera",
                                    valueCm = latestWithMeasurements.entity.glutesCm,
                                    diffCm = latestWithMeasurements.glutesDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MeasurementMetricTile(
                                    label = "Muslo / Cuádriceps",
                                    valueCm = latestWithMeasurements.entity.thighCm,
                                    diffCm = latestWithMeasurements.thighDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                                MeasurementMetricTile(
                                    label = "Gemelos / Pantorrilla",
                                    valueCm = latestWithMeasurements.entity.calfCm,
                                    diffCm = latestWithMeasurements.calfDiffCm,
                                    isWaist = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Registra tus 8 perímetros corporales en cm (Bíceps relajado, Bíceps contraído, Antebrazo, Pecho, Cintura, Glúteos, Muslo y Gemelos) para verificar que la subida de peso provenga de hipertrofia limpia y no de grasa abdominal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Encabezado de historial de medidas
        item(key = "measurements_history_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Perímetros Corporales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para editar cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 3. Tarjetas cronológicas con medidas
        items(items, key = { "meas_${it.entity.id}" }) { item ->
            MeasurementHistoryCard(
                item = item,
                onEditClick = { onEditLogClick(item) }
            )
        }
    }
}

@Composable
private fun CleanGainDiagnosticBanner(item: WeightUiItem) {
    val isClean = item.isCleanMuscleGain
    val bgColor = when (isClean) {
        true -> CleanGainGreen.copy(alpha = 0.14f)
        false -> WaistAlertAmber.copy(alpha = 0.15f)
        null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val accentColor = when (isClean) {
        true -> CleanGainGreen
        false -> WaistAlertAmber
        null -> MaterialTheme.colorScheme.primary
    }
    val message = when (isClean) {
        true -> "Hipertrofia Limpia Verificada: tus perímetros musculares crecen o se mantienen mientras la cintura permanece bajo control."
        false -> "Atención en Superávit: la cintura aumentó sin incremento proporcional en brazo, pecho o muslo. Revisa el superávit calórico."
        null -> "Registra al menos 2 controles con cinta métrica para comparar la evolución muscular frente a la cintura."
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isClean == false) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MeasurementMetricTile(
    label: String,
    valueCm: Float?,
    diffCm: Float?,
    isWaist: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = valueCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (diffCm != null) {
                    MeasurementDiffBadge(diffCm = diffCm, isWaist = isWaist)
                }
            }
        }
    }
}

@Composable
fun MeasurementDiffBadge(
    diffCm: Float,
    isWaist: Boolean
) {
    val isNeutral = abs(diffCm) < 0.05f
    val formatted = when {
        isNeutral -> "0.0 cm"
        diffCm > 0f -> "+%.1f cm".format(Locale.US, diffCm)
        else -> "%.1f cm".format(Locale.US, diffCm)
    }

    // Para músculos: subir (+cm) es verde.
    // Para cintura: bajar o mantenerse (<= +0.3 cm) es verde; subir fuerte es ámbar.
    val badgeColor = when {
        isNeutral -> MaterialTheme.colorScheme.onSurfaceVariant
        isWaist -> if (diffCm <= 0.3f) CleanGainGreen else WaistAlertAmber
        else -> if (diffCm > 0f) CleanGainGreen else WaistAlertAmber
    }

    Surface(
        color = badgeColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = formatted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

private data class HistoryPerimeterEntry(
    val label: String,
    val valueCm: Float?,
    val diffCm: Float?,
    val isWaist: Boolean = false
)

@Composable
private fun MeasurementHistoryCard(
    item: WeightUiItem,
    onEditClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .testTag("measurement_history_card_${item.entity.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Peso: ${item.formattedWeight}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar medidas",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (item.hasMeasurements) {
                val perimeters = listOf(
                    HistoryPerimeterEntry("Bíceps relajado", item.entity.bicepsRelaxedCm, item.bicepsRelaxedDiffCm),
                    HistoryPerimeterEntry("Bíceps contraído", item.entity.resolvedBicepsContractedCm, item.bicepsContractedDiffCm ?: item.armDiffCm),
                    HistoryPerimeterEntry("Antebrazo", item.entity.forearmCm, item.forearmDiffCm),
                    HistoryPerimeterEntry("Pecho / Torso", item.entity.chestCm, item.chestDiffCm),
                    HistoryPerimeterEntry("Cintura", item.entity.waistCm, item.waistDiffCm, isWaist = true),
                    HistoryPerimeterEntry("Glúteos / Cadera", item.entity.glutesCm, item.glutesDiffCm),
                    HistoryPerimeterEntry("Muslo / Cuádriceps", item.entity.thighCm, item.thighDiffCm),
                    HistoryPerimeterEntry("Gemelos", item.entity.calfCm, item.calfDiffCm)
                ).filter { it.valueCm != null }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    perimeters.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { entry ->
                                CompactMeasurementChip(
                                    label = entry.label,
                                    valueCm = entry.valueCm,
                                    diffCm = entry.diffCm,
                                    isWaist = entry.isWaist,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Sin cinta métrica en esta fecha • Toca para registrar tus 8 perímetros corporales",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactMeasurementChip(
    label: String,
    valueCm: Float?,
    diffCm: Float?,
    isWaist: Boolean,
    modifier: Modifier = Modifier
) {
    if (valueCm == null) return
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "%.1f cm".format(Locale.US, valueCm),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (diffCm != null) {
                Spacer(modifier = Modifier.width(4.dp))
                MeasurementDiffBadge(diffCm = diffCm, isWaist = isWaist)
            }
        }
    }
}

/**
 * Contenido de la sub-pestaña "Comparador" (Antes vs. Después):
 * Permite adjuntar o simular tarjetas de registro visual vinculadas a la fecha y al peso corporal
 * de ese día para comparar dos fechas lado a lado.
 */
@Composable
fun ProgressComparatorTabContent(
    items: List<WeightUiItem>,
    beforeItem: WeightUiItem?,
    afterItem: WeightUiItem?,
    onSelectBefore: (Long) -> Unit,
    onSelectAfter: (Long) -> Unit,
    onEditCard: (WeightUiItem) -> Unit,
    onSimulateCards: () -> Unit,
    onAddCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("comparator_tab_list")
    ) {
        // 1. Cabecera del Comparador Antes vs. Después
        item(key = "comparator_header") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AccentCyan.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Comparador Antes vs. Después",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Compara tarjetas visuales, peso y perímetros entre 2 fechas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onSimulateCards,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_before_after_button")
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simular Antes/Después", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onAddCard,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_visual_card_button")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nueva Tarjeta", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (items.isEmpty() || beforeItem == null || afterItem == null) {
            item(key = "empty_comparator") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aún no hay suficientes fechas para comparar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pulsa 'Simular Antes/Después' para generar dos tarjetas vinculadas a fecha, peso y medidas o registra tu propio control.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 2. Selectores de Fecha ANTES y Fecha DESPUÉS
            item(key = "date_selectors") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Selecciona Fecha ANTES (Base):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items.forEach { candidate ->
                            FilterChip(
                                selected = candidate.entity.id == beforeItem.entity.id,
                                onClick = { onSelectBefore(candidate.entity.id) },
                                label = {
                                    Text(
                                        "${candidate.shortDate} (${candidate.formattedWeight})",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentPurple.copy(alpha = 0.22f),
                                    selectedLabelColor = AccentPurple
                                )
                            )
                        }
                    }

                    Text(
                        text = "2. Selecciona Fecha DESPUÉS (Actual):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items.forEach { candidate ->
                            FilterChip(
                                selected = candidate.entity.id == afterItem.entity.id,
                                onClick = { onSelectAfter(candidate.entity.id) },
                                label = {
                                    Text(
                                        "${candidate.shortDate} (${candidate.formattedWeight})",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CleanGainGreen.copy(alpha = 0.22f),
                                    selectedLabelColor = CleanGainGreen
                                )
                            )
                        }
                    }
                }
            }

            // 3. Tarjetas Visuales Lado a Lado (ANTES vs. DESPUÉS)
            item(key = "side_by_side_visual_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("side_by_side_comparison_row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VisualProgressCard(
                        badgeLabel = "ANTES",
                        badgeColor = AccentPurple,
                        item = beforeItem,
                        onEditClick = { onEditCard(beforeItem) },
                        modifier = Modifier.weight(1f)
                    )
                    VisualProgressCard(
                        badgeLabel = "DESPUÉS",
                        badgeColor = CleanGainGreen,
                        item = afterItem,
                        onEditClick = { onEditCard(afterItem) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Tabla Comparativa de Diferencias Lado a Lado
            item(key = "side_by_side_metrics_table") {
                SideBySideDeltaTable(
                    before = beforeItem,
                    after = afterItem
                )
            }
        }
    }
}

@Composable
private fun VisualProgressCard(
    badgeLabel: String,
    badgeColor: Color,
    item: WeightUiItem,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pose = item.entity.poseType ?: "Doble Bíceps Frontal"
    val photoUriStr = item.entity.photoUri
    val hasCustomPhoto = !photoUriStr.isNullOrBlank() && !photoUriStr.startsWith("simulated://")

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .border(1.5.dp, badgeColor.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .clickable { onEditClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge superior ANTES / DESPUÉS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = item.shortDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenedor visual (Foto adjunta o Silueta Anatómica de Pose vinculada al peso y medidas)
            Surface(
                color = Color(0xFF13141C),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                if (hasCustomPhoto) {
                    AsyncImage(
                        model = Uri.parse(photoUriStr),
                        contentDescription = "Foto de progreso $badgeLabel",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    SimulatedPhysiquePoseCanvas(
                        poseType = pose,
                        weightKg = item.entity.weightKg,
                        armCm = item.entity.armCm,
                        chestCm = item.entity.chestCm,
                        waistCm = item.entity.waistCm,
                        thighCm = item.entity.thighCm,
                        accentColor = badgeColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Peso y Pose del día
            Text(
                text = item.formattedWeight,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = pose,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!item.entity.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.entity.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Foto / Medidas", fontSize = 11.sp)
            }
        }
    }
}

/**
 * Lienzo que dibuja una tarjeta anatómica visual proporcionada según el peso y perímetros de ese día
 * cuando el usuario simula o adjunta un registro visual sin fotografía externa.
 */
@Composable
private fun SimulatedPhysiquePoseCanvas(
    poseType: String,
    weightKg: Float,
    armCm: Float?,
    chestCm: Float?,
    waistCm: Float?,
    thighCm: Float?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    // Escala visual sutil según las medidas para reflejar visualmente el progreso
    val chestScale = ((chestCm ?: 102f) / 102f).coerceIn(0.90f, 1.16f)
    val waistScale = ((waistCm ?: 80f) / 80f).coerceIn(0.88f, 1.15f)
    val armScale = ((armCm ?: 37f) / 37f).coerceIn(0.90f, 1.20f)
    val thighScale = ((thighCm ?: 56f) / 56f).coerceIn(0.90f, 1.18f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Líneas de cuadrícula biométrica de fondo
            val gridColor = Color.White.copy(alpha = 0.05f)
            for (i in 1..4) {
                val y = h * (i / 5f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            drawLine(gridColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 1f)

            val bodyFill = accentColor.copy(alpha = 0.30f)
            val bodyStroke = accentColor.copy(alpha = 0.85f)

            // Cabeza
            drawCircle(
                color = bodyFill,
                radius = w * 0.085f,
                center = Offset(w * 0.5f, h * 0.14f)
            )
            drawCircle(
                color = bodyStroke,
                radius = w * 0.085f,
                center = Offset(w * 0.5f, h * 0.14f),
                style = Stroke(width = 2.2f)
            )

            // Torso en V (escalado por chestScale y waistScale)
            val halfChest = (w * 0.21f) * chestScale
            val halfWaist = (w * 0.125f) * waistScale
            val torsoPath = Path().apply {
                moveTo(w * 0.5f - halfChest, h * 0.24f)
                lineTo(w * 0.5f + halfChest, h * 0.24f)
                lineTo(w * 0.5f + halfWaist, h * 0.52f)
                lineTo(w * 0.5f - halfWaist, h * 0.52f)
                close()
            }
            drawPath(torsoPath, color = bodyFill)
            drawPath(torsoPath, color = bodyStroke, style = Stroke(width = 2.4f))

            // Brazos (en pose Doble Bíceps o Relajado)
            val isFlexed = poseType.contains("Bíceps", ignoreCase = true) ||
                poseType.contains("Dorsal", ignoreCase = true)
            val armThickness = (w * 0.075f) * armScale

            if (isFlexed) {
                // Brazos elevados en doble bíceps
                drawRoundRect(
                    color = bodyFill,
                    topLeft = Offset(w * 0.5f - halfChest - armThickness * 1.3f, h * 0.18f),
                    size = Size(armThickness * 1.3f, h * 0.14f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = bodyStroke,
                    topLeft = Offset(w * 0.5f - halfChest - armThickness * 1.3f, h * 0.18f),
                    size = Size(armThickness * 1.3f, h * 0.14f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 2f)
                )
                drawRoundRect(
                    color = bodyFill,
                    topLeft = Offset(w * 0.5f + halfChest, h * 0.18f),
                    size = Size(armThickness * 1.3f, h * 0.14f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = bodyStroke,
                    topLeft = Offset(w * 0.5f + halfChest, h * 0.18f),
                    size = Size(armThickness * 1.3f, h * 0.14f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 2f)
                )
            } else {
                // Brazos a los lados
                drawRoundRect(
                    color = bodyFill,
                    topLeft = Offset(w * 0.5f - halfChest - armThickness, h * 0.25f),
                    size = Size(armThickness, h * 0.24f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = bodyFill,
                    topLeft = Offset(w * 0.5f + halfChest, h * 0.25f),
                    size = Size(armThickness, h * 0.24f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            // Piernas / Cuádriceps (escalados por thighScale)
            val thighWidth = (w * 0.11f) * thighScale
            drawRoundRect(
                color = bodyFill,
                topLeft = Offset(w * 0.48f - thighWidth, h * 0.54f),
                size = Size(thighWidth, h * 0.34f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = bodyStroke,
                topLeft = Offset(w * 0.48f - thighWidth, h * 0.54f),
                size = Size(thighWidth, h * 0.34f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
            drawRoundRect(
                color = bodyFill,
                topLeft = Offset(w * 0.52f, h * 0.54f),
                size = Size(thighWidth, h * 0.34f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = bodyStroke,
                topLeft = Offset(w * 0.52f, h * 0.54f),
                size = Size(thighWidth, h * 0.34f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2f)
            )
        }

        // Etiquetas flotantes de medidas sobre la tarjeta visual
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            if (armCm != null) {
                Text(
                    text = "Brazo: %.1fcm".format(Locale.US, armCm),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (waistCm != null) {
                Text(
                    text = "Cintura: %.1fcm".format(Locale.US, waistCm),
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SideBySideDeltaTable(
    before: WeightUiItem,
    after: WeightUiItem
) {
    val weightDelta = after.entity.weightKg - before.entity.weightKg
    val bicepsRelaxedDelta = if (after.entity.bicepsRelaxedCm != null && before.entity.bicepsRelaxedCm != null) {
        after.entity.bicepsRelaxedCm - before.entity.bicepsRelaxedCm
    } else null
    val afterContracted = after.entity.resolvedBicepsContractedCm
    val beforeContracted = before.entity.resolvedBicepsContractedCm
    val armDelta = if (afterContracted != null && beforeContracted != null) {
        afterContracted - beforeContracted
    } else null
    val forearmDelta = if (after.entity.forearmCm != null && before.entity.forearmCm != null) {
        after.entity.forearmCm - before.entity.forearmCm
    } else null
    val chestDelta = if (after.entity.chestCm != null && before.entity.chestCm != null) after.entity.chestCm - before.entity.chestCm else null
    val waistDelta = if (after.entity.waistCm != null && before.entity.waistCm != null) after.entity.waistCm - before.entity.waistCm else null
    val glutesDelta = if (after.entity.glutesCm != null && before.entity.glutesCm != null) {
        after.entity.glutesCm - before.entity.glutesCm
    } else null
    val thighDelta = if (after.entity.thighCm != null && before.entity.thighCm != null) after.entity.thighCm - before.entity.thighCm else null
    val calfDelta = if (after.entity.calfCm != null && before.entity.calfCm != null) after.entity.calfCm - before.entity.calfCm else null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("comparator_delta_table")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Diferencias Biométricas (${before.shortDate} → ${after.shortDate})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            ComparisonDeltaRow(
                metricName = "Peso Corporal",
                beforeVal = "%.1f kg".format(Locale.US, before.entity.weightKg),
                afterVal = "%.1f kg".format(Locale.US, after.entity.weightKg),
                deltaVal = weightDelta,
                unit = "kg",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Bíceps relajado",
                beforeVal = before.entity.bicepsRelaxedCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.bicepsRelaxedCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = bicepsRelaxedDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Bíceps contraído",
                beforeVal = beforeContracted?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = afterContracted?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = armDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Antebrazo",
                beforeVal = before.entity.forearmCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.forearmCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = forearmDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Pecho / Torso",
                beforeVal = before.entity.chestCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.chestCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = chestDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Cintura (Control)",
                beforeVal = before.entity.waistCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.waistCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = waistDelta,
                unit = "cm",
                isWaist = true
            )
            ComparisonDeltaRow(
                metricName = "Glúteos / Cadera",
                beforeVal = before.entity.glutesCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.glutesCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = glutesDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Muslo / Cuádriceps",
                beforeVal = before.entity.thighCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.thighCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = thighDelta,
                unit = "cm",
                isWaist = false
            )
            ComparisonDeltaRow(
                metricName = "Gemelos / Pantorrilla",
                beforeVal = before.entity.calfCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                afterVal = after.entity.calfCm?.let { "%.1f cm".format(Locale.US, it) } ?: "—",
                deltaVal = calfDelta,
                unit = "cm",
                isWaist = false
            )
        }
    }
}

@Composable
private fun ComparisonDeltaRow(
    metricName: String,
    beforeVal: String,
    afterVal: String,
    deltaVal: Float?,
    unit: String,
    isWaist: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = metricName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = "$beforeVal → $afterVal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.3f),
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier.weight(0.9f),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (deltaVal != null) {
                val isNeutral = abs(deltaVal) < 0.05f
                val text = when {
                    isNeutral -> "0.0 $unit"
                    deltaVal > 0f -> "+%.1f %s".format(Locale.US, deltaVal, unit)
                    else -> "%.1f %s".format(Locale.US, deltaVal, unit)
                }
                val color = when {
                    isNeutral -> MaterialTheme.colorScheme.onSurfaceVariant
                    isWaist -> if (deltaVal <= 0.3f) CleanGainGreen else WaistAlertAmber
                    else -> if (deltaVal > 0f) CleanGainGreen else WaistAlertAmber
                }
                Surface(
                    color = color.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
