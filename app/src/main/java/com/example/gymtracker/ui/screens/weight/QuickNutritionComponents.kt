package com.example.gymtracker.ui.screens.weight

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtracker.domain.util.QuickMealBlock
import java.util.Locale

private val HydrationBlue = Color(0xFF0EA5E9)
private val CreatineEmerald = Color(0xFF10B981)
private val CreatinePendingRed = Color(0xFFEF4444)

/**
 * 1. BARRA DIARIA DE MACROS CONECTADA AL PESO ACTUAL ("Consumido hoy vs. Meta")
 */
@Composable
fun DailyMacroProgressCard(
    dailyState: DailyNutritionUiState,
    onOpenMacroDetails: () -> Unit,
    onResetDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val macros = dailyState.targetMacros
    val kcalProgress by animateFloatAsState(
        targetValue = if (macros.totalCalories > 0) {
            (dailyState.consumedCalories.toFloat() / macros.totalCalories.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        label = "kcalProgress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_macro_progress_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: Título + Peso conectado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Consumido Hoy vs. Meta Diaria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (dailyState.isUsingFallbackWeight) {
                            "Meta base (75.0 kg) • Registra tu peso para ajustar"
                        } else {
                            "Meta conectada a tu último peso: %.1f kg".format(Locale.US, dailyState.referenceWeightKg)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (dailyState.consumedCalories > 0) {
                    TextButton(
                        onClick = onResetDay,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Reiniciar hoy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Barra Principal de Calorías del Día
            Surface(
                color = CalorieAccentColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMacroDetails() }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = CalorieAccentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calorías de Hipertrofia",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${dailyState.consumedCalories} / ${macros.totalCalories} kcal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = CalorieAccentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Barra estilizada con degradado Naranja/Ámbar Fuego y punta brillante
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(com.example.ui.theme.GymInputBg)
                    ) {
                        if (kcalProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(kcalProgress.coerceAtLeast(0.03f))
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(com.example.ui.theme.FireAmberGradient),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .padding(end = 1.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.85f))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val remainingKcal = (macros.totalCalories - dailyState.consumedCalories).coerceAtLeast(0)
                    Text(
                        text = if (remainingKcal > 0) {
                            "Restan $remainingKcal kcal para completar tu superávit limpio de hoy"
                        } else {
                            "¡Meta calórica de hipertrofia alcanzada hoy! ✅"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Barras individuales de Proteína, Carbohidratos y Grasas (Consumido hoy vs. Meta)
            MacroProgressRowItem(
                label = "Proteína (Síntesis muscular)",
                consumedGrams = dailyState.consumedProteinGrams,
                targetGrams = macros.proteinTargetGrams,
                color = ProteinColor,
                testTag = "macro_bar_protein"
            )
            MacroProgressRowItem(
                label = "Carbohidratos (Energía y glucógeno)",
                consumedGrams = dailyState.consumedCarbsGrams,
                targetGrams = macros.carbsGrams,
                color = CarbsColor,
                testTag = "macro_bar_carbs"
            )
            MacroProgressRowItem(
                label = "Grasas Saludables (Hormonal)",
                consumedGrams = dailyState.consumedFatsGrams,
                targetGrams = macros.fatsGrams,
                color = FatsColor,
                testTag = "macro_bar_fats"
            )
        }
    }
}

@Composable
private fun MacroProgressRowItem(
    label: String,
    consumedGrams: Float,
    targetGrams: Float,
    color: Color,
    testTag: String
) {
    val progress by animateFloatAsState(
        targetValue = if (targetGrams > 0f) (consumedGrams / targetGrams).coerceIn(0f, 1f) else 0f,
        label = testTag
    )
    val pct = if (targetGrams > 0f) ((consumedGrams / targetGrams) * 100f).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
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
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Text(
                text = "%.1fg / %.0fg (%d%%)".format(Locale.US, consumedGrams, targetGrams, pct),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(com.example.ui.theme.GymInputBg)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceAtLeast(0.03f))
                        .height(9.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(color.copy(alpha = 0.80f), color)
                            )
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .padding(end = 1.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.88f))
                    )
                }
            }
        }
    }
}

/**
 * 2. BOTONES RÁPIDOS DE COMIDAS HABITUALES (1-Tap Meal Tracker)
 */
@Composable
fun OneTapMealTrackerCard(
    dailyState: DailyNutritionUiState,
    onQuickAddMeal: (QuickMealBlock) -> Unit,
    onUndoLastTap: () -> Unit,
    onOpenCustomBlockDialog: () -> Unit,
    onDeleteCustomBlock: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .testTag("one_tap_meal_tracker_card")
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
                            .size(38.dp)
                            .background(ProteinColor.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = ProteinColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Registro Rápido 1-Tap (Comidas)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca cualquier bloque para sumar macros al instante",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (dailyState.canUndoLastTap) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
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
                                text = "Último toque: ${dailyState.lastTapLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${dailyState.tapHistory.size} bloque(s) sumados hoy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = onUndoLastTap,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("undo_last_meal_tap_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Deshacer último toque",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Deshacer",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dailyState.mealBlocks.forEach { block ->
                    QuickMealTapButton(
                        block = block,
                        onTap = { onQuickAddMeal(block) },
                        onDeleteCustom = block.customEntityId?.let { id -> { onDeleteCustomBlock(id) } }
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenCustomBlockDialog,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("create_custom_meal_block_button")
            ) {
                Text(
                    text = "+ Crear bloque personalizado",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun QuickMealTapButton(
    block: QuickMealBlock,
    onTap: () -> Unit,
    onDeleteCustom: (() -> Unit)?
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ProteinColor.copy(alpha = 0.30f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onTap() }
            .testTag("quick_meal_button_${block.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val protText = formatMacroGram(block.proteinGrams)
                val carbText = formatMacroGram(block.carbsGrams)
                val fatText = formatMacroGram(block.fatsGrams)
                Text(
                    text = "${protText}g Prot | ${carbText}g Carb | ${fatText}g Grasas | ${block.calories} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ProteinColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${formatMacroGram(block.proteinGrams)}g P",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProteinColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (onDeleteCustom != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDeleteCustom,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar bloque personalizado",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMacroGram(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        "%.1f".format(Locale.US, value)
    }
}

/**
 * Vista de pestaña dedicada para "Suplementación Diaria e Hidratación" unificada.
 */
@Composable
fun UnifiedHydrationAndCreatineTabContent(
    dailyState: DailyNutritionUiState,
    onTakeCreatine: () -> Unit,
    onAddWaterMl: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectCalendarDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("hydration_creatine_tab_list")
    ) {
        item(key = "unified_hydration_creatine_card") {
            CreatineAndHydrationChecklistCard(
                dailyState = dailyState,
                onToggleCreatine = { onTakeCreatine() },
                onAddWaterMl = onAddWaterMl,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectCalendarDate = onSelectCalendarDate
            )
        }
    }
}

/**
 * 3. VISTA UNIFICADA DE SUPLEMENTACIÓN DIARIA E HIDRATACIÓN:
 * A) Tarjetas de Racha (Racha de Hidratación y Racha de Creatina) + Calendario Mensual (< y >)
 *    con indicadores por día (💧/💦 y ✅) y detalle al tocar cualquier fecha.
 * B) Botón de Creatina Diaria (5g) en ROJO cuando está pendiente y en VERDE bloqueado (enabled = false)
 *    por el resto del día tras tocarlo.
 * C) Panel Exclusivo de Hidratación con botones rápidos (+250 ml, +350 ml, +420 ml, +500 ml, +750 ml, -250 ml)
 *    y campo numérico manual (OutlinedTextField + botón "+ Sumar ml").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatineAndHydrationChecklistCard(
    dailyState: DailyNutritionUiState,
    onToggleCreatine: (Boolean) -> Unit,
    onAddWaterMl: (Int) -> Unit,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSelectCalendarDate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val waterProgress by animateFloatAsState(
        targetValue = if (dailyState.waterGoalMl > 0) {
            (dailyState.waterConsumedMl.toFloat() / dailyState.waterGoalMl.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        label = "waterProgress"
    )
    val glassesCount = dailyState.waterConsumedMl / 250
    val goalGlasses = (dailyState.waterGoalMl + 249) / 250
    val isWaterGoalMet = dailyState.isWaterGoalMetToday

    var customWaterInput by remember { mutableStateOf("") }
    var customWaterError by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .testTag("creatine_hydration_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Unificada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Suplementación Diaria e Hidratación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calendario mensual, rachas, creatina 5g y control de agua",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = if (isWaterGoalMet) HydrationBlue.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isWaterGoalMet) HydrationBlue else com.example.ui.theme.GymGlassBorder
                    )
                ) {
                    Text(
                        text = if (isWaterGoalMet) "💦 Meta OK" else "💧 $glassesCount/$goalGlasses v",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWaterGoalMet) HydrationBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // =========================================================================
            // A) TARJETAS DE RACHA SUPERIORES (Racha de Hidratación y Racha de Creatina)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hydration_creatine_streaks_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tarjeta 1: Racha de Hidratación
                Surface(
                    color = HydrationBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, HydrationBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hydration_streak_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Racha de Hidratación",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HydrationBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isWaterGoalMet) "💦" else "💧",
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${dailyState.hydrationStreakDays} días",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = "${dailyState.totalHydrationDaysMet} día(s) cumplidos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Tarjeta 2: Racha de Creatina
                Surface(
                    color = CreatineEmerald.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CreatineEmerald.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("creatine_streak_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Racha de Creatina",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CreatineEmerald,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${dailyState.creatineStreakDays} días",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = "${dailyState.totalCreatineDaysMet} día(s) cumplidos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // =========================================================================
            // CALENDARIO MENSUAL DE HIDRATACIÓN Y CREATINA CON NAVEGACIÓN (< y >)
            // =========================================================================
            if (dailyState.calendarCells.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, com.example.ui.theme.GymGlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hydration_creatine_calendar_container")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Barra de mes con flechas (< y >)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onPreviousMonth,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .testTag("nutrition_cal_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Mes anterior",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dailyState.calendarMonthTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "💦 Meta agua  •  💧 Parcial  •  ✅ Creatina 5g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }

                            IconButton(
                                onClick = onNextMonth,
                                enabled = dailyState.canGoNextMonth,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (dailyState.canGoNextMonth) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                                    )
                                    .testTag("nutrition_cal_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Mes siguiente",
                                    tint = if (dailyState.canGoNextMonth) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // Cabecera de días de la semana
                        val weekDays = listOf("L", "M", "X", "J", "V", "S", "D")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekDays.forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Cuadrícula de días del mes
                        val rows = dailyState.calendarCells.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            rows.forEach { weekRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (i in 0 until 7) {
                                        val cell = weekRow.getOrNull(i)
                                        if (cell == null) {
                                            Spacer(modifier = Modifier.weight(1f).height(44.dp))
                                        } else {
                                            val bgColor = when {
                                                cell.isSelected -> HydrationBlue.copy(alpha = 0.24f)
                                                cell.isHydrationGoalMet && cell.creatineTaken -> CreatineEmerald.copy(alpha = 0.16f)
                                                cell.isHydrationGoalMet -> HydrationBlue.copy(alpha = 0.14f)
                                                cell.creatineTaken -> CreatineEmerald.copy(alpha = 0.12f)
                                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                                            }
                                            val borderColor = when {
                                                cell.isSelected -> HydrationBlue
                                                cell.isToday -> MaterialTheme.colorScheme.primary
                                                cell.isHydrationGoalMet && cell.creatineTaken -> CreatineEmerald.copy(alpha = 0.6f)
                                                else -> Color.Transparent
                                            }

                                            Surface(
                                                color = bgColor,
                                                shape = RoundedCornerShape(10.dp),
                                                border = if (borderColor != Color.Transparent) {
                                                    BorderStroke(1.2.dp, borderColor)
                                                } else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onSelectCalendarDate(cell.dateKey) }
                                                    .testTag("nutrition_cal_day_${cell.dateKey}")
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(vertical = 3.dp, horizontal = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = cell.dayOfMonth.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (cell.isToday || cell.isSelected) FontWeight.Black else FontWeight.SemiBold,
                                                        color = if (cell.isFuture) {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                        fontSize = 11.sp
                                                    )

                                                    // Indicadores visuales de Agua (💧/💦) y Creatina (✅)
                                                    val waterIcon = when {
                                                        cell.isHydrationGoalMet -> "💦"
                                                        cell.hasAnyWater -> "💧"
                                                        else -> ""
                                                    }
                                                    val creatineIcon = if (cell.creatineTaken) "✅" else ""
                                                    val indicators = "$waterIcon$creatineIcon"

                                                    Text(
                                                        text = indicators.ifEmpty { "·" },
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Detalle del día seleccionado en el calendario
                        dailyState.selectedDayDetail?.let { sel ->
                            val selGlasses = sel.waterMl / 250
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, HydrationBlue.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nutrition_calendar_selected_day_detail")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Detalle: ${sel.displayDateFormatted}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (sel.isHydrationGoalMet) {
                                                "💦 Agua: ${sel.waterMl} / ${sel.waterGoalMl} ml ($selGlasses vasos • Meta cumplida)"
                                            } else {
                                                "💧 Agua: ${sel.waterMl} / ${sel.waterGoalMl} ml ($selGlasses vasos)"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (sel.isHydrationGoalMet) HydrationBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (sel.creatineTaken) {
                                                "✅ Creatina 5g: Tomada"
                                            } else {
                                                "🔴 Creatina 5g: No registrada"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (sel.creatineTaken) CreatineEmerald else CreatinePendingRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 1. BOTÓN DE CREATINA DIARIA (5g) CON BLOQUEO POR DÍA (ROJO -> VERDE)
            // =========================================================================
            val isCreatineTaken = dailyState.creatineTaken
            val creatineAccent = if (isCreatineTaken) CreatineEmerald else CreatinePendingRed

            Surface(
                color = creatineAccent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, creatineAccent.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("creatine_toggle_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                .size(38.dp)
                                .background(creatineAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCreatineTaken) Icons.Default.CheckCircle else Icons.Default.Science,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isCreatineTaken) {
                                    "Creatina diaria tomada (5g) ✅"
                                } else {
                                    "Creatina diaria pendiente (5g)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = creatineAccent
                            )
                            Text(
                                text = if (isCreatineTaken) {
                                    "Registrada hoy • Bloqueada hasta mañana"
                                } else {
                                    "Pendiente hoy • Toca el botón para registrar tus 5g"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { onToggleCreatine(true) },
                        enabled = !isCreatineTaken,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CreatinePendingRed,
                            contentColor = Color.White,
                            disabledContainerColor = CreatineEmerald,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("creatine_daily_switch")
                    ) {
                        Text(
                            text = if (isCreatineTaken) "✅ Tomada" else "Tomar 5g",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // =========================================================================
            // B) PANEL EXCLUSIVO DE HIDRATACIÓN CON NUEVOS BOTONES Y CAMPO MANUAL
            // =========================================================================
            Surface(
                color = HydrationBlue.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = if (isWaterGoalMet) 1.5.dp else 1.dp,
                    color = if (isWaterGoalMet) HydrationBlue else HydrationBlue.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Text(
                                text = if (isWaterGoalMet) "💦" else "💧",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isWaterGoalMet) {
                                        "Hidratación Diaria ¡Meta Cumplida!"
                                    } else {
                                        "Panel Exclusivo de Hidratación"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Meta sugerida (${"%.1f".format(Locale.US, dailyState.referenceWeightKg)} kg): ${dailyState.waterGoalMl} ml (~$goalGlasses vasos)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${dailyState.waterConsumedMl} / ${dailyState.waterGoalMl} ml",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = HydrationBlue,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Barra de Hidratación con Degradado Cian Eléctrico y brillo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(com.example.ui.theme.GymInputBg)
                    ) {
                        if (waterProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(waterProgress.coerceAtLeast(0.03f))
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(com.example.ui.theme.ElectricCyanGradient),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .padding(end = 1.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.90f))
                                )
                            }
                        }
                    }

                    Text(
                        text = "Equivalente actual: $glassesCount vaso(s) de 250 ml",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = HydrationBlue
                    )

                    // Botones de toque rápido: +250 ml, +350 ml, +420 ml, +500 ml, +750 ml y -250 ml
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WaterQuickActionChip(
                            label = "+250 ml",
                            onClick = { onAddWaterMl(250) },
                            testTag = "add_water_250_button"
                        )
                        WaterQuickActionChip(
                            label = "+350 ml",
                            onClick = { onAddWaterMl(350) },
                            testTag = "add_water_350_button"
                        )
                        WaterQuickActionChip(
                            label = "+420 ml",
                            onClick = { onAddWaterMl(420) },
                            testTag = "add_water_420_button"
                        )
                        WaterQuickActionChip(
                            label = "+500 ml",
                            onClick = { onAddWaterMl(500) },
                            testTag = "add_water_500_button"
                        )
                        WaterQuickActionChip(
                            label = "+750 ml",
                            onClick = { onAddWaterMl(750) },
                            testTag = "add_water_750_button"
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable(enabled = dailyState.waterConsumedMl > 0) { onAddWaterMl(-250) }
                                .testTag("remove_water_250_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Restar 250ml",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "-250 ml",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = HydrationBlue.copy(alpha = 0.25f))

                    // Campo numérico manual (OutlinedTextField + botón "+ Sumar ml")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customWaterInput,
                            onValueChange = {
                                customWaterInput = it.filter { ch -> ch.isDigit() }
                                customWaterError = null
                            },
                            label = { Text("Cantidad manual (ml)") },
                            placeholder = { Text("Ej. 460 o 520") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_water_ml_input")
                        )

                        Button(
                            onClick = {
                                val parsed = customWaterInput.trim().toIntOrNull()
                                if (parsed != null && parsed in 1..5000) {
                                    onAddWaterMl(parsed)
                                    customWaterInput = ""
                                    customWaterError = null
                                } else {
                                    customWaterError = "Introduce entre 1 y 5000 ml"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HydrationBlue,
                                contentColor = Color(0xFF04131A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("manual_water_add_button")
                        ) {
                            Text(
                                text = "+ Sumar ml",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (customWaterError != null) {
                        Text(
                            text = customWaterError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterQuickActionChip(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = HydrationBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HydrationBlue.copy(alpha = 0.55f)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = HydrationBlue,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Diálogo para crear un nuevo bloque personalizado de comida rápida 1-Tap.
 */
@Composable
fun CreateCustomMealBlockDialog(
    state: CustomMealBlockDialogState,
    onNameChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatsChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Crear Bloque Personalizado 1-Tap",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configura una comida frecuente para sumarla a tus macros diarios con un solo toque.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = onNameChange,
                    label = { Text("Nombre del bloque") },
                    placeholder = { Text("Ej. +Lata de Atún + Arroz") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_meal_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.proteinInput,
                        onValueChange = onProteinChange,
                        label = { Text("Proteína (g)") },
                        placeholder = { Text("30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_meal_protein_input")
                    )
                    OutlinedTextField(
                        value = state.carbsInput,
                        onValueChange = onCarbsChange,
                        label = { Text("Carbos (g)") },
                        placeholder = { Text("40") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_meal_carbs_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.fatsInput,
                        onValueChange = onFatsChange,
                        label = { Text("Grasas (g)") },
                        placeholder = { Text("5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_meal_fats_input")
                    )
                    OutlinedTextField(
                        value = state.caloriesInput,
                        onValueChange = onCaloriesChange,
                        label = { Text("Calorías (kcal)") },
                        placeholder = { Text("325") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_meal_calories_input")
                    )
                }

                if (state.inputError != null) {
                    Text(
                        text = state.inputError,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_custom_meal_button")
            ) {
                Text("Guardar bloque")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
