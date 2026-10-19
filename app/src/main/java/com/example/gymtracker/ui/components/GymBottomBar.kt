package com.example.gymtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtracker.ui.navigation.Screen
import com.example.ui.theme.GymBottomBarGlass
import com.example.ui.theme.GymGreenLight
import com.example.ui.theme.GymGreenPrimary
import com.example.ui.theme.GymTextSecondary
import com.example.ui.theme.GymTopHairlineBorder

@Composable
fun GymBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GymBottomBarGlass)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = GymTopHairlineBorder
        )
        NavigationBar(
            modifier = Modifier.testTag("gym_bottom_nav_bar"),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    modifier = Modifier.testTag("nav_item_${screen.route}"),
                    selected = selected,
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
                            screen.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = screen.title
                                )
                            }
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(GymGreenLight)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
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
