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

    private val mockRoute2 = listOf(
        RoutePoint(7.8852680184807245, -72.49784461735025), // Start near Ventura Plaza
        RoutePoint(7.890650792915418, -72.49894996318916),
        RoutePoint(7.891757274611185, -72.5004680187285),
        RoutePoint(7.899956495451716, -72.50198607414032),
        RoutePoint(7.90659519169184, -72.49814797176698),  // End near Barrio Latino
        RoutePoint(7.90778674128528, -72.48964113288292),
        RoutePoint(7.91048190015655, -72.49021398396846),
        RoutePoint(7.910623750135551, -72.48938334989442),
        RoutePoint(7.910510270156249, -72.49027126907703),
        RoutePoint(7.911134409655885, -72.48946927755725),
    )

    fun createTrips(orders: List<Order>): MutableList<Trip> {
        val todayTimestampRange = DateUtils.getTimestampRange("10-03-2026")
        val todayOrders = orders.filter { 
            it.status == OrderStatus.DELIVERED && it.timestamp in todayTimestampRange.first..todayTimestampRange.second 
        }
        val otherOrders = orders.filter { 
            it.status == OrderStatus.DELIVERED && it.timestamp !in todayTimestampRange.first..todayTimestampRange.second 
        }

        val trips = mutableListOf<Trip>()

        // Create a single trip for today with both orders if they exist
        if (todayOrders.size >= 2) {
            val order1 = todayOrders[0].copy(
                pickupLocation = mockRoute2[0],
                deliveryLocation = mockRoute2[4]
            )
            val order2 = todayOrders[1].copy(
                pickupLocation = mockRoute2[5],
                deliveryLocation = mockRoute2[9]
            )
            
            trips.add(
                Trip(
                    orders = listOf(order1, order2),
                    status = TripStatus.COMPLETED,
                    totalDistanceKm = 5.4,
                    startTimestamp = todayOrders.minOf { it.timestamp },
                    endTimestamp = todayOrders.maxOf { it.timestamp },
                    route = mockRoute2
                )
            )
        } else if (todayOrders.isNotEmpty()) {
            trips.add(
                Trip(
                    orders = todayOrders,
                    status = TripStatus.COMPLETED,
                    totalDistanceKm = 5.4, // Custom distance for today's trip
                    startTimestamp = todayOrders.minOf { it.timestamp },
                    endTimestamp = todayOrders.maxOf { it.timestamp },
                    route = mockRoute2
                )
            )
        }

        // Keep other orders as individual trips for now
        otherOrders.forEach { order ->
            trips.add(
                Trip(
                    orders = listOf(order),
                    status = TripStatus.COMPLETED,
                    totalDistanceKm = 3.0,
                    startTimestamp = order.timestamp,
                    endTimestamp = order.timestamp,
                    route = mockRouteCucuta
                )
            )
        }

        return trips
    }
}
