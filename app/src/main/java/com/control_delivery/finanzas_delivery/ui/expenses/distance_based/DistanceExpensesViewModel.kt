package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.usecases.AddDistanceBasedExpenseUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteDistanceBasedExpenseUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetDistanceBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.ResetDistanceExpenseUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.UpdateDistanceBasedExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DistanceExpensesViewModel @Inject constructor(
    private val getDistanceBasedExpensesUseCase: GetDistanceBasedExpensesUseCase,
    private val resetDistanceExpenseUseCase: ResetDistanceExpenseUseCase,
    private val addDistanceBasedExpenseUseCase: AddDistanceBasedExpenseUseCase,
    private val deleteDistanceBasedExpenseUseCase: DeleteDistanceBasedExpenseUseCase,
    private val updateDistanceBasedExpenseUseCase: UpdateDistanceBasedExpenseUseCase
) : ViewModel() {

    var uiState by mutableStateOf(DistanceExpensesUiState())
        private set

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
        maximumFractionDigits = 0
    }

    /** Map of domain models by id for lookup when swipe triggers edit/delete */
    private var domainExpensesById = mapOf<String, DistanceBasedExpense>()

    init {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            getDistanceBasedExpensesUseCase().collect { domainList ->
                domainExpensesById = domainList.associateBy { it.id }
                val uiList = domainList.map { mapToUiState(it) }
                uiState = uiState.copy(
                    expenses = uiList,
                    isLoading = false
                )
            }
        }
    }

    // --- Add ---

    fun onAddClick() {
        uiState = uiState.copy(isAddDialogVisible = true)
    }

    fun onDismissDialog() {
        uiState = uiState.copy(isAddDialogVisible = false, expenseToEdit = null)
    }

    fun onSaveExpense(expense: DistanceBasedExpense) {
        viewModelScope.launch {
            addDistanceBasedExpenseUseCase(expense)
            onDismissDialog()
        }
    }

    // --- Edit ---

    fun onEditClick(id: String) {
        val expense = domainExpensesById[id] ?: return
        uiState = uiState.copy(expenseToEdit = expense)
    }

    fun onUpdateExpense(expense: DistanceBasedExpense) {
        viewModelScope.launch {
            updateDistanceBasedExpenseUseCase(expense)
            uiState = uiState.copy(expenseToEdit = null)
        }
    }

    // --- Delete ---

    fun onDeleteClick(id: String) {
        val expense = domainExpensesById[id] ?: return
        uiState = uiState.copy(expenseToDelete = expense)
    }

    fun onDismissDelete() {
        uiState = uiState.copy(expenseToDelete = null)
    }

    fun onConfirmDelete() {
        val expense = uiState.expenseToDelete ?: return
        viewModelScope.launch {
            deleteDistanceBasedExpenseUseCase(expense.id)
            uiState = uiState.copy(expenseToDelete = null)
        }
    }

    // --- Reset ---

    fun onResetExpense(id: String) {
        viewModelScope.launch {
            resetDistanceExpenseUseCase(id)
        }
    }

    // --- Mapper ---

    private fun mapToUiState(expense: DistanceBasedExpense): DistanceExpenseItemUiState {
        val type = expense.type

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
                kmGoalText = "Service: ${String.format("%.0f", type.targetKm)} km"
            )
        } else if (type is DistanceExpenseType.PureDeduction) {
            DistanceExpenseItemUiState(
                id = expense.id,
                description = expense.description,
                costPerKmText = "Cost: ${currencyFormat.format(expense.costPerKm)}/km",
                isSavingsGoal = false,
                pricePerUnitText = "Unit Price: ${currencyFormat.format(type.pricePerUnit)}",
                kmPerUnitText = "Yield: ${type.kmPerUnit} km/unit"
            )
        } else {
            DistanceExpenseItemUiState(id = expense.id, description = expense.description, costPerKmText = "", isSavingsGoal = false)
        }
    }
}
