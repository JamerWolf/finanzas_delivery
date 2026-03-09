package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository

/**
 * Use case to reset a distance-based expense after maintenance.
 */
class ResetDistanceExpenseUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    suspend operator fun invoke(id: String) {
        repository.resetExpense(id)
    }
}
