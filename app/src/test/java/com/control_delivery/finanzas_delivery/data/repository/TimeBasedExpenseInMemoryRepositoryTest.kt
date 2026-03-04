package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest

class TimeBasedExpenseInMemoryRepositoryTest {
    val today: LocalDate = LocalDate.of(2026,2,26)
        .atStartOfDay(ZoneId.systemDefault())
        .toLocalDate()

    val timeBasedExpenses: MutableList<TimeBasedExpense> = mutableListOf(
        TimeBasedExpense(
            id = "1",
            description = "Test Bill",
            amount = 30000.0,
            accumulatedAmount = 0.0,
            frequency = ExpenseFrequency.Monthly(dayOfMonth = 1),
            startTimestamp = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ),
        TimeBasedExpense(
            id = "2",
            description = "Test Bill 2",
            amount = 15000.0,
            accumulatedAmount = 0.0,
            frequency = ExpenseFrequency.Monthly(dayOfMonth = 1),
            startTimestamp = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    )

    val repository = TimeBasedExpenseInMemoryRepository(timeBasedExpenses)

    @Test
    fun `getDailyExpenses should return the correct amount for today`() = runTest {
        val dailyExpenses = repository.getDailyAmountExpenses(today).first()
        assertEquals(15000.0, dailyExpenses, 0.01)
    }

    @Test
    fun `syncExpenses should reset accumulated amount and renew deadline for expired expenses`() = runTest {
        val startDay = LocalDate.of(2026, 2, 26)
        val expirationDay = LocalDate.of(2026, 3, 1)

        val initialExpense = TimeBasedExpense(
            description = "Overdue Invoice",
            amount = 30000.0,
            accumulatedAmount = 15000.0,
            frequency = ExpenseFrequency.Monthly(dayOfMonth = 1),
            startTimestamp = startDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val repository = TimeBasedExpenseInMemoryRepository(mutableListOf(initialExpense))

        repository.syncExpenses(expirationDay)

        val updatedExpense = repository.getAllExpenses().first().first()

        assertEquals("The cumulative total must be reset.",
            0.0, updatedExpense.accumulatedAmount, 0.01)

        assertTrue("The deadline must be later than the previous one.",
            updatedExpense.nextDeadline > initialExpense.nextDeadline)
    }

    @Test
    fun `updateExpenses should update the expenses list`() = runTest {
        val updatedExpense = timeBasedExpenses.first { it.id == "1" }.copy(
            contributionToday = 15000.0,
            lastContributionTimestamp = LocalDate.of(2026,2,27)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        val updatedList = listOf(updatedExpense)

        repository.updateExpenses(updatedList)

        val expense = repository.getAllExpenses().first().first()

        assertEquals(updatedExpense, expense)

    }

}