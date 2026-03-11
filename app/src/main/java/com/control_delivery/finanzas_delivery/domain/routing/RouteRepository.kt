package com.control_delivery.finanzas_delivery.domain.routing

import com.control_delivery.finanzas_delivery.domain.model.RoutePoint

interface RouteRepository {
    /**
     * Takes a list of raw GPS coordinates and returns a snapped path
     * perfectly aligned to the street network.
     * Returns null if the network request fails or no match is found.
     */
    suspend fun getSnappedRoute(points: List<RoutePoint>): List<RoutePoint>?
}
