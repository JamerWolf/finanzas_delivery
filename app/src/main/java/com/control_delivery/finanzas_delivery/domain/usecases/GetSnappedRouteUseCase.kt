package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.RoutePoint
import com.control_delivery.finanzas_delivery.domain.routing.RouteRepository
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Handles snapping a GPS trace to the road network.
 * To respect OSRM public server limits:
 * - Batches coordinates if there are too many (OSRM usually limits to 100).
 * - Implements a slight delay between requests to avoid rate limits.
 */
class GetSnappedRouteUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(rawRoute: List<RoutePoint>): List<RoutePoint> {
        if (rawRoute.size < 2) return rawRoute

        // OSRM public server has limits on URL length and number of points for Match service.
        // We reduce from 90 to 40 to avoid "TooBig" errors (HTTP 400).
        val chunkSize = 40
        val chunkedRoutes = rawRoute.chunked(chunkSize)
        val finalSnappedRoute = mutableListOf<RoutePoint>()

        try {
            for ((index, chunk) in chunkedRoutes.withIndex()) {
                // If it's not the first chunk, we prepend the LAST point of the previous chunk 
                // so the sub-routes connect perfectly without gaps.
                val pointsToSnap = if (index > 0 && finalSnappedRoute.isNotEmpty()) {
                    listOf(rawRoute[index * chunkSize - 1]) + chunk
                } else {
                    chunk
                }

                val snappedChunk = routeRepository.getSnappedRoute(pointsToSnap)
                
                // Avoid duplicating the connecting point
                if (index > 0 && finalSnappedRoute.isNotEmpty() && snappedChunk.isNotEmpty()) {
                    finalSnappedRoute.addAll(snappedChunk.drop(1))
                } else {
                    finalSnappedRoute.addAll(snappedChunk)
                }

                // Respect the public OSRM rate limit (1 request per second)
                if (index < chunkedRoutes.size - 1) {
                    delay(1100) 
                }
            }
            return finalSnappedRoute
        } catch (e: Exception) {
            Timber.e(e, "Error snapping route in UseCase. Falling back to raw trace.")
            return rawRoute
        }
    }
}
