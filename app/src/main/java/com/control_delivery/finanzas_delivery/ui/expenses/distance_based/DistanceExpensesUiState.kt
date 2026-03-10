package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense

/**
 * Status that defines the information visible in the distance expenses tab (KM).
 */
data class DistanceExpensesUiState(
    val expenses: List<DistanceExpenseItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val isAddDialogVisible: Boolean = false,
    val expenseToEdit: DistanceBasedExpense? = null,
    val expenseToDelete: DistanceBasedExpense? = null,
    val error: String? = null
)
