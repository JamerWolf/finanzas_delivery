package com.control_delivery.finanzas_delivery.ui.order_detail

import com.control_delivery.finanzas_delivery.domain.model.Order

data class OrderDetailUiState(
    val platform: String = "",
    val address: String = "",
    val km: String = "",
    val dateText: String = "",
    val grossAmount: String = "",
    val kmDeduction: String = "",
    val netAmount: String = "",
    val timeExpensesAmount: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)