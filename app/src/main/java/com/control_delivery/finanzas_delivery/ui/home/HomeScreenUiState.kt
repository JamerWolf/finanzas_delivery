package com.control_delivery.finanzas_delivery.ui.home

import com.control_delivery.finanzas_delivery.domain.model.Order

data class HomeScreenUiState(
    val ordersDeliveredToday: List<Order> = emptyList(),
    val isAddOrderDialogVisible: Boolean = false,
)
