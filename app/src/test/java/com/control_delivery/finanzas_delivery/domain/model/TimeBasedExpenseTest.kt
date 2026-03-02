package com.control_delivery.finanzas_delivery.domain.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TimeBasedExpenseTest {
    private lateinit var expense: TimeBasedExpense
    val zoneId: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = LocalDate.of(2026,2,26)

    @Before
    fun setUp() {

        val deadline = LocalDate.of(2026,3,1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        expense = TimeBasedExpense(
            description = "Test Bill",
            amount = 30000.0,
            accumulatedAmount = 0.0,
            frequency = ExpenseFrequency.Monthly(dayOfMonth = 1),
            startTimestamp = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            nextDeadline = deadline
        )
    }


    @Test
    fun `getDailyAmount should return the correct amount for until 3 days`() {
        val dailyAmount = expense.getDailyAmount(today)

        assertEquals(10000.0, dailyAmount, 0.01)
    }

    @Test
    fun `getDaysUntilDeadline should return the correct number of days until deadline`() {
        val daysUntilDeadline = expense.getDaysUntilDeadline(today)

        assertEquals(3, daysUntilDeadline)
    }

    @Test
    fun `isExpired should return false when not expired`() {
        val isExpired = expense.isExpired(today)

        assertFalse(isExpired)
    }

    @Test
    fun `isExpired should return true when expired`() {
        val today = LocalDate.of(2026,3,1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val deadline = LocalDate.of(2026,3,1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val expiredExpense = expense.copy(
            startTimestamp = today,
            nextDeadline = deadline)

        val isExpired = expiredExpense.isExpired(Instant.ofEpochMilli(today).atZone(zoneId).toLocalDate())
        println(isExpired)

        assertTrue(isExpired)
    }

    @Test
    fun renew() {
        val today = LocalDate.of(2026,3,1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val deadline = LocalDate.of(2026,3,1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val expiredExpense = expense.copy(
            startTimestamp = today,
            nextDeadline = deadline)

        val deadlineExpectedLocalDate = LocalDate.of(2026,4,1)

        val renewedExpense = expiredExpense.renew(Instant.ofEpochMilli(today).atZone(zoneId).toLocalDate())
        val deadlineActualLocalDate = Instant.ofEpochMilli(renewedExpense.nextDeadline).atZone(zoneId).toLocalDate()

        print(renewedExpense.nextDeadline)

        assertEquals(deadlineExpectedLocalDate, deadlineActualLocalDate)

    }

}