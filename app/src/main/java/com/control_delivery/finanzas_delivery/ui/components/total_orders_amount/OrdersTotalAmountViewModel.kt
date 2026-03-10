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
import kotlinx.coroutines.flow.combine
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

            val (todayStart, todayEnd) = DateUtils.getTimestampRange("TODAY")
            val (weekStart, weekEnd) = DateUtils.getTimestampRange("THIS_WEEK")

            combine(
                getAmountNetUseCase(todayStart, todayEnd),
                getAmountNetUseCase(weekStart, weekEnd)
            ) { dailyNet, weeklyNet ->
                TotalOrdersUiState(
                    totalAmountDay = currencyFormat.format(dailyNet),
                    totalAmountWeek = currencyFormat.format(weeklyNet),
                    isLoading = false
                )
            }.collect { newState ->
                uiState = newState
            }
        }
    }
}
