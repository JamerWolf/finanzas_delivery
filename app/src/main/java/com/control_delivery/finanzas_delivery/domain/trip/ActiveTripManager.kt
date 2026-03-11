package com.control_delivery.finanzas_delivery.domain.trip

import android.location.Location
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that manages the currently active trip.
 *
 * Responsibilities:
 * - Holds the active [Trip] in a [StateFlow] so the UI can observe it reactively.
 * - Accumulates GPS distance via [onNewLocation] using [Location.distanceTo].
 * - Allows adding/removing orders to/from the active trip.
 * - Provides [completeTrip] to finalize the trip (GPS stops externally via TrackingService).
 *
 * This class does NOT start/stop GPS itself — that's the TrackingService's job.
 * It only receives location updates and accumulates distance.
 */
@Singleton
class ActiveTripManager @Inject constructor(
    private val locationTracker: com.control_delivery.finanzas_delivery.domain.location.LocationTracker
) {

    private val _activeTrip = MutableStateFlow<Trip?>(null)
    val activeTrip: StateFlow<Trip?> = _activeTrip.asStateFlow()

    private var lastLocation: Location? = null

    /** Whether a trip is currently in progress. */
    val isTracking: Boolean
        get() = _activeTrip.value != null

    /**
     * Starts a new trip with the given initial order.
     * Captures the current location immediately to establish the starting point.
     * If a trip is already active, this is a no-op (use [addOrder] instead).
     *
     * @return the Trip ID, or null if a trip was already active.
     */
    suspend fun startTrip(initialOrder: Order): String? {
        if (_activeTrip.value != null) {
            Timber.w("startTrip called but a trip is already active (id=${_activeTrip.value?.id})")
            return null
        }

        // Capture initial location immediately
        val startLocation = locationTracker.getCurrentLocation()
        lastLocation = startLocation

        val initialRoute = startLocation?.let {
            listOf(com.control_delivery.finanzas_delivery.domain.model.RoutePoint(it.latitude, it.longitude))
        } ?: emptyList()

        val trip = Trip(
            orders = listOf(initialOrder),
            status = TripStatus.ACTIVE,
            totalDistanceKm = 0.0,
            startTimestamp = System.currentTimeMillis(),
            route = initialRoute
        )
        _activeTrip.value = trip
        
        if (startLocation != null) {
            Timber.d("Trip started at: lat=${startLocation.latitude}, lng=${startLocation.longitude}")
        } else {
            Timber.w("Trip started but could not capture initial GPS point immediately")
        }
        
        return trip.id
    }

    /**
     * Adds an order to the currently active trip.
     * No-op if no trip is active or the order is already in the trip.
     */
    fun addOrder(order: Order) {
        _activeTrip.update { currentTrip ->
            if (currentTrip == null) {
                Timber.w("addOrder called but no active trip")
                return@update null
            }
            if (currentTrip.orders.any { it.id == order.id }) {
                Timber.w("Order ${order.id} already in trip ${currentTrip.id}")
                return@update currentTrip
            }
            Timber.d("Order ${order.id} added to trip ${currentTrip.id}")
            currentTrip.copy(orders = currentTrip.orders + order)
        }
    }

    /**
     * Advances the status of an order and captures the current location for pickup/delivery points.
     */
    fun advanceOrderStatus(orderId: String) {
        _activeTrip.update { currentTrip ->
            if (currentTrip == null) return@update null
            
            val updatedOrders = currentTrip.orders.map { order ->
                if (order.id == orderId) {
                    val newStatus = when (order.status) {
                        com.control_delivery.finanzas_delivery.domain.model.OrderStatus.ON_THE_WAY_TO_RECEIVE -> 
                            com.control_delivery.finanzas_delivery.domain.model.OrderStatus.RECEIVED
                        com.control_delivery.finanzas_delivery.domain.model.OrderStatus.RECEIVED -> 
                            com.control_delivery.finanzas_delivery.domain.model.OrderStatus.ON_THE_WAY_TO_DELIVERY
                        com.control_delivery.finanzas_delivery.domain.model.OrderStatus.ON_THE_WAY_TO_DELIVERY -> 
                            com.control_delivery.finanzas_delivery.domain.model.OrderStatus.DELIVERED
                        else -> order.status
                    }
                    
                    val currentLocation = lastLocation?.let { 
                        com.control_delivery.finanzas_delivery.domain.model.RoutePoint(it.latitude, it.longitude) 
                    }
                    
                    order.copy(
                        status = newStatus,
                        pickupLocation = if (newStatus == com.control_delivery.finanzas_delivery.domain.model.OrderStatus.RECEIVED) currentLocation else order.pickupLocation,
                        deliveryLocation = if (newStatus == com.control_delivery.finanzas_delivery.domain.model.OrderStatus.DELIVERED) currentLocation else order.deliveryLocation
                    )
                } else {
                    order
                }
            }
            currentTrip.copy(orders = updatedOrders)
        }
    }

    /**
     * Updates an existing order inside the active trip (e.g. platform or address edit).
     */
    fun updateOrder(updatedOrder: Order) {
        _activeTrip.update { currentTrip ->
            if (currentTrip == null) return@update null
            val updatedOrders = currentTrip.orders.map {
                if (it.id == updatedOrder.id) updatedOrder else it
            }
            currentTrip.copy(orders = updatedOrders)
        }
    }

    /**
     * Called by TrackingService each time a new GPS location arrives.
     * Calculates delta distance from previous location and accumulates it.
     */
    fun onNewLocation(location: Location) {
        val previous = lastLocation
        lastLocation = location

        if (previous == null) {
            Timber.d("First GPS fix received after start (or previous was missing). Recording initial point.")
            _activeTrip.update { currentTrip ->
                if (currentTrip == null) return@update null
                // If route is empty, add this as the first point
                if (currentTrip.route.isEmpty()) {
                    currentTrip.copy(
                        route = listOf(com.control_delivery.finanzas_delivery.domain.model.RoutePoint(location.latitude, location.longitude))
                    )
                } else {
                    currentTrip
                }
            }
            return
        }

        val deltaMeters = previous.distanceTo(location)
        val deltaKm = deltaMeters / 1000.0

        // Ignore unrealistically large jumps (> 2km in one update = likely GPS glitch)
        if (deltaKm > 2.0) {
            Timber.w("GPS jump too large (${deltaKm}km), ignoring")
            return
        }

        _activeTrip.update { currentTrip ->
            if (currentTrip == null) return@update null
            val newTotal = currentTrip.totalDistanceKm + deltaKm
            Timber.d("Distance: +${String.format("%.3f", deltaKm)}km = ${String.format("%.3f", newTotal)}km total")
            
            val newRoutePoint = com.control_delivery.finanzas_delivery.domain.model.RoutePoint(
                lat = location.latitude,
                lng = location.longitude
            )
            
            // Avoid adding duplicate consecutive points (simple check)
            if (currentTrip.route.isNotEmpty()) {
                val lastPoint = currentTrip.route.last()
                if (lastPoint.lat == newRoutePoint.lat && lastPoint.lng == newRoutePoint.lng) {
                    return@update currentTrip
                }
            }
            
            currentTrip.copy(
                totalDistanceKm = newTotal,
                route = currentTrip.route + newRoutePoint
            )
        }
    }

    /**
     * Completes the active trip and returns it for financial processing.
     * Resets internal state so a new trip can be started.
     *
     * @return the completed [Trip] snapshot, or null if no trip was active.
     */
    fun completeTrip(): Trip? {
        val trip = _activeTrip.value ?: run {
            Timber.w("completeTrip called but no active trip")
            return null
        }

        val completedTrip = trip.copy(
            status = TripStatus.COMPLETED,
            endTimestamp = System.currentTimeMillis()
        )

        _activeTrip.value = null
        lastLocation = null

        Timber.d(
            "Trip completed: id=${completedTrip.id}, " +
                "distance=${String.format("%.3f", completedTrip.totalDistanceKm)}km, " +
                "orders=${completedTrip.orders.size}, " +
                "totalAmount=${completedTrip.totalOrdersAmount}"
        )

        return completedTrip
    }

    /**
     * Cancels the active trip without processing financials.
     * Use only if the driver abandons the trip entirely.
     */
    fun cancelTrip() {
        val trip = _activeTrip.value
        _activeTrip.value = null
        lastLocation = null
        Timber.d("Trip cancelled: id=${trip?.id ?: "none"}")
    }

    /**
     * Returns the current accumulated distance in km, or 0.0 if no trip is active.
     */
    fun getCurrentDistanceKm(): Double {
        return _activeTrip.value?.totalDistanceKm ?: 0.0
    }

    /**
     * Returns the number of orders in the active trip, or 0 if no trip is active.
     */
    fun getActiveOrderCount(): Int {
        return _activeTrip.value?.orders?.size ?: 0
    }
}
