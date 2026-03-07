package com.control_delivery.finanzas_delivery.ui.components.bottom_nav_bar


import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.control_delivery.finanzas_delivery.navigation.Screen

@Composable
fun AppBottomBar(navController: NavHostController) {
    // List of screens we want on the bar
    val items = listOf(
        Screen.Home,
        Screen.Expenses
    )
    NavigationBar {
        // We observe which screen we are currently on.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    // We avoid navigating to the same screen if we are already on it.
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // When navigating to the start, we clear the history so as not to accumulate screens.
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}