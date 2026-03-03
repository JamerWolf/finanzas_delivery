package com.control_delivery.finanzas_delivery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.control_delivery.finanzas_delivery.navigation.Screen
import com.control_delivery.finanzas_delivery.ui.expenses.ExpensesScreen
import com.control_delivery.finanzas_delivery.ui.home.HomeScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Association of the "home" route with the Composable HomeScreen
        composable(Screen.Home.route) {
            HomeScreen()
        }
        // Association of the “expenses” route with the Composable ExpensesScreen
        composable(Screen.Expenses.route) {
            ExpensesScreen()
        }
    }
}