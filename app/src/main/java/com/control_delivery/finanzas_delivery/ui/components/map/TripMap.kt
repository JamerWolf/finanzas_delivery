package com.control_delivery.finanzas_delivery.ui.components.map

import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.control_delivery.finanzas_delivery.domain.model.RoutePoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import timber.log.Timber

private const val MAPTILER_API_KEY = "WHxrBddYD6ejeLg3Udkr"
private const val DARK_STYLE_URL = "https://api.maptiler.com/maps/dataviz-dark/style.json?key=$MAPTILER_API_KEY"
private const val LIGHT_STYLE_URL = "https://api.maptiler.com/maps/dataviz-light/style.json?key=$MAPTILER_API_KEY"

@Composable
fun TripMap(
    route: List<RoutePoint>,
    orders: List<com.control_delivery.finanzas_delivery.domain.model.Order> = emptyList(),
    pickupColor: Int = Color.GREEN,
    deliveryColor: Int = Color.RED,
    modifier: Modifier = Modifier,
    bottomPadding: Int = 0,
    resetTrigger: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()
    val targetStyle = if (isDarkTheme) DARK_STYLE_URL else LIGHT_STYLE_URL

    // Initialize MapLibre globally
    remember {
        MapLibre.getInstance(context)
        null
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val lastRouteSize = remember { mutableIntStateOf(0) }
    val lastResetTrigger = remember { mutableIntStateOf(resetTrigger) }
    val lastStyleUri = remember { mutableStateOf("") }

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Effect to handle Data/Style updates when map is ready
    LaunchedEffect(mapInstance, targetStyle, route, orders, resetTrigger) {
        val map = mapInstance ?: return@LaunchedEffect
        
        // 1. Handle Style change (Theme)
        if (lastStyleUri.value != targetStyle) {
            Timber.d("TripMap: Setting style to $targetStyle")
            map.setStyle(targetStyle) { style ->
                lastStyleUri.value = targetStyle
                drawRoute(style, route, orders, pickupColor, deliveryColor)
                adjustCameraToRoute(map, route, bottomPadding)
                lastRouteSize.intValue = route.size
                lastResetTrigger.intValue = resetTrigger
            }
        } else {
            // 2. Handle Data updates if style is already loaded
            val currentStyle = map.style
            if (currentStyle != null && currentStyle.isFullyLoaded) {
                updateRouteInStyle(currentStyle, route, orders)
                
                // 3. Handle Auto-zoom if route changed or reset requested
                if (route.size != lastRouteSize.intValue || resetTrigger != lastResetTrigger.intValue) {
                    adjustCameraToRoute(map, route, bottomPadding)
                    lastRouteSize.intValue = route.size
                    lastResetTrigger.intValue = resetTrigger
                }
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.uiSettings.isRotateGesturesEnabled = false
                    map.uiSettings.isTiltGesturesEnabled = false
                    mapInstance = map
                }
            }
        },
        update = { /* Updates handled by LaunchedEffect */ }
    )
}

private fun adjustCameraToRoute(
    map: MapLibreMap, 
    route: List<RoutePoint>,
    bottomPadding: Int
) {
    if (route.isEmpty()) return
    val paddingSide = 100
    val paddingTop = 100
    val finalBottomPadding = bottomPadding + 50

    if (route.size > 1) {
        val boundsBuilder = LatLngBounds.Builder()
        route.forEach { boundsBuilder.include(LatLng(it.lat, it.lng)) }
        
        try {
            map.easeCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(), 
                    paddingSide, paddingTop, paddingSide, finalBottomPadding
                ),
                1200
            )
        } catch (e: Exception) {
            Timber.w("Failed to adjust camera: ${e.message}")
        }
    } else {
        map.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(route[0].lat, route[0].lng), 15.0), 1200)
    }
}

private fun updateRouteInStyle(
    style: Style, 
    routePoints: List<RoutePoint>,
    orders: List<com.control_delivery.finanzas_delivery.domain.model.Order>
) {
    val source = style.getSourceAs<GeoJsonSource>("route-source")
    val markerSource = style.getSourceAs<GeoJsonSource>("marker-source")
    val ordersSource = style.getSourceAs<GeoJsonSource>("orders-source")

    if (routePoints.size >= 2) {
        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
        source?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(points)))

        val startFeature = Feature.fromGeometry(points.first()).apply { addStringProperty("marker-type", "start") }
        val endFeature = Feature.fromGeometry(points.last()).apply { addStringProperty("marker-type", "end") }
        markerSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(startFeature, endFeature)))
    }

    val orderFeatures = orders.flatMap { order ->
        listOfNotNull(
            order.pickupLocation?.let { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply { addStringProperty("point-type", "pickup") } },
            order.deliveryLocation?.let { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply { addStringProperty("point-type", "delivery") } }
        )
    }
    ordersSource?.setGeoJson(FeatureCollection.fromFeatures(orderFeatures))
}

private fun drawRoute(
    style: Style, 
    routePoints: List<RoutePoint>,
    orders: List<com.control_delivery.finanzas_delivery.domain.model.Order>,
    pickupColor: Int,
    deliveryColor: Int
) {
    if (routePoints.size >= 2) {
        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
        val lineSource = GeoJsonSource("route-source", FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points))))
        style.addSource(lineSource)

        style.addLayer(LineLayer("route-layer", "route-source").apply {
            setProperties(
                PropertyFactory.lineColor(Color.parseColor("#4B6BFA")),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND)
            )
        })

        val startFeature = Feature.fromGeometry(points.first()).apply { addStringProperty("marker-type", "start") }
        val endFeature = Feature.fromGeometry(points.last()).apply { addStringProperty("marker-type", "end") }
        style.addSource(GeoJsonSource("marker-source", FeatureCollection.fromFeatures(arrayOf(startFeature, endFeature))))

        style.addLayer(CircleLayer("start-marker-layer", "marker-source").apply {
            setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("marker-type"), "start"))
            setProperties(PropertyFactory.circleRadius(8f), PropertyFactory.circleColor(Color.WHITE), PropertyFactory.circleStrokeWidth(2f), PropertyFactory.circleStrokeColor(Color.parseColor("#333333")))
        })

        style.addLayer(CircleLayer("end-marker-layer", "marker-source").apply {
            setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("marker-type"), "end"))
            setProperties(PropertyFactory.circleRadius(8f), PropertyFactory.circleColor(Color.parseColor("#4B6BFA")), PropertyFactory.circleStrokeWidth(2f), PropertyFactory.circleStrokeColor(Color.parseColor("#333333")))
        })
    }

    val orderFeatures = orders.flatMap { order ->
        listOfNotNull(
            order.pickupLocation?.let { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply { addStringProperty("point-type", "pickup") } },
            order.deliveryLocation?.let { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply { addStringProperty("point-type", "delivery") } }
        )
    }
    style.addSource(GeoJsonSource("orders-source", FeatureCollection.fromFeatures(orderFeatures)))

    style.addLayer(CircleLayer("pickup-layer", "orders-source").apply {
        setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("point-type"), "pickup"))
        setProperties(PropertyFactory.circleRadius(6f), PropertyFactory.circleColor(pickupColor), PropertyFactory.circleStrokeWidth(2f), PropertyFactory.circleStrokeColor(Color.WHITE))
    })

    style.addLayer(CircleLayer("delivery-layer", "orders-source").apply {
        setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("point-type"), "delivery"))
        setProperties(PropertyFactory.circleRadius(6f), PropertyFactory.circleColor(deliveryColor), PropertyFactory.circleStrokeWidth(2f), PropertyFactory.circleStrokeColor(Color.WHITE))
    })
}
