package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TimeBasedExpenseInMemoryRepository(
    private val expenses: MutableList<TimeBasedExpense> = TimeBasedExpensesFake.expenses
): TimeBasedExpenseRepository {
    private val _expensesFlow = MutableStateFlow(expenses.toList())

    override fun getAllExpenses(): Flow<List<TimeBasedExpense>> {
        val today = LocalDate.now()
        syncExpenses(today)
        return _expensesFlow.asStateFlow()
    }

    override fun getExpenseById(id: String): Flow<TimeBasedExpense?> {
        return _expensesFlow.map { expenses ->
            expenses.find { it.id == id }
        }
    }

    override suspend fun addExpense(expense: TimeBasedExpense) {
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }

    override suspend fun updateExpenses(expenses: List<TimeBasedExpense>) {

        expenses.forEach { updated ->
            val index = this.expenses.indexOfFirst { it.id == updated.id }
            if (index != -1) {
                this.expenses[index] = updated
            }
        }

        _expensesFlow.value = this.expenses.toList()
    }

    override suspend fun subtractContribution(id: String, amount: Long, contributionDate: LocalDate) {
        val index = expenses.indexOfFirst { it.id == id }
        if (index != -1) {
            val expense = expenses[index]
            val newAccumulated = (expense.accumulatedAmount - amount).coerceAtLeast(0)
            
            // Only subtract from today's contribution if the trip being reversed was actually made today
            val newContributionToday = if (contributionDate == LocalDate.now()) {
                (expense.contributionToday - amount).coerceAtLeast(0)
            } else {
                expense.contributionToday
            }
            
            expenses[index] = expense.copy(
                accumulatedAmount = newAccumulated,
                contributionToday = newContributionToday
            )
            _expensesFlow.value = expenses.toList()
        }
    }

    override suspend fun deleteExpense(id: String) {
        val index = expenses.indexOfFirst { it.id == id }
        if (index != -1) {
            expenses[index] = expenses[index].copy(isDeleted = true)
            _expensesFlow.value = expenses.toList()
        }
    }

    override fun syncExpenses(today: LocalDate) {
        val currentList = _expensesFlow.value
        var hasChanges = false

        val synchronizedList = currentList.map { expense ->
            // First we sync the daily contribution (reset if it's a new day)
            var updatedExpense = expense.syncDailyContribution(today)
            
            // Then we check for renewal
            if (updatedExpense.isExpired(today)) {
                hasChanges = true
                updatedExpense = updatedExpense.renew(today)
            }
            
            // If the day changed but it wasn't expired, it still counts as a change in the flow
            if (updatedExpense != expense) {
                hasChanges = true
            }
            
            updatedExpense
        }

        // We only issue if there were actual renovations to avoid endless recompositions.
        if (hasChanges) {
            // Update the underlying list items
            synchronizedList.forEach { updated ->
                val index = expenses.indexOfFirst { it.id == updated.id }
                if (index != -1) {
                    expenses[index] = updated
                }
            }
            
            _expensesFlow.value = expenses.toList()
        }
    }

}