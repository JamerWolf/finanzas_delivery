package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import java.time.Instant

class GetTotalTimeBasedExpensesImpactUseCase(private val repository: TimeBasedExpenseRepository) {

    /**
     * Calculate the total impact daily of time-based expenses within a specified date range.
     * @param startDate The start date of the date range.
     * @param endDate The end date of the date range.
     * @return A Flow emitting the total impact daily of time-based expenses.
     * */
    operator fun invoke(startDate: Long, endDate: Long): Flow<Double> {
        val zoneId = ZoneId.systemDefault()
        val startLocalDate = Instant.ofEpochMilli(startDate).atZone(zoneId).toLocalDate()
        val endLocalDate = Instant.ofEpochMilli(endDate).atZone(zoneId).toLocalDate()
        val days = endLocalDate.toEpochDay() - startLocalDate.toEpochDay() + 1

        return repository.getDailyExpenses(startLocalDate).map { dailyExpenses ->
            dailyExpenses * days
        }
    }
}