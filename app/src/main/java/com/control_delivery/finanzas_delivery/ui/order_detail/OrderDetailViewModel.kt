package com.control_delivery.finanzas_delivery.ui.order_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteOrderUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetDistanceBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetOrderByIdUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val deleteOrderUseCase: DeleteOrderUseCase,
    private val getTimeBasedExpensesUseCase: GetTimeBasedExpensesUseCase,
    private val getDistanceBasedExpensesUseCase: GetDistanceBasedExpensesUseCase
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

                    // Find deleted time-based expenses to mark with asterisk
                    val allTimeExpenses = getTimeBasedExpensesUseCase(includeDeleted = true).first()
                    val deletedTimeNames = allTimeExpenses
                        .filter { it.isDeleted }
                        .map { it.description }
                        .toSet()
                    val deletedInTimeBreakdown = order.timeExpensesDeductionsBreakdown.keys
                        .filter { it in deletedTimeNames }
                        .toSet()

                    // Find deleted distance-based expenses to mark with asterisk
                    val allKmExpenses = getDistanceBasedExpensesUseCase(includeDeleted = true).first()
                    val deletedKmNames = allKmExpenses
                        .filter { it.isDeleted }
                        .map { it.description }
                        .toSet()
                    val deletedInKmBreakdown = order.kmDeductionsBreakdown.keys
                        .filter { it in deletedKmNames }
                        .toSet()

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
                        deletedTimeExpenseNames = deletedInTimeBreakdown,
                        deletedKmExpenseNames = deletedInKmBreakdown,
                        isLoading = false
                    )


                } else {
                    uiState = uiState.copy(isLoading = false, error = "Orden no encontrada")
                }
            }
        }
    }
}