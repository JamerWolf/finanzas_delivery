package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager
import timber.log.Timber

/**
 * Orchestrates the completion of an active trip:
 * 1. Snapshots the trip from [ActiveTripManager] (stops accumulating distance).
 * 2. Runs the financial pipeline via [ProcessTripIncomeUseCase].
 * 3. Persists the finalized trip to [TripRepository].
 * 4. Persists all orders from the trip to [OrderRepository].
 *
 * This is the single entry point for "driver finishes a trip".
 */
class CompleteTripUseCase(
    private val activeTripManager: ActiveTripManager,
    private val processTripIncome: ProcessTripIncomeUseCase,
    private val tripRepository: TripRepository,
    private val orderRepository: OrderRepository
) {
    /**
     * Completes the active trip and processes all financial deductions.
     *
     * @return the finalized [Trip] with all deduction fields populated, or null if no trip was active.
     */
    suspend operator fun invoke(): Trip? {
        // 1. Snapshot the trip from ActiveTripManager
        val rawTrip = activeTripManager.completeTrip()
        if (rawTrip == null) {
            Timber.w("CompleteTripUseCase: no active trip to complete")
            return null
        }

        Timber.d(
            "CompleteTripUseCase: processing trip ${rawTrip.id}, " +
                "amount=${rawTrip.totalOrdersAmount}, distance=${rawTrip.totalDistanceKm}km"
        )

        // 2. Run financial pipeline
        val result = processTripIncome(rawTrip.totalOrdersAmount, rawTrip.totalDistanceKm)

        // 3. Build the finalized trip with deduction data
        val finalizedTrip = rawTrip.copy(
            kmDeduction = result.kmDeduction,
            timeExpensesDeduction = result.timeExpensesDeduction,
            kmDeductionsBreakdown = result.kmDeductionsBreakdown,
            timeExpensesDeductionsBreakdown = result.timeExpensesDeductionsBreakdown
        )

        // 4. Persist the trip
        tripRepository.addTrip(finalizedTrip)

        // 5. Persist all orders from the trip to the order repository
        for (order in finalizedTrip.orders) {
            orderRepository.addOrder(order)
        }

        Timber.d(
            "CompleteTripUseCase: trip ${finalizedTrip.id} saved. " +
                "Net=${finalizedTrip.netAmount}, " +
                "kmDeduction=${finalizedTrip.kmDeduction}, " +
                "timeDeduction=${finalizedTrip.timeExpensesDeduction}"
        )

        return finalizedTrip
    }
}
