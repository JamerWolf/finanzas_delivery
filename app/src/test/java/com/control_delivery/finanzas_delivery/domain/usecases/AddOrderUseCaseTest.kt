package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.createOrder
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AddOrderUseCaseTest {
    private val repository: OrderRepository = mockk()
    private val timeBasedExpensesUseCase: TimeBasedExpenseRepository = mockk()

    private val useCase = AddOrderUseCase(
        repository,
        timeBasedExpensesUseCase
    )

    @Test
    fun `add order to repository`() = runTest {
        val order = createOrder("DIDI",10.0)

        coEvery { timeBasedExpensesUseCase.syncExpenses() } just runs

        coEvery { repository.addOrder(order) } returns order.id

        val idOrderAdded = useCase(order)

        coVerify { repository.addOrder(order) }

        assertEquals(order.id, idOrderAdded)
    }

}