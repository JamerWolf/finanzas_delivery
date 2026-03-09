package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.utils.DateUtils

data class Orders(var orders: MutableList<Order> = mutableListOf(
    Order(platform = "DIDI",
        customerAddress = "123 Main St",
        totalAmount = 5300,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("16-02-2026").first),
    Order(platform = "ARMIRENE",
        customerAddress = "456 Elm St",
        totalAmount = 4700,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("16-02-2026").first),
    Order(platform = "DIDI",
        customerAddress = "789 Oak St",
        totalAmount = 3600,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("16-02-2026").first),
    Order(platform = "RAPPI",
        customerAddress = "321 Pine St",
        totalAmount = 6850,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("16-02-2026").first),
    Order(platform = "CABIFY",
        customerAddress = "654 Maple St",
        totalAmount = 3490,
        status = OrderStatus.ON_THE_WAY_TO_DELIVERY,
        timestamp = DateUtils.getTimestampRange("16-02-2026").first),
    Order(platform = "DIDI",
        customerAddress = "987 Cedar St",
        totalAmount = 5200,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("23-02-2026").first),
    Order(platform = "ARMIRENE",
        customerAddress = "159 Birch",
        totalAmount = 4700,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("24-02-2026").first),
    Order(platform = "RAPPI",
        customerAddress = "452 Street",
        totalAmount = 4700,
        status = OrderStatus.DELIVERED,
        timestamp = DateUtils.getTimestampRange("24-02-2026").first),
    )) {
    fun addOrder(order: Order): Order {
        orders.add(order)
        if (orders.lastOrNull() != null) {
            return orders.last()
        }
        else {
            error("Error to add order")
        }
    }

    fun getOrdersTotalAmount(startDate: Long, endDate: Long): Long {
        return orders.filter {
            it.status == OrderStatus.DELIVERED && it.timestamp in startDate..endDate}
            .sumOf { it.totalAmount }
        }


    fun getOrdersByStatesInDateRange(status: List<OrderStatus>,
                                     startDate: Long, endDate: Long): List<Order> {
        return orders.filter {
            status.contains(it.status) && it.timestamp in startDate..endDate}
    }

    fun deleteOrder(id: String) {
        orders.removeIf { it.id == id }
    }

    fun updateOrder(order: Order) {
        val index = orders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            orders[index] = order
        }
    }
}

val orderInstance = Orders()

