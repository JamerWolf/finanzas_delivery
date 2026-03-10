package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository

/**
 * Use case to soft-delete a distance-based expense.
 */
class DeleteDistanceBasedExpenseUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteExpense(id)
    }
}
