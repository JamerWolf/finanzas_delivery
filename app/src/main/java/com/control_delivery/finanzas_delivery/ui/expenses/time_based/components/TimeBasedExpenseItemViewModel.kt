package com.control_delivery.finanzas_delivery.ui.expenses.time_based.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpenseByIdUseCase
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.TimeBasedExpensesViewModel
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
class TimeBasedExpenseItemViewModel @Inject constructor(
    private val getTimeBasedExpenseByIdUseCase: GetTimeBasedExpenseByIdUseCase
) : ViewModel() {
    var state by mutableStateOf(TimeBasedExpenseItemUiState())
        private set

    /**
     * Initialize the observer for a specific expense.
     */
    fun initialize(expenseId: String) {
        viewModelScope.launch {
            getTimeBasedExpenseByIdUseCase(expenseId).collect { expense ->
                expense?.let { updateUiState(it) }
            }
        }
    }

    /**
     * Mapper function to transform a Domain model into a UI State model.
     * This keeps formatting and time logic outside the Composable functions.
     */
    private fun updateUiState(expense: TimeBasedExpense) {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val deadlineDate = Instant.ofEpochMilli(expense.nextDeadline).atZone(zoneId).toLocalDate()
        val daysLeft = ChronoUnit.DAYS.between(today, deadlineDate).coerceAtLeast(0)

        val progressValue = (expense.accumulatedAmount / expense.amount).toFloat().coerceIn(0f, 1f)
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)

        state = TimeBasedExpenseItemUiState(
            id = expense.id,
            description = expense.description,
            daysLeftText = if (daysLeft > 0) "In $daysLeft days" else "¡Today!",
            // Using a darker green for better contrast in Light mode
            daysLeftColor = if (daysLeft <= 1) Color.Red else Color(0xFF2E7D32),
            progress = progressValue,
            progressText = "${(progressValue * 100).toInt()}%",
            savedAmountText = "Saved: ${currencyFormat.format(expense.accumulatedAmount)}",
            goalAmountText = "Goal: ${currencyFormat.format(expense.amount)}"
        )
    }
}
