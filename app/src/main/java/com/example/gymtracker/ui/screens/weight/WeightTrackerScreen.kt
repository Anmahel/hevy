package com.example.gymtracker.ui.screens.weight

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.data.local.entity.WeightLogEntity
import com.example.gymtracker.domain.util.MacroBreakdown
import com.example.gymtracker.ui.components.GlobalTopHeader
import com.example.ui.theme.safeHeaderTopPadding
import java.util.Locale

// Paleta distintiva de colores para Macronutrientes (Athletic Neon High Contrast)
val ProteinColor = Color(0xFFA855F7)       // Violeta neón vibrante
val CarbsColor = Color(0xFFFBBF24)         // Ámbar energético
val FatsColor = Color(0xFF34D399)          // Verde Volt / Esmeralda
val CalorieAccentColor = Color(0xFFF97316) // Naranja Fuego (from-orange-500 to-amber-400)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    viewModel: WeightTrackerViewModel,
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
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            GlobalTopHeader(
                title = "Nutrición",
                waterGlassesToday = waterGlassesToday,
                isWaterGoalMetToday = isWaterGoalMetToday,
                creatineTakenToday = creatineTakenToday,
                athleteLevel = athleteLevel,
                onQuickAddWaterGlass = onQuickAddWaterGlass,
                onQuickTakeCreatine = onQuickTakeCreatine,
                onOpenRpgProfile = onOpenRpgProfile,
                onNavigateToBackup = onNavigateToBackup,
                modifier = Modifier.testTag("weight_tracker_top_bar")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Sub-barra compacta de acción rápida para registrar Peso o Medidas del día
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Control de Peso & Antropometría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.example.ui.theme.GymGreenLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    FilledTonalButton(
                        onClick = {
                            viewModel.onOpenAddDialog(
                                focusMeasurements = uiState.selectedSubTab == WeightSubTab.MEDIDAS
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("top_add_weight_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.selectedSubTab == WeightSubTab.MEDIDAS) Icons.Default.Straighten else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.selectedSubTab == WeightSubTab.MEDIDAS) "Medir hoy" else "Pesar hoy",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Sub-pestañas dentro del módulo de Control de Peso:
                // 1) Peso & Macros  2) Agua & Creatina  3) Medidas  4) Comparador
                WeightSubTabSelector(
                    selectedTab = uiState.selectedSubTab,
                    onSelectTab = { viewModel.onSelectSubTab(it) }
                )

                when (uiState.selectedSubTab) {
                    WeightSubTab.PESO -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("weight_logs_list")
                        ) {
                            // Tarjeta Resumen Superior de Estado Actual
                            uiState.latestMacros?.let { macros ->
                                item(key = "header_summary_card") {
                                    CurrentNutritionHeaderCard(
                                        macros = macros,
                                        latestWeightKg = uiState.latestWeightKg ?: uiState.dailyNutrition.referenceWeightKg,
                                        onCardClick = {
                                            val latestLog = uiState.items.firstOrNull()?.entity
                                            if (latestLog != null) {
                                                viewModel.onSelectLogForMacros(latestLog)
                                            }
                                        }
                                    )
                                }
                            }

                            // MÓDULO 3.1: Barra Diaria de Macros Conectada al Peso Actual ("Consumido hoy vs. Meta")
                            item(key = "daily_macro_progress_card") {
                                DailyMacroProgressCard(
                                    dailyState = uiState.dailyNutrition,
                                    onOpenMacroDetails = {
                                        val latestLog = uiState.items.firstOrNull()?.entity
                                        if (latestLog != null) {
                                            viewModel.onSelectLogForMacros(latestLog)
                                        }
                                    },
                                    onResetDay = { viewModel.onResetDailyMacros() }
                                )
                            }

                            // MÓDULO 3.2: Botones Rápidos de Comidas Habituales (1-Tap Meal Tracker) + Deshacer
                            item(key = "one_tap_meal_tracker_card") {
                                OneTapMealTrackerCard(
                                    dailyState = uiState.dailyNutrition,
                                    onQuickAddMeal = { block -> viewModel.onQuickAddMealBlock(block) },
                                    onUndoLastTap = { viewModel.onUndoLastMealTap() },
                                    onOpenCustomBlockDialog = { viewModel.onOpenCustomMealDialog() },
                                    onDeleteCustomBlock = { customId -> viewModel.onDeleteCustomMealBlock(customId) }
                                )
                            }

                            // MÓDULO 3.3: Suplementación Diaria e Hidratación Unificada (con Calendario, Rachas y Registro Manual)
                            item(key = "creatine_hydration_checklist_card") {
                                CreatineAndHydrationChecklistCard(
                                    dailyState = uiState.dailyNutrition,
                                    onToggleCreatine = { taken -> viewModel.onToggleCreatine(taken) },
                                    onAddWaterMl = { delta -> viewModel.onAddWaterMl(delta) },
                                    onPreviousMonth = { viewModel.onPreviousNutritionMonth() },
                                    onNextMonth = { viewModel.onNextNutritionMonth() },
                                    onSelectCalendarDate = { dateKey -> viewModel.onSelectNutritionCalendarDate(dateKey) }
                                )
                            }

                            if (uiState.items.isEmpty()) {
                                item(key = "empty_weight_logs_state") {
                                    EmptyWeightState(
                                        onAddClick = { viewModel.onOpenAddDialog() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                // Título de la sección de historial
                                item(key = "history_header") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Historial de Pesajes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "Toca para ver macros",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Lista cronológica de registros
                                items(
                                    items = uiState.items,
                                    key = { it.entity.id }
                                ) { item ->
                                    WeightLogCard(
                                        item = item,
                                        onClick = { viewModel.onSelectLogForMacros(item.entity) },
                                        onEdit = { viewModel.onOpenEditDialog(item.entity) },
                                        onDelete = { viewModel.onDeleteWeightLog(item.entity) }
                                    )
                                }
                            }
                        }
                    }

                    WeightSubTab.HIDRATACION -> {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("hydration_creatine_tab_list")
                        ) {
                            item(key = "unified_hydration_creatine_card") {
                                CreatineAndHydrationChecklistCard(
                                    dailyState = uiState.dailyNutrition,
                                    onToggleCreatine = { taken -> viewModel.onToggleCreatine(taken) },
                                    onAddWaterMl = { delta -> viewModel.onAddWaterMl(delta) },
                                    onPreviousMonth = { viewModel.onPreviousNutritionMonth() },
                                    onNextMonth = { viewModel.onNextNutritionMonth() },
                                    onSelectCalendarDate = { dateKey -> viewModel.onSelectNutritionCalendarDate(dateKey) }
                                )
                            }
                        }
                    }

                    WeightSubTab.MEDIDAS -> {
                        BodyMeasurementsTabContent(
                            items = uiState.items,
                            onAddMeasurementsClick = {
                                viewModel.onOpenAddDialog(focusMeasurements = true)
                            },
                            onEditLogClick = { item ->
                                viewModel.onOpenEditDialog(item.entity, focusMeasurements = true)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WeightSubTab.COMPARADOR -> {
                        ProgressComparatorTabContent(
                            items = uiState.items,
                            beforeItem = uiState.compareBeforeItem,
                            afterItem = uiState.compareAfterItem,
                            onSelectBefore = { id -> viewModel.onSelectCompareBefore(id) },
                            onSelectAfter = { id -> viewModel.onSelectCompareAfter(id) },
                            onEditCard = { item ->
                                viewModel.onOpenEditDialog(item.entity, focusMeasurements = true)
                            },
                            onSimulateCards = {
                                viewModel.onSimulateBeforeAfterCards()
                            },
                            onAddCard = {
                                viewModel.onOpenAddDialog(focusMeasurements = true)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Modal BottomSheet con el desglose exacto de macronutrientes
            if (uiState.selectedLogForMacros != null && uiState.selectedMacroBreakdown != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.onDismissMacroSheet() },
                    sheetState = bottomSheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("macro_bottom_sheet")
                ) {
                    MacroBreakdownSheetContent(
                        macros = uiState.selectedMacroBreakdown!!,
                        log = uiState.selectedLogForMacros!!,
                        onClose = { viewModel.onDismissMacroSheet() }
                    )
                }
            }

            // Diálogo para registrar / editar peso, 8 medidas antropométricas y tarjeta visual
            if (uiState.isAddEditDialogOpen) {
                AddEditWeightDialog(
                    dialogState = uiState.dialogState,
                    isEditing = uiState.editingLogId != null,
                    onWeightChange = { viewModel.onWeightInputChanged(it) },
                    onNotesChange = { viewModel.onNotesInputChanged(it) },
                    onBicepsRelaxedCmChange = { viewModel.onBicepsRelaxedCmChanged(it) },
                    onBicepsContractedCmChange = { viewModel.onBicepsContractedCmChanged(it) },
                    onForearmCmChange = { viewModel.onForearmCmChanged(it) },
                    onChestCmChange = { viewModel.onChestCmChanged(it) },
                    onWaistCmChange = { viewModel.onWaistCmChanged(it) },
                    onGlutesCmChange = { viewModel.onGlutesCmChanged(it) },
                    onThighCmChange = { viewModel.onThighCmChanged(it) },
                    onCalfCmChange = { viewModel.onCalfCmChanged(it) },
                    onPhotoUriChange = { viewModel.onPhotoUriChanged(it) },
                    onPoseTypeChange = { viewModel.onPoseTypeChanged(it) },
                    onConfirm = { viewModel.onSaveWeightLog() },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }

            // Diálogo para crear bloque personalizado de comida rápida 1-Tap
            if (uiState.customMealDialogState.isOpen) {
                CreateCustomMealBlockDialog(
                    state = uiState.customMealDialogState,
                    onNameChange = { viewModel.onCustomMealNameChanged(it) },
                    onProteinChange = { viewModel.onCustomMealProteinChanged(it) },
                    onCarbsChange = { viewModel.onCustomMealCarbsChanged(it) },
                    onFatsChange = { viewModel.onCustomMealFatsChanged(it) },
                    onCaloriesChange = { viewModel.onCustomMealCaloriesChanged(it) },
                    onConfirm = { viewModel.onSaveCustomMealBlock() },
                    onDismiss = { viewModel.onDismissCustomMealDialog() }
                )
            }
        }
    }
}

/**
 * Tarjeta superior que resume el último peso y su cálculo nutricional activo.
 */
@Composable
fun CurrentNutritionHeaderCard(
    macros: MacroBreakdown,
    latestWeightKg: Float,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("current_nutrition_header_card")
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
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Peso Actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1f kg".format(Locale.US, latestWeightKg),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CalorieAccentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CalorieAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${macros.totalCalories} kcal",
                            fontWeight = FontWeight.Bold,
                            color = CalorieAccentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra visual compacta de macronutrientes
            MacroDistributionBar(
                proteinPercentage = macros.proteinPercentage,
                carbsPercentage = macros.carbsPercentage,
                fatsPercentage = macros.fatsPercentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pills rápidos de macronutrientes
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MacroMiniPill(
                    label = "Proteína",
                    value = "${macros.proteinTargetGrams.toInt()}g",
                    color = ProteinColor
                )
                MacroMiniPill(
                    label = "Carbohidratos",
                    value = "${macros.carbsGrams.toInt()}g",
                    color = CarbsColor
                )
                MacroMiniPill(
                    label = "Grasas",
                    value = "${macros.fatsGrams.toInt()}g",
                    color = FatsColor
                )
            }
        }
    }
}

@Composable
fun MacroMiniPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Tarjeta individual de registro de peso con variación porcentual.
 */
@Composable
fun WeightLogCard(
    item: WeightUiItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.GymGlassSurface
        ),
        border = com.example.ui.theme.CarbonGlassBorderStroke,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("weight_card_${item.entity.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)
        ) {
            // Icono con indicador
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información de fecha y notas
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!item.entity.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.entity.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Peso y Variación Porcentual
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.formattedWeight,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Indicador de variación respecto al peso anterior
                if (item.formattedPercentage != null) {
                    val badgeColor = when (item.isPositive) {
                        true -> Color(0xFF10B981) // Verde (+peso en hipertrofia)
                        false -> Color(0xFFF97316) // Naranja / Ámbar (-peso)
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val iconVector = when (item.isPositive) {
                        true -> Icons.Default.TrendingUp
                        false -> Icons.Default.TrendingDown
                        null -> Icons.Default.TrendingFlat
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.formattedPercentage,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                } else {
                    Text(
                        text = "Base inicial",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón de eliminar directo
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("delete_weight_${item.entity.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar pesaje",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Contenido del ModalBottomSheet con el desglose exacto de macronutrientes para hipertrofia.
 */
@Composable
fun MacroBreakdownSheetContent(
    macros: MacroBreakdown,
    log: WeightLogEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("macro_sheet_content")
    ) {
        // Encabezado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Nutrición para Hipertrofia",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cálculo para peso actual de %.1f kg".format(Locale.US, log.weightKg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tarjeta de Calorías Totales con desglose de superávit
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CalorieAccentColor.copy(alpha = 0.12f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(CalorieAccentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Calorías Diarias Objetivo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${macros.totalCalories} kcal / día",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalorieAccentColor
                    )
                    Text(
                        text = "Mantenimiento (~${macros.maintenanceCalories} kcal) + Superávit limpio (+${macros.surplusCalories} kcal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Barra visual compuesta de macronutrientes
        Text(
            text = "Distribución de Macronutrientes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        MacroDistributionBar(
            proteinPercentage = macros.proteinPercentage,
            carbsPercentage = macros.carbsPercentage,
            fatsPercentage = macros.fatsPercentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjetas diferenciadas por color para cada macronutriente
        // 1. Proteína
        MacroDetailCard(
            title = "Proteína",
            recommendationRange = "2.0g – 2.2g por kg de peso",
            grams = "${macros.proteinTargetGrams.toInt()}g",
            subtextRange = "Rango: ${macros.proteinMinGrams.toInt()}g - ${macros.proteinMaxGrams.toInt()}g",
            calories = "${macros.proteinCalories} kcal • ${(macros.proteinPercentage * 100).toInt()}%",
            color = ProteinColor,
            description = "Fundamental para síntesis proteica muscular y reparación de micro-roturas.",
            modifier = Modifier.testTag("macro_protein_card")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Carbohidratos
        MacroDetailCard(
            title = "Carbohidratos",
            recommendationRange = "Resto de calorías ÷ 4 kcal/g",
            grams = "${macros.carbsGrams.toInt()}g",
            subtextRange = "Energía principal para hipertrofia",
            calories = "${macros.carbsCalories} kcal • ${(macros.carbsPercentage * 100).toInt()}%",
            color = CarbsColor,
            description = "Recarga de glucógeno muscular y soporte de alta intensidad en series pesadas.",
            modifier = Modifier.testTag("macro_carbs_card")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Grasas
        MacroDetailCard(
            title = "Grasas Saludables",
            recommendationRange = "0.9g por kg de peso corporal",
            grams = "${macros.fatsGrams.toInt()}g",
            subtextRange = "Fuentes esenciales y no saturadas",
            calories = "${macros.fatsCalories} kcal • ${(macros.fatsPercentage * 100).toInt()}%",
            color = FatsColor,
            description = "Salud hormonal, producción de testosterona y absorción de vitaminas liposolubles.",
            modifier = Modifier.testTag("macro_fats_card")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("macro_sheet_close_button")
        ) {
            Text(
                text = "Entendido",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Barra visual de distribución porcentual de los 3 macronutrientes.
 */
@Composable
fun MacroDistributionBar(
    proteinPercentage: Float,
    carbsPercentage: Float,
    fatsPercentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (proteinPercentage > 0f) {
            Box(
                modifier = Modifier
                    .weight(proteinPercentage.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(ProteinColor)
            )
        }
        if (carbsPercentage > 0f) {
            Box(
                modifier = Modifier
                    .weight(carbsPercentage.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(CarbsColor)
            )
        }
        if (fatsPercentage > 0f) {
            Box(
                modifier = Modifier
                    .weight(fatsPercentage.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(FatsColor)
            )
        }
    }
}

/**
 * Tarjeta de desglose detallado para cada macronutriente individual.
 */
@Composable
fun MacroDetailCard(
    title: String,
    recommendationRange: String,
    grams: String,
    subtextRange: String,
    calories: String,
    color: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = grams,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Separado en dos líneas limpias para evitar colisión entre el rango en gramos y las calorías
            Text(
                text = "$recommendationRange • $subtextRange",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = calories,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}

/**
 * Diálogo para introducir o modificar el peso corporal, medidas antropométricas (cinta métrica)
 * y tarjeta de registro visual (foto o pose anatómica).
 */
@Composable
fun AddEditWeightDialog(
    dialogState: WeightDialogState,
    isEditing: Boolean,
    onWeightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onBicepsRelaxedCmChange: (String) -> Unit,
    onBicepsContractedCmChange: (String) -> Unit,
    onForearmCmChange: (String) -> Unit,
    onChestCmChange: (String) -> Unit,
    onWaistCmChange: (String) -> Unit,
    onGlutesCmChange: (String) -> Unit,
    onThighCmChange: (String) -> Unit,
    onCalfCmChange: (String) -> Unit,
    onPhotoUriChange: (String) -> Unit,
    onPoseTypeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoUriChange(uri.toString())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Control Corporal" else "Registrar Peso y Medidas",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Introduce tu peso en ayunas y tus 8 perímetros corporales (cm) para verificar que la ganancia sea masa muscular limpia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = dialogState.weightInput,
                    onValueChange = onWeightChange,
                    label = { Text("Peso corporal (kg)") },
                    placeholder = { Text("Ej. 75.4") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = dialogState.inputError != null,
                    supportingText = dialogState.inputError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field")
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Text(
                    text = "Cinta Métrica (8 Perímetros en cm - Opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Fila 1: Bíceps flaco / relajado | Bíceps contraído
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.bicepsRelaxedCmInput,
                        onValueChange = onBicepsRelaxedCmChange,
                        label = { Text("Bíceps relajado (cm)") },
                        placeholder = { Text("35.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("biceps_relaxed_cm_input")
                    )
                    OutlinedTextField(
                        value = dialogState.bicepsContractedCmInput,
                        onValueChange = onBicepsContractedCmChange,
                        label = { Text("Bíceps contraído (cm)") },
                        placeholder = { Text("38.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("arm_cm_input")
                    )
                }

                // Fila 2: Antebrazo | Pecho / Torso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.forearmCmInput,
                        onValueChange = onForearmCmChange,
                        label = { Text("Antebrazo (cm)") },
                        placeholder = { Text("30.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("forearm_cm_input")
                    )
                    OutlinedTextField(
                        value = dialogState.chestCmInput,
                        onValueChange = onChestCmChange,
                        label = { Text("Pecho / Torso (cm)") },
                        placeholder = { Text("104.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chest_cm_input")
                    )
                }

                // Fila 3: Cintura / Abdomen | Glúteos / Cadera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.waistCmInput,
                        onValueChange = onWaistCmChange,
                        label = { Text("Cintura (cm)") },
                        placeholder = { Text("80.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("waist_cm_input")
                    )
                    OutlinedTextField(
                        value = dialogState.glutesCmInput,
                        onValueChange = onGlutesCmChange,
                        label = { Text("Glúteos (cm)") },
                        placeholder = { Text("96.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("glutes_cm_input")
                    )
                }

                // Fila 4: Muslo / Cuádriceps | Gemelos / Pantorrilla
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dialogState.thighCmInput,
                        onValueChange = onThighCmChange,
                        label = { Text("Muslo (cm)") },
                        placeholder = { Text("58.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("thigh_cm_input")
                    )
                    OutlinedTextField(
                        value = dialogState.calfCmInput,
                        onValueChange = onCalfCmChange,
                        label = { Text("Gemelos (cm)") },
                        placeholder = { Text("38.5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calf_cm_input")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Text(
                    text = "Tarjeta Visual (Comparador Antes vs. Después)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AVAILABLE_POSES.forEach { pose ->
                        FilterChip(
                            selected = dialogState.poseTypeInput == pose,
                            onClick = { onPoseTypeChange(pose) },
                            label = { Text(pose, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_progress_photo_button")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dialogState.photoUriInput.isNotBlank() && !dialogState.photoUriInput.startsWith("simulated://"))
                                "Foto Adjuntada ✓"
                            else
                                "Adjuntar Foto",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = dialogState.notesInput,
                    onValueChange = onNotesChange,
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Ej. En ayunas, post-entreno...") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_notes_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_weight_button")
            ) {
                Text(if (isEditing) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_weight_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Estado vacío cuando no hay registros de peso aún.
 */
@Composable
fun EmptyWeightState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Scale,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Sin registros de peso aún",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registra tu peso diario para calcular tus calorías con superávit de hipertrofia y desglose exacto de proteínas, carbos y grasas.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_add_weight_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar primer peso")
        }
    }
}
