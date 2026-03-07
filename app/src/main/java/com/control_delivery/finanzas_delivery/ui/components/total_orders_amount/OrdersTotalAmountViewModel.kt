package com.control_delivery.finanzas_delivery.ui.components.total_orders_amount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.usecases.GetAmountNetUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.SyncTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
        maximumFractionDigits = 0
    }

    init {
        viewModelScope.launch {
            syncTimeBasedExpensesUseCase()

            // Observe gross weekly amount
            launch {
                val (start, end) = DateUtils.getTimestampRange("THIS_WEEK")
                getAmountNetUseCase(start, end).collect { weeklyNet ->
                    uiState = uiState.copy(
                        totalAmountWeek = currencyFormat.format(weeklyNet)
                    )
                }
            }

            // Observe daily gross amount
            launch {
                val (start, end) = DateUtils.getTimestampRange("TODAY")
                getAmountNetUseCase(start, end).collect { total ->
                    Timber.d("Total: $total")
                    uiState = uiState.copy(
                        totalAmountDay = currencyFormat.format(total)
                    )
                }
            }
        }
    }
}
