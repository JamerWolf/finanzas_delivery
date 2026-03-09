package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository

/**
 * Use case to add a new time-based expense.
 */
class AddTimeBasedExpenseUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(expense: TimeBasedExpense) {
        repository.addExpense(expense)
    }
}
