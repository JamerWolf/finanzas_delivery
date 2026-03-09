package com.control_delivery.finanzas_delivery.domain.model

sealed class DistanceType {
    abstract val value: Double

    data class ToPickup (override val value: Double) : DistanceType()
    data class ToDelivery (override val value: Double) : DistanceType()
}
