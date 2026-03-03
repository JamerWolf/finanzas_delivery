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
        return _expensesFlow.asStateFlow()
    }

    override fun getDailyExpenses(today: LocalDate): Flow<Double> {
        return _expensesFlow.map {
            TimeBasedExpensesFake.getDailyExpense(today)
        }
    }

    override fun getExpenseById(id: String): Flow<TimeBasedExpense?> {
        return _expensesFlow.map { expenses ->
            expenses.find { it.id == id }
        }
    }

    override fun syncExpenses(today: LocalDate) {

        val currentList = _expensesFlow.value
        var hasChanges = false

        val synchronizedList = currentList.map { expense ->
            if (expense.isExpired(today)) {
                hasChanges = true
                expense.renew(today)
            } else {
                expense
            }
        }
        // We only issue if there were actual renovations to avoid endless recompositions.
        if (hasChanges) {
            _expensesFlow.value = synchronizedList

            TimeBasedExpensesFake.expenses.clear()
            TimeBasedExpensesFake.expenses.addAll(synchronizedList)

        }
    }
}