package com.control_delivery.finanzas_delivery.ui.trip_detail

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.RoutePoint

data class TripDetailUiState(
    val trip: Trip? = null,
    val snappedRoute: List<RoutePoint> = emptyList(),
    val isSnappingRoute: Boolean = false,
    val dateText: String = "",
    val statusText: String = "",
    val totalDistanceText: String = "",
    val grossAmountText: String = "",
    val netAmountText: String = "",
    val kmDeductionText: String = "",
    val timeDeductionText: String = "",
    val kmBreakdown: Map<String, Long> = emptyMap(),
    val timeBreakdown: Map<String, Long> = emptyMap(),
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleteDialogVisible: Boolean = false,
    val isInfoPopupVisible: Boolean = false
)
