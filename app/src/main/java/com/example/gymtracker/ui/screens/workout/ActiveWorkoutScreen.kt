package com.example.gymtracker.ui.screens.workout

import com.example.ui.theme.safeHeaderTopPadding

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymtracker.domain.model.Exercise
import com.example.gymtracker.ui.components.ExerciseCard
import com.example.gymtracker.ui.components.PlateCalculatorDialog
import com.example.gymtracker.ui.components.RestTimerOverlay
import com.example.gymtracker.ui.components.WorkoutSummaryDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }

    // Pantalla siempre encendida nativa (Keep Screen On) mientras el entrenamiento esté en curso
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    BackHandler {
        onNavigateBack()
    }

    if (uiState.isLoading) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("workout_loading_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Entrenamiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier.testTag("active_workout_loading")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Iniciando sesión de entrenamiento...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    if (uiState.isError) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("workout_error_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier.testTag("active_workout_error")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Ocurrió un error al cargar la sesión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Volver", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    val workout = uiState.workoutWithSets?.workout

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.GymStickyHeaderBg)
                    .safeHeaderTopPadding()
            ) {
                CenterAlignedTopAppBar(
                    windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("workout_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    title = {
                        // Stopwatch timer (tabular-nums, font-black, tracking-tight)
                        val hours = uiState.elapsedSeconds / 3600
                        val minutes = (uiState.elapsedSeconds % 3600) / 60
                        val seconds = uiState.elapsedSeconds % 60
                        val timeStr = if (hours > 0) {
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = com.example.ui.theme.GymInputBg,
                            border = com.example.ui.theme.CarbonGlassBorderStroke
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.GymGreenLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFeatureSettings = "tnum"
                                    ),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        Button(
                            onClick = { viewModel.onFinishWorkoutClicked() },
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.GymGreenPrimary,
                                contentColor = Color(0xFF04130D),
                                disabledContainerColor = com.example.ui.theme.GymGreenPrimary.copy(alpha = 0.4f),
                                disabledContentColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("finish_workout_button")
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(text = "Finalizar", fontWeight = FontWeight.ExtraBold)
                            }
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
        modifier = modifier.testTag("active_workout_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Workout Title input & Discard Action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        var titleText by remember(workout?.title) {
                            mutableStateOf(workout?.title ?: "")
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = {
                                titleText = it
                                viewModel.updateWorkoutTitle(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workout_title_input"),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            placeholder = { Text("Nombre del entrenamiento") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        TextButton(
                            onClick = { showDiscardConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Descartar")
                        }
                    }
                }

                // Overview stats banner (Carbon Glass + Technical Uppercase Labels + Tabular-Nums)
                item {
                    workout?.let {
                        val liveSessionXp = it.totalSets * com.example.gymtracker.domain.util.RpgGamificationEngine.XP_PER_COMPLETED_SET
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = com.example.ui.theme.GymGlassSurface
                            ),
                            border = com.example.ui.theme.CarbonGlassBorderStroke,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "VOLUMEN",
                                        style = com.example.ui.theme.TechnicalLabelStyle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${it.totalVolumeKg.toInt()} kg",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Black,
                                        color = com.example.ui.theme.GymGreenLight
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "SERIES TOTALES",
                                        style = com.example.ui.theme.TechnicalLabelStyle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${it.totalSets}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "XP EN SESIÓN",
                                        style = com.example.ui.theme.TechnicalLabelStyle
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+$liveSessionXp XP ⚡",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFeatureSettings = "tnum"
                                        ),
                                        fontWeight = FontWeight.Black,
                                        color = com.example.ui.theme.GymAmberFlame,
                                        modifier = Modifier.testTag("live_workout_xp_counter")
                                    )
                                }
                            }
                        }
                    }
                }

                // Banner de notificación o alternancia de Superserie
                uiState.supersetFeedbackMessage?.let { feedbackMsg ->
                    item {
                        Surface(
                            color = Color(0xFF7C4DFF).copy(alpha = 0.22f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB388FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.dismissSupersetFeedback() }
                                .testTag("superset_feedback_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = feedbackMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFEDE7F6),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Exercise Cards
                val exerciseGroups = uiState.workoutWithSets?.exercises.orEmpty()
                items(exerciseGroups, key = { it.exercise.id }) { group ->
                    ExerciseCard(
                        group = group,
                        onAddSet = { exId -> viewModel.addSetToExercise(exId) },
                        onWeightChanged = { set, w -> viewModel.updateSetWeight(set, w) },
                        onRepsChanged = { set, r -> viewModel.updateSetReps(set, r) },
                        onToggleCompleted = { set -> viewModel.toggleSetCompleted(set, group.exercise.defaultRestSeconds) },
                        onDeleteSet = { setId -> viewModel.deleteSet(setId) },
                        onDeleteExercise = { exId -> viewModel.deleteExercise(exId) },
                        onOpenTimer = { viewModel.openRestTimer(group.exercise.defaultRestSeconds) },
                        onOpenPlateCalculator = {
                            val lastWorkingWeight = group.sets.lastOrNull { it.weightKg > 0.0 }?.weightKg ?: 60.0
                            viewModel.openPlateCalculator(group.exercise, lastWorkingWeight)
                        },
                        onUpdatePinnedNote = { exId, note ->
                            viewModel.updatePinnedNote(exId, note)
                        },
                        onOpenSupersetLink = {
                            viewModel.openSupersetLinkDialog(group.exercise)
                        },
                        onUnlinkSuperset = {
                            viewModel.unlinkSuperset(group.exercise.id)
                        },
                        isFocused = (uiState.focusedExerciseId == group.exercise.id)
                    )
                }

                // Add Exercises Button
                item {
                    Button(
                        onClick = { viewModel.openAddExerciseDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_exercise_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "+ Agregar Ejercicio a esta sesión",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Bottom spacer - larger when floating rest timer is active so content isn't covered
                item {
                    val bottomSpacerHeight = if (uiState.isRestTimerActive && !uiState.isRestTimerExpanded) 88.dp else 24.dp
                    Spacer(modifier = Modifier.height(bottomSpacerHeight))
                }
            }

            // Rest Timer floating overlay (minimized bar or expanded sheet)
            RestTimerOverlay(
                isActive = uiState.isRestTimerActive,
                isExpanded = uiState.isRestTimerExpanded,
                totalSeconds = uiState.restTimerTotalSeconds,
                remainingSeconds = uiState.restTimerRemainingSeconds,
                onAddSeconds = { delta -> viewModel.addRestTimerSeconds(delta) },
                onSkip = { viewModel.closeRestTimer() },
                onToggleExpand = { viewModel.toggleRestTimerExpanded() },
                onSetExpanded = { exp -> viewModel.setRestTimerExpanded(exp) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Modal de Calculadora Visual de Discos y Series de Calentamiento
    if (uiState.plateCalculatorExercise != null) {
        val ex = uiState.plateCalculatorExercise!!
        PlateCalculatorDialog(
            exerciseName = ex.name,
            initialTargetWeight = uiState.plateCalculatorInitialWeight,
            onDismiss = { viewModel.closePlateCalculator() },
            onAddWarmupSets = { warmupSets ->
                viewModel.addWarmupSets(ex.id, warmupSets)
            }
        )
    }

    // Modal de Vinculación de Superserie
    if (uiState.supersetLinkExercise != null) {
        val currentEx = uiState.supersetLinkExercise!!
        val otherExercises = uiState.workoutWithSets?.exercises
            ?.map { it.exercise }
            ?.filter { it.id != currentEx.id }
            .orEmpty()

        SupersetLinkSelectionDialog(
            targetExercise = currentEx,
            availableExercises = otherExercises,
            onSelectExercise = { partner ->
                viewModel.linkExercisesInSuperset(currentEx.id, partner.id)
            },
            onDismiss = { viewModel.closeSupersetLinkDialog() }
        )
    }

    // Add Exercise Selection Dialog
    if (uiState.isAddExerciseDialogOpen) {
        ExerciseSelectionDialog(
            exercises = uiState.allAvailableExercises,
            onSelectExercise = { ex -> viewModel.addExerciseToWorkout(ex) },
            onDismiss = { viewModel.closeAddExerciseDialog() }
        )
    }

    // Discard Confirmation Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            title = { Text("¿Descartar entrenamiento?") },
            text = { Text("Se perderán todas las series registradas en esta sesión.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirmDialog = false
                        viewModel.discardWorkout(onNavigateBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Post-Workout Summary Dialog
    if (uiState.isSummaryDialogOpen && uiState.workoutSummary != null) {
        WorkoutSummaryDialog(
            summary = uiState.workoutSummary!!,
            isSaving = uiState.isSaving,
            onSaveAndFinish = { notes ->
                viewModel.saveWorkoutFromSummary(notes = notes, onFinished = onNavigateBack)
            },
            onDismiss = {
                viewModel.dismissSummaryDialog()
            }
        )
    }
}

@Composable
fun ExerciseSelectionDialog(
    exercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = exercises.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.muscleGroup.displayName.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .fillMaxWidth()
                .height(500.dp)
                .testTag("exercise_selection_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionar Ejercicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_search_input"),
                    placeholder = { Text("Buscar por nombre o músculo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { ex ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectExercise(ex) }
                                .testTag("select_exercise_${ex.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ex.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${ex.muscleGroup.displayName} • ${ex.equipment}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupersetLinkSelectionDialog(
    targetExercise: Exercise,
    availableExercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("superset_link_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vincular Superserie (Biserie)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "con ${targetExercise.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Selecciona el ejercicio con el que alternarás series sin descanso intermedio:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (availableExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Agrega más ejercicios a la sesión para poder formar una superserie",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableExercises) { ex ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectExercise(ex) }
                                    .testTag("select_superset_partner_${ex.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ex.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${ex.muscleGroup.displayName} • ${ex.equipment}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Vincular",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

