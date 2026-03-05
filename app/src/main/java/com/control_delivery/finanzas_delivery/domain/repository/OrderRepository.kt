package com.control_delivery.finanzas_delivery.domain.repository;

import com.control_delivery.finanzas_delivery.domain.model.Order;
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus;
import kotlinx.coroutines.flow.Flow

public interface OrderRepository {
    fun getOrderById(id: String): Flow<Order?>
    fun getOrdersFlow(): Flow<List<Order>>
    fun getOrdersByStates(status: List<OrderStatus>): Flow<List<Order>>
    suspend fun addOrder(order: Order): String
    fun getOrdersTotalAmount(): Flow<Long>
    fun getOrdersTotalAmount(startDate: Long, endDate: Long): Flow<Long>
    fun getOrdersByStatesInDateRange(status: List<OrderStatus>,
                                     startDate: Long, endDate: Long): Flow<List<Order>>
    fun getOrdersAmountAfterKm(startDate: Long, endDate: Long): Flow<Long>
}

