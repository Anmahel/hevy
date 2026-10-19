package com.example.gymtracker.ui.screens.recovery

import com.example.ui.theme.safeHeaderTopPadding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.domain.model.DailyVolumeBar
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.model.MuscleRecoveryItem
import com.example.gymtracker.domain.model.ProgressiveOverloadSummary
import com.example.gymtracker.domain.model.RecoveryChipState
import com.example.gymtracker.domain.model.StimulusClassification
import com.example.gymtracker.ui.components.GlobalTopHeader
import com.example.gymtracker.ui.components.MuscleHeatmapCard
import com.example.gymtracker.ui.screens.stats.PerformanceStatsBannerCard
import java.util.Locale

// Paleta de colores para indicadores de estímulo y recuperación
private val StimulusGreen = Color(0xFF2E7D32)
private val StimulusGreenBg = Color(0xFF1B5E20).copy(alpha = 0.22f)
private val StimulusWarningYellow = Color(0xFFFFB300)
private val StimulusDangerRed = Color(0xFFE53935)
private val StimulusModerateBlue = Color(0xFF0288D1)
private val RecoveryAmber = Color(0xFFFF9800)
private val ReadyCyan = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleRecoveryScreen(
    viewModel: MuscleProgressViewModel,
    onNavigateToPerformanceStats: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    athleteLevel: Int = 1,
    onOpenRpgProfile: () -> Unit = {},
    waterGlassesToday: Int = 0,
    isWaterGoalMetToday: Boolean = false,
    creatineTakenToday: Boolean = false,
    onQuickAddWaterGlass: () -> Unit = {},
    onQuickTakeCreatine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMuscleItem by remember { mutableStateOf<MuscleRecoveryItem?>(null) }

    Scaffold(
        topBar = {
            GlobalTopHeader(
                title = "Estímulo",
                waterGlassesToday = waterGlassesToday,
                isWaterGoalMetToday = isWaterGoalMetToday,
                creatineTakenToday = creatineTakenToday,
                athleteLevel = athleteLevel,
                onQuickAddWaterGlass = onQuickAddWaterGlass,
                onQuickTakeCreatine = onQuickTakeCreatine,
                onOpenRpgProfile = onOpenRpgProfile,
                onNavigateToBackup = onNavigateToBackup
            )
        },
        modifier = modifier.testTag("muscle_recovery_screen")
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
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Acceso directo a Estadísticas de Rendimiento (Gráfica Recharts 1RM Top 5 - 6 Meses)
                item {
                    PerformanceStatsBannerCard(
                        onClick = onNavigateToPerformanceStats,
                        testTag = "open_performance_stats_from_recovery"
                    )
                }

                // 1. Mapa Corporal Anatómico Interactivo (Muscle Heatmap SVG)
                item {
                    MuscleHeatmapCard(
                        muscleItems = uiState.muscleItems,
                        onMuscleSelected = { selectedMuscleItem = it }
                    )
                }

                // 2. Tarjeta Resumen de Sobrecarga Progresiva
                item {
                    ProgressiveOverloadCard(summary = uiState.overloadSummary)
                }

                // 3. Fila de Métricas Rápidas
                item {
                    QuickMetricsRow(
                        totalSets = uiState.totalEffectiveSetsWeekly,
                        optimalCount = uiState.optimalMusclesCount,
                        recoveryCount = uiState.inRecoveryMusclesCount
                    )
                }

                // 3. Filtros
                item {
                    FilterRow(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.setFilter(it) }
                    )
                }

                // 4. Encabezado de la lista
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grupos Musculares (${uiState.filteredItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Meta: 10–20 series / semana",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 5. Lista de Músculos
                items(uiState.filteredItems, key = { it.muscle.name }) { item ->
                    MuscleCard(
                        item = item,
                        onClick = { selectedMuscleItem = item }
                    )
                }

                // Espacio al final
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Modal BottomSheet con detalles de hipertrofia y recomendaciones
        selectedMuscleItem?.let { item ->
            MuscleDetailBottomSheet(
                item = item,
                onDismiss = { selectedMuscleItem = null }
            )
        }
    }
}

/**
 * Tarjeta superior con gráfico de barras semanal y resumen de sobrecarga progresiva.
 */
@Composable
private fun ProgressiveOverloadCard(summary: ProgressiveOverloadSummary) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progressive_overload_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera con icono de tendencia fijo 40x40 a la derecha (sin estiramiento vertical)
            val isPositive = summary.percentageDiff > 0
            val diffFormatted = String.format(Locale.getDefault(), "%.1f%%", summary.percentageDiff)
            val badgeBg = if (summary.isOverloadAchieved) StimulusGreenBg else MaterialTheme.colorScheme.surfaceVariant
            val badgeTextColor = if (summary.isOverloadAchieved) StimulusGreen else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sobrecarga Progresiva",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = if (isPositive) "+$diffFormatted" else diffFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Comparativa últimos 7 días vs semana previa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Contenedor de icono de tendencia con tamaño fijo 40x40 (sin estiramiento vertical)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            summary.percentageDiff > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                            summary.percentageDiff < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                            else -> Icons.AutoMirrored.Filled.TrendingFlat
                        },
                        contentDescription = null,
                        tint = badgeTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Datos de volumen en números grandes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Esta Semana",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f kg", summary.currentWeekVolumeKg),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${summary.currentWeekSets} series efectivas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Semana Anterior",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f kg", summary.previousWeekVolumeKg),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary.previousWeekSets} series",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Gráfico de barras de los últimos 7 días
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Volumen Diario (Últimos 7 días)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                DailyBarsChart(bars = summary.dailyBars)
            }
        }
    }
}

/**
 * Gráfico simple de barras en Jetpack Compose para los 7 días de la semana.
 */
@Composable
private fun DailyBarsChart(bars: List<DailyVolumeBar>) {
    val maxVolume = bars.maxOfOrNull { it.volumeKg }?.coerceAtLeast(1.0) ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val heightFraction = if (maxVolume > 0) {
                (bar.volumeKg / maxVolume).toFloat().coerceIn(0.06f, 1f)
            } else 0.06f

            val animatedFraction by animateFloatAsState(
                targetValue = heightFraction,
                label = "bar_height_${bar.dayLabel}"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Etiqueta de volumen si hubo
                if (bar.volumeKg > 0) {
                    Text(
                        text = if (bar.volumeKg >= 1000) {
                            String.format(Locale.getDefault(), "%.1fk", bar.volumeKg / 1000)
                        } else {
                            "${bar.volumeKg.toInt()}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (bar.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Barra
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height((50 * animatedFraction).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            brush = if (bar.volumeKg > 0) {
                                if (bar.isToday) {
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nombre del día
                Text(
                    text = bar.dayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (bar.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Fila con métricas clave resumidas.
 */
@Composable
private fun QuickMetricsRow(
    totalSets: Int,
    optimalCount: Int,
    recoveryCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItem(
            title = "Series Semanales",
            value = "$totalSets",
            subtitle = "volumen total",
            icon = Icons.Default.FitnessCenter,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        MetricItem(
            title = "Rango Óptimo",
            value = "$optimalCount / 7",
            subtitle = "10–20 series",
            icon = Icons.Default.CheckCircle,
            accentColor = StimulusGreen,
            modifier = Modifier.weight(1f)
        )
        MetricItem(
            title = "En Recuperación",
            value = "$recoveryCount / 7",
            subtitle = "< 48h descanso",
            icon = Icons.Default.AccessTime,
            accentColor = RecoveryAmber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricItem(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Filtro por categorías de estado.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(
    selectedFilter: MuscleFilter,
    onFilterSelected: (MuscleFilter) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        MuscleFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Tarjeta individual para cada grupo muscular con sus barras de estímulo y recuperación.
 */
@Composable
private fun MuscleCard(
    item: MuscleRecoveryItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("muscle_card_${item.muscle.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: Nombre de músculo y Chip de estado de recuperación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.muscle.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (item.weeklyVolumeKg > 0) {
                            "${String.format(Locale.getDefault(), "%,.0f", item.weeklyVolumeKg)} kg acumulados"
                        } else {
                            "0 kg acumulados"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chip de estado: "Óptimo", "En recuperación" o "Listo para entrenar"
                RecoveryStatusChip(chipState = item.chipState)
            }

            // Barra 1: Volumen semanal acumulado y clasificación de hipertrofia (organizado en dos filas limpias)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Volumen semanal: ${item.weeklySets} / 20 series",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )

                    // Badge de estímulo de hipertrofia en su propia línea / flex-wrap para no aplastar el texto
                    StimulusBadge(stimulus = item.stimulus)
                }

                // Barra de progreso horizontal con zona óptima
                val progressFraction = (item.weeklySets / 20f).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    label = "progress_${item.muscle.id}"
                )

                val barColor = when (item.stimulus) {
                    StimulusClassification.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    StimulusClassification.MODERATE -> StimulusModerateBlue
                    StimulusClassification.OPTIMAL -> StimulusGreen
                    StimulusClassification.FATIGUE_LIMIT -> StimulusDangerRed
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Marcador de la zona óptima (de 10/20 a 20/20 = 50% al 100%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .background(StimulusGreen.copy(alpha = 0.12f))
                    )

                    // Barra activa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(barColor)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Barra 2: Estado de recuperación de fibras (0 a 48h)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = when (item.chipState) {
                                RecoveryChipState.IN_RECOVERY -> RecoveryAmber
                                RecoveryChipState.OPTIMAL -> StimulusGreen
                                RecoveryChipState.READY -> ReadyCyan
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.recoveryMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${(item.recoveryProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = when (item.chipState) {
                            RecoveryChipState.IN_RECOVERY -> RecoveryAmber
                            RecoveryChipState.OPTIMAL -> StimulusGreen
                            RecoveryChipState.READY -> ReadyCyan
                        }
                    )
                }

                // Barra de llenado de recuperación
                val animatedRecovery by animateFloatAsState(
                    targetValue = item.recoveryProgress,
                    label = "recovery_${item.muscle.id}"
                )

                LinearProgressIndicator(
                    progress = { animatedRecovery },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when (item.chipState) {
                        RecoveryChipState.IN_RECOVERY -> RecoveryAmber
                        RecoveryChipState.OPTIMAL -> StimulusGreen
                        RecoveryChipState.READY -> ReadyCyan
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Chip visual de estado de recuperación: "Óptimo", "En recuperación" o "Listo para entrenar".
 */
@Composable
private fun RecoveryStatusChip(chipState: RecoveryChipState) {
    val (bgColor, textColor, borderColor) = when (chipState) {
        RecoveryChipState.IN_RECOVERY -> Triple(
            RecoveryAmber.copy(alpha = 0.15f),
            RecoveryAmber,
            RecoveryAmber.copy(alpha = 0.4f)
        )
        RecoveryChipState.OPTIMAL -> Triple(
            StimulusGreen.copy(alpha = 0.15f),
            StimulusGreen,
            StimulusGreen.copy(alpha = 0.4f)
        )
        RecoveryChipState.READY -> Triple(
            ReadyCyan.copy(alpha = 0.15f),
            ReadyCyan,
            ReadyCyan.copy(alpha = 0.4f)
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = chipState.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Badge que clasifica el estímulo de series según estándares científicos de hipertrofia.
 */
@Composable
private fun StimulusBadge(stimulus: StimulusClassification) {
    val (bgColor, textColor) = when (stimulus) {
        StimulusClassification.LOW -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        StimulusClassification.MODERATE -> Pair(
            StimulusModerateBlue.copy(alpha = 0.15f),
            StimulusModerateBlue
        )
        StimulusClassification.OPTIMAL -> Pair(
            StimulusGreenBg,
            StimulusGreen
        )
        StimulusClassification.FATIGUE_LIMIT -> Pair(
            StimulusDangerRed.copy(alpha = 0.15f),
            StimulusDangerRed
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = stimulus.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

/**
 * Modal BottomSheet con desglose detallado de hipertrofia para un grupo muscular.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleDetailBottomSheet(
    item: MuscleRecoveryItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera con botón de cierre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.muscle.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Análisis Fisiológico de Hipertrofia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            HorizontalDivider()

            // Estado de recuperación actual
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Estado Fisiológico Actual",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.recoveryMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Último estímulo: ${item.lastTrainedDateFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Explicación científica del volumen semanal
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Estándar de Volumen para Hipertrofia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.stimulus.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "• < 6 series: Mantenimiento (estímulo mínimo)\n• 6 a 9 series: Estímulo moderado\n• 10 a 20 series: Rango hipertrófico óptimo (máximo crecimiento)\n• > 22 series: Límite de fatiga y daño muscular excesivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Comparativa de volumen semanal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Esta Semana",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.weeklySets} series",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f kg", item.weeklyVolumeKg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Semana Previa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.previousWeekSets} series",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,.0f kg", item.previousWeekVolumeKg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
