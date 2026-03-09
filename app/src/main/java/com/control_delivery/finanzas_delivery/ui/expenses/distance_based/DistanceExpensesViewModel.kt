package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.usecases.GetDistanceBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.ResetDistanceExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DistanceExpensesViewModel @Inject constructor(
    private val getDistanceBasedExpensesUseCase: GetDistanceBasedExpensesUseCase,
    private val resetDistanceExpenseUseCase: ResetDistanceExpenseUseCase
) : ViewModel() {

    var uiState by mutableStateOf(DistanceExpensesUiState())
        private set

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
        maximumFractionDigits = 0
    }

    init {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            getDistanceBasedExpensesUseCase().collect { domainList ->
                val uiList = domainList.map { mapToUiState(it) }
                uiState = uiState.copy(
                    expenses = uiList,
                    isLoading = false
                )
            }
        }
    }

    private fun mapToUiState(expense: DistanceBasedExpense): DistanceExpenseItemUiState {
        val type = expense.type
        val appliedToText = expense.appliedTo.joinToString(", ") { 
            it.simpleName.replace("To", "") 
        }

        return if (type is DistanceExpenseType.SavingsGoal) {
            val moneyProgress = if (type.targetAmount > 0) {
                (type.accumulatedAmount.toFloat() / type.targetAmount.toFloat()).coerceIn(0f, 1f)
            } else 0f
            
            val kmProgress = if (type.targetKm > 0) {
                (type.accumulatedKm.toFloat() / type.targetKm.toFloat()).coerceIn(0f, 1f)
            } else 0f

            DistanceExpenseItemUiState(
                id = expense.id,
                description = expense.description,
                costPerKmText = "Cost: ${currencyFormat.format(expense.costPerKm)}/km",
                isSavingsGoal = true,
                isGoalReached = expense.isGoalReached(),
                moneyProgress = moneyProgress,
                moneyProgressText = "${(moneyProgress * 100).toInt()}%",
                moneySavedText = "Saved: ${currencyFormat.format(type.accumulatedAmount)}",
                moneyGoalText = "Goal: ${currencyFormat.format(type.targetAmount)}",
                kmProgress = kmProgress,
                kmProgressText = "${(kmProgress * 100).toInt()}%",
                kmCurrentText = "Driven: ${String.format("%.1f", type.accumulatedKm)} km",
                kmGoalText = "Service: ${String.format("%.0f", type.targetKm)} km",
                appliedToText = "Applies to: $appliedToText"
            )
        } else if (type is DistanceExpenseType.PureDeduction) {
            DistanceExpenseItemUiState(
                id = expense.id,
                description = expense.description,
                costPerKmText = "Cost: ${currencyFormat.format(expense.costPerKm)}/km",
                isSavingsGoal = false,
                pricePerUnitText = "Unit Price: ${currencyFormat.format(type.pricePerUnit)}",
                kmPerUnitText = "Yield: ${type.kmPerUnit} km/unit",
                appliedToText = "Applies to: $appliedToText"
            )
        } else {
            DistanceExpenseItemUiState(id = expense.id, description = expense.description, costPerKmText = "", isSavingsGoal = false)
        }
    }

    fun onResetExpense(id: String) {
        viewModelScope.launch {
            resetDistanceExpenseUseCase(id)
        }
    }
}
