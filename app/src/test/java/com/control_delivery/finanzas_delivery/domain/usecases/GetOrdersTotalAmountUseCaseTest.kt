package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.createOrder
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetOrdersTotalAmountUseCaseTest {
    private val repository: OrderRepository = mockk()
    private val useCase = GetOrdersTotalAmountUseCase(repository)

    @Test
        fun `invoke returns correct total amount`(): Unit = runTest {

            val expectedAmount = 30.0
            every { repository.getOrdersTotalAmount() } returns flowOf(expectedAmount)

            val result = useCase().first()

            assertEquals(expectedAmount, result)

        }

}