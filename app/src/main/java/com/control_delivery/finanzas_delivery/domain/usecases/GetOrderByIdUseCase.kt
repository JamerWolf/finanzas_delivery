package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get an order by its ID.
 * */
class GetOrderByIdUseCase (
    private val repository: OrderRepository
) {
    operator fun invoke(id: String) : Flow<Order?> = repository.getOrderById(id)
}