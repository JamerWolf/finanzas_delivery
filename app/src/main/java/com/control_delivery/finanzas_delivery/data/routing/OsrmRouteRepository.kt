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

    override suspend fun getSnappedRoute(points: List<RoutePoint>): List<RoutePoint> {
        if (points.size < 2) return points

        // OSRM format: lon,lat;lon,lat;...
        val coordinateString = points.joinToString(";") { "${it.lng},${it.lat}" }
        
        try {
            val response = apiService.getMatch(coordinates = coordinateString)
            
            if (response.code == "Ok" && !response.matchings.isNullOrEmpty()) {
                val snappedPoints = mutableListOf<RoutePoint>()
                
                // OSRM might return multiple matchings if there are gaps. We combine them.
                response.matchings.forEach { matching ->
                    matching.geometry.coordinates.forEach { coord ->
                        if (coord.size >= 2) {
                            val lon = coord[0]
                            val lat = coord[1]
                            snappedPoints.add(RoutePoint(lat, lon))
                        }
                    }
                }
                Timber.d("Successfully snapped route. Original: ${points.size}, Snapped: ${snappedPoints.size}")
                return snappedPoints
            } else {
                Timber.w("OSRM returned non-Ok code: ${response.code}. Matchings null? ${response.matchings == null}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to snap route via OSRM")
        }

        // Fallback to original straight lines if API fails
        return points
    }
}
