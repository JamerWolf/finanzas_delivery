package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.model.RoutePoint
import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import com.control_delivery.finanzas_delivery.utils.DateUtils

/**
 * Fake data: Each existing order is wrapped in its own individual completed Trip.
 * This maintains backward compatibility with the existing order data.
 */
object TripsFake {
    // Mock route in Cúcuta, Colombia for testing the map UI
    private val mockRouteCucuta = listOf(
        RoutePoint(7.887132704604933, -72.49695635807714), // Start near Ventura Plaza
        RoutePoint(7.887366506114272, -72.4955562449858),
        RoutePoint(7.888009459583429, -72.49508417620407),
        RoutePoint(7.891404875025091, -72.49550260079735),
        RoutePoint(7.892233798514237, -72.49436534417217),  // End near Barrio Latino
        RoutePoint(7.894066988756731, -72.49572790634895),
        RoutePoint(7.893801309519301, -72.49611414444311),
        RoutePoint(7.893801309519301, -72.49611414444311),
        RoutePoint(7.903149381800159, -72.49689395695123),
        RoutePoint(7.903186576061779, -72.49584789544622),
    )

    fun createTrips(orders: List<Order>): MutableList<Trip> {
        return orders.filter { it.status == OrderStatus.DELIVERED }.map { order ->
            Trip(
                orders = listOf(order),
                status = TripStatus.COMPLETED,
                totalDistanceKm = 3.0, // Default placeholder distance for migrated orders
                startTimestamp = order.timestamp,
                endTimestamp = order.timestamp,
                route = mockRouteCucuta // Add the mock route here
            )
        }.toMutableList()
    }
}
