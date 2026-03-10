package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Case of use to list all time-based expenses.
 * By default, it filters the expenses that have not been deleted.
 * @param includeDeleted If true, returns all expenses including deleted ones.
 */
class GetTimeBasedExpensesUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    operator fun invoke(includeDeleted: Boolean = false): Flow<List<TimeBasedExpense>> {
        return repository.getAllExpenses().map { expenses ->
            if (includeDeleted) expenses
            else expenses.filter { !it.isDeleted }
        }
    }
}
