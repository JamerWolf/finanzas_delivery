package com.control_delivery.finanzas_delivery.ui.trip_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteTripUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetDistanceBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetSnappedRouteUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTripByIdUseCase
import com.control_delivery.finanzas_delivery.utils.AppConfig
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
    private val getDistanceBasedExpensesUseCase: GetDistanceBasedExpensesUseCase,
    private val tripRepository: TripRepository
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
                        error = null,
                        snappedRoute = trip.snappedRoute ?: emptyList()
                    )
                    
                    // If feature is enabled and snapped route is not yet in cache, try to fetch it
                    if (AppConfig.FEATURE_ROUTE_SNAPPING && trip.snappedRoute == null && trip.route.isNotEmpty() && !uiState.isSnappingRoute) {
                        snapAndCacheRoute(trip)
                    }
                    
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Trip not found")
                }
            }
        }
    }
    
    private fun snapAndCacheRoute(trip: com.control_delivery.finanzas_delivery.domain.model.Trip) {
        viewModelScope.launch {
            uiState = uiState.copy(isSnappingRoute = true)
            val snapped = getSnappedRouteUseCase(trip.route)
            
            if (snapped != null) {
                // SUCCESS: Save to DB cache
                val updatedTrip = trip.copy(snappedRoute = snapped)
                tripRepository.updateTrip(updatedTrip)
                
                uiState = uiState.copy(
                    snappedRoute = snapped,
                    isSnappingRoute = false
                )
            } else {
                // FAILURE: Just show raw route for now, don't update DB
                uiState = uiState.copy(
                    isSnappingRoute = false
                )
            }
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
