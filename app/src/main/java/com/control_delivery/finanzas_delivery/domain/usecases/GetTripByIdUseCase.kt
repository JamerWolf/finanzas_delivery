package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns a Flow of a single Trip by its ID.
 * Used by TripDetailScreen to observe a specific trip's data reactively.
 */
class GetTripByIdUseCase(
    private val tripRepository: TripRepository
) {
    operator fun invoke(tripId: String): Flow<Trip?> {
        return tripRepository.getTripById(tripId)
    }
}
