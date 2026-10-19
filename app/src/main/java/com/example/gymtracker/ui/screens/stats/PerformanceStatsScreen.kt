package com.example.gymtracker.ui.screens.stats

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.ui.components.media.ExerciseThumbnail
import com.example.ui.theme.CarbonGlassBorderStroke
import com.example.ui.theme.GymAmberFlame
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGlassBorder
import com.example.ui.theme.GymGlassSurface
import com.example.ui.theme.GymGreenLight
import com.example.ui.theme.GymGreenPrimary
import com.example.ui.theme.GymStickyHeaderBg
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pantalla de 'Estadísticas de Rendimiento' con gráfica interactiva estilo Recharts
 * que muestra la evolución del 1RM de los 5 ejercicios principales del usuario
 * a lo largo de los últimos 6 meses.
 */
@Composable
fun PerformanceStatsScreen(
    viewModel: PerformanceStatsViewModel,
    onNavigateBack: () -> Unit,
    onExerciseClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymStickyHeaderBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 6.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("back_from_performance_stats_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Estadísticas de Rendimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Píldora indicadora de ventana temporal (Últimos 6 Meses)
                    Surface(
                        color = GymGreenPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GymGreenPrimary.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = GymGreenLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "6 Meses • 1RM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = GymGreenLight,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = GymGlassBorder)
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("performance_stats_screen")
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 56.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Fila de KPIs de Fuerza 1RM (Últimos 6 Meses)
                item(key = "kpi_summary_row") {
                    PerformanceKpiSummaryRow(uiState = uiState)
                }

                // 2. Tarjeta Principal: Gráfica Interactiva Recharts (Top 5 Ejercicios - 6 Meses)
                item(key = "recharts_1rm_chart_card") {
                    Recharts1RmEvolutionCard(
                        uiState = uiState,
                        onSelectMonthIndex = { viewModel.selectMonthIndex(it) },
                        onToggleSeries = { viewModel.toggleExerciseSeriesVisibility(it) },
                        onSelectChartStyle = { viewModel.setChartStyle(it) },
                        onToggleDemoProjection = { viewModel.toggleDemoProjection() }
                    )
                }

                // 3. Encabezado de Desglose Individual de los 5 Ejercicios Principales
                item(key = "top5_breakdown_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Top 5 Ejercicios Principales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Progresión mensual de 1RM estimado (Epley) en los últimos 6 meses",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                // 4. Tarjetas de cada uno de los 5 ejercicios principales
                items(
                    items = uiState.top5Series,
                    key = { "top_ex_${it.exerciseId}" }
                ) { series ->
                    TopExercise1RmDetailCard(
                        series = series,
                        isVisibleInChart = !uiState.hiddenExerciseIds.contains(series.exerciseId),
                        onToggleChartVisibility = { viewModel.toggleExerciseSeriesVisibility(series.exerciseId) },
                        onClick = { onExerciseClick(series.exerciseId) }
                    )
                }
            }
        }
    }
}

/**
 * Fila superior de métricas clave de rendimiento en los últimos 6 meses.
 */
@Composable
private fun PerformanceKpiSummaryRow(uiState: PerformanceStatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // KPI 1: Mayor 1RM Absoluto
        KpiMetricCard(
            label = "MAYOR 1RM ACTUAL",
            value = if (uiState.bestOverall1RmKg > 0) String.format(Locale.US, "%.1f kg", uiState.bestOverall1RmKg) else "—",
            subLabel = uiState.bestOverallExerciseName,
            accentColor = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )

        // KPI 2: Mayor Ganancia en 6 Meses
        KpiMetricCard(
            label = "MAYOR MEJORA 6M",
            value = if (uiState.biggestGainerDeltaKg > 0) String.format(Locale.US, "+%.1f kg", uiState.biggestGainerDeltaKg) else "0.0 kg",
            subLabel = uiState.biggestGainerExerciseName,
            accentColor = GymGreenLight,
            modifier = Modifier.weight(1f)
        )

        // KPI 3: Volumen de Entrenamiento Analizado
        KpiMetricCard(
            label = "HISTORIAL 6M",
            value = "${uiState.totalSessionsLast6Months} ses.",
            subLabel = "${uiState.totalSetsLast6Months} series ef.",
            accentColor = GymCyanSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiMetricCard(
    label: String,
    value: String,
    subLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141821)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF9CA3AF),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFFD1D5DB),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Componente de gráfica multi-serie estilo Recharts (<ResponsiveContainer>, <LineChart>/<AreaChart>/<BarChart>,
 * <CartesianGrid strokeDasharray="3 3" />, <XAxis />, <YAxis />, <Tooltip />, <Legend />).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Recharts1RmEvolutionCard(
    uiState: PerformanceStatsUiState,
    onSelectMonthIndex: (Int) -> Unit,
    onToggleSeries: (Long) -> Unit,
    onSelectChartStyle: (RechartsChartStyle) -> Unit,
    onToggleDemoProjection: () -> Unit
) {
    val visibleSeries = uiState.top5Series.filter { it.exerciseId !in uiState.hiddenExerciseIds }
    val selectedMonthBucket = uiState.months.getOrNull(uiState.selectedMonthIndex)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12161F)),
        border = CarbonGlassBorderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_1rm_evolution_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera de la gráfica + Selector de modo Recharts (LineChart / AreaChart / BarChart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = GymGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Evolución del 1RM (6 Meses)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Toca o desliza sobre los meses para inspeccionar el Tooltip de 1RM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Botón para alternar entre Datos Reales y Simulación 6M cuando se desee comparar
                Surface(
                    onClick = onToggleDemoProjection,
                    color = if (uiState.isShowingDemoProjection) {
                        GymAmberFlame.copy(alpha = 0.16f)
                    } else {
                        GymGreenPrimary.copy(alpha = 0.16f)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isShowingDemoProjection) GymAmberFlame.copy(alpha = 0.55f)
                        else GymGreenPrimary.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.testTag("toggle_data_projection_mode_button")
                ) {
                    Text(
                        text = if (uiState.isShowingDemoProjection) "Demo 6M" else "Datos Reales",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isShowingDemoProjection) GymAmberFlame else GymGreenLight,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            // Selector de tipo de gráfico Recharts (Monotone LineChart / AreaChart / BarChart)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A202C))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RechartsChartStyle.entries.forEach { style ->
                    val isSelected = uiState.chartStyle == style
                    Surface(
                        onClick = { onSelectChartStyle(style) },
                        color = if (isSelected) GymGreenPrimary.copy(alpha = 0.24f) else Color.Transparent,
                        shape = RoundedCornerShape(9.dp),
                        border = if (isSelected) BorderStroke(1.dp, GymGreenPrimary.copy(alpha = 0.6f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chart_style_${style.name.lowercase()}")
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) GymGreenLight else Color(0xFF9CA3AF),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Lienzo principal Recharts (<CartesianGrid />, <XAxis />, <YAxis />, <Line type="monotone" />)
            RechartsMultiSeriesCanvas(
                months = uiState.months,
                allSeries = uiState.top5Series,
                visibleSeries = visibleSeries,
                selectedMonthIndex = uiState.selectedMonthIndex,
                chartStyle = uiState.chartStyle,
                onSelectMonthIndex = onSelectMonthIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .testTag("recharts_canvas")
            )

            // <Legend /> interactiva estilo Recharts (Toque para activar/ocultar cada uno de los 5 ejercicios)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "LEYENDA INTERACTIVA (TOCA PARA FILTRAR LÍNEAS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF9CA3AF)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.top5Series.forEach { series ->
                        val isHidden = series.exerciseId in uiState.hiddenExerciseIds
                        val chipAlpha = if (isHidden) 0.38f else 1f
                        Surface(
                            onClick = { onToggleSeries(series.exerciseId) },
                            color = series.lineColor.copy(alpha = if (isHidden) 0.06f else 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                series.lineColor.copy(alpha = if (isHidden) 0.25f else 0.65f)
                            ),
                            modifier = Modifier.testTag("legend_chip_${series.rankOrder}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(series.lineColor.copy(alpha = chipAlpha))
                                )
                                Text(
                                    text = "${series.rankOrder}. ${series.shortName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = chipAlpha),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // <Tooltip /> interactivo de Recharts para el mes seleccionado en el eje X
            if (selectedMonthBucket != null) {
                RechartsActiveTooltipBox(
                    monthBucket = selectedMonthBucket,
                    visibleSeries = visibleSeries
                )
            }
        }
    }
}

/**
 * Renderizador Canvas de la gráfica Recharts con cuadrícula cartesiana punteada, ejes X/Y,
 * curvas cúbicas monótonas suavizadas, sombreado de área opcional y cursor interactivo.
 */
@Composable
private fun RechartsMultiSeriesCanvas(
    months: List<MonthBucket>,
    allSeries: List<TopExercise1RmSeries>,
    visibleSeries: List<TopExercise1RmSeries>,
    selectedMonthIndex: Int,
    chartStyle: RechartsChartStyle,
    onSelectMonthIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Calcular el dominio [minY, maxY] de 1RM en kg
    val activeSeries = if (visibleSeries.isNotEmpty()) visibleSeries else allSeries
    val allNonZeroValues = activeSeries.flatMap { s -> s.points.map { it.oneRepMaxKg } }.filter { it > 0.0 }
    val rawMin = allNonZeroValues.minOrNull() ?: 40.0
    val rawMax = allNonZeroValues.maxOrNull() ?: 140.0

    val yMin = (floor((rawMin * 0.85) / 10.0) * 10.0).coerceAtLeast(0.0)
    val yMax = max(yMin + 40.0, ceil((rawMax * 1.08) / 10.0) * 10.0)
    val yRange = (yMax - yMin).coerceAtLeast(10.0)

    val monthCount = months.size.coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D1017))
            .border(1.dp, Color(0xFF1F2735), RoundedCornerShape(14.dp))
            .pointerInput(monthCount) {
                detectTapGestures { offset ->
                    val leftPad = 44.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val relX = ((offset.x - leftPad) / plotW).coerceIn(0f, 1f)
                    val idx = (relX * (monthCount - 1)).roundToInt().coerceIn(0, monthCount - 1)
                    onSelectMonthIndex(idx)
                }
            }
            .pointerInput(monthCount) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val leftPad = 44.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val relX = ((change.position.x - leftPad) / plotW).coerceIn(0f, 1f)
                    val idx = (relX * (monthCount - 1)).roundToInt().coerceIn(0, monthCount - 1)
                    onSelectMonthIndex(idx)
                }
            }
            .padding(4.dp)
    ) {
        val leftPad = 42.dp.toPx()
        val rightPad = 14.dp.toPx()
        val topPad = 16.dp.toPx()
        val bottomPad = 28.dp.toPx()

        val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        val gridColor = Color(0xFF232B3A)

        // 1. <CartesianGrid strokeDasharray="3 3" /> (5 niveles horizontales + etiquetas YAxis en kg)
        val ySteps = 4
        for (i in 0..ySteps) {
            val fraction = i.toFloat() / ySteps
            val y = topPad + plotHeight * (1f - fraction)
            val kgValue = (yMin + yRange * fraction).roundToInt()

            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + plotWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (i == 0) null else dashEffect
            )

            val labelText = "${kgValue}kg"
            val measured = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = Color(0xFF8A94A6),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (leftPad - measured.size.width - 6.dp.toPx()).coerceAtLeast(2f),
                    y = y - measured.size.height / 2f
                )
            )
        }

        // 2. Columnas verticales de los 6 meses + etiquetas <XAxis dataKey="month" />
        fun xForMonth(index: Int): Float {
            return if (monthCount <= 1) {
                leftPad + plotWidth / 2f
            } else {
                leftPad + (index.toFloat() / (monthCount - 1)) * plotWidth
            }
        }

        fun yForKg(kg: Double): Float {
            if (kg <= 0.0) return topPad + plotHeight
            val normalized = ((kg - yMin) / yRange).toFloat().coerceIn(0f, 1f)
            return topPad + plotHeight * (1f - normalized)
        }

        months.forEachIndexed { idx, bucket ->
            val x = xForMonth(idx)
            val isSelectedMonth = idx == selectedMonthIndex

            // Línea guía vertical punteada
            drawLine(
                color = if (isSelectedMonth) GymGreenLight.copy(alpha = 0.55f) else gridColor.copy(alpha = 0.6f),
                start = Offset(x, topPad),
                end = Offset(x, topPad + plotHeight),
                strokeWidth = if (isSelectedMonth) 1.5.dp.toPx() else 1.dp.toPx(),
                pathEffect = if (isSelectedMonth) null else dashEffect
            )

            // Etiqueta del mes en el Eje X
            val monthMeasured = textMeasurer.measure(
                text = bucket.shortLabel,
                style = TextStyle(
                    color = if (isSelectedMonth) GymGreenLight else Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = if (isSelectedMonth) FontWeight.ExtraBold else FontWeight.Medium
                )
            )
            drawText(
                textLayoutResult = monthMeasured,
                topLeft = Offset(
                    x = x - monthMeasured.size.width / 2f,
                    y = topPad + plotHeight + 7.dp.toPx()
                )
            )
        }

        // 3. Renderizado de las 5 series según el modo Recharts seleccionado
        if (chartStyle == RechartsChartStyle.GROUPED_BARS) {
            val seriesCount = activeSeries.size.coerceAtLeast(1)
            val groupSlotWidth = (plotWidth / monthCount) * 0.68f
            val barWidth = (groupSlotWidth / seriesCount).coerceAtLeast(3.dp.toPx())

            months.indices.forEach { mIdx ->
                val centerX = xForMonth(mIdx)
                val startGroupX = centerX - (barWidth * seriesCount) / 2f

                activeSeries.forEachIndexed { sIdx, series ->
                    val kg = series.points.getOrNull(mIdx)?.oneRepMaxKg ?: 0.0
                    if (kg > 0.0) {
                        val barTopY = yForKg(kg)
                        val barHeight = (topPad + plotHeight - barTopY).coerceAtLeast(2.dp.toPx())
                        val barLeftX = startGroupX + sIdx * barWidth
                        drawRoundRect(
                            color = series.lineColor.copy(alpha = if (mIdx == selectedMonthIndex) 0.95f else 0.72f),
                            topLeft = Offset(barLeftX, barTopY),
                            size = Size((barWidth - 1.5.dp.toPx()).coerceAtLeast(2f), barHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
        } else {
            // Modo MONOTONE_LINE o AREA_GRADIENT
            activeSeries.forEach { series ->
                val coords = series.points.mapIndexed { idx, pt ->
                    Offset(xForMonth(idx), yForKg(pt.oneRepMaxKg))
                }

                if (coords.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.lastIndex) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            cubicTo(
                                controlX, p0.y,
                                controlX, p1.y,
                                p1.x, p1.y
                            )
                        }
                    }

                    if (chartStyle == RechartsChartStyle.AREA_GRADIENT) {
                        val areaPath = Path().apply {
                            addPath(linePath)
                            lineTo(coords.last().x, topPad + plotHeight)
                            lineTo(coords.first().x, topPad + plotHeight)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    series.lineColor.copy(alpha = 0.26f),
                                    series.lineColor.copy(alpha = 0.02f)
                                ),
                                startY = topPad,
                                endY = topPad + plotHeight
                            )
                        )
                    }

                    // Trazo principal de la curva (<Line type="monotone" strokeWidth={2.5} />)
                    drawPath(
                        path = linePath,
                        color = series.lineColor,
                        style = Stroke(
                            width = 2.6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Puntos (<Dot /> y <ActiveDot r={6} />)
                    coords.forEachIndexed { idx, ptOffset ->
                        val isSelected = idx == selectedMonthIndex
                        if (isSelected) {
                            drawCircle(
                                color = series.lineColor.copy(alpha = 0.28f),
                                radius = 8.dp.toPx(),
                                center = ptOffset
                            )
                            drawCircle(
                                color = Color(0xFF0D1017),
                                radius = 5.dp.toPx(),
                                center = ptOffset
                            )
                            drawCircle(
                                color = series.lineColor,
                                radius = 3.8.dp.toPx(),
                                center = ptOffset
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFF0D1017),
                                radius = 3.8.dp.toPx(),
                                center = ptOffset
                            )
                            drawCircle(
                                color = series.lineColor,
                                radius = 2.5.dp.toPx(),
                                center = ptOffset
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cuadro <Tooltip /> estilo Recharts que muestra los valores exactos de 1RM de los 5 ejercicios
 * en el mes seleccionado del gráfico.
 */
@Composable
private fun RechartsActiveTooltipBox(
    monthBucket: MonthBucket,
    visibleSeries: List<TopExercise1RmSeries>
) {
    Surface(
        color = Color(0xFF171C28),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF2D3748)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_active_tooltip")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${monthBucket.fullLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "1RM Estimado (Epley)",
                    style = MaterialTheme.typography.labelSmall,
                    color = GymGreenLight,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFF252F3F))

            visibleSeries.forEach { series ->
                val point = series.points.getOrNull(monthBucket.index)
                val oneRm = point?.oneRepMaxKg ?: 0.0
                val prevOneRm = if (monthBucket.index > 0) {
                    series.points.getOrNull(monthBucket.index - 1)?.oneRepMaxKg ?: 0.0
                } else {
                    0.0
                }
                val monthDelta = if (prevOneRm > 0.0 && oneRm > 0.0) oneRm - prevOneRm else 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(series.lineColor)
                        )
                        Text(
                            text = series.shortName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE5E7EB),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (monthDelta != 0.0) {
                            val sign = if (monthDelta > 0) "+" else ""
                            Text(
                                text = String.format(Locale.US, "%s%.1f kg", sign, monthDelta),
                                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                color = if (monthDelta > 0) GymGreenLight else Color(0xFFEF4444),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = if (oneRm > 0.0) String.format(Locale.US, "%.1f kg", oneRm) else "—",
                            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Black,
                            color = series.lineColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta detallada de evolución de 1RM para cada uno de los 5 ejercicios principales.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopExercise1RmDetailCard(
    series: TopExercise1RmSeries,
    isVisibleInChart: Boolean,
    onToggleChartVisibility: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GymGlassSurface),
        border = BorderStroke(1.dp, series.lineColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("top_exercise_1rm_card_${series.rankOrder}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Miniatura 3D del ejercicio con borde del color de su línea en el gráfico
                Box(
                    modifier = Modifier
                        .border(2.dp, series.lineColor, RoundedCornerShape(12.dp))
                        .padding(1.dp)
                ) {
                    ExerciseThumbnail(
                        imageSource = series.thumbnailUrl.ifEmpty { series.gifPathOrUrl },
                        size = 46.dp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#${series.rankOrder} • ${series.exerciseName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = series.lineColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = series.muscleGroup,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = series.lineColor,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        if (series.equipment.isNotBlank()) {
                            Surface(
                                color = Color(0xFF1F2937),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = series.equipment,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD1D5DB),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        series.strengthRankInfo?.let { rank ->
                            Surface(
                                color = rank.currentTier.primaryColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${rank.currentTier.badgeIcon} ${rank.currentTier.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = rank.currentTier.primaryColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Columna derecha: Mejor 1RM y ganancia en 6 meses
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (series.currentBest1RmKg > 0) String.format(Locale.US, "%.1f kg", series.currentBest1RmKg) else "—",
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black,
                        color = series.lineColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    val deltaSign = if (series.deltaKg >= 0) "+" else ""
                    Surface(
                        color = if (series.deltaKg > 0) GymGreenPrimary.copy(alpha = 0.18f) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%s%.1f kg (%.1f%%)", deltaSign, series.deltaKg, series.deltaPercentage),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            color = if (series.deltaKg > 0) GymGreenLight else Color(0xFF9CA3AF),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Fila de los 6 meses con el valor de 1RM mes a mes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10141D))
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                series.points.forEach { pt ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = pt.monthShortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF8A94A6),
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = if (pt.oneRepMaxKg > 0) String.format(Locale.US, "%.0f", pt.oneRepMaxKg) else "—",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pt.oneRepMaxKg > 0) Color.White else Color(0xFF4B5563),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de acceso rápido reutilizable para abrir 'Estadísticas de Rendimiento' (Evolución 1RM 6 Meses)
 * desde Historial, Estímulo o Perfil RPG de Atleta.
 */
@Composable
fun PerformanceStatsBannerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "open_performance_stats_banner"
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    GymGreenPrimary.copy(alpha = 0.65f),
                    GymCyanSecondary.copy(alpha = 0.65f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GymGreenPrimary.copy(alpha = 0.28f),
                                    GymCyanSecondary.copy(alpha = 0.28f)
                                )
                            )
                        )
                        .border(1.dp, GymGreenLight.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = GymGreenLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Estadísticas de Rendimiento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Gráfica 1RM • Top 5 ejercicios (Últimos 6 meses)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = GymGreenPrimary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GymGreenPrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Ver 1RM →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = GymGreenLight,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
