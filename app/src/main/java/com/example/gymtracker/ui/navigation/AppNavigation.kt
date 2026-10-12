package com.example.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
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
    modifier: Modifier = Modifier
) {
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
                }
            )
        }

        // Pestaña Historial
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.Factory(workoutRepository, gymRepository)
            )
            HistoryScreen(viewModel = historyViewModel)
        }

        // Pestaña Ejercicios
        composable(Screen.Exercises.route) {
            val exerciseListViewModel: ExerciseListViewModel = viewModel(
                factory = ExerciseListViewModel.Factory(exerciseRepository)
            )
            ExerciseListScreen(
                viewModel = exerciseListViewModel,
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
                factory = ExerciseDetailViewModel.Factory(exerciseId, exerciseRepository, workoutRepository)
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
                }
            )
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
            val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                factory = ActiveWorkoutViewModel.Factory(workoutId, gymRepository, workoutRepository)
            )
            ActiveWorkoutScreen(
                viewModel = activeWorkoutViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
