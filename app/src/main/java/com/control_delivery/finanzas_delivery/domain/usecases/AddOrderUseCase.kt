package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository

class AddOrderUseCase (
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(order: Order): String {
        return orderRepository.addOrder(order)
    }
}