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
        val initialAmount = 100.0
        val afterKmAmount = 90.0
        val finalNetAmount = 70.0

        // Mock the sequence of filtering
        coEvery { applyKmDeduction(initialAmount) } returns afterKmAmount
        coEvery { applyTimeBasedDeduction(afterKmAmount) } returns finalNetAmount

        // 2. ACT
        val result = useCase(initialAmount)

        // 3. ASSERT
        assertEquals("The final net profit should be correct after both filters", finalNetAmount, result, 0.1)
        
        // Verify the exact order of execution
        coVerifyOrder {
            applyKmDeduction(initialAmount)
            applyTimeBasedDeduction(afterKmAmount)
        }
    }
}
