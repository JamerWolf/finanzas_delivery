package com.control_delivery.finanzas_delivery.ui.order_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteOrderUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetOrderByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val deleteOrderUseCase: DeleteOrderUseCase
) : ViewModel() {
    var uiState by mutableStateOf(OrderDetailUiState())
        private set

    fun showDeleteDialog() {
        uiState = uiState.copy(isDeleteDialogVisible = true)
    }

    fun dismissDeleteDialog() {
        uiState = uiState.copy(isDeleteDialogVisible = false)
    }

    fun showEditDialog() {
        uiState = uiState.copy(isEditDialogVisible = true)
    }

    fun dismissEditDialog() {
        uiState = uiState.copy(isEditDialogVisible = false)
    }

    fun deleteOrder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteOrderUseCase(orderId)
            onSuccess()
        }
    }
    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            getOrderByIdUseCase(orderId).collect { order ->
                if (order != null) {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
                        maximumFractionDigits = 0
                    }

                    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    
                    val pickupDistance = order.distances.filterIsInstance<DistanceType.ToPickup>().sumOf { it.value }
                    val deliveryDistance = order.distances.filterIsInstance<DistanceType.ToDelivery>().sumOf { it.value }

                    uiState = uiState.copy(
                        order = order,
                        platform = order.platform.uppercase(),
                        address = order.customerAddress,
                        dateText = order.toLocalDate().atStartOfDay().format(dateFormat),
                        grossAmount = currencyFormat.format(order.totalAmount),
                        kmDeduction = currencyFormat.format(order.kmDeduction),
                        netAmount = currencyFormat.format(order.netAmount),
                        km = String.format(Locale.GERMANY, "%.1f", order.getTotalDistance()),
                        pickupKm = String.format(Locale.GERMANY, "%.1f", pickupDistance),
                        deliveryKm = String.format(Locale.GERMANY, "%.1f", deliveryDistance),
                        timeExpensesAmount = currencyFormat.format(order.timeExpensesDeduction),
                        kmDeductionsBreakdown = order.kmDeductionsBreakdown.mapValues { currencyFormat.format(it.value) },
                        timeExpensesBreakdown = order.timeExpensesDeductionsBreakdown.mapValues { currencyFormat.format(it.value) },
                        isLoading = false
                    )


                } else {
                    uiState = uiState.copy(isLoading = false, error = "Orden no encontrada")
                }
            }
        }
    }
}