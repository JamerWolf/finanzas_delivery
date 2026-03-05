package com.control_delivery.finanzas_delivery.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
/**
 * Represents the application screens.
 * Each screen has a path (ID), a title for the bar, and an icon.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    // Home Screen
    object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )
    // Expenses Screen
    object Expenses : Screen(
        route = "expenses",
        title = "Expenses",
        icon = Icons.Default.Wallet
    )

    // Order Details Screen (Does not appear in the BottomBar)
    object OrderDetail : Screen(
        route = "order_detail/{orderId}",
        title = "Order Details",
        icon = Icons.Default.Home
    ) {
        /**
         * Auxiliary function to generate the actual route by injecting the ID.
         */
        fun createRoute(orderId: String): String = "order_detail/$orderId"
    }
}