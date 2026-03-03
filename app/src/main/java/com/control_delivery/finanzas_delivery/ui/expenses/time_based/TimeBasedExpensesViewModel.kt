package com.control_delivery.finanzas_delivery.ui.expenses.time_based

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.components.TimeBasedExpenseItemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TimeBasedExpensesViewModel @Inject constructor(
    private val getTimeBasedExpensesUseCase: GetTimeBasedExpensesUseCase
) : ViewModel() {
    var uiState by mutableStateOf(TimeBasedExpensesUiState())
        private set

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            getTimeBasedExpensesUseCase().collect { expensesList ->
                uiState = uiState.copy(
                    expenses = expensesList,
                    isLoading = false
                )
            }
        }
    }

    //TODO: hacer un metodo que reciva una expense y llamar a un metodo del viewmodel de expenseitem para
    // configurar el estado del expenseitem.
}
