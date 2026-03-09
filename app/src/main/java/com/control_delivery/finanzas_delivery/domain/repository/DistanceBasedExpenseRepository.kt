package com.control_delivery.finanzas_delivery.domain.repository

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing mileage-based expenses.
 * Provides reactive streams to observe changes and methods to persist wear configurations.
 */
interface DistanceBasedExpenseRepository {

    /**
     * Returns a reactive flow of all active mileage-based expenses.
     */
    fun getDistanceBasedExpenses(): Flow<List<DistanceBasedExpense>>

    /**
     * Saves or updates an individual mileage-based expense.
     */
    suspend fun saveExpense(expense: DistanceBasedExpense)

    /**
     * Updates a list of mileage-based expenses atomically.
     * Useful for updating accumulated savings when processing an order.
     */
    suspend fun updateExpenses(expenses: List<DistanceBasedExpense>)

    /**
     * Resets a specific mileage-based expense (usually after maintenance is performed).
     */
    suspend fun resetExpense(id: String)

    /**
     * Deletes or marks a mileage-based expense as deleted.
     */
    suspend fun deleteExpense(id: String)
}
