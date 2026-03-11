package com.control_delivery.finanzas_delivery.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Represents a single GPS coordinate recorded during a trip.
 */
data class RoutePoint(
    val lat: Double,
    val lng: Double
)

/**
 * Represents a group of delivery orders completed in a single driving session.
 * Distance is tracked continuously via GPS for the entire Trip.
 * Financial deductions (KM + time-based) are applied at the Trip level, not per-order.
 */
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val orders: List<Order> = emptyList(),
    val status: TripStatus = TripStatus.ACTIVE,
    val totalDistanceKm: Double = 0.0,
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val route: List<RoutePoint> = emptyList(),
    val snappedRoute: List<RoutePoint>? = null,

    // Financial fields — populated when trip is COMPLETED
    val kmDeduction: Long = 0,
    val timeExpensesDeduction: Long = 0,
    val kmDeductionsBreakdown: Map<String, Long> = emptyMap(),
    val timeExpensesDeductionsBreakdown: Map<String, Long> = emptyMap(),
    val isDeleted: Boolean = false
) {
    /** Sum of all order amounts in this trip. */
    val totalOrdersAmount: Long
        get() = orders.filter { !it.isDeleted }.sumOf { it.totalAmount }

    /** Amount remaining after KM-based vehicle expense deductions. */
    val amountAfterKmDeduction: Long
        get() = totalOrdersAmount - kmDeduction

    /** Final net profit after all deductions. */
    val netAmount: Long
        get() = totalOrdersAmount - kmDeduction - timeExpensesDeduction

    fun toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(startTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /** Returns true when all orders in the trip have been delivered. */
    fun allOrdersDelivered(): Boolean {
        return orders.isNotEmpty() && orders.all {
            it.status == OrderStatus.DELIVERED || it.status == OrderStatus.CANCELLED || it.isDeleted
        }
    }
}

enum class TripStatus {
    ACTIVE,
    COMPLETED
}
