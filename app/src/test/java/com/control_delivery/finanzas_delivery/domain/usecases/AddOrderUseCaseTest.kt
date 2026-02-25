package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.createOrder
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AddOrderUseCaseTest {
    private val repository: OrderRepository = mockk()
    private val useCase = AddOrderUseCase(repository)

    @Test
    fun `add order to repository`() = runTest {
        val order = createOrder("DIDI",10.0)

        coEvery { repository.addOrder(order) } returns order.id

        val idOrderAdded = useCase(order)

        coVerify { repository.addOrder(order) }

        assertEquals(order.id, idOrderAdded)
    }

}