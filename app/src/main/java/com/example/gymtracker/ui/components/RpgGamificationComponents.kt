package com.example.gymtracker.ui.components

import com.example.ui.theme.safeHeaderTopPadding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.domain.model.HypertrophyMuscle
import com.example.gymtracker.domain.util.AthleteXpProfile
import com.example.gymtracker.domain.util.RpgGamificationEngine
import com.example.gymtracker.domain.util.StrengthRankInfo
import com.example.gymtracker.domain.util.StrengthRankTier
import java.util.Locale

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold

private enum class StrengthRankMuscleFilter(val label: String) {
    ACTIVE("⚡ Con 1RM"),
    PECHO("Pecho"),
    ESPALDA("Espalda"),
    HOMBROS("Hombros"),
    PIERNAS("Piernas"),
    BRAZOS("Brazos"),
    CORE("Core"),
    ALL("Todos")
}

private fun matchesMuscleFilter(rank: StrengthRankInfo, filter: StrengthRankMuscleFilter): Boolean {
    val text = "${rank.muscleGroup} ${rank.exerciseName}".lowercase(Locale.ROOT)
    return when (filter) {
        StrengthRankMuscleFilter.ACTIVE -> rank.bestOneRepMaxKg > 0.0
        StrengthRankMuscleFilter.ALL -> true
        StrengthRankMuscleFilter.PECHO -> text.contains("pecho") || text.contains("pectoral") || text.contains("banca") || text.contains("aperturas") || text.contains("cruce")
        StrengthRankMuscleFilter.ESPALDA -> text.contains("espalda") || text.contains("dorsal") || text.contains("remo") || text.contains("jalón") || text.contains("jalon") || text.contains("dominada") || text.contains("lumbar") || text.contains("pull")
        StrengthRankMuscleFilter.HOMBROS -> text.contains("hombro") || text.contains("deltoide") || text.contains("militar") || text.contains("lateral") || text.contains("pájaro") || text.contains("pajaro") || text.contains("face pull")
        StrengthRankMuscleFilter.PIERNAS -> text.contains("cuádriceps") || text.contains("cuadriceps") || text.contains("isquio") || text.contains("glúteo") || text.contains("gluteo") || text.contains("pierna") || text.contains("sentadilla") || text.contains("prensa") || text.contains("peso muerto") || text.contains("abductor") || text.contains("aductor") || text.contains("gemelo") || text.contains("pantorrilla") || text.contains("zancada") || text.contains("hip thrust") || text.contains("extensión de cuádriceps") || text.contains("femor")
        StrengthRankMuscleFilter.BRAZOS -> text.contains("bíceps") || text.contains("biceps") || text.contains("tríceps") || text.contains("triceps") || text.contains("curl") || text.contains("francés") || text.contains("frances") || text.contains("fondos") || text.contains("brazo") || text.contains("antebrazo")
        StrengthRankMuscleFilter.CORE -> text.contains("abdomen") || text.contains("core") || text.contains("plancha") || text.contains("crunch") || text.contains("oblicuo") || text.contains("lumbar")
    }
}

/**
 * Pantalla sólida a pantalla completa (FullScreen integrada en la navegación principal)
 * dedicada exclusivamente al "Perfil RPG de Atleta", sin contenedor de ventana emergente
 * ni recuadro exterior verde redundante.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteRpgProfileScreen(
    xpProfile: AthleteXpProfile,
    strengthRanks: List<StrengthRankInfo>,
    bodyweightKg: Double = 75.0,
    onExerciseClick: (Long) -> Unit = {},
    onNavigateToPerformanceStats: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.GymStickyHeaderBg)
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
                            modifier = Modifier.testTag("close_rpg_profile_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "🏆 Perfil RPG de Atleta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Badge de XP total en una sola línea horizontal en la cabecera
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFFFFD700).copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "⚡ ${xpProfile.totalXp} XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = com.example.ui.theme.GymGlassBorder
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("athlete_rpg_profile_fullscreen_modal")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "athlete_xp_summary_card") {
                AthleteXpHeaderCard(
                    xpProfile = xpProfile,
                    strengthRanks = emptyList(),
                    bodyweightKg = bodyweightKg,
                    onExerciseClick = onExerciseClick
                )
            }

            item(key = "performance_stats_banner_rpg") {
                com.example.gymtracker.ui.screens.stats.PerformanceStatsBannerCard(
                    onClick = onNavigateToPerformanceStats,
                    testTag = "open_performance_stats_from_rpg"
                )
            }

            if (strengthRanks.isNotEmpty()) {
                item(key = "competitive_strength_ranks_section") {
                    CompetitiveStrengthRanksSection(
                        strengthRanks = strengthRanks,
                        bodyweightKg = bodyweightKg,
                        onExerciseClick = onExerciseClick
                    )
                }
            }
        }
    }
}

/**
 * Wrapper de compatibilidad que delega directamente a la pantalla sólida AthleteRpgProfileScreen.
 */
@Composable
fun AthleteRpgProfileDialog(
    xpProfile: AthleteXpProfile,
    strengthRanks: List<StrengthRankInfo>,
    bodyweightKg: Double = 75.0,
    onExerciseClick: (Long) -> Unit = {},
    onDismiss: () -> Unit
) {
    AthleteRpgProfileScreen(
        xpProfile = xpProfile,
        strengthRanks = strengthRanks,
        bodyweightKg = bodyweightKg,
        onExerciseClick = onExerciseClick,
        onNavigateBack = onDismiss
    )
}

/**
 * 1. SISTEMA DE NIVEL Y EXPERIENCIA (XP) - Tarjeta limpia de borde a borde sin recuadro verde exterior.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AthleteXpHeaderCard(
    xpProfile: AthleteXpProfile,
    strengthRanks: List<StrengthRankInfo> = emptyList(),
    bodyweightKg: Double = 75.0,
    onExerciseClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRulesBreakdown by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = xpProfile.levelProgressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "athlete_xp_bar_anim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF141821)
            ),
            border = com.example.ui.theme.CarbonGlassBorderStroke,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("athlete_xp_header_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Fila superior: Medalla de Nivel + Subtítulo/Título + Columna derecha con Badge XP (1 sola línea) y botón XP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Insignia Hexagonal / Circular de Nivel RPG
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF7C4DFF),
                                        Color(0xFF00E676)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NIVEL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Text(
                                text = "${xpProfile.level}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.testTag("athlete_level_badge_text")
                            )
                        }
                    }

                    // Columna central: Subtítulo "PERFIL RPG DE ATLETA" independiente y limpio
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "PERFIL RPG DE ATLETA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E676),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = xpProfile.athleteTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "${xpProfile.xpInCurrentLevel} / ${xpProfile.xpForNextLevel} XP para Nivel ${xpProfile.level + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    // Columna derecha: Badge de XP total en 1 sola línea horizontal + Botón desplegable XP
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFFFD700).copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = "⚡ ${xpProfile.totalXp} XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFeatureSettings = "tnum"
                                ),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("athlete_total_xp_text")
                            )
                        }

                        // Botón para expandir desglose de XP
                        Surface(
                            color = Color(0xFF1F2633),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clickable { showRulesBreakdown = !showRulesBreakdown }
                                .testTag("toggle_xp_breakdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Icon(
                                    imageVector = if (showRulesBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ver desglose de XP",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra de Progreso de Nivel de Atleta (XP)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF232B3A))
                        .testTag("athlete_xp_progress_bar")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceAtLeast(0.04f))
                            .height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF00E5FF),
                                        Color(0xFF7C4DFF)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chips rápidos de fuentes de XP (Siempre visibles para transparencia de reglas)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    XpSourceBadge(
                        label = "+${RpgGamificationEngine.XP_PER_COMPLETED_SET} XP Serie",
                        countText = "${xpProfile.completedSetsCount}",
                        accentColor = Color(0xFF00E676)
                    )
                    XpSourceBadge(
                        label = "+${RpgGamificationEngine.XP_PER_FINISHED_WORKOUT} XP Entreno",
                        countText = "${xpProfile.completedWorkoutsCount}",
                        accentColor = Color(0xFF00E5FF)
                    )
                    XpSourceBadge(
                        label = "+${RpgGamificationEngine.XP_PER_PR_1RM} XP Récord 1RM",
                        countText = "${xpProfile.totalPrsCount} 🏆",
                        accentColor = Color(0xFFFFD700)
                    )
                    XpSourceBadge(
                        label = "+${RpgGamificationEngine.XP_PER_WEIGHT_OR_NUTRITION_LOG} XP Peso/Nutr.",
                        countText = "${xpProfile.nutritionAndWeightLogsCount}",
                        accentColor = Color(0xFFB388FF)
                    )
                }

                // Desglose detallado desplegable
                AnimatedVisibility(
                    visible = showRulesBreakdown,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C222E))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Desglose de Experiencia Acumulada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        XpBreakdownRow(
                            title = "Series completadas (${xpProfile.completedSetsCount} × +10 XP)",
                            xpValue = "+${xpProfile.setsXp} XP",
                            color = Color(0xFF00E676)
                        )
                        XpBreakdownRow(
                            title = "Entrenamientos terminados (${xpProfile.completedWorkoutsCount} × +150 XP)",
                            xpValue = "+${xpProfile.workoutsXp} XP",
                            color = Color(0xFF00E5FF)
                        )
                        XpBreakdownRow(
                            title = "Récords Personales 1RM (${xpProfile.totalPrsCount} × +300 XP)",
                            xpValue = "+${xpProfile.prsXp} XP",
                            color = Color(0xFFFFD700)
                        )
                        XpBreakdownRow(
                            title = "Registros de Peso y Nutrición (${xpProfile.nutritionAndWeightLogsCount} × +50 XP)",
                            xpValue = "+${xpProfile.nutritionLogsXp} XP",
                            color = Color(0xFFB388FF)
                        )
                    }
                }
            }
        }

        // Si se pasa strengthRanks directamente a AthleteXpHeaderCard, renderizar la sección acotada
        if (strengthRanks.isNotEmpty()) {
            CompetitiveStrengthRanksSection(
                strengthRanks = strengthRanks,
                bodyweightKg = bodyweightKg,
                onExerciseClick = onExerciseClick
            )
        }
    }
}

private const val INITIAL_VISIBLE_RANKS_LIMIT = 5

/**
 * Sección independiente de "Rangos de Fuerza Competitivos" sin scroll infinito:
 * - Filtra por ejercicios con 1RM activo o por grupo muscular (Pecho, Espalda, Hombros, Piernas, Brazos, Core, Todos).
 * - Muestra inicialmente el Top 5 de ejercicios y permite expandir/contraer bajo demanda ("Ver todos / Mostrar menos").
 */
@Composable
private fun CompetitiveStrengthRanksSection(
    strengthRanks: List<StrengthRankInfo>,
    bodyweightKg: Double,
    onExerciseClick: (Long) -> Unit
) {
    val activeRanksCount = remember(strengthRanks) {
        strengthRanks.count { it.bestOneRepMaxKg > 0.0 }
    }
    var selectedFilter by remember(activeRanksCount) {
        mutableStateOf(
            if (activeRanksCount > 0) StrengthRankMuscleFilter.ACTIVE else StrengthRankMuscleFilter.ALL
        )
    }
    var isExpandedAll by remember(selectedFilter) { mutableStateOf(false) }
    var showRanksShowcase by remember { mutableStateOf(true) }

    val filteredRanks = remember(strengthRanks, selectedFilter) {
        val matching = strengthRanks.filter { matchesMuscleFilter(it, selectedFilter) }
        if (matching.isEmpty() && selectedFilter == StrengthRankMuscleFilter.ACTIVE) {
            strengthRanks
        } else {
            matching
        }
    }

    val visibleRanks = remember(filteredRanks, isExpandedAll) {
        if (isExpandedAll) {
            filteredRanks
        } else {
            filteredRanks.take(INITIAL_VISIBLE_RANKS_LIMIT)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141821)
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRanksShowcase = !showRanksShowcase }
                    .testTag("toggle_strength_ranks_panel_button"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Rangos de Fuerza Competitivos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Fuerza relativa • PC: ${String.format(Locale.US, "%.1f", bodyweightKg)} kg • $activeRanksCount con 1RM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                Icon(
                    imageVector = if (showRanksShowcase) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = showRanksShowcase,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_strength_ranks_row"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Píldoras de filtro por grupo muscular / activos para evitar el scroll infinito de 43 ejercicios
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(StrengthRankMuscleFilter.entries) { filter ->
                            val isSelected = selectedFilter == filter
                            val chipLabel = when (filter) {
                                StrengthRankMuscleFilter.ACTIVE -> "⚡ Con 1RM ($activeRanksCount)"
                                StrengthRankMuscleFilter.ALL -> "Todos (${strengthRanks.size})"
                                else -> filter.label
                            }
                            Surface(
                                onClick = { selectedFilter = filter },
                                color = if (isSelected) {
                                    com.example.ui.theme.GymGreenPrimary.copy(alpha = 0.20f)
                                } else {
                                    Color(0xFF1C222E)
                                },
                                shape = RoundedCornerShape(999.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        com.example.ui.theme.GymGreenPrimary
                                    } else {
                                        Color(0xFF2B3548)
                                    }
                                )
                            ) {
                                Text(
                                    text = chipLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) com.example.ui.theme.GymGreenPrimary else Color(0xFFD1D5DB),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    visibleRanks.forEach { rankInfo ->
                        FullWidthExerciseRankCard(
                            rankInfo = rankInfo,
                            onClick = {
                                if (rankInfo.exerciseId > 0L) {
                                    onExerciseClick(rankInfo.exerciseId)
                                }
                            }
                        )
                    }

                    if (filteredRanks.size > INITIAL_VISIBLE_RANKS_LIMIT) {
                        OutlinedButton(
                            onClick = { isExpandedAll = !isExpandedAll },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFFFD700).copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_show_all_ranks_button")
                        ) {
                            Text(
                                text = if (isExpandedAll) {
                                    "Mostrar menos (Top $INITIAL_VISIBLE_RANKS_LIMIT) ▲"
                                } else {
                                    "Ver todos (${filteredRanks.size} ejercicios) ▼"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun XpSourceBadge(
    label: String,
    countText: String,
    accentColor: Color
) {
    Surface(
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE5E7EB)
            )
            Text(
                text = "($countText)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun XpBreakdownRow(
    title: String,
    xpValue: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD1D5DB),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = xpValue,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun FullWidthExerciseRankCard(
    rankInfo: StrengthRankInfo,
    onClick: () -> Unit
) {
    val tier = rankInfo.currentTier
    val nextTier = rankInfo.nextTier

    Surface(
        color = Color(0xFF1C222E),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tier.primaryColor.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rankInfo.exerciseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                )

                Surface(
                    color = tier.primaryColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${tier.badgeIcon} ${tier.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = tier.primaryColor,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { rankInfo.progressToNextTier },
                color = tier.primaryColor,
                trackColor = Color(0xFF2B3548),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(
                        Locale.US,
                        "1RM: %.1f kg (%.2fx PC)",
                        rankInfo.bestOneRepMaxKg,
                        rankInfo.relativeStrengthRatio
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (nextTier != null) {
                        String.format(
                            Locale.US,
                            "Faltan +%.1f kg 1RM → %s %s",
                            rankInfo.kgNeededForNextTier,
                            nextTier.displayName,
                            nextTier.badgeIcon
                        )
                    } else {
                        "¡Rango Máximo Titán! ⚡"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = if (nextTier != null) Color(0xFF9CA3AF) else Color(0xFFFFD700)
                )
            }
        }
    }
}

/**
 * 2. INSIGNIA COMPACTA DE RANGO DE FUERZA POR EJERCICIO (Para Catálogo de Ejercicios y Tarjetas).
 * Muestra la insignia con color (Bronce, Plata, Oro, Platino, Diamante, Titán), el mejor 1RM
 * y cuántos kilos faltan en el 1RM para ascender al siguiente rango.
 */
@Composable
fun StrengthRankBadgeChip(
    rankInfo: StrengthRankInfo,
    modifier: Modifier = Modifier
) {
    val tier = rankInfo.currentTier
    val nextTier = rankInfo.nextTier

    Surface(
        color = tier.primaryColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tier.primaryColor.copy(alpha = 0.55f)),
        modifier = modifier.testTag("strength_rank_badge_${rankInfo.exerciseId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Línea 1: Rango actual a la izquierda y progreso "Faltan +XX kg -> Siguiente" a la derecha sin partirse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tier.badgeIcon} Rango ${tier.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = tier.primaryColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                )

                Text(
                    text = if (nextTier != null) {
                        String.format(
                            Locale.US,
                            "Faltan +%.1f kg → %s",
                            rankInfo.kgNeededForNextTier,
                            nextTier.displayName
                        )
                    } else {
                        "Rango Máximo ⚡"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = nextTier?.primaryColor ?: tier.primaryColor,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Línea 2: Estado de 1RM registrado limpio en su propia línea
            Text(
                text = if (rankInfo.bestOneRepMaxKg > 0.0) {
                    String.format(
                        Locale.US,
                        "1RM estimado: %.1f kg (%.2fx PC)",
                        rankInfo.bestOneRepMaxKg,
                        rankInfo.relativeStrengthRatio
                    )
                } else {
                    "Sin 1RM registrado aún"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            LinearProgressIndicator(
                progress = { rankInfo.progressToNextTier },
                color = tier.primaryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
            )
        }
    }
}

/**
 * 2B. TARJETA DETALLADA DE RANGO DE FUERZA COMPETITIVO (Para Pantalla de Detalle de Ejercicio).
 * Muestra todos los rangos (Bronce, Plata, Oro, Platino, Diamante, Titán), fuerza relativa
 * (1RM vs Peso Corporal) y los kg exactos que faltan para ascender.
 */
@Composable
fun ExerciseStrengthRankCard(
    rankInfo: StrengthRankInfo,
    modifier: Modifier = Modifier
) {
    val tier = rankInfo.currentTier
    val nextTier = rankInfo.nextTier

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141821)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = tier.primaryColor.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exercise_strength_rank_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado con Insignia de Rango Actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tier.primaryColor.copy(alpha = 0.18f))
                            .border(1.5.dp, tier.primaryColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tier.badgeIcon,
                            fontSize = 24.sp
                        )
                    }

                    Column {
                        Text(
                            text = "RANGO DE FUERZA COMPETITIVO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = tier.primaryColor
                        )
                        Text(
                            text = "${tier.displayName} (${String.format(Locale.US, "%.2fx Peso Corporal", rankInfo.relativeStrengthRatio)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "Mejor 1RM: %.1f kg • Peso Corporal: %.1f kg",
                                rankInfo.bestOneRepMaxKg,
                                rankInfo.bodyweightKg
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            // Barra de ascenso al siguiente rango
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (nextTier != null) {
                            String.format(
                                Locale.US,
                                "Faltan +%.1f kg en tu 1RM para ascender a %s %s",
                                rankInfo.kgNeededForNextTier,
                                nextTier.displayName,
                                nextTier.badgeIcon
                            )
                        } else {
                            "¡Has alcanzado el rango supremo TITÁN en este ejercicio! ⚡"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = nextTier?.primaryColor ?: Color(0xFFFFD700)
                    )

                    if (rankInfo.targetOneRepMaxForNextTierKg != null) {
                        Text(
                            text = String.format(Locale.US, "Meta: %.1f kg", rankInfo.targetOneRepMaxForNextTierKg),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { rankInfo.progressToNextTier },
                    color = tier.primaryColor,
                    trackColor = Color(0xFF252D3D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
            }

            // Escalera de los 6 Rangos Competitivos (Bronce -> Plata -> Oro -> Platino -> Diamante -> Titán)
            if (rankInfo.allTierThresholds.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rankInfo.allTierThresholds.forEach { thresholdItem ->
                        val isCurrent = thresholdItem.tier == tier
                        val itemColor = thresholdItem.tier.primaryColor
                        Surface(
                            color = if (isCurrent) itemColor.copy(alpha = 0.24f) else Color(0xFF1D2432),
                            shape = RoundedCornerShape(8.dp),
                            border = if (isCurrent) {
                                androidx.compose.foundation.BorderStroke(1.dp, itemColor)
                            } else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = thresholdItem.tier.badgeIcon,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = thresholdItem.tier.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isCurrent || thresholdItem.isUnlocked) itemColor else Color(0xFF6B7280),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (thresholdItem.required1RmKg <= 0.0) "Base" else String.format(Locale.US, "%.0fkg", thresholdItem.required1RmKg),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = if (isCurrent) Color.White else Color(0xFF9CA3AF),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. MINI MAPA MUSCULAR ANATÓMICO (Frontal + Posterior) para la Tarjeta Resumen de Fin de Entreno.
 * Ilumina los músculos trabajados hoy sobre siluetas anatómicas en modo oscuro coleccionable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiniWorkoutMuscleMap(
    workedMuscles: Set<HypertrophyMuscle>,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF00E676)
    val inactiveColor = Color(0xFF283142)
    val silhouetteStroke = Color(0xFF3E4C63)

    Surface(
        color = Color(0xFF111622),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF253047)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("workout_share_card_mini_muscle_map")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MAPA MUSCULAR TRABAJADO HOY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E676)
                )
                Text(
                    text = "${workedMuscles.size} grupos activados",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Siluetas Frontal y Posterior lado a lado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vista Frontal Mini
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(95.dp)
                            .height(122.dp)
                    ) {
                        drawMiniAnatomicalSilhouette(
                            isFront = true,
                            workedMuscles = workedMuscles,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            strokeColor = silhouetteStroke
                        )
                    }
                    Text(
                        text = "FRONTAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vista Posterior Mini
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(95.dp)
                            .height(122.dp)
                    ) {
                        drawMiniAnatomicalSilhouette(
                            isFront = false,
                            workedMuscles = workedMuscles,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            strokeColor = silhouetteStroke
                        )
                    }
                    Text(
                        text = "POSTERIOR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (workedMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workedMuscles.forEach { muscle ->
                        Surface(
                            color = activeColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "🔥 ${muscle.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completa al menos una serie para iluminar los músculos trabajados",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun DrawScope.drawMiniAnatomicalSilhouette(
    isFront: Boolean,
    workedMuscles: Set<HypertrophyMuscle>,
    activeColor: Color,
    inactiveColor: Color,
    strokeColor: Color
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    fun colorFor(muscle: HypertrophyMuscle): Color {
        return if (workedMuscles.contains(muscle)) activeColor else inactiveColor
    }

    // Cabeza
    drawCircle(
        color = inactiveColor,
        radius = w * 0.09f,
        center = Offset(cx, h * 0.09f)
    )
    drawCircle(
        color = strokeColor,
        radius = w * 0.09f,
        center = Offset(cx, h * 0.09f),
        style = Stroke(width = 1.5f)
    )

    // Deltoides / Hombros (Frontal y Posterior)
    val shoulderColor = colorFor(HypertrophyMuscle.HOMBROS)
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx - w * 0.30f, h * 0.18f),
        size = Size(w * 0.12f, h * 0.10f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx + w * 0.18f, h * 0.18f),
        size = Size(w * 0.12f, h * 0.10f),
        cornerRadius = CornerRadius(8f, 8f)
    )

    if (isFront) {
        // Pectoral
        val chestColor = colorFor(HypertrophyMuscle.PECHO)
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx - w * 0.17f, h * 0.19f),
            size = Size(w * 0.16f, h * 0.11f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx + w * 0.01f, h * 0.19f),
            size = Size(w * 0.16f, h * 0.11f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Abdomen
        val absColor = colorFor(HypertrophyMuscle.ABDOMEN)
        drawRoundRect(
            color = absColor,
            topLeft = Offset(cx - w * 0.12f, h * 0.31f),
            size = Size(w * 0.24f, h * 0.18f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Bíceps
        val bicepsColor = colorFor(HypertrophyMuscle.BICEPS)
        drawRoundRect(
            color = bicepsColor,
            topLeft = Offset(cx - w * 0.31f, h * 0.29f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = bicepsColor,
            topLeft = Offset(cx + w * 0.21f, h * 0.29f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Cuádriceps
        val quadsColor = colorFor(HypertrophyMuscle.CUADRICEPS)
        drawRoundRect(
            color = quadsColor,
            topLeft = Offset(cx - w * 0.18f, h * 0.51f),
            size = Size(w * 0.15f, h * 0.24f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = quadsColor,
            topLeft = Offset(cx + w * 0.03f, h * 0.51f),
            size = Size(w * 0.15f, h * 0.24f),
            cornerRadius = CornerRadius(8f, 8f)
        )
    } else {
        // Dorsales / Espalda
        val backColor = colorFor(HypertrophyMuscle.ESPALDA)
        val backPath = Path().apply {
            moveTo(cx - w * 0.18f, h * 0.19f)
            lineTo(cx + w * 0.18f, h * 0.19f)
            lineTo(cx + w * 0.12f, h * 0.42f)
            lineTo(cx - w * 0.12f, h * 0.42f)
            close()
        }
        drawPath(path = backPath, color = backColor)

        // Tríceps
        val tricepsColor = colorFor(HypertrophyMuscle.TRICEPS)
        drawRoundRect(
            color = tricepsColor,
            topLeft = Offset(cx - w * 0.31f, h * 0.29f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = tricepsColor,
            topLeft = Offset(cx + w * 0.21f, h * 0.29f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Glúteos
        val glutesColor = colorFor(HypertrophyMuscle.GLUTEOS)
        drawRoundRect(
            color = glutesColor,
            topLeft = Offset(cx - w * 0.17f, h * 0.43f),
            size = Size(w * 0.16f, h * 0.10f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = glutesColor,
            topLeft = Offset(cx + w * 0.01f, h * 0.43f),
            size = Size(w * 0.16f, h * 0.10f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Isquiosurales
        val hamsColor = colorFor(HypertrophyMuscle.ISQUIOTIBIALES)
        drawRoundRect(
            color = hamsColor,
            topLeft = Offset(cx - w * 0.17f, h * 0.54f),
            size = Size(w * 0.14f, h * 0.21f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = hamsColor,
            topLeft = Offset(cx + w * 0.03f, h * 0.54f),
            size = Size(w * 0.14f, h * 0.21f),
            cornerRadius = CornerRadius(8f, 8f)
        )
    }

    // Gemelos (Frontal y Posterior)
    val calvesColor = colorFor(HypertrophyMuscle.GEMELOS)
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(cx - w * 0.16f, h * 0.77f),
        size = Size(w * 0.12f, h * 0.18f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = calvesColor,
        topLeft = Offset(cx + w * 0.04f, h * 0.77f),
        size = Size(w * 0.12f, h * 0.18f),
        cornerRadius = CornerRadius(6f, 6f)
    )
}
