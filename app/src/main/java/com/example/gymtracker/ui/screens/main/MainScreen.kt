package com.example.gymtracker.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymtracker.domain.repository.ExerciseRepository
import com.example.gymtracker.domain.repository.GymRepository
import com.example.gymtracker.domain.repository.RoutineRepository
import com.example.gymtracker.domain.repository.WeightRepository
import com.example.gymtracker.domain.repository.WorkoutRepository
import com.example.gymtracker.ui.navigation.AppNavigation
import com.example.gymtracker.ui.navigation.Screen
import com.example.ui.theme.GymBottomBarGlass
import com.example.ui.theme.GymEmeraldGlow
import com.example.ui.theme.GymGreenLight
import com.example.ui.theme.GymGreenPrimary
import com.example.ui.theme.GymTextSecondary
import com.example.ui.theme.GymTopHairlineBorder

/**
 * Pantalla principal con Scaffold que integra AppNavigation y la barra de navegación inferior
 * con acabado Carbon OLED & Frosted Glass.
 */
@Composable
fun MainScreen(
    exerciseRepository: ExerciseRepository,
    routineRepository: RoutineRepository,
    workoutRepository: WorkoutRepository,
    gymRepository: GymRepository,
    weightRepository: WeightRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ocultar la barra inferior en entrenamientos activos, edición de rutinas, detalle de ejercicio, perfil RPG o estadísticas de rendimiento
    val isFullscreenScreen = currentRoute?.startsWith("active_workout") == true ||
        currentRoute?.startsWith("edit_routine") == true ||
        currentRoute?.startsWith("exercise_detail") == true ||
        currentRoute?.startsWith("rpg_profile") == true ||
        currentRoute?.startsWith("performance_stats") == true
    val showBottomBar = !isFullscreenScreen && currentRoute != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            weightRepository = weightRepository,
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GymBottomBarGlass)
    ) {
        // Borde superior ultra fino (border-t border-white/10)
        HorizontalDivider(
            thickness = 1.dp,
            color = GymTopHairlineBorder
        )

        NavigationBar(
            modifier = Modifier.testTag("main_bottom_nav_bar"),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val animatedLabelColor by animateColorAsState(
                    targetValue = if (isSelected) GymGreenLight else GymTextSecondary,
                    animationSpec = tween(200),
                    label = "nav_label_color"
                )

                NavigationBarItem(
                    modifier = Modifier.testTag("nav_item_${screen.route}"),
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != screen.route) {
                            onNavigateToRoute(screen.route)
                        }
                    },
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            screen.icon?.let { iconVector ->
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = screen.title
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .shadow(4.dp, CircleShape, ambientColor = GymEmeraldGlow, spotColor = GymGreenLight)
                                        .clip(CircleShape)
                                        .background(GymGreenLight)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.3.sp
                            ),
                            color = animatedLabelColor,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GymGreenLight,
                        selectedTextColor = GymGreenLight,
                        indicatorColor = GymGreenPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = GymTextSecondary,
                        unselectedTextColor = GymTextSecondary
                    )
                )
            }
        }
    }
}
