package com.control_delivery.finanzas_delivery.ui.order_detail

import com.control_delivery.finanzas_delivery.domain.model.Order

data class OrderDetailUiState(
    val order: Order? = null,
    val platform: String = "",
    val address: String = "",
    val km: String = "",
    val pickupKm: String = "",
    val deliveryKm: String = "",
    val dateText: String = "",
    val grossAmount: String = "",
    val kmDeduction: String = "",
    val netAmount: String = "",
    val timeExpensesAmount: String = "",
    val kmDeductionsBreakdown: Map<String, String> = emptyMap(),
    val timeExpensesBreakdown: Map<String, String> = emptyMap(),
    val isDeleteDialogVisible: Boolean = false,
    val isEditDialogVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
