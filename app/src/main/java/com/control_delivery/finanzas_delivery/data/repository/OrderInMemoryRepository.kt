package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class OrderInMemoryRepository (
    private val orderI: Orders = orderInstance
): OrderRepository {
    private val _ordersFlow = MutableStateFlow(orderI.orders.toList())

    override fun getOrderById(id: String): Flow<Order?> {
        return _ordersFlow.map { orders -> orders.find { it.id == id } }
    }

    override fun getOrdersFlow(): Flow<List<Order>> = _ordersFlow.asStateFlow()

    override fun getOrdersByStates(status: List<OrderStatus>): Flow<List<Order>> {
        return _ordersFlow.map { orders -> orders.filter { status.contains(it.status) } }
    }

    override suspend fun addOrder(order: Order): String {
        val addOrder = orderI.addOrder(order)
        _ordersFlow.value = orderI.orders.toList()
        return addOrder.id
    }

    override fun getOrdersTotalAmount(): Flow<Long> {
        return _ordersFlow.map { orderI.getOrdersTotalAmount() }
    }

    override fun getOrdersTotalAmount(startDate: Long, endDate: Long): Flow<Long> {
        return _ordersFlow.map { orderI.getOrdersTotalAmount(startDate, endDate) }
    }

    override fun getOrdersByStatesInDateRange(
        status: List<OrderStatus>,
        startDate: Long, endDate: Long
    ): Flow<List<Order>> {
        return _ordersFlow.map {
            orderI.getOrdersByStatesInDateRange(status, startDate, endDate)
        }
    }

    override fun getOrdersAmountAfterKm(startDate: Long, endDate: Long): Flow<Long> {
        return _ordersFlow.map { orders ->
            orders.filter { it.status == OrderStatus.DELIVERED && it.timestamp in startDate..endDate }
                .sumOf { it.amountAfterKmDeduction }
        }
    }
}