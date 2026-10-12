package com.example.gymtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.ui.components.GymBottomBar
import com.example.gymtracker.ui.screens.exercises.ExerciseListScreen
import com.example.gymtracker.ui.screens.exercises.ExerciseListViewModel
import com.example.gymtracker.ui.screens.history.HistoryScreen
import com.example.gymtracker.ui.screens.history.HistoryViewModel
import com.example.gymtracker.ui.screens.home.HomeScreen
import com.example.gymtracker.ui.screens.home.HomeViewModel
import com.example.gymtracker.ui.screens.routines.EditRoutineScreen
import com.example.gymtracker.ui.screens.routines.EditRoutineViewModel
import com.example.gymtracker.ui.screens.routines.RoutineListScreen
import com.example.gymtracker.ui.screens.routines.RoutineListViewModel
import com.example.gymtracker.ui.screens.workout.ActiveWorkoutScreen
import com.example.gymtracker.ui.screens.workout.ActiveWorkoutViewModel

@Composable
fun GymNavGraph(
    repository: GymRepository,
    exerciseRepository: ExerciseRepository,
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                GymBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToActiveWorkout = { workoutId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(workoutId))
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Routines.route) {
                val routineListViewModel: RoutineListViewModel = viewModel(
                    factory = RoutineListViewModel.Factory(routineRepository, repository)
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

            composable(
                route = Screen.EditRoutine.route,
                arguments = listOf(
                    navArgument("routineId") { type = NavType.LongType }
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

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(workoutRepository, repository)
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Exercises.route) {
                val exerciseListViewModel: ExerciseListViewModel = viewModel(
                    factory = ExerciseListViewModel.Factory(exerciseRepository)
                )
                ExerciseListScreen(viewModel = exerciseListViewModel)
            }

            composable(
                route = Screen.ActiveWorkout.route,
                arguments = listOf(
                    navArgument("workoutId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                    factory = ActiveWorkoutViewModel.Factory(workoutId, repository, workoutRepository)
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
}
