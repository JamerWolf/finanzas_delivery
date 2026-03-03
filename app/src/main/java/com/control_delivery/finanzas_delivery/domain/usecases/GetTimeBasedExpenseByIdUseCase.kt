package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Case of use to get a specific time-based expense by its ID.
 */
class GetTimeBasedExpenseByIdUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    operator fun invoke(id: String): Flow<TimeBasedExpense?> {
        return repository.getExpenseById(id)
    }
}
