package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import java.time.Instant

import java.time.LocalDate

class GetTotalTimeBasedExpensesImpactUseCase(private val repository: TimeBasedExpenseRepository) {

    /**
     * Calculate the total impact daily of time-based expenses within a specified date range.
     * @param startDate The start date of the date range.
     * @param endDate The end date of the date range.
     * @return A Flow emitting the total impact daily of time-based expenses.
     * */
    operator fun invoke(startDate: Long, endDate: Long): Flow<Long> {
        val zoneId = ZoneId.systemDefault()
        // Convertimos los timestamps del ViewModel a fechas locales
        val startLocalDate = Instant.ofEpochMilli(startDate).atZone(zoneId).toLocalDate()
        val endLocalDate = Instant.ofEpochMilli(endDate).atZone(zoneId).toLocalDate()

        return repository.getAllExpenses().map { expenses ->
            var totalImpact = 0L
            var currentDate = startLocalDate

            while (!currentDate.isAfter(endLocalDate)) {
                totalImpact += expenses.filter { !it.isDeleted }
                    .sumOf { it.getFullDailyQuota(currentDate) }
                currentDate = currentDate.plusDays(1)
            }
            totalImpact
        }
    }
}
