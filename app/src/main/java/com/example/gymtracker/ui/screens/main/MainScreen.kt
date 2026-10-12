package com.example.gymtracker.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.AppNavigation
import com.example.gymtracker.ui.navigation.Screen

/**
 * Pantalla principal con Scaffold que integra AppNavigation y la barra de navegación inferior.
 * Oculta automáticamente la barra inferior en pantalla de entrenamiento activo y editor de rutina
 * para maximizar el área de trabajo y evitar distracciones.
 */
@Composable
fun MainScreen(
    exerciseRepository: ExerciseRepository,
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    gymRepository: GymRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ocultar la barra inferior en entrenamientos activos, edición de rutinas o detalle de ejercicio
    val isFullscreenScreen = currentRoute?.startsWith("active_workout") == true ||
        currentRoute?.startsWith("edit_routine") == true ||
        currentRoute?.startsWith("exercise_detail") == true
    val showBottomBar = !isFullscreenScreen && currentRoute != null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                MainBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Routines.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("main_screen")
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            exerciseRepository = exerciseRepository,
            routineRepository = routineRepository,
            workoutRepository = workoutRepository,
            gymRepository = gymRepository,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun MainBottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("main_bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                modifier = Modifier.testTag("nav_item_${screen.route}"),
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        onNavigateToRoute(screen.route)
                    }
                },
                icon = {
                    screen.icon?.let { iconVector ->
                        Icon(
                            imageVector = iconVector,
                            contentDescription = screen.title
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
