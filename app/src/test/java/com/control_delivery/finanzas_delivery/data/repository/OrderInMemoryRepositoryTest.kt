package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.createOrder
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OrderInMemoryRepositoryTest {
    @Test
    fun `should return only orders with matching status`() = runTest {
       val orders = Orders(mutableListOf(
            createOrder("DIDI",10.0),
            createOrder("UBER",20.0),
            createOrder("DIDI",30.0, OrderStatus.ON_THE_WAY_TO_DELIVERY),
            createOrder("UBER",40.0, OrderStatus.CANCELLED)
        ))

        val repository = OrderInMemoryRepository(orders)

        val result = repository.getOrdersByStates(listOf(OrderStatus.DELIVERED))
        assertEquals(2, result.size)
    }

    @Test
    fun `should add order to store`() = runTest {
        val orders = Orders(mutableListOf(
            createOrder("DIDI",10.0)
        ))

        val repository = OrderInMemoryRepository(orders)

        val order = createOrder("UBER",20.0)
        repository.addOrder(order)

        assertEquals(2, orders.orders.size)
        assertEquals(order, orders.orders.last())
    }

}