package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get all distance-based expenses (Gasoline, Oil, etc.).
 */
class GetDistanceBasedExpensesUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    operator fun invoke(): Flow<List<DistanceBasedExpense>> {
        return repository.getDistanceBasedExpenses()
    }
}
