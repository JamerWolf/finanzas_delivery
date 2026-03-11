package com.control_delivery.finanzas_delivery.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.control_delivery.finanzas_delivery.domain.location.LocationTracker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * GPS implementation using Google Play Services Fused Location Provider.
 * Balanced mode: ~10s interval, 5m minimum displacement filter.
 */
class FusedLocationTrackerImpl(
    context: Context
) : LocationTracker {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10_000L // 10 second interval
    ).apply {
        setMinUpdateDistanceMeters(5f)
        setMinUpdateIntervalMillis(5_000L) // fastest 5 seconds
    }.build()

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Timber.d("GPS update: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")
                    trySend(location)
                }
            }
        }

        Timber.d("Starting GPS location updates")
        fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())

        awaitClose {
            Timber.d("Stopping GPS location updates (flow closed)")
            fusedClient.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        // Try getting last known location first (fastest)
        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
            if (lastLoc != null && (System.currentTimeMillis() - lastLoc.time) < 30_000) {
                Timber.d("Using fresh lastKnownLocation: lat=${lastLoc.latitude}, lng=${lastLoc.longitude}")
                continuation.resume(lastLoc)
            } else {
                // If last location is null or too old, request a fresh one
                Timber.d("Last location missing or old, requesting fresh fix...")
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { freshLoc ->
                        Timber.d("Fresh fix received: lat=${freshLoc?.latitude}, lng=${freshLoc?.longitude}")
                        continuation.resume(freshLoc)
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Failed to get fresh current location")
                        continuation.resume(null)
                    }
            }
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to get last known location")
            continuation.resume(null)
        }
    }

    override fun stopTracking() {
        // No-op: cleanup is handled by the callbackFlow's awaitClose
        Timber.d("stopTracking called")
    }
}
