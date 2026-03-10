package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import com.control_delivery.finanzas_delivery.utils.DateUtils

/**
 * Fake data: Each existing order is wrapped in its own individual completed Trip.
 * This maintains backward compatibility with the existing order data.
 */
object TripsFake {
    fun createTrips(orders: List<Order>): MutableList<Trip> {
        return orders.filter { it.status == OrderStatus.DELIVERED }.map { order ->
            Trip(
                orders = listOf(order),
                status = TripStatus.COMPLETED,
                totalDistanceKm = 3.0, // Default placeholder distance for migrated orders
                startTimestamp = order.timestamp,
                endTimestamp = order.timestamp
            )
        }.toMutableList()
    }
}
