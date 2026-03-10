package com.control_delivery.finanzas_delivery.ui.add_order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager
import com.control_delivery.finanzas_delivery.domain.usecases.UpdateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrderViewModel @Inject constructor(
    private val activeTripManager: ActiveTripManager,
    private val updateOrderUseCase: UpdateOrderUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AddOrderUiState())
        private set

    fun loadOrder(order: Order) {
        uiState = uiState.copy(
            orderId = order.id,
            platform = order.platform,
            address = order.customerAddress,
            amount = order.totalAmount.toString(),
            isEditing = true
        )
    }

    fun onPlatformChange(newValue: String) {
        uiState = uiState.copy(platform = newValue)
    }
    fun onAddressChange(newValue: String) {
        uiState = uiState.copy(address = newValue)
    }
    fun onAmountChange(newValue: String) {
        uiState = uiState.copy(amount = newValue)
    }

    /**
     * @param onSuccess Callback invoked when saving is complete.
     * The boolean indicates if a NEW trip was started (true) or just an order added/edited (false).
     */
    fun saveOrder(onSuccess: (isNewTrip: Boolean) -> Unit) {
        if (!uiState.isFormValid) return
        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true)
            var startedNewTrip = false

            val amount = uiState.amount.toDoubleOrNull()?.toLong() ?: 0L

            if (uiState.isEditing && uiState.orderId != null) {
                // If it's an active trip order, update it in ActiveTripManager
                val activeTrip = activeTripManager.activeTrip.value
                val isActiveOrder = activeTrip?.orders?.any { it.id == uiState.orderId } == true

                if (isActiveOrder) {
                    val existingOrder = activeTrip!!.orders.first { it.id == uiState.orderId }
                    activeTripManager.updateOrder(
                        existingOrder.copy(
                            platform = uiState.platform,
                            customerAddress = uiState.address,
                            totalAmount = amount
                        )
                    )
                } else {
                    // Update past order in the repository
                    updateOrderUseCase(
                        oldOrderId = uiState.orderId!!,
                        updatedPlatform = uiState.platform,
                        updatedAddress = uiState.address,
                        updatedAmount = amount
                    )
                }
            } else {
                // New Order
                val newOrder = Order(
                    platform = uiState.platform,
                    customerAddress = uiState.address,
                    totalAmount = amount,
                    status = OrderStatus.ON_THE_WAY_TO_RECEIVE
                )
                
                if (activeTripManager.isTracking) {
                    activeTripManager.addOrder(newOrder)
                } else {
                    activeTripManager.startTrip(newOrder)
                    startedNewTrip = true
                }
            }
            
            uiState = uiState.copy(isSaving = false)
            onSuccess(startedNewTrip)
        }
    }

    fun resetState() {
        uiState = AddOrderUiState()
    }
}
