package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// TODO
@Composable
fun DistanceExpensesScreen(
    viewModel: DistanceExpensesViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Coming soon: Mileage Expenses (Gasoline, Oil, etc.))")
    }
}