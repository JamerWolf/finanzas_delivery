package com.control_delivery.finanzas_delivery.ui.expenses.time_based

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense

/**
 * Status that defines the information visible in the time expense tab.
 */
data class TimeBasedExpensesUiState(
    val expenses: List<TimeBasedExpense> = emptyList(),
    val isLoading: Boolean = false,
    val isAddDialogVisible: Boolean = false,
    val errorMessage: String? = null
)
