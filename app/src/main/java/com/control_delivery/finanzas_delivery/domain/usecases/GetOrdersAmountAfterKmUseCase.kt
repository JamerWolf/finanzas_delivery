package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns the total amount after KM deductions for completed trips in a date range.
 * Now reads from TripRepository instead of OrderRepository.
 */
class GetOrdersAmountAfterKmUseCase(private val tripRepository: TripRepository) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<Long> {
        return tripRepository.getTripsTotalAmountAfterKm(startDate, endDate)
    }
}
