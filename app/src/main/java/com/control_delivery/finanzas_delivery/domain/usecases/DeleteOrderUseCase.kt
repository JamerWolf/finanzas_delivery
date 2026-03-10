package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import kotlinx.coroutines.flow.first

/**
 * Soft-deletes an order. Financial recalculation is handled at the Trip level.
 */
class DeleteOrderUseCase(
    private val orderRepository: OrderRepository,
    private val tripRepository: TripRepository,
    private val processTripIncomeUseCase: ProcessTripIncomeUseCase,
    private val reverseTripIncomeUseCase: ReverseTripIncomeUseCase
) {
    suspend operator fun invoke(orderId: String) {
        // 1. Soft-delete the order
        orderRepository.deleteOrder(orderId)

        // 2. Find the Trip this order belongs to
        val trip = tripRepository.getTripByOrderId(orderId).first()
        if (trip != null) {
            // Update the order inside the trip's list (mark as deleted)
            val updatedOrders = trip.orders.map { 
                if (it.id == orderId) it.copy(isDeleted = true) else it 
            }
            val updatedTrip = trip.copy(orders = updatedOrders)

            if (updatedTrip.status == TripStatus.COMPLETED) {
                // 3. Reverse previous financials
                reverseTripIncomeUseCase(trip)

                // 4. Recalculate financial deductions
                val result = processTripIncomeUseCase(
                    totalAmount = updatedTrip.totalOrdersAmount,
                    totalDistanceKm = updatedTrip.totalDistanceKm
                )
                
                val finalizedTrip = updatedTrip.copy(
                    kmDeduction = result.kmDeduction,
                    timeExpensesDeduction = result.timeExpensesDeduction,
                    kmDeductionsBreakdown = result.kmDeductionsBreakdown,
                    timeExpensesDeductionsBreakdown = result.timeExpensesDeductionsBreakdown
                )
                tripRepository.updateTrip(finalizedTrip)
            } else {
                tripRepository.updateTrip(updatedTrip)
            }
        }
    }
}
