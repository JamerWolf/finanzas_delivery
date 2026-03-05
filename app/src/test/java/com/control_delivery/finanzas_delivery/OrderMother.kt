package com.control_delivery.finanzas_delivery

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus


fun createOrder(platform: String,
                amount: Long,
                status: OrderStatus = OrderStatus.DELIVERED): Order {
    return Order(
        platform = platform,
        customerAddress = "Test Address",
        totalAmount = amount,
        status = status
    )
}

