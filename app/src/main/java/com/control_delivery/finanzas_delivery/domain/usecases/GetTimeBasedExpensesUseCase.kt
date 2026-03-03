package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Case of use to list all time-based expenses.
 * It filters the expenses that have not been deleted.
 */
class GetTimeBasedExpensesUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    operator fun invoke(): Flow<List<TimeBasedExpense>> {
        return repository.getAllExpenses().map { expenses ->
            expenses.filter { !it.isDeleted }
        }
    }
}
