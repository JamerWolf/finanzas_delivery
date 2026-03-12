package com.control_delivery.finanzas_delivery.data.routing

import com.control_delivery.finanzas_delivery.domain.model.RoutePoint
import com.control_delivery.finanzas_delivery.domain.routing.RouteRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsrmRouteRepository @Inject constructor(
    private val apiService: OsrmApiService
) : RouteRepository {

    override suspend fun getSnappedRoute(points: List<RoutePoint>): List<RoutePoint>? {
        if (points.size < 2) return points

        // OSRM format: lon,lat;lon,lat;...
        val coordinateString = points.joinToString(";") { "${it.lng},${it.lat}" }
        
        // Calculate the "straight-line" distance of the original trace for validation
        val originalDistance = calculateTotalDistance(points)
        
        // 1. Try Match API first (best for high-density GPS traces)
        try {
            // Using a very small radius (10m) to force snapping to the closest street
            // and disabling 'tidy' to preserve all points.
            val radiusesString = points.joinToString(";") { "10" } 
            
            // Note: We are now using the FOSSGIS bicycle server.
            val response = apiService.getMatch(
                profile = "bicycle", 
                coordinates = coordinateString, 
                radiuses = radiusesString,
                tidy = false
            )
            
            if (response.code == "Ok" && !response.matchings.isNullOrEmpty()) {
                val matchedRoute = response.matchings.first()
                val osrmDistance = matchedRoute.distance ?: 0.0
                
                // VALIDATION: If OSRM distance is > 35% longer than the GPS trace, 
                // it likely created a "loop" to follow traffic rules (one-way streets, etc).
                if (osrmDistance > originalDistance * 1.35) {
                    Timber.w("OSRM Match rejected: Potential loop detected. GPS: ${originalDistance.toInt()}m, OSRM: ${osrmDistance.toInt()}m")
                    return null
                }

                val snappedPoints = mutableListOf<RoutePoint>()
                response.matchings.forEach { matching ->
                    matching.geometry.coordinates.forEach { coord ->
                        if (coord.size >= 2) {
                            snappedPoints.add(RoutePoint(lat = coord[1], lng = coord[0]))
                        }
                    }
                }
                Timber.d("OSRM Match successful. GPS Dist: ${originalDistance.toInt()}m, Snapped Dist: ${osrmDistance.toInt()}m")
                return snappedPoints
            } else {
                Timber.w("OSRM Match returned code: ${response.code}. Trying Route API fallback...")
            }
        } catch (e: Exception) {
            Timber.w("OSRM Match failed (${e.message}). Trying Route API fallback...")
        }

        // 2. Fallback to Route API (best for sparse waypoints or when Match fails)
        try {
            val response = apiService.getRoute(profile = "bicycle", coordinates = coordinateString)
            if (response.code == "Ok" && !response.routes.isNullOrEmpty()) {
                val routeData = response.routes.first()
                val osrmDistance = routeData.distance ?: 0.0

                if (osrmDistance > originalDistance * 1.35) {
                    Timber.w("OSRM Route rejected: Potential loop detected. GPS: ${originalDistance.toInt()}m, OSRM: ${osrmDistance.toInt()}m")
                    return null
                }

                val routePoints = mutableListOf<RoutePoint>()
                response.routes.forEach { route ->
                    route.geometry.coordinates.forEach { coord ->
                        if (coord.size >= 2) {
                            routePoints.add(RoutePoint(lat = coord[1], lng = coord[0]))
                        }
                    }
                }
                Timber.d("OSRM Route successful. GPS Dist: ${originalDistance.toInt()}m, Snapped Dist: ${osrmDistance.toInt()}m")
                return routePoints
            } else {
                Timber.w("OSRM Route failed with code: ${response.code}")
            }
        } catch (e: Exception) {
            Timber.e(e, "OSRM Route failed too.")
        }

        // Final fallback: Return null to indicate snapping failure.
        // The calling UseCase/ViewModel should handle this by showing the raw GPS trace.
        Timber.d("All OSRM attempts failed or rejected. Returning null to fallback to raw GPS.")
        return null
    }

    /**
     * Calculates the cumulative distance of a list of points in meters.
     */
    private fun calculateTotalDistance(points: List<RoutePoint>): Double {
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += haversineDistance(points[i], points[i + 1])
        }
        return total
    }

    private fun haversineDistance(p1: RoutePoint, p2: RoutePoint): Double {
        val r = 6371e3 // Earth radius in meters
        val lat1 = Math.toRadians(p1.lat)
        val lat2 = Math.toRadians(p2.lat)
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c
    }
}
