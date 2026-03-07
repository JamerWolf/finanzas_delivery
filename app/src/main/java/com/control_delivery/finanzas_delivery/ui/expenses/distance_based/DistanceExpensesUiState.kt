package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

/**
 * Status that defines the information visible in the distance expenses tab (KM).
 */
data class DistanceExpensesUiState(
    val expenses: List<Any> = emptyList(),
    val isLoading: Boolean = false
)
