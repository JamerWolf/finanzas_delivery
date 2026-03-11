package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import java.time.LocalDate

/**

 * Use case responsible for synchronizing the status of time-based expenses.
 * Checks if any expenses have reached their deadline and executes
 * cycle renewal if necessary.
 */
class SyncTimeBasedExpensesUseCase (
    private val repository: TimeBasedExpenseRepository
) {
    /**
     * Executes synchronization using the current date or a specific date.
     * @param today Reference date for synchronization (default is today).
     */
    suspend operator fun invoke(today: LocalDate = LocalDate.now()) {
        repository.syncExpenses(today)
    }
}