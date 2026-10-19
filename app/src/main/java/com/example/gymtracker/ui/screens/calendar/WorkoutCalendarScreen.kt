package com.example.gymtracker.ui.screens.calendar

import com.example.ui.theme.safeHeaderTopPadding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.local.relation.WorkoutSessionWithDetails
import com.example.gymtracker.domain.util.StreakInfo
import java.util.Locale

val FireOrange = Color(0xFFFF5722)
val GoldYellow = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCalendarScreen(
    viewModel: WorkoutCalendarViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.GymStickyHeaderBg)
                    .safeHeaderTopPadding()
                    .testTag("calendar_top_bar")
            ) {
                CenterAlignedTopAppBar(
                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = com.example.ui.theme.GymAmberFlame,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Almanaque & Racha",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = { viewModel.onGoToToday() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("calendar_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Hoy", style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                androidx.compose.material3.HorizontalDivider(
                    thickness = 1.dp,
                    color = com.example.ui.theme.GymGlassBorder
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("workout_calendar_content")
        ) {
            // 1. Tarjeta Superior de Racha Inteligente
            item(key = "streak_card") {
                StreakHeroCard(
                    streakInfo = uiState.streakInfo
                )
            }

            // 2. Selector de Mes y Cuadrícula
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
                        // Selector de Mes
                        MonthSelectorHeader(
                            monthTitle = uiState.currentMonthTitle,
                            onPreviousMonth = { viewModel.onPreviousMonth() },
                            onNextMonth = { viewModel.onNextMonth() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Encabezados de los 7 días de la semana
                        DaysOfWeekHeader()

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cuadrícula de días
                        CalendarDaysGrid(
                            days = uiState.days,
                            onDayClick = { viewModel.onSelectDay(it) }
                        )
                    }
                }
            }

            // 3. Encabezado del Día Seleccionado
            item(key = "selected_day_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = uiState.selectedDayFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 4. Detalle de Entrenamientos o Recuperación
            if (uiState.selectedDayWorkouts.isNotEmpty()) {
                items(
                    items = uiState.selectedDayWorkouts,
                    key = { it.session.id }
                ) { detail ->
                    WorkoutSessionDetailCard(detail = detail)
                }
            } else {
                item(key = "rest_day_card") {
                    RestDayRecoveryCard()
                }
            }
        }
    }
}

/**
 * Tarjeta superior que destaca la racha con icono de fuego y mensaje motivacional de descanso.
 */
@Composable
fun StreakHeroCard(
    streakInfo: StreakInfo,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = BorderStroke(1.dp, com.example.ui.theme.GymAccentOrange.copy(alpha = 0.40f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_hero_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(com.example.ui.theme.FireAmberGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Racha de entrenamientos",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Racha de Hipertrofia",
                            style = MaterialTheme.typography.labelMedium,
                            color = FireOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (streakInfo.currentStreakDays > 0) {
                                "${streakInfo.currentStreakDays} Días Activa"
                            } else {
                                "Racha sin iniciar"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FireOrange.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (streakInfo.isRestDayToday) "Descanso" else "Entrenado",
                        color = FireOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Contadores Clave
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StreakStatPill(
                    label = "Días activos",
                    value = "${streakInfo.activeDaysCount}"
                )
                StreakStatPill(
                    label = "Semanas consecutivas",
                    value = "${streakInfo.consecutiveWeeksCompleted} sem"
                )
                StreakStatPill(
                    label = "Entrenos este mes",
                    value = "${streakInfo.workoutsThisMonth}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mensaje motivacional de recuperación
            Text(
                text = streakInfo.motivationalMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun StreakStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cabecera con selector de mes (Anterior, Mes actual, Siguiente).
 */
@Composable
fun MonthSelectorHeader(
    monthTitle: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.testTag("btn_prev_month")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mes anterior"
            )
        }

        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.testTag("btn_next_month")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mes siguiente"
            )
        }
    }
}

/**
 * Encabezados de los 7 días de la semana (Lunes a Domingo).
 */
@Composable
fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    val weekDays = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        weekDays.forEach { dayName ->
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Renderizado de las celdas del calendario organizadas en filas de 7.
 */
@Composable
fun CalendarDaysGrid(
    days: List<CalendarDayUi>,
    onDayClick: (CalendarDayUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        days.chunked(7).forEach { weekChunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                weekChunk.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        CalendarDayCell(
                            day = day,
                            onClick = { onDayClick(day) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Celda individual de día en el calendario:
 * - Entrenamiento completado: círculo resaltado en color primario con icono de mancuerna.
 * - Día de descanso: tono neutro sutil con etiqueta "Descanso".
 * - Hoy: borde resaltado.
 */
@Composable
fun CalendarDayCell(
    day: CalendarDayUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = day.isCurrentMonth
    val baseBorder = if (day.isToday) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else if (day.isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
    } else null

    val backgroundColor = when {
        day.hasWorkout -> MaterialTheme.colorScheme.primaryContainer
        day.isSelected -> MaterialTheme.colorScheme.surfaceVariant
        day.isPlannedRestDay -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val contentColor = when {
        day.hasWorkout -> MaterialTheme.colorScheme.onPrimaryContainer
        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = baseBorder,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("calendar_day_${day.dayNumber}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${day.dayNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.hasWorkout || day.isToday || day.isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                color = contentColor
            )

            if (day.hasWorkout) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Entrenamiento realizado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(10.dp)
                )
            } else if (day.isPlannedRestDay) {
                Text(
                    text = "Descanso",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Tarjeta de detalle de un entrenamiento realizado en la fecha seleccionada.
 */
@Composable
fun WorkoutSessionDetailCard(
    detail: WorkoutSessionWithDetails,
    modifier: Modifier = Modifier
) {
    val durationMin = (detail.session.durationSeconds / 60).coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("workout_session_card_${detail.session.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = detail.session.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${detail.sets.size} series registradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pills con Volumen y Duración
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.1f kg volumen".format(Locale.US, detail.session.totalVolumeKg),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$durationMin min",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta informativa que se presenta al pulsar en un día de descanso planificado.
 */
@Composable
fun RestDayRecoveryCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("rest_day_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Recuperación muscular",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Día de Recuperación Muscular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sin entrenamientos en esta fecha. Los días de descanso son obligatorios para la síntesis de proteína miofibrilar, reposición de glucógeno y crecimiento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
