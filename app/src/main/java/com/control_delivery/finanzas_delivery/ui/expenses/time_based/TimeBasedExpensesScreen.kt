package com.control_delivery.finanzas_delivery.ui.expenses.time_based

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.components.TimeBasedExpenseItem

@Composable
fun TimeBasedExpensesScreen(
    viewModel: TimeBasedExpensesViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.expenses) { expense ->
            TimeBasedExpenseItem(expense = expense)
        }
    }
}
