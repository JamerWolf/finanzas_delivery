package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.createOrder
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetOrdersTotalAmountUseCaseTest {
    private val repository: OrderRepository = mockk()
    private val useCase = GetOrdersTotalAmountUseCase(repository)

    @Test
        fun `invoke returns correct total amount`(): Unit = runTest {
            val orders = listOf(
                createOrder("DIDI",10.0),
                createOrder("UBER",20.0)
            )

            val statusList = listOf(OrderStatus.DELIVERED)

            coEvery { repository.getOrdersByStates(statusList) } returns orders

            val result = useCase(statusList)

            assertEquals(30.0, result)

        }

}