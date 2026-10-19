package com.example.gymtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.model.MuscleRecoveryItem

// Colores oficiales del Mapa de Calor Anatómico
val HeatmapUntrainedGray = Color(0xFF2C2D38)
val HeatmapInitialBlue = Color(0xFF0288D1)
val HeatmapInitialAmber = Color(0xFFFFB300)
val HeatmapOptimalEmerald = Color(0xFF10B981)
val HeatmapFatigueRed = Color(0xFFE53935)
private val BodySilhouetteOutline = Color(0xFF45475A)
private val BodyHeadColor = Color(0xFF23242D)

/**
 * Calcula el color térmico de un grupo muscular según las reglas fisiológicas:
 * - Gris oscuro: Sin entrenar (0 series)
 * - Azul/Amarillo: En estímulo inicial (1-9 series)
 * - Verde esmeralda: Rango óptimo de hipertrofia (10-20 series)
 * - Rojo intenso: Alta fatiga / Recuperando (>20 series o entrenado hace menos de 24h)
 */
fun resolveMuscleHeatmapColor(item: MuscleRecoveryItem?): Color {
    if (item == null || item.weeklySets == 0) {
        return HeatmapUntrainedGray
    }
    val trainedUnder24h = item.hoursSinceLastTrained != null && item.hoursSinceLastTrained < 24L
    return when {
        item.weeklySets > 20 || trainedUnder24h -> HeatmapFatigueRed
        item.weeklySets in 10..20 -> HeatmapOptimalEmerald
        item.weeklySets in 6..9 -> HeatmapInitialAmber
        else -> HeatmapInitialBlue // 1..5 series
    }
}

fun resolveMuscleHeatmapStatusLabel(item: MuscleRecoveryItem?): String {
    if (item == null || item.weeklySets == 0) return "Sin entrenar (0 series)"
    val trainedUnder24h = item.hoursSinceLastTrained != null && item.hoursSinceLastTrained < 24L
    return when {
        item.weeklySets > 20 -> "Alta fatiga (>20 series)"
        trainedUnder24h -> "Recuperando (<24h)"
        item.weeklySets in 10..20 -> "Óptimo (${item.weeklySets} series)"
        else -> "Estímulo inicial (${item.weeklySets} series)"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleHeatmapCard(
    muscleItems: List<MuscleRecoveryItem>,
    onMuscleSelected: (MuscleRecoveryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Ambas Vistas (Frontal + Posterior), 1 = Solo Frontal, 2 = Solo Posterior
    var viewMode by remember { mutableIntStateOf(0) }
    val itemsMap = remember(muscleItems) {
        muscleItems.associateBy { it.muscle }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("muscle_heatmap_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera y selector de vista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mapa Anatómico de Calor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toca un grupo muscular para ver su biometría (últimos 7 días)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chips de selección de vista: Ambas / Frontal / Posterior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Ambas Vistas", "Frontal", "Posterior").forEachIndexed { idx, label ->
                    FilterChip(
                        selected = viewMode == idx,
                        onClick = { viewMode = idx },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (viewMode == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("heatmap_view_mode_$idx")
                    )
                }
            }

            // Lienzo SVG/Vectorial con las siluetas anatómicas
            Surface(
                color = Color(0xFF14151B),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262733), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewMode == 0 || viewMode == 1) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "VISTA FRONTAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FrontBodySvgCanvas(
                                itemsMap = itemsMap,
                                onMuscleTapped = { muscle ->
                                    itemsMap[muscle]?.let(onMuscleSelected)
                                },
                                modifier = Modifier
                                    .width(if (viewMode == 1) 210.dp else 148.dp)
                                    .height(if (viewMode == 1) 310.dp else 255.dp)
                                    .testTag("heatmap_front_canvas")
                            )
                        }
                    }

                    if (viewMode == 0 || viewMode == 2) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "VISTA POSTERIOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BackBodySvgCanvas(
                                itemsMap = itemsMap,
                                onMuscleTapped = { muscle ->
                                    itemsMap[muscle]?.let(onMuscleSelected)
                                },
                                modifier = Modifier
                                    .width(if (viewMode == 2) 210.dp else 148.dp)
                                    .height(if (viewMode == 2) 310.dp else 255.dp)
                                    .testTag("heatmap_back_canvas")
                            )
                        }
                    }
                }
            }

            // Leyenda cromática de estados fisiológicos
            HeatmapLegendRow()

            // Selector rápido de regiones anatómicas con su color actual y series
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                muscleItems.forEach { item ->
                    val color = resolveMuscleHeatmapColor(item)
                    Surface(
                        color = color.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { onMuscleSelected(item) }
                            .testTag("heatmap_chip_${item.muscle.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                text = "${item.muscle.displayName}: ${item.weeklySets}s",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegendRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendBadge(color = HeatmapUntrainedGray, text = "Gris: Sin entrenar (0)")
            LegendBadge(color = HeatmapInitialBlue, text = "Azul/Ámbar: Inicial (1-9)")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendBadge(color = HeatmapOptimalEmerald, text = "Verde: Óptimo (10-20)")
            LegendBadge(color = HeatmapFatigueRed, text = "Rojo: Fatiga (>20 o <24h)")
        }
    }
}

@Composable
private fun LegendBadge(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

/**
 * Silueta Anatómica Frontal (SVG Path en Canvas):
 * - Deltoides (Hombros)
 * - Pectoral (Pecho)
 * - Bíceps (Brazos frontales)
 * - Abdomen (Core / Recto abdominal)
 * - Cuádriceps (Muslos frontales)
 * - Gemelos / Tibial (Pierna inferior)
 */
@Composable
private fun FrontBodySvgCanvas(
    itemsMap: Map<HypertrophyMuscle, MuscleRecoveryItem>,
    onMuscleTapped: (HypertrophyMuscle) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldersColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.HOMBROS])
    val chestColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.PECHO])
    val bicepsColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.BICEPS])
    val absColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.ABDOMEN])
    val quadsColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.CUADRICEPS])
    val calvesColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.GEMELOS])

    Canvas(
        modifier = modifier.pointerInput(itemsMap) {
            detectTapGestures { offset ->
                val normX = offset.x / size.width
                val normY = offset.y / size.height
                val tappedMuscle = when {
                    normY in 0.16f..0.28f && (normX < 0.32f || normX > 0.68f) -> HypertrophyMuscle.HOMBROS
                    normY in 0.17f..0.31f && normX in 0.32f..0.68f -> HypertrophyMuscle.PECHO
                    normY in 0.28f..0.44f && (normX < 0.28f || normX > 0.72f) -> HypertrophyMuscle.BICEPS
                    normY in 0.31f..0.50f && normX in 0.36f..0.64f -> HypertrophyMuscle.ABDOMEN
                    normY in 0.50f..0.75f -> HypertrophyMuscle.CUADRICEPS
                    normY > 0.75f -> HypertrophyMuscle.GEMELOS
                    else -> null
                }
                if (tappedMuscle != null) {
                    onMuscleTapped(tappedMuscle)
                }
            }
        }
    ) {
        val w = size.width
        val h = size.height

        // 1. Cabeza y cuello
        drawCircle(
            color = BodyHeadColor,
            radius = w * 0.095f,
            center = Offset(w * 0.50f, h * 0.08f)
        )
        drawCircle(
            color = BodySilhouetteOutline,
            radius = w * 0.095f,
            center = Offset(w * 0.50f, h * 0.08f),
            style = Stroke(width = 2f)
        )
        // Cuello
        drawRoundRect(
            color = BodyHeadColor,
            topLeft = Offset(w * 0.44f, h * 0.14f),
            size = Size(w * 0.12f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 2. Deltoides (Hombro Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = shouldersColor,
            points = listOf(
                Offset(w * 0.33f, h * 0.17f),
                Offset(w * 0.20f, h * 0.19f),
                Offset(w * 0.17f, h * 0.27f),
                Offset(w * 0.29f, h * 0.27f)
            )
        )
        drawAnatomicalPolygon(
            color = shouldersColor,
            points = listOf(
                Offset(w * 0.67f, h * 0.17f),
                Offset(w * 0.80f, h * 0.19f),
                Offset(w * 0.83f, h * 0.27f),
                Offset(w * 0.71f, h * 0.27f)
            )
        )

        // 3. Pectorales (Pecho Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = chestColor,
            points = listOf(
                Offset(w * 0.33f, h * 0.175f),
                Offset(w * 0.49f, h * 0.175f),
                Offset(w * 0.49f, h * 0.295f),
                Offset(w * 0.31f, h * 0.285f)
            )
        )
        drawAnatomicalPolygon(
            color = chestColor,
            points = listOf(
                Offset(w * 0.51f, h * 0.175f),
                Offset(w * 0.67f, h * 0.175f),
                Offset(w * 0.69f, h * 0.285f),
                Offset(w * 0.51f, h * 0.295f)
            )
        )

        // 4. Bíceps (Brazo Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = bicepsColor,
            points = listOf(
                Offset(w * 0.18f, h * 0.28f),
                Offset(w * 0.28f, h * 0.28f),
                Offset(w * 0.25f, h * 0.40f),
                Offset(w * 0.15f, h * 0.40f)
            )
        )
        drawAnatomicalPolygon(
            color = bicepsColor,
            points = listOf(
                Offset(w * 0.72f, h * 0.28f),
                Offset(w * 0.82f, h * 0.28f),
                Offset(w * 0.85f, h * 0.40f),
                Offset(w * 0.75f, h * 0.40f)
            )
        )

        // Antebrazos (neutrales)
        drawAnatomicalPolygon(
            color = BodyHeadColor,
            points = listOf(
                Offset(w * 0.15f, h * 0.41f),
                Offset(w * 0.24f, h * 0.41f),
                Offset(w * 0.20f, h * 0.52f),
                Offset(w * 0.12f, h * 0.52f)
            )
        )
        drawAnatomicalPolygon(
            color = BodyHeadColor,
            points = listOf(
                Offset(w * 0.76f, h * 0.41f),
                Offset(w * 0.85f, h * 0.41f),
                Offset(w * 0.88f, h * 0.52f),
                Offset(w * 0.80f, h * 0.52f)
            )
        )

        // 5. Abdomen (6-pack segmentado)
        val absTop = h * 0.305f
        val absRowH = h * 0.055f
        for (row in 0..2) {
            val y = absTop + row * (absRowH + h * 0.008f)
            // Lado izquierdo
            drawRoundRect(
                color = absColor,
                topLeft = Offset(w * 0.37f, y),
                size = Size(w * 0.115f, absRowH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = BodySilhouetteOutline,
                topLeft = Offset(w * 0.37f, y),
                size = Size(w * 0.115f, absRowH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f)
            )
            // Lado derecho
            drawRoundRect(
                color = absColor,
                topLeft = Offset(w * 0.515f, y),
                size = Size(w * 0.115f, absRowH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = BodySilhouetteOutline,
                topLeft = Offset(w * 0.515f, y),
                size = Size(w * 0.115f, absRowH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f)
            )
        }

        // 6. Cuádriceps (Muslo Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = quadsColor,
            points = listOf(
                Offset(w * 0.32f, h * 0.50f),
                Offset(w * 0.48f, h * 0.50f),
                Offset(w * 0.45f, h * 0.73f),
                Offset(w * 0.33f, h * 0.73f)
            )
        )
        drawAnatomicalPolygon(
            color = quadsColor,
            points = listOf(
                Offset(w * 0.52f, h * 0.50f),
                Offset(w * 0.68f, h * 0.50f),
                Offset(w * 0.67f, h * 0.73f),
                Offset(w * 0.55f, h * 0.73f)
            )
        )

        // 7. Gemelos / Pierna inferior frontal
        drawAnatomicalPolygon(
            color = calvesColor,
            points = listOf(
                Offset(w * 0.335f, h * 0.75f),
                Offset(w * 0.445f, h * 0.75f),
                Offset(w * 0.42f, h * 0.94f),
                Offset(w * 0.35f, h * 0.94f)
            )
        )
        drawAnatomicalPolygon(
            color = calvesColor,
            points = listOf(
                Offset(w * 0.555f, h * 0.75f),
                Offset(w * 0.665f, h * 0.75f),
                Offset(w * 0.65f, h * 0.94f),
                Offset(w * 0.58f, h * 0.94f)
            )
        )
    }
}

/**
 * Silueta Anatómica Posterior (SVG Path en Canvas):
 * - Deltoides Posterior (Hombros)
 * - Dorsales / Trapecio (Espalda en V)
 * - Tríceps (Brazos posteriores)
 * - Glúteos (Cadera posterior)
 * - Isquiotibiales (Muslo posterior)
 * - Gemelos (Pantorrillas)
 */
@Composable
private fun BackBodySvgCanvas(
    itemsMap: Map<HypertrophyMuscle, MuscleRecoveryItem>,
    onMuscleTapped: (HypertrophyMuscle) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldersColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.HOMBROS])
    val backColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.ESPALDA])
    val tricepsColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.TRICEPS])
    val glutesColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.GLUTEOS])
    val hamstringsColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.ISQUIOTIBIALES])
    val calvesColor = resolveMuscleHeatmapColor(itemsMap[HypertrophyMuscle.GEMELOS])

    Canvas(
        modifier = modifier.pointerInput(itemsMap) {
            detectTapGestures { offset ->
                val normX = offset.x / size.width
                val normY = offset.y / size.height
                val tappedMuscle = when {
                    normY in 0.16f..0.27f && (normX < 0.31f || normX > 0.69f) -> HypertrophyMuscle.HOMBROS
                    normY in 0.17f..0.44f && normX in 0.31f..0.69f -> HypertrophyMuscle.ESPALDA
                    normY in 0.27f..0.43f && (normX < 0.30f || normX > 0.70f) -> HypertrophyMuscle.TRICEPS
                    normY in 0.44f..0.56f && normX in 0.30f..0.70f -> HypertrophyMuscle.GLUTEOS
                    normY in 0.56f..0.75f -> HypertrophyMuscle.ISQUIOTIBIALES
                    normY > 0.75f -> HypertrophyMuscle.GEMELOS
                    else -> null
                }
                if (tappedMuscle != null) {
                    onMuscleTapped(tappedMuscle)
                }
            }
        }
    ) {
        val w = size.width
        val h = size.height

        // 1. Cabeza y nuca
        drawCircle(
            color = BodyHeadColor,
            radius = w * 0.095f,
            center = Offset(w * 0.50f, h * 0.08f)
        )
        drawCircle(
            color = BodySilhouetteOutline,
            radius = w * 0.095f,
            center = Offset(w * 0.50f, h * 0.08f),
            style = Stroke(width = 2f)
        )
        drawRoundRect(
            color = BodyHeadColor,
            topLeft = Offset(w * 0.44f, h * 0.14f),
            size = Size(w * 0.12f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 2. Deltoides Posterior
        drawAnatomicalPolygon(
            color = shouldersColor,
            points = listOf(
                Offset(w * 0.32f, h * 0.17f),
                Offset(w * 0.19f, h * 0.19f),
                Offset(w * 0.17f, h * 0.27f),
                Offset(w * 0.29f, h * 0.27f)
            )
        )
        drawAnatomicalPolygon(
            color = shouldersColor,
            points = listOf(
                Offset(w * 0.68f, h * 0.17f),
                Offset(w * 0.81f, h * 0.19f),
                Offset(w * 0.83f, h * 0.27f),
                Offset(w * 0.71f, h * 0.27f)
            )
        )

        // 3. Dorsales y Espalda Alta (Forma en V izquierda y derecha)
        drawAnatomicalPolygon(
            color = backColor,
            points = listOf(
                Offset(w * 0.32f, h * 0.175f),
                Offset(w * 0.49f, h * 0.175f),
                Offset(w * 0.49f, h * 0.43f),
                Offset(w * 0.36f, h * 0.43f),
                Offset(w * 0.30f, h * 0.29f)
            )
        )
        drawAnatomicalPolygon(
            color = backColor,
            points = listOf(
                Offset(w * 0.51f, h * 0.175f),
                Offset(w * 0.68f, h * 0.175f),
                Offset(w * 0.70f, h * 0.29f),
                Offset(w * 0.64f, h * 0.43f),
                Offset(w * 0.51f, h * 0.43f)
            )
        )

        // 4. Tríceps (Brazo posterior izquierdo y derecho)
        drawAnatomicalPolygon(
            color = tricepsColor,
            points = listOf(
                Offset(w * 0.18f, h * 0.28f),
                Offset(w * 0.28f, h * 0.28f),
                Offset(w * 0.25f, h * 0.41f),
                Offset(w * 0.15f, h * 0.41f)
            )
        )
        drawAnatomicalPolygon(
            color = tricepsColor,
            points = listOf(
                Offset(w * 0.72f, h * 0.28f),
                Offset(w * 0.82f, h * 0.28f),
                Offset(w * 0.85f, h * 0.41f),
                Offset(w * 0.75f, h * 0.41f)
            )
        )

        // Antebrazos posteriores
        drawAnatomicalPolygon(
            color = BodyHeadColor,
            points = listOf(
                Offset(w * 0.15f, h * 0.42f),
                Offset(w * 0.24f, h * 0.42f),
                Offset(w * 0.20f, h * 0.52f),
                Offset(w * 0.12f, h * 0.52f)
            )
        )
        drawAnatomicalPolygon(
            color = BodyHeadColor,
            points = listOf(
                Offset(w * 0.76f, h * 0.42f),
                Offset(w * 0.85f, h * 0.42f),
                Offset(w * 0.88f, h * 0.52f),
                Offset(w * 0.80f, h * 0.52f)
            )
        )

        // 5. Glúteos (Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = glutesColor,
            points = listOf(
                Offset(w * 0.35f, h * 0.44f),
                Offset(w * 0.49f, h * 0.44f),
                Offset(w * 0.49f, h * 0.55f),
                Offset(w * 0.32f, h * 0.55f)
            )
        )
        drawAnatomicalPolygon(
            color = glutesColor,
            points = listOf(
                Offset(w * 0.51f, h * 0.44f),
                Offset(w * 0.65f, h * 0.44f),
                Offset(w * 0.68f, h * 0.55f),
                Offset(w * 0.51f, h * 0.55f)
            )
        )

        // 6. Isquiotibiales (Femoral Izquierdo y Derecho)
        drawAnatomicalPolygon(
            color = hamstringsColor,
            points = listOf(
                Offset(w * 0.325f, h * 0.56f),
                Offset(w * 0.48f, h * 0.56f),
                Offset(w * 0.45f, h * 0.74f),
                Offset(w * 0.34f, h * 0.74f)
            )
        )
        drawAnatomicalPolygon(
            color = hamstringsColor,
            points = listOf(
                Offset(w * 0.52f, h * 0.56f),
                Offset(w * 0.675f, h * 0.56f),
                Offset(w * 0.66f, h * 0.74f),
                Offset(w * 0.55f, h * 0.74f)
            )
        )

        // 7. Gemelos (Pantorrilla Izquierda y Derecha)
        drawAnatomicalPolygon(
            color = calvesColor,
            points = listOf(
                Offset(w * 0.33f, h * 0.755f),
                Offset(w * 0.45f, h * 0.755f),
                Offset(w * 0.42f, h * 0.94f),
                Offset(w * 0.35f, h * 0.94f)
            )
        )
        drawAnatomicalPolygon(
            color = calvesColor,
            points = listOf(
                Offset(w * 0.55f, h * 0.755f),
                Offset(w * 0.67f, h * 0.755f),
                Offset(w * 0.65f, h * 0.94f),
                Offset(w * 0.58f, h * 0.94f)
            )
        )
    }
}

private fun DrawScope.drawAnatomicalPolygon(
    color: Color,
    points: List<Offset>
) {
    if (points.size < 3) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path = path, color = color)
    drawPath(path = path, color = BodySilhouetteOutline, style = Stroke(width = 2f))
}
