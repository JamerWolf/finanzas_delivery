package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository

/**
 * Use case to update an existing time-based expense.
 */
class UpdateTimeBasedExpenseUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(expense: TimeBasedExpense) {
        repository.updateExpenses(listOf(expense))
    }
}
