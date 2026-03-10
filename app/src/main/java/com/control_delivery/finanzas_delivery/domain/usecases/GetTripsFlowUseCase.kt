package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns a Flow of completed trips within a date range.
 * Used by the UI to display trip history on the HomeScreen.
 */
class GetTripsFlowUseCase(
    private val tripRepository: TripRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<Trip>> {
        return tripRepository.getCompletedTripsInDateRange(startDate, endDate)
    }
}
