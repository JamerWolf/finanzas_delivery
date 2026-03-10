package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository

/**
 * Use case to add a new distance-based expense.
 */
class AddDistanceBasedExpenseUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    suspend operator fun invoke(expense: DistanceBasedExpense) {
        repository.saveExpense(expense)
    }
}
