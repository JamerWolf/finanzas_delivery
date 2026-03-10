package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


class DistanceBasedExpenseInMemoryRepository: DistanceBasedExpenseRepository {

    private val _expensesFlow = MutableStateFlow(DistanceBasedExpensesFake.expenses.toList())

    override fun getDistanceBasedExpenses(): Flow<List<DistanceBasedExpense>> {
        return _expensesFlow.asStateFlow()
    }

    override suspend fun saveExpense(expense: DistanceBasedExpense) {
        val index = DistanceBasedExpensesFake.expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            DistanceBasedExpensesFake.expenses[index] = expense
        } else {
            DistanceBasedExpensesFake.expenses.add(expense)
        }
        _expensesFlow.value = DistanceBasedExpensesFake.expenses.toList()
    }

    override suspend fun updateExpenses(expenses: List<DistanceBasedExpense>) {
        expenses.forEach { updatedExpense ->
            val index = DistanceBasedExpensesFake.expenses.indexOfFirst { it.id == updatedExpense.id }
            if (index != -1) {
                DistanceBasedExpensesFake.expenses[index] = updatedExpense
            }
        }
        _expensesFlow.value = DistanceBasedExpensesFake.expenses.toList()
    }

    override suspend fun resetExpense(id: String) {
        val index = DistanceBasedExpensesFake.expenses.indexOfFirst { it.id == id }
        if (index != -1) {
            val expense = DistanceBasedExpensesFake.expenses[index]
            DistanceBasedExpensesFake.expenses[index] = expense.reset()
            _expensesFlow.value = DistanceBasedExpensesFake.expenses.toList()
        }
    }

    override suspend fun deleteExpense(id: String) {
        val index = DistanceBasedExpensesFake.expenses.indexOfFirst { it.id == id }
        if (index != -1) {
            DistanceBasedExpensesFake.expenses[index] = DistanceBasedExpensesFake.expenses[index].copy(isDeleted = true)
            _expensesFlow.value = DistanceBasedExpensesFake.expenses.toList()
        }
    }
}
