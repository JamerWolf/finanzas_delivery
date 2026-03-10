package com.control_delivery.finanzas_delivery.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager
import com.control_delivery.finanzas_delivery.domain.usecases.CompleteTripUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTripsFlowUseCase
import com.control_delivery.finanzas_delivery.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTripsFlowUseCase: GetTripsFlowUseCase,
    private val activeTripManager: ActiveTripManager,
    private val completeTripUseCase: CompleteTripUseCase
) : ViewModel() {
    var uiState by mutableStateOf(HomeScreenUiState())
        private set

    init {
        // Observe completed trips for today
        viewModelScope.launch {
            val (start, end) = DateUtils.getTimestampRange("TODAY")
            getTripsFlowUseCase(start, end).collect { trips ->
                uiState = uiState.copy(tripsCompletedToday = trips.sortedByDescending { it.endTimestamp })
            }
        }

        // Observe active trip
        viewModelScope.launch {
            activeTripManager.activeTrip.collect { trip ->
                uiState = uiState.copy(activeTrip = trip)
            }
        }
    }

    fun onAddOrderClick() {
        uiState = uiState.copy(isAddOrderDialogVisible = true)
    }

    fun onDismissDialog() {
        uiState = uiState.copy(isAddOrderDialogVisible = false)
    }

    fun advanceOrderStatus(order: Order, onTripAutoCompleted: () -> Unit) {
        activeTripManager.advanceOrderStatus(order.id)

        // Check if all orders in the trip are now delivered
        val currentTrip = activeTripManager.activeTrip.value
        if (currentTrip != null && currentTrip.allOrdersDelivered()) {
            completeActiveTrip(onTripAutoCompleted)
        }
    }

    fun completeActiveTrip(onTripCompleted: () -> Unit) {
        viewModelScope.launch {
            completeTripUseCase()
            onTripCompleted()
        }
    }
}
