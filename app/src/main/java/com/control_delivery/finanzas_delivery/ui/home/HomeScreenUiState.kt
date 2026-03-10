package com.control_delivery.finanzas_delivery.ui.home

import com.control_delivery.finanzas_delivery.domain.model.Trip

data class HomeScreenUiState(
    val tripsCompletedToday: List<Trip> = emptyList(),
    val activeTrip: Trip? = null,
    val isAddOrderDialogVisible: Boolean = false,
)