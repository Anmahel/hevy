package com.example.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gymtracker.data.local.ActiveWorkoutSessionStore
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.ui.components.AthleteRpgProfileScreen
import com.example.gymtracker.ui.components.BackupRestoreDialog
import com.example.gymtracker.ui.screens.weight.WeightTrackerScreen
import com.example.gymtracker.ui.screens.weight.WeightTrackerViewModel
import com.example.gymtracker.ui.screens.calendar.WorkoutCalendarScreen
import com.example.gymtracker.ui.screens.calendar.WorkoutCalendarViewModel
import com.example.gymtracker.ui.screens.exercises.ExerciseDetailScreen
import com.example.gymtracker.ui.screens.exercises.ExerciseDetailViewModel
import com.example.gymtracker.ui.screens.exercises.ExerciseListScreen
import com.example.gymtracker.ui.screens.exercises.ExerciseListViewModel
import com.example.gymtracker.ui.screens.history.HistoryScreen
import com.example.gymtracker.ui.screens.history.HistoryViewModel
import com.example.gymtracker.ui.screens.routines.EditRoutineScreen
import com.example.gymtracker.ui.screens.routines.EditRoutineViewModel
import com.example.gymtracker.ui.screens.routines.RoutineListScreen
import com.example.gymtracker.ui.screens.routines.RoutineListViewModel
import com.example.gymtracker.ui.screens.workout.ActiveWorkoutScreen
import com.example.gymtracker.ui.screens.workout.ActiveWorkoutViewModel
import com.example.gymtracker.ui.screens.recovery.MuscleRecoveryScreen
import com.example.gymtracker.ui.screens.recovery.MuscleProgressViewModel
import com.example.gymtracker.ui.screens.stats.PerformanceStatsScreen
import com.example.gymtracker.ui.screens.stats.PerformanceStatsViewModel
import kotlinx.coroutines.flow.firstOrNull

/**
 * Host de navegación principal de la aplicación con rutas tipadas.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    exerciseRepository: ExerciseRepository,
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    gymRepository: GymRepository,
    weightRepository: WeightRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCheckedActiveWorkoutOnLaunch by rememberSaveable { mutableStateOf(false) }

    // Recuperación automática de la sesión activa si la app se cerró o fue terminada en segundo plano sin finalizar el entreno
    LaunchedEffect(Unit) {
        if (!hasCheckedActiveWorkoutOnLaunch) {
            hasCheckedActiveWorkoutOnLaunch = true
            try {
                ActiveWorkoutSessionStore.init(context)
                val persisted = ActiveWorkoutSessionStore.loadState(context)
                val activeInRoom = gymRepository.getActiveWorkout().firstOrNull()

                val targetWorkoutId = when {
                    persisted != null && persisted.isActive && activeInRoom?.id == persisted.workoutId -> persisted.workoutId
                    activeInRoom != null && activeInRoom.id > 0L -> activeInRoom.id
                    else -> 0L
                }

                if (targetWorkoutId > 0L) {
                    val targetRoutineId = persisted?.routineId ?: 0L
                    navController.navigate(
                        Screen.ActiveWorkout.createRoute(
                            workoutId = targetWorkoutId,
                            routineId = targetRoutineId
                        )
                    ) {
                        launchSingleTop = true
                    }
                } else if (persisted != null) {
                    ActiveWorkoutSessionStore.clearState(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val sharedHistoryViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(
            workoutRepository = workoutRepository,
            gymRepository = gymRepository,
            weightRepository = weightRepository,
            exerciseRepository = exerciseRepository,
            routineRepository = routineRepository
        )
    )
    val historyUiState by sharedHistoryViewModel.uiState.collectAsStateWithLifecycle()

    val sharedCalendarViewModel: WorkoutCalendarViewModel = viewModel(
        factory = WorkoutCalendarViewModel.Factory(workoutRepository)
    )

    val sharedWeightTrackerViewModel: WeightTrackerViewModel = viewModel(
        factory = WeightTrackerViewModel.Factory(weightRepository)
    )
    val weightUiState by sharedWeightTrackerViewModel.uiState.collectAsStateWithLifecycle()

    val openRpgProfileScreen: () -> Unit = {
        navController.navigate(Screen.RpgProfile.route) {
            launchSingleTop = true
        }
    }
    val openPerformanceStatsScreen: () -> Unit = {
        navController.navigate(Screen.PerformanceStats.route) {
            launchSingleTop = true
        }
    }
    val openBackupModal: () -> Unit = {
        sharedHistoryViewModel.openBackupDialog()
    }
    val quickAddWaterShaker: () -> Unit = {
        sharedWeightTrackerViewModel.onAddWaterMl(750)
    }
    val quickTakeCreatine: () -> Unit = {
        sharedWeightTrackerViewModel.onTakeDailyCreatine()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Routines.route,
        modifier = modifier
    ) {
        // Pestaña Rutinas (Pantalla principal)
        composable(Screen.Routines.route) {
            val routineListViewModel: RoutineListViewModel = viewModel(
                factory = RoutineListViewModel.Factory(routineRepository, gymRepository)
            )
            RoutineListScreen(
                viewModel = routineListViewModel,
                onNavigateToCreateRoutine = {
                    navController.navigate(Screen.EditRoutine.createRoute(0L))
                },
                onNavigateToEditRoutine = { routineId ->
                    navController.navigate(Screen.EditRoutine.createRoute(routineId))
                },
                onNavigateToActiveWorkout = { workoutId ->
                    navController.navigate(Screen.ActiveWorkout.createRoute(workoutId))
                },
                onNavigateToBackup = openBackupModal,
                athleteLevel = historyUiState.athleteXpProfile.level,
                onOpenRpgProfile = openRpgProfileScreen,
                waterGlassesToday = weightUiState.dailyNutrition.glassesTakenToday,
                isWaterGoalMetToday = weightUiState.dailyNutrition.isWaterGoalMetToday,
                creatineTakenToday = weightUiState.dailyNutrition.creatineTaken,
                onQuickAddWaterGlass = quickAddWaterShaker,
                onQuickTakeCreatine = quickTakeCreatine
            )
        }

        // Pestaña Historial (Almanaque & Racha + Sesiones detalladas)
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = sharedHistoryViewModel,
                calendarViewModel = sharedCalendarViewModel,
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToExerciseDetail = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                },
                onNavigateToPerformanceStats = openPerformanceStatsScreen,
                onNavigateToBackup = openBackupModal,
                athleteLevel = historyUiState.athleteXpProfile.level,
                onOpenRpgProfile = openRpgProfileScreen,
                waterGlassesToday = weightUiState.dailyNutrition.glassesTakenToday,
                isWaterGoalMetToday = weightUiState.dailyNutrition.isWaterGoalMetToday,
                creatineTakenToday = weightUiState.dailyNutrition.creatineTaken,
                onQuickAddWaterGlass = quickAddWaterShaker,
                onQuickTakeCreatine = quickTakeCreatine
            )
        }

        // Pestaña Ejercicios
        composable(Screen.Exercises.route) {
            val exerciseListViewModel: ExerciseListViewModel = viewModel(
                factory = ExerciseListViewModel.Factory(
                    repository = exerciseRepository,
                    workoutRepository = workoutRepository,
                    weightRepository = weightRepository
                )
            )
            ExerciseListScreen(
                viewModel = exerciseListViewModel,
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                },
                onNavigateToBackup = openBackupModal,
                athleteLevel = historyUiState.athleteXpProfile.level,
                onOpenRpgProfile = openRpgProfileScreen,
                waterGlassesToday = weightUiState.dailyNutrition.glassesTakenToday,
                isWaterGoalMetToday = weightUiState.dailyNutrition.isWaterGoalMetToday,
                creatineTakenToday = weightUiState.dailyNutrition.creatineTaken,
                onQuickAddWaterGlass = quickAddWaterShaker,
                onQuickTakeCreatine = quickTakeCreatine
            )
        }

        // Pestaña Control de Peso y Nutrición para Hipertrofia
        composable(Screen.WeightTracker.route) {
            WeightTrackerScreen(
                viewModel = sharedWeightTrackerViewModel,
                onNavigateToBackup = openBackupModal,
                athleteLevel = historyUiState.athleteXpProfile.level,
                onOpenRpgProfile = openRpgProfileScreen,
                waterGlassesToday = weightUiState.dailyNutrition.glassesTakenToday,
                isWaterGoalMetToday = weightUiState.dailyNutrition.isWaterGoalMetToday,
                creatineTakenToday = weightUiState.dailyNutrition.creatineTaken,
                onQuickAddWaterGlass = quickAddWaterShaker,
                onQuickTakeCreatine = quickTakeCreatine
            )
        }

        // Pantalla de Calendario y Racha de Entrenamientos
        composable(Screen.Calendar.route) {
            WorkoutCalendarScreen(viewModel = sharedCalendarViewModel)
        }

        // Pantalla de Análisis de Estímulo y Recuperación para Hipertrofia
        composable(Screen.MuscleRecovery.route) {
            val muscleProgressViewModel: MuscleProgressViewModel = viewModel(
                factory = MuscleProgressViewModel.Factory(workoutRepository)
            )
            MuscleRecoveryScreen(
                viewModel = muscleProgressViewModel,
                onNavigateToPerformanceStats = openPerformanceStatsScreen,
                onNavigateToBackup = openBackupModal,
                athleteLevel = historyUiState.athleteXpProfile.level,
                onOpenRpgProfile = openRpgProfileScreen,
                waterGlassesToday = weightUiState.dailyNutrition.glassesTakenToday,
                isWaterGoalMetToday = weightUiState.dailyNutrition.isWaterGoalMetToday,
                creatineTakenToday = weightUiState.dailyNutrition.creatineTaken,
                onQuickAddWaterGlass = quickAddWaterShaker,
                onQuickTakeCreatine = quickTakeCreatine
            )
        }

        // Pantalla sólida FullScreen dedicada al Perfil RPG de Atleta
        composable(Screen.RpgProfile.route) {
            AthleteRpgProfileScreen(
                xpProfile = historyUiState.athleteXpProfile,
                strengthRanks = historyUiState.strengthRanks,
                bodyweightKg = historyUiState.currentBodyweightKg,
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                },
                onNavigateToPerformanceStats = openPerformanceStatsScreen,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Estadísticas de Rendimiento (Gráfica Recharts 1RM Top 5 Ejercicios - 6 Meses)
        composable(Screen.PerformanceStats.route) {
            val performanceStatsViewModel: PerformanceStatsViewModel = viewModel(
                factory = PerformanceStatsViewModel.Factory(
                    workoutRepository = workoutRepository,
                    exerciseRepository = exerciseRepository,
                    weightRepository = weightRepository
                )
            )
            PerformanceStatsScreen(
                viewModel = performanceStatsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                }
            )
        }

        // Pantalla de Detalle del Ejercicio (Estilo Hevy)
        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(
                navArgument("exerciseId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
            val exerciseDetailViewModel: ExerciseDetailViewModel = viewModel(
                factory = ExerciseDetailViewModel.Factory(
                    exerciseId = exerciseId,
                    exerciseRepository = exerciseRepository,
                    workoutRepository = workoutRepository,
                    weightRepository = weightRepository
                )
            )
            ExerciseDetailScreen(
                viewModel = exerciseDetailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Editor / Creador de Rutina
        composable(
            route = Screen.EditRoutine.route,
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
            val editRoutineViewModel: EditRoutineViewModel = viewModel(
                factory = EditRoutineViewModel.Factory(routineId, routineRepository, exerciseRepository)
            )
            EditRoutineScreen(
                viewModel = editRoutineViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Entrenamiento Activo en curso
        composable(
            route = Screen.ActiveWorkout.route,
            arguments = listOf(
                navArgument("workoutId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("routineId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
            val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                factory = ActiveWorkoutViewModel.Factory(
                    workoutId = workoutId,
                    routineId = routineId,
                    repository = gymRepository,
                    workoutRepository = workoutRepository,
                    routineRepository = routineRepository
                )
            )
            ActiveWorkoutScreen(
                viewModel = activeWorkoutViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Modal único de Datos y Copia de Seguridad (.JSON) (abierto desde el icono de Escudo en "Mis Rutinas")
    if (historyUiState.isBackupDialogOpen) {
        BackupRestoreDialog(
            storageStats = historyUiState.storageStats,
            onGenerateBackupJson = { sharedHistoryViewModel.generateBackupJson() },
            onRestoreFromJson = { rawJson, onResult ->
                sharedHistoryViewModel.restoreBackupFromJson(rawJson, onResult)
            },
            onDismiss = { sharedHistoryViewModel.closeBackupDialog() }
        )
    }
}
