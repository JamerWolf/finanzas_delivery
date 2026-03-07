package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrdersFlowUseCase(
    private val orderRepository: OrderRepository
) {
    /*
    * Returns a flow of orders by status and date range
    */
    operator fun invoke(status: List<OrderStatus>,
                        startDate: Long, endDate: Long): Flow<List<Order>> {
        return orderRepository.getOrdersByStatesInDateRange(status, startDate, endDate)
    }
}
