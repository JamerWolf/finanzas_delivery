package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import com.control_delivery.finanzas_delivery.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TripInMemoryRepository(
    initialTrips: List<Trip> = TripsFake.createTrips(orderInstance.orders)
) : TripRepository {

    private val trips = initialTrips.toMutableList()
    private val _tripsFlow = MutableStateFlow(trips.toList())

    override fun getTripById(id: String): Flow<Trip?> {
        return _tripsFlow.map { list -> list.find { it.id == id } }
    }

    override fun getTripByOrderId(orderId: String): Flow<Trip?> {
        return _tripsFlow.map { list -> list.find { trip -> trip.orders.any { it.id == orderId } } }
    }

    override fun getTripsInDateRange(startDate: Long, endDate: Long): Flow<List<Trip>> {
        return _tripsFlow.map { list ->
            list.filter { !it.isDeleted && it.startTimestamp in startDate..endDate }
        }
    }

    override fun getCompletedTripsInDateRange(startDate: Long, endDate: Long): Flow<List<Trip>> {
        return _tripsFlow.map { list ->
            list.filter {
                !it.isDeleted &&
                it.status == TripStatus.COMPLETED &&
                it.startTimestamp in startDate..endDate
            }
        }
    }

    override fun getTripsTotalAmountAfterKm(startDate: Long, endDate: Long): Flow<Long> {
        return _tripsFlow.map { list ->
            list.filter {
                !it.isDeleted &&
                it.status == TripStatus.COMPLETED &&
                it.startTimestamp in startDate..endDate
            }.sumOf { it.amountAfterKmDeduction }
        }
    }

    override suspend fun addTrip(trip: Trip): String {
        trips.add(trip)
        _tripsFlow.value = trips.toList()
        return trip.id
    }

    override suspend fun updateTrip(trip: Trip) {
        val index = trips.indexOfFirst { it.id == trip.id }
        if (index != -1) {
            trips[index] = trip
            _tripsFlow.value = trips.toList()
        }
    }

    override suspend fun deleteTrip(id: String) {
        val index = trips.indexOfFirst { it.id == id }
        if (index != -1) {
            trips[index] = trips[index].copy(isDeleted = true)
            _tripsFlow.value = trips.toList()
        }
    }
}
