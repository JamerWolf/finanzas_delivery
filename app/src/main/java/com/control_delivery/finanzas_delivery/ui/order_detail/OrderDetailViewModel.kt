package com.control_delivery.finanzas_delivery.ui.order_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.usecases.GetOrderByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase
) : ViewModel() {
    var uiState by mutableStateOf(OrderDetailUiState())
        private set
    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            getOrderByIdUseCase(orderId).collect { order ->
                if (order != null) {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    uiState = uiState.copy(
                        platform = order.platform.uppercase(),
                        address = order.customerAddress,
                        dateText = order.toLocalDate().atStartOfDay().format(dateFormat),
                        grossAmount = currencyFormat.format(order.totalAmount),
                        kmDeduction = currencyFormat.format(order.kmDeduction),
                        netAmount = currencyFormat.format(order.netAmount),
                        km = "3.2",
                        timeExpensesAmount = currencyFormat.format(order.timeExpensesDeduction),
                        isLoading = false
                    )
                } else {
                    uiState = uiState.copy(isLoading = false, error = "Orden no encontrada")
                }
            }
        }
    }
}