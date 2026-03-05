package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ApplyTimeBasedDeductionUseCaseTest {

    private val repository: TimeBasedExpenseRepository = mockk()
    private val useCase = ApplyTimeBasedDeductionUseCase(repository)
    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.of(2026, 2, 26)

    /**
     * Helper to create an expense that needs exactly $10 per day.
     * With 10 days left and $100 amount, daily amount is $10.
     */
    private fun createExpense(id: String, dailyQuota: Long = 10, contributionToday: Long = 0): TimeBasedExpense {
        val daysLeft = 10
        val targetAmount = dailyQuota * daysLeft
        val deadline = today.plusDays(daysLeft.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
        
        return TimeBasedExpense(
            id = id,
            description = "Expense $id",
            amount = targetAmount,
            accumulatedAmount = 0,
            frequency = ExpenseFrequency.Daily,
            startTimestamp = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            nextDeadline = deadline,
            contributionToday = contributionToday,
            lastContributionTimestamp = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
    }

    @Test
    fun `should distribute income equitably when pool is limited`() = runTest {
        // Arrange: 2 expenses, each needs $10 today. Total needed: $20.
        val expenseA = createExpense("A")
        val expenseB = createExpense("B")
        
        coEvery { repository.getAllExpenses() } returns flowOf(listOf(expenseA, expenseB))
        coEvery { repository.updateExpenses(any()) } just runs

        // Act: Only $10 enters.
        val result = useCase(10, today)

        // Assert: Each should receive $5.
        val capturedList = slot<List<TimeBasedExpense>>()
        coVerify { repository.updateExpenses(capture(capturedList)) }
        
        val updatedA = capturedList.captured.find { it.id == "A" }!!
        val updatedB = capturedList.captured.find { it.id == "B" }!!
        
        assertEquals("Expense A should receive half of the pool", 5L, updatedA.contributionToday)
        assertEquals("Expense B should receive half of the pool", 5L, updatedB.contributionToday)
        assertEquals("All money should be used", 0L, result.amountAfterDeduction)
    }

    @Test
    fun `should fulfill all daily quotas and return leftover profit`() = runTest {
        // Arrange: Total needed today is $20 ($10 each).
        val expenseA = createExpense("A")
        val expenseB = createExpense("B")
        
        coEvery { repository.getAllExpenses() } returns flowOf(listOf(expenseA, expenseB))
        coEvery { repository.updateExpenses(any()) } just runs

        // Act: $50 enters.
        val result = useCase(50, today)

        // Assert: Both should be filled ($10 each), $30 leftover.
        val capturedList = slot<List<TimeBasedExpense>>()
        coVerify { repository.updateExpenses(capture(capturedList)) }
        
        val updatedA = capturedList.captured.find { it.id == "A" }!!
        val updatedB = capturedList.captured.find { it.id == "B" }!!
        
        assertEquals(10L, updatedA.contributionToday)
        assertEquals(10L, updatedB.contributionToday)
        assertEquals("Profit should be 50 - 20 = 30", 30L, result.amountAfterDeduction)
    }

    @Test
    fun `should redistribute surplus when one expense completes early`() = runTest {
        // Arrange: 
        // Expense A needs $2 today (almost done).
        // Expense B needs $10 today.
        // Fair share for $10 pool would be $5 each. 
        // But A only takes $2, so B should get $5 + $3 = $8.
        val expenseA = createExpense("A", dailyQuota = 2)
        val expenseB = createExpense("B", dailyQuota = 10)
        
        coEvery { repository.getAllExpenses() } returns flowOf(listOf(expenseA, expenseB))
        coEvery { repository.updateExpenses(any()) } just runs

        // Act: $10 enters.
        val result = useCase(10, today)

        // Assert
        val capturedList = slot<List<TimeBasedExpense>>()
        coVerify { repository.updateExpenses(capture(capturedList)) }
        
        val updatedA = capturedList.captured.find { it.id == "A" }!!
        val updatedB = capturedList.captured.find { it.id == "B" }!!
        
        assertEquals(2L, updatedA.contributionToday)
        assertEquals(8L, updatedB.contributionToday)
        assertEquals(0L, result.amountAfterDeduction)
    }

    @Test
    fun `should return all amount when all expenses are already covered for today`() = runTest {
        // Arrange: 2 expenses already have their $10 contribution today.
        val expenseA = createExpense("A", dailyQuota = 10, contributionToday = 10)
        val expenseB = createExpense("B", dailyQuota = 10, contributionToday = 10)
        
        coEvery { repository.getAllExpenses() } returns flowOf(listOf(expenseA, expenseB))
        coEvery { repository.updateExpenses(any()) } just runs

        // Act: $50 enters.
        val result = useCase(50, today)

        // Assert: No changes to expenses, all money remains in pool.
        coVerify { repository.updateExpenses(any()) }
        assertEquals(50L, result.amountAfterDeduction)
    }
}
