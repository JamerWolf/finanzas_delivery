package com.control_delivery.finanzas_delivery.ui.add_order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.usecases.AddOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddOrderViewModel @Inject constructor(
    private val addOrderUseCase: AddOrderUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AddOrderUiState())
        private set

    fun onPlatformChange(newValue: String) {
        uiState = uiState.copy(platform = newValue)
    }
    fun onAddressChange(newValue: String) {
        uiState = uiState.copy(address = newValue)
    }
    fun onAmountChange(newValue: String) {
        uiState = uiState.copy(amount = newValue)
    }

    fun saveOrder(onSuccess: () -> Unit) {
        if (!uiState.isFormValid) return
        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true)

            val amount = uiState.amount.toDoubleOrNull() ?: 0.0
            val newOrder = Order(
                platform = uiState.platform,
                customerAddress = uiState.address,
                totalAmount = amount,
                status = OrderStatus.DELIVERED
            )
            
            addOrderUseCase(newOrder)
            Timber.d("Order saved: $newOrder")
            
            uiState = uiState.copy(isSaving = false)
            onSuccess()
        }
    }

    fun resetState() {
        uiState = AddOrderUiState()
    }
}
