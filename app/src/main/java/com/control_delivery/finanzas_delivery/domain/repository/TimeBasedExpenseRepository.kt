package com.control_delivery.finanzas_delivery.domain.repository

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TimeBasedExpenseRepository {
    /**
     * Get a flow of all the expenses.
     */
    fun getAllExpenses(): Flow<List<TimeBasedExpense>>

    /**
     * Get a flow of a specific expense by its ID.
     */
    fun getExpenseById(id: String): Flow<TimeBasedExpense?>

    /**
     * Add a new time-based expense.
     */
    suspend fun addExpense(expense: TimeBasedExpense)

    /**
     * Update the list of expenses.
     * @param expenses The new list of expenses.
     */
    suspend fun updateExpenses(expenses: List<TimeBasedExpense>)

    /**
     * Check if any expense has reached the next deadline if yes reset accumulated amount and renew deadline of the expenses.

     * @param today The date to check.
     */
    fun syncExpenses(today: LocalDate = LocalDate.now())

    /**
     * Subtracts a contribution from a specific expense.
     * @param id The ID of the expense.
     * @param amount The amount to subtract.
     * @param contributionDate The date the contribution was originally made.
     */
    suspend fun subtractContribution(id: String, amount: Long, contributionDate: LocalDate)

    /**
     * Soft-deletes a time-based expense by marking it as deleted.
     * @param id The ID of the expense to delete.
     */
    suspend fun deleteExpense(id: String)
}
