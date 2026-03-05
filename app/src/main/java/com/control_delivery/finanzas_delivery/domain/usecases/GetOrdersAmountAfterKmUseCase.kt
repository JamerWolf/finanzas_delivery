package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrdersAmountAfterKmUseCase(private val repository: OrderRepository) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<Long> {
        return repository.getOrdersAmountAfterKm(startDate, endDate)
    }
}
