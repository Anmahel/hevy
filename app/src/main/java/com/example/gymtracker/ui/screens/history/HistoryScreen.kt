package com.example.gymtracker.ui.screens.history

import com.example.ui.theme.safeHeaderTopPadding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.local.entity.ExerciseEntity
import com.example.gymtracker.data.local.entity.WorkoutSetEntity
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.ui.components.media.ExerciseThumbnail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import com.example.gymtracker.ui.components.WorkoutSummaryDialog
import com.example.gymtracker.ui.screens.calendar.CalendarDaysGrid
import com.example.gymtracker.ui.screens.calendar.DaysOfWeekHeader
import com.example.gymtracker.ui.screens.calendar.MonthSelectorHeader
import com.example.gymtracker.ui.screens.calendar.RestDayRecoveryCard
import com.example.gymtracker.ui.screens.calendar.StreakHeroCard
import com.example.gymtracker.ui.screens.calendar.WorkoutCalendarViewModel

import com.example.gymtracker.ui.components.GlobalTopHeader
import com.example.gymtracker.ui.screens.stats.PerformanceStatsBannerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    calendarViewModel: WorkoutCalendarViewModel? = null,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToExerciseDetail: (Long) -> Unit = {},
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
    val calendarUiState = calendarViewModel?.uiState?.collectAsStateWithLifecycle()?.value
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            GlobalTopHeader(
                title = "Almanaque & Racha",
                waterGlassesToday = waterGlassesToday,
                isWaterGoalMetToday = isWaterGoalMetToday,
                creatineTakenToday = creatineTakenToday,
                athleteLevel = athleteLevel,
                onQuickAddWaterGlass = onQuickAddWaterGlass,
                onQuickTakeCreatine = onQuickTakeCreatine,
                onOpenRpgProfile = onOpenRpgProfile,
                onNavigateToBackup = onNavigateToBackup,
                modifier = Modifier.testTag("calendar_top_bar")
            )
        },
        modifier = modifier.testTag("history_screen")
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
                    .padding(horizontal = 16.dp)
                    .testTag("workout_calendar_content"),
                contentPadding = PaddingValues(bottom = 112.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Acceso directo a Estadísticas de Rendimiento (Gráfica Recharts 1RM Top 5 - 6 Meses)
                item(key = "performance_stats_banner_history") {
                    PerformanceStatsBannerCard(
                        onClick = onNavigateToPerformanceStats,
                        testTag = "open_performance_stats_from_history"
                    )
                }

                // 1. Tarjeta Superior de Racha de Hipertrofia + Calendario Mensual Interactivo
                if (calendarUiState != null && calendarViewModel != null) {
                    item(key = "streak_card") {
                        StreakHeroCard(
                            streakInfo = calendarUiState.streakInfo
                        )
                    }

                    item(key = "calendar_grid_card") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = com.example.ui.theme.GymGlassSurface
                            ),
                            border = com.example.ui.theme.CarbonGlassBorderStroke,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        MonthSelectorHeader(
                                            monthTitle = calendarUiState.currentMonthTitle,
                                            onPreviousMonth = { calendarViewModel.onPreviousMonth() },
                                            onNextMonth = { calendarViewModel.onNextMonth() }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = { calendarViewModel.onGoToToday() },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("calendar_today_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Today,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Hoy",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                DaysOfWeekHeader()

                                Spacer(modifier = Modifier.height(8.dp))

                                CalendarDaysGrid(
                                    days = calendarUiState.days,
                                    onDayClick = { calendarViewModel.onSelectDay(it) }
                                )
                            }
                        }
                    }

                    // 2. Encabezado del Día Seleccionado (ej. "Sábado, 17 de octubre")
                    item(key = "selected_day_header") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = calendarUiState.selectedDayFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (calendarUiState.selectedDayWorkouts.isNotEmpty()) {
                                Text(
                                    text = "${calendarUiState.selectedDayWorkouts.size} entreno(s) en este día",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.ui.theme.GymGreenLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 3. Tarjetas detalladas completas del día seleccionado o Día de Recuperación Muscular
                    if (calendarUiState.selectedDayWorkouts.isNotEmpty()) {
                        items(
                            items = calendarUiState.selectedDayWorkouts,
                            key = { "selected_day_${it.session.id}" }
                        ) { detail ->
                            val isExpanded = uiState.expandedSessionIds.contains(detail.session.id)
                            HistorySessionCard(
                                sessionWithDetails = detail,
                                isExpanded = isExpanded,
                                onToggleExpand = { viewModel.toggleSessionExpanded(detail.session.id) },
                                onOpenShareCard = { viewModel.openShareCardForSession(detail) },
                                onDeleteClick = { sessionToDelete = detail.session.id }
                            )
                        }
                    } else {
                        item(key = "rest_day_card") {
                            RestDayRecoveryCard()
                        }
                    }
                } else if (uiState.sessions.isEmpty()) {
                    item(key = "empty_history_card_item") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GymGlassSurface),
                            border = com.example.ui.theme.CarbonGlassBorderStroke,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_history_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Sin entrenamientos completados",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Cuando finalices un entrenamiento, se guardará aquí con el detalle completo de tus series y pesos levantados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.sessions,
                        key = { "all_history_${it.session.id}" }
                    ) { item ->
                        val isExpanded = uiState.expandedSessionIds.contains(item.session.id)
                        HistorySessionCard(
                            sessionWithDetails = item,
                            isExpanded = isExpanded,
                            onToggleExpand = { viewModel.toggleSessionExpanded(item.session.id) },
                            onOpenShareCard = { viewModel.openShareCardForSession(item) },
                            onDeleteClick = { sessionToDelete = item.session.id }
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Modal de Tarjeta Resumen Coleccionable (Workout Share Card) desde Historial
    uiState.selectedShareCardSummary?.let { shareSummary ->
        WorkoutSummaryDialog(
            summary = shareSummary,
            onSaveAndFinish = { viewModel.dismissShareCard() },
            onDismiss = { viewModel.dismissShareCard() },
            readOnlyShareMode = true
        )
    }

    // Diálogo de confirmación para eliminar sesión
    sessionToDelete?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Eliminar entrenamiento?") },
            text = { Text("Se eliminará este entrenamiento y todas sus series registradas de tu historial local.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(sessionId)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sessionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta individual del historial con métricas y desglose plegable de ejercicios.
 */
@Composable
fun HistorySessionCard(
    sessionWithDetails: WorkoutSessionWithDetails,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenShareCard: () -> Unit = {},
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = sessionWithDetails.session
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM • HH:mm", Locale("es", "ES")) }
    val formattedDate = remember(session.startTime) {
        dateFormat.format(Date(session.startTime)).replaceFirstChar { it.uppercase() }
    }

    val formattedDuration = remember(session.durationSeconds) {
        val hours = session.durationSeconds / 3600
        val minutes = (session.durationSeconds % 3600) / 60
        val seconds = session.durationSeconds % 60
        if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %02dm", hours, minutes)
        } else if (minutes > 0) {
            String.format(Locale.getDefault(), "%d min", minutes)
        } else {
            String.format(Locale.getDefault(), "%d s", seconds)
        }
    }

    val totalCompletedSets = remember(sessionWithDetails.sets) {
        sessionWithDetails.sets.count { it.set.isCompleted }
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.GymGlassSurface),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${session.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Fecha, Botón Tarjeta Resumen y Botón de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = com.example.ui.theme.GymGreenPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onOpenShareCard() }
                            .testTag("open_share_card_${session.id}")
                    ) {
                        Text(
                            text = "🃏 Tarjeta",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = com.example.ui.theme.GymGreenLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_workout_${session.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar entrenamiento",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Título de la sesión
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Fila de Métricas Clave (DURACIÓN, VOLUMEN, SERIES) con tipografía técnica y tabular-nums
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(com.example.ui.theme.GymInputBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duración
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = com.example.ui.theme.GymTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "DURACIÓN",
                            style = com.example.ui.theme.TechnicalLabelStyle
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDuration,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black
                    )
                }

                // Volumen Total
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = com.example.ui.theme.GymGreenLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "VOLUMEN",
                            style = com.example.ui.theme.TechnicalLabelStyle
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f kg", session.totalVolumeKg),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.theme.GymGreenLight
                    )
                }

                // Series Completadas
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = com.example.ui.theme.GymCyanSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "SERIES",
                            style = com.example.ui.theme.TechnicalLabelStyle
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalCompletedSets",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de acción para Plegar / Desplegar desglose
            val exercisesCount = sessionWithDetails.setsGroupedByExercise.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleExpand() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("expand_workout_${session.id}"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Ocultar desglose" else "Ver desglose de ejercicios ($exercisesCount)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Plegar" else "Desplegar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            // Desglose Plegable Animado de Ejercicios y Pesos
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    sessionWithDetails.setsGroupedByExercise.forEach { (exercise, sets) ->
                        ExerciseDetailSection(exercise = exercise, sets = sets)
                    }
                }
            }
        }
    }
}

/**
 * Sección de cada ejercicio dentro del desglose plegable.
 */
@Composable
private fun ExerciseDetailSection(
    exercise: ExerciseEntity,
    sets: List<WorkoutSetEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Nombre del ejercicio y grupo muscular
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExerciseThumbnail(
                        imageSource = exercise.thumbnailUrl.ifEmpty { exercise.gifPathOrUrl },
                        size = 36.dp
                    )
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = exercise.muscleGroup,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encabezado de la tabla de series
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SERIE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    text = "TIPO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(60.dp)
                )
                Text(
                    text = "KG × REPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "ESTADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(60.dp)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Filas de series
            sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${set.setNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(50.dp)
                    )
                    Text(
                        text = set.setType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        text = "${set.weightKg} kg × ${set.reps}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier.width(60.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (set.isCompleted) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completada",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Hecho",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
