package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import kotlinx.coroutines.flow.first

/**
 * Soft-deletes a trip by setting isDeleted = true and reverses its financial impact.
 */
class DeleteTripUseCase(
    private val tripRepository: TripRepository,
    private val reverseTripIncomeUseCase: ReverseTripIncomeUseCase
) {
    suspend operator fun invoke(tripId: String) {
        val trip = tripRepository.getTripById(tripId).first()
        if (trip != null && trip.status == TripStatus.COMPLETED) {
            reverseTripIncomeUseCase(trip)
        }
        tripRepository.deleteTrip(tripId)
    }
}
