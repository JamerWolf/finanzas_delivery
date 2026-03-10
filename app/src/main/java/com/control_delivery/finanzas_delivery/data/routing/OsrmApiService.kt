package com.control_delivery.finanzas_delivery.data.routing

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApiService {
    @GET("match/v1/driving/{coordinates}")
    suspend fun getMatch(
        @Path(value = "coordinates", encoded = true) coordinates: String,
        @Query("geometries") geometries: String = "geojson",
        @Query("overview") overview: String = "full"
    ): OsrmMatchResponse
}
