package com.control_delivery.finanzas_delivery.domain.repository

import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.model.TripStatus
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getTripById(id: String): Flow<Trip?>
    fun getTripByOrderId(orderId: String): Flow<Trip?>
    fun getTripsInDateRange(startDate: Long, endDate: Long): Flow<List<Trip>>
    fun getCompletedTripsInDateRange(startDate: Long, endDate: Long): Flow<List<Trip>>
    fun getTripsTotalAmountAfterKm(startDate: Long, endDate: Long): Flow<Long>
    suspend fun addTrip(trip: Trip): String
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(id: String)
}
