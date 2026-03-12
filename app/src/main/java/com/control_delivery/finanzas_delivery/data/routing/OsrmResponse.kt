package com.control_delivery.finanzas_delivery.data.routing

import com.google.gson.annotations.SerializedName

data class OsrmResponse(
    val code: String,
    val matchings: List<OsrmRouteData>?,
    val routes: List<OsrmRouteData>?
)

data class OsrmRouteData(
    val geometry: OsrmGeometry,
    val distance: Double?,
    val duration: Double?
)

data class OsrmGeometry(
    val type: String,
    val coordinates: List<List<Double>> // [ [lon, lat], [lon, lat] ]
)
