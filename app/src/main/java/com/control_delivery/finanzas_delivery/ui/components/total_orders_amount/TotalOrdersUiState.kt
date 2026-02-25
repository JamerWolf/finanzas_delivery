package com.control_delivery.finanzas_delivery.ui.components.total_orders_amount

data class TotalOrdersUiState(
    val totalAmountWeek: String = "",
    val totalAmountDay: String = "",
    val isLoading: Boolean = false
)
