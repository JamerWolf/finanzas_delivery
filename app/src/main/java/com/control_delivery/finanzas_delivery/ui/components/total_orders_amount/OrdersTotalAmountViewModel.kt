package com.control_delivery.finanzas_delivery.ui.components.total_orders_amount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.usecases.GetAmountNetUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetOrdersTotalAmountUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.SyncTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrdersTotalAmountViewModel @Inject constructor(
    private val getAmountNetUseCase: GetAmountNetUseCase,
    private val syncTimeBasedExpensesUseCase: SyncTimeBasedExpensesUseCase
) : ViewModel() {
    var uiState by mutableStateOf(TotalOrdersUiState())
        private set

    init {
        viewModelScope.launch {
            syncTimeBasedExpensesUseCase()

            // Observar monto bruto semanal
            launch {
                val (start, end) = DateUtils.getTimestampRange("THIS_WEEK")
                getAmountNetUseCase(start, end).collect { weeklyNet ->
                    uiState = uiState.copy(
                        totalAmountWeek = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(weeklyNet)
                    )
                }
            }

            // Observar monto bruto diario
            launch {
                val (start, end) = DateUtils.getTimestampRange("TODAY")
                getAmountNetUseCase(start, end).collect { total ->
                    uiState = uiState.copy(
                        totalAmountDay = NumberFormat.getCurrencyInstance(Locale.GERMANY).format(total)
                    )
                }
            }
        }
    }
}
