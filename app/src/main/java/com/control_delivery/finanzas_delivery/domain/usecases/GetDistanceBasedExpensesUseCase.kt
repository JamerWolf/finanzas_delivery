package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to get all distance-based expenses (Gasoline, Oil, etc.).
 * By default, filters out deleted expenses.
 * @param includeDeleted If true, returns all expenses including deleted ones.
 */
class GetDistanceBasedExpensesUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    operator fun invoke(includeDeleted: Boolean = false): Flow<List<DistanceBasedExpense>> {
        return repository.getDistanceBasedExpenses().map { expenses ->
            if (includeDeleted) expenses
            else expenses.filter { !it.isDeleted }
        }
    }
}
