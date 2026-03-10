package com.control_delivery.finanzas_delivery.domain.repository;

import com.control_delivery.finanzas_delivery.domain.model.Order;
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus;
import kotlinx.coroutines.flow.Flow

public interface OrderRepository {
    fun getOrderById(id: String): Flow<Order?>
    suspend fun addOrder(order: Order): String
    fun getOrdersTotalAmount(startDate: Long, endDate: Long): Flow<Long>
    fun getOrdersByStatesInDateRange(status: List<OrderStatus>,
                                     startDate: Long, endDate: Long): Flow<List<Order>>
    suspend fun deleteOrder(id: String)
    suspend fun updateOrder(order: Order)
}



