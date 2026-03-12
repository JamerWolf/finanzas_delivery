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
        
        // 1. Try Match API first (best for high-density GPS traces)
        try {
            // Reduced radius from 50 to 20 to force snapping to the actual road 
            // and avoid loops in side-streets or parallel roads.
            val radiusesString = points.joinToString(";") { "20" } 
            val response = apiService.getMatch(coordinates = coordinateString, radiuses = radiusesString)
            
            if (response.code == "Ok" && !response.matchings.isNullOrEmpty()) {
                val snappedPoints = mutableListOf<RoutePoint>()
                response.matchings.forEach { matching ->
                    matching.geometry.coordinates.forEach { coord ->
                        if (coord.size >= 2) {
                            snappedPoints.add(RoutePoint(lat = coord[1], lng = coord[0]))
                        }
                    }
                }
                Timber.d("OSRM Match successful. Original: ${points.size}, Snapped: ${snappedPoints.size}")
                return snappedPoints
            } else {
                Timber.w("OSRM Match returned code: ${response.code}. Trying Route API fallback...")
            }
        } catch (e: Exception) {
            Timber.w("OSRM Match failed (${e.message}). Trying Route API fallback...")
        }

        // 2. Fallback to Route API (best for sparse waypoints or when Match fails)
        try {
            val response = apiService.getRoute(coordinates = coordinateString)
            if (response.code == "Ok" && !response.routes.isNullOrEmpty()) {
                val routePoints = mutableListOf<RoutePoint>()
                response.routes.forEach { route ->
                    route.geometry.coordinates.forEach { coord ->
                        if (coord.size >= 2) {
                            routePoints.add(RoutePoint(lat = coord[1], lng = coord[0]))
                        }
                    }
                }
                Timber.d("OSRM Route successful. Points: ${routePoints.size}")
                return routePoints
            } else {
                Timber.w("OSRM Route failed with code: ${response.code}")
            }
        } catch (e: Exception) {
            Timber.e(e, "OSRM Route failed too.")
        }

        // Final fallback: Return null to indicate snapping failure (to avoid caching bad data)
        Timber.d("All OSRM attempts failed. Returning null.")
        return null
    }
}
