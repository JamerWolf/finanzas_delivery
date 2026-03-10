package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository

/**
 * Use case to soft-delete a time-based expense.
 * Marks the expense as deleted so it no longer impacts net calculations.
 */
class DeleteTimeBasedExpenseUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteExpense(id)
    }
}
