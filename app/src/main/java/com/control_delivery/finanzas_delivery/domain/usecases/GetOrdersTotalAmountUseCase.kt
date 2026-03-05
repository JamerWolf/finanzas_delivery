package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to get the total amount of orders
 */
class GetOrdersTotalAmountUseCase (
    private val orderRepository: OrderRepository
) {
        /**
         * Returns a flow of the total amount of orders
         */
        operator fun invoke(): Flow<Long> {
            return orderRepository.getOrdersTotalAmount()
        }

        /**
         * Returns a flow of the total amount of orders in a date range
         */
        operator fun invoke(startDate: Long, endDate: Long): Flow<Long> {
            return orderRepository.getOrdersTotalAmount(startDate, endDate)
        }
    }
