package com.control_delivery.finanzas_delivery.domain.usecases

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessOrderIncomeUseCaseTest {

    private val applyKmDeduction: ApplyKmDeductionUseCase = mockk()
    private val applyTimeBasedDeduction: ApplyTimeBasedDeductionUseCase = mockk()
    private val useCase = ProcessOrderIncomeUseCase(applyKmDeduction, applyTimeBasedDeduction)

    @Test
    fun `invoke should pipe amount through km filter then time filter`() = runTest {
        // 1. ARRANGE
        val initialAmount = 100L
        val afterKmAmount = 90L
        val kmDeduction = 10L
        val timeExpensesDeduction = 20L
        val finalNetAmount = 70L

        // Mock the sequence of filtering
        coEvery { applyKmDeduction(initialAmount) } returns KmDeductionResult(
            amountAfterDeduction = afterKmAmount,
            deductionAmount = kmDeduction
        )
        coEvery { applyTimeBasedDeduction(afterKmAmount) } returns TimeBasedExpenseResult(
            amountAfterDeduction = finalNetAmount,
            deductionAmount = timeExpensesDeduction
        )

        // 2. ACT
        val result = useCase(initialAmount)

        // 3. ASSERT
        assertEquals("The final net profit should be correct after both filters", finalNetAmount, result.finalNetProfit)
        assertEquals("The km deduction should be correct", kmDeduction, result.kmDeduction)
        assertEquals("The time expenses deduction should be correct", timeExpensesDeduction, result.timeExpensesDeduction)
        
        // Verify the exact order of execution
        coVerifyOrder {
            applyKmDeduction(initialAmount)
            applyTimeBasedDeduction(afterKmAmount)
        }
    }
}
