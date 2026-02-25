package com.control_delivery.finanzas_delivery.domain.model

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OrderModelTest {
    private lateinit var order: Order

    @Before
    fun setUp() {
        order = Order(
            platform = "DIDI",
            customerAddress = "123 Main St",
            totalAmount = 100.0
        )
    }

    @Test
    fun createOrder() {
        Assert.assertEquals("DIDI", order.platform)
        Assert.assertEquals("123 Main St", order.customerAddress)
        Assert.assertEquals(OrderStatus.ON_THE_WAY_TO_RECEIVE, order.status)
        Assert.assertEquals(100.0, order.totalAmount, 0.0)
        Assert.assertFalse(order.isDeleted)
    }

    @Test
    fun invalidTotalAmount() {
        try {
            order.run { copy(totalAmount = -100.0) }
        } catch (e: IllegalArgumentException) {
            Assert.assertEquals("Total amount must be non-negative", e.message)
        }
    }
}