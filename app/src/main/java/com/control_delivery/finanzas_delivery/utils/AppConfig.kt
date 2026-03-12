package com.control_delivery.finanzas_delivery.utils

/**
 * Global application configuration flags.
 */
object AppConfig {
    /**
     * Whether to attempt snapping raw GPS traces to the road network using OSRM.
     * Set to false to show only raw GPS lines and save data/battery.
     */
    const val FEATURE_ROUTE_SNAPPING = false
}
