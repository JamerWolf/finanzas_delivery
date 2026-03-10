package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import kotlinx.coroutines.flow.first

/**
 * Updates an order and recalculates its parent Trip's financials if the Trip is COMPLETED.
 */
class UpdateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val tripRepository: TripRepository,
    private val processTripIncomeUseCase: ProcessTripIncomeUseCase,
    private val reverseTripIncomeUseCase: ReverseTripIncomeUseCase
) {
    suspend operator fun invoke(
        oldOrderId: String,
        updatedPlatform: String,
        updatedAddress: String,
        updatedAmount: Long
    ) {
        // 1. Update the order in OrderRepository
        val oldOrder = orderRepository.getOrderById(oldOrderId).first() ?: return
        val updatedOrder = oldOrder.copy(
            platform = updatedPlatform,
            customerAddress = updatedAddress,
            totalAmount = updatedAmount
        )
        orderRepository.updateOrder(updatedOrder)

        // 2. Find the Trip this order belongs to
        val trip = tripRepository.getTripByOrderId(oldOrderId).first()
        if (trip != null) {
            // Update the order inside the trip's list
            val updatedOrders = trip.orders.map { 
                if (it.id == oldOrderId) updatedOrder else it 
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
                // Should be handled by ActiveTripManager before completion, 
                // but just in case it's here, update it without financial calculation
                tripRepository.updateTrip(updatedTrip)
            }
        }
    }
}
