package com.control_delivery.finanzas_delivery.ui.add_order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.usecases.AddOrderUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.ProcessOrderIncomeUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.UpdateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddOrderViewModel @Inject constructor(
    private val addOrderUseCase: AddOrderUseCase,
    private val updateOrderUseCase: UpdateOrderUseCase,
    private val processOrderIncomeUseCase: ProcessOrderIncomeUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AddOrderUiState())
        private set

    fun loadOrder(order: Order) {
        val pickupKm = order.distances.filterIsInstance<DistanceType.ToPickup>().sumOf { it.value }
        val deliveryKm = order.distances.filterIsInstance<DistanceType.ToDelivery>().sumOf { it.value }
        
        uiState = uiState.copy(
            orderId = order.id,
            platform = order.platform,
            address = order.customerAddress,
            amount = order.totalAmount.toString(),
            toPickupKm = String.format(Locale.GERMANY, "%.1f", pickupKm),
            toDeliveryKm = String.format(Locale.GERMANY, "%.1f", deliveryKm),
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
    fun onToPickupKmChange(newValue: String) {
        uiState = uiState.copy(toPickupKm = newValue)
    }
    fun onToDeliveryKmChange(newValue: String) {
        uiState = uiState.copy(toDeliveryKm = newValue)
    }

    fun saveOrder(onSuccess: () -> Unit) {
        if (!uiState.isFormValid) return
        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true)

            val amount = uiState.amount.toDoubleOrNull()?.toLong() ?: 0L
            val pickupKm = uiState.toPickupKm.toDoubleOrNull() ?: 0.0
            val deliveryKm = uiState.toDeliveryKm.toDoubleOrNull() ?: 0.0
            val distances = listOf(
                DistanceType.ToPickup(pickupKm),
                DistanceType.ToDelivery(deliveryKm)
            )

            if (uiState.isEditing && uiState.orderId != null) {
                updateOrderUseCase(
                    oldOrderId = uiState.orderId!!,
                    updatedPlatform = uiState.platform,
                    updatedAddress = uiState.address,
                    updatedAmount = amount,
                    updatedDistances = distances
                )
            } else {
                val processingResult = processOrderIncomeUseCase(amount, distances)
                val newOrder = Order(
                    platform = uiState.platform,
                    customerAddress = uiState.address,
                    totalAmount = amount,
                    kmDeduction = processingResult.kmDeduction,
                    timeExpensesDeduction = processingResult.timeExpensesDeduction,
                    kmDeductionsBreakdown = processingResult.kmDeductionsBreakdown,
                    timeExpensesDeductionsBreakdown = processingResult.timeExpensesDeductionsBreakdown,
                    distances = distances,
                    status = OrderStatus.DELIVERED
                )
                addOrderUseCase(newOrder)
            }
            
            uiState = uiState.copy(isSaving = false)
            onSuccess()
        }
    }

    fun resetState() {
        uiState = AddOrderUiState()
    }
}
