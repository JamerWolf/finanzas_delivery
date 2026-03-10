package com.control_delivery.finanzas_delivery.ui.expenses.time_based

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.usecases.AddTimeBasedExpenseUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.DeleteTimeBasedExpenseUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.GetTimeBasedExpensesUseCase
import com.control_delivery.finanzas_delivery.domain.usecases.UpdateTimeBasedExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeBasedExpensesViewModel @Inject constructor(
    private val getTimeBasedExpensesUseCase: GetTimeBasedExpensesUseCase,
    private val addTimeBasedExpenseUseCase: AddTimeBasedExpenseUseCase,
    private val deleteTimeBasedExpenseUseCase: DeleteTimeBasedExpenseUseCase,
    private val updateTimeBasedExpenseUseCase: UpdateTimeBasedExpenseUseCase
) : ViewModel() {
    var uiState by mutableStateOf(TimeBasedExpensesUiState())
        private set

    init {
        loadExpenses()
    }

    fun onAddClick() {
        uiState = uiState.copy(isAddDialogVisible = true)
    }

    fun onDismissDialog() {
        uiState = uiState.copy(isAddDialogVisible = false, expenseToEdit = null)
    }

    fun onSaveExpense(expense: TimeBasedExpense) {
        viewModelScope.launch {
            addTimeBasedExpenseUseCase(expense)
            onDismissDialog()
        }
    }

    // --- Edit ---

    fun onEditClick(expense: TimeBasedExpense) {
        uiState = uiState.copy(expenseToEdit = expense)
    }

    fun onUpdateExpense(expense: TimeBasedExpense) {
        viewModelScope.launch {
            updateTimeBasedExpenseUseCase(expense)
            uiState = uiState.copy(expenseToEdit = null)
        }
    }

    // --- Delete ---

    fun onDeleteClick(expense: TimeBasedExpense) {
        uiState = uiState.copy(expenseToDelete = expense)
    }

    fun onDismissDelete() {
        uiState = uiState.copy(expenseToDelete = null)
    }

    fun onConfirmDelete() {
        val expense = uiState.expenseToDelete ?: return
        viewModelScope.launch {
            deleteTimeBasedExpenseUseCase(expense.id)
            uiState = uiState.copy(expenseToDelete = null)
        }
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
}
