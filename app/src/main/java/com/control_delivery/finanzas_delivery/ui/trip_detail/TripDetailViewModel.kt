package com.control_delivery.finanzas_delivery.ui.trip_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.usecases.GetTripByIdUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteTripUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetSnappedRouteUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetDistanceBasedExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val getSnappedRouteUseCase: GetSnappedRouteUseCase,
    private val getTimeBasedExpensesUseCase: GetTimeBasedExpensesUseCase,
    private val getDistanceBasedExpensesUseCase: GetDistanceBasedExpensesUseCase
) : ViewModel() {
    var uiState by mutableStateOf(TripDetailUiState())
        private set

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            combine(
                getTripByIdUseCase(tripId),
                getTimeBasedExpensesUseCase(),
                getDistanceBasedExpensesUseCase()
            ) { trip, timeExpenses, distanceExpenses ->
                Triple(trip, timeExpenses, distanceExpenses)
            }.collect { (trip, timeExpenses, distanceExpenses) ->
                if (trip != null) {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
                        maximumFractionDigits = 0
                    }
                    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    
                    val activeDescs = (timeExpenses.map { it.description } + distanceExpenses.map { it.description }).toSet()

                    uiState = uiState.copy(
                        trip = trip,
                        dateText = trip.toLocalDate().atStartOfDay().format(dateFormat),
                        statusText = trip.status.name,
                        totalDistanceText = String.format(Locale.US, "%.1f km", trip.totalDistanceKm),
                        grossAmountText = currencyFormat.format(trip.totalOrdersAmount),
                        netAmountText = currencyFormat.format(trip.netAmount),
                        kmDeductionText = currencyFormat.format(trip.kmDeduction),
                        timeDeductionText = currencyFormat.format(trip.timeExpensesDeduction),
                        kmBreakdown = trip.kmDeductionsBreakdown,
                        timeBreakdown = trip.timeExpensesDeductionsBreakdown,
                        activeExpenseDescriptions = activeDescs,
                        orders = trip.orders.filter { !it.isDeleted }, // Don't display soft-deleted orders
                        isLoading = false,
                        error = null
                    )
                    
                    // Fetch snapped route from OSRM if there is a raw route and we haven't snapped it yet
                    if (trip.route.isNotEmpty() && uiState.snappedRoute.isEmpty() && !uiState.isSnappingRoute) {
                        snapRoute(trip.route)
                    }
                    
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Trip not found")
                }
            }
        }
    }
    
    private fun snapRoute(rawRoute: List<com.control_delivery.finanzas_delivery.domain.model.RoutePoint>) {
        viewModelScope.launch {
            uiState = uiState.copy(isSnappingRoute = true)
            val snapped = getSnappedRouteUseCase(rawRoute)
            uiState = uiState.copy(
                snappedRoute = snapped,
                isSnappingRoute = false
            )
        }
    }

    fun showDeleteDialog() {
        uiState = uiState.copy(isDeleteDialogVisible = true)
    }

    fun dismissDeleteDialog() {
        uiState = uiState.copy(isDeleteDialogVisible = false)
    }

    fun deleteTrip(tripId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteTripUseCase(tripId)
            onSuccess()
        }
    }

    fun toggleInfoPopup() {
        uiState = uiState.copy(isInfoPopupVisible = !uiState.isInfoPopupVisible)
    }
}
