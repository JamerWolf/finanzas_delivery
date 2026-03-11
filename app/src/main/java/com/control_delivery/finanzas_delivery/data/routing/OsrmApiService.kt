package com.control_delivery.finanzas_delivery.data.routing

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApiService {
    @GET("match/v1/driving/{coordinates}")
    suspend fun getMatch(
        @Path(value = "coordinates", encoded = true) coordinates: String,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("tidy") tidy: Boolean = true,
        @Query("gaps") gaps: String = "ignore",
        @Query("radiuses") radiuses: String? = null
    ): OsrmResponse

    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path(value = "coordinates", encoded = true) coordinates: String,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full",
        @Query("alternatives") alternatives: Boolean = false
    ): OsrmResponse
}
