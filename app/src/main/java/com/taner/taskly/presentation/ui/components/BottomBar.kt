package com.taner.taskly.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.taner.taskly.presentation.ui.theme.TasklyTheme

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Add", Icons.Default.Add, "add_task"),
        BottomNavItem("Habits", Icons.Default.History, "habits"),
        BottomNavItem("Stats", Icons.Default.BarChart, "stats"),
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, tint = MaterialTheme.colorScheme.onSecondary, contentDescription = item.title) },
                label = { Text(item.title, color = MaterialTheme.colorScheme.onSecondary) },
                selected = currentRoute == item.route,
                colors =  NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSecondary,
                    disabledIconColor = MaterialTheme.colorScheme.onSecondary,
                    disabledTextColor = MaterialTheme.colorScheme.onSecondary,
                ),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(currentRoute ?: item.route) { inclusive = true }
                        launchSingleTop = true
                    }


                }
            )
        }
    }
}