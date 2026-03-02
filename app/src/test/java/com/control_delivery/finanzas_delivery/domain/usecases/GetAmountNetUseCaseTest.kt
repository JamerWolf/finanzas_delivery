package com.control_delivery.finanzas_delivery.domain.usecases

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetAmountNetUseCaseTest  {
    private val getOrdersTotalAmountUseCase: GetOrdersTotalAmountUseCase = mockk()
    private val getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase = mockk()

    private val useCase = GetAmountNetUseCase(
        getOrdersTotalAmountUseCase,
        getTotalTimeBasedExpensesImpactUseCase
    )
    @Test
    fun invoke() = runTest{
        val startTs = 1000L
        val endTs = 2000L
        val incomeValue = 500.0
        val expensesValue = 120.0
        val expectedNet = 380.0

        every { getOrdersTotalAmountUseCase(startTs, endTs) } returns flowOf(incomeValue)
        every { getTotalTimeBasedExpensesImpactUseCase(startTs, endTs) } returns flowOf(expensesValue)

        val result = useCase(startTs, endTs).first()

        assertEquals("The result should be the exact subtraction: Income - Expenses", expectedNet, result, 0.01)
    }

}