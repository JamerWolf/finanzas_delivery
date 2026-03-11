package com.control_delivery.finanzas_delivery.domain.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Domain interface for location tracking.
 * Abstracts the GPS provider so the domain layer doesn't depend on Google Play Services.
 */
interface LocationTracker {
    /**
     * Returns a Flow of Location updates.
     * The implementation decides the interval and accuracy.
     */
    fun getLocationUpdates(): Flow<Location>

    /**
     * Attempts to get the current location of the device once.
     */
    suspend fun getCurrentLocation(): Location?

    /**
     * Stops receiving location updates and cleans up resources.
     */
    fun stopTracking()
}
