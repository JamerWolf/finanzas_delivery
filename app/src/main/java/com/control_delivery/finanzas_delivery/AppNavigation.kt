package com.control_delivery.finanzas_delivery

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.control_delivery.finanzas_delivery.navigation.Screen
import com.control_delivery.finanzas_delivery.ui.expenses.ExpensesScreen
import com.control_delivery.finanzas_delivery.ui.home.HomeScreen
import com.control_delivery.finanzas_delivery.ui.trip_detail.TripDetailScreen

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
            HomeScreen(onTripClick = { tripId ->
                navController.navigate(Screen.TripDetail.createRoute(tripId))
            })
        }
        // Association of the “expenses” route with the Composable ExpensesScreen
        composable(Screen.Expenses.route) {
            ExpensesScreen()
        }
        // Association of the “trip_detail” route with the Composable TripDetailScreen
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripDetailScreen(
                tripId = tripId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}