package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetTotalTimeBasedExpensesImpactUseCaseTest {
    private val repository: TimeBasedExpenseRepository = mockk()
    private val useCase = GetTotalTimeBasedExpensesImpactUseCase(repository)
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun `invoke should return correct total impact for a 3-day range`() = runTest {
        val startDate = LocalDate.of(2026,2,26)
        val endDate = LocalDate.of(2026,2,28)

        val startTimestamp = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endTimestamp = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val dailyImpact = 10000.0
        val expectedTotalImpact = dailyImpact * 3

        every { repository.getDailyExpenses(startDate) } returns flowOf(dailyImpact)

        val result = useCase(startTimestamp, endTimestamp).first()

        assertEquals("The total impact should be the sum of the 3 days.",
            expectedTotalImpact, result, 0.01)
    }

    @Test
    fun `invoke should return daily impact when range is only one day`() = runTest {
        val date = LocalDate.of(2026, 2, 26)
        val timestamp = date.atStartOfDay(zoneId).toInstant().toEpochMilli()

        val dailyImpact = 15.0
        every { repository.getDailyExpenses(date) } returns flowOf(dailyImpact)

        val result = useCase(timestamp, timestamp).first()

        assertEquals("For a single day, the total must be equal to the daily amount.",
            dailyImpact, result, 0.01)
    }

}