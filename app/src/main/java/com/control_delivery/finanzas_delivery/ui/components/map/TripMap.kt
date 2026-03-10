package com.control_delivery.finanzas_delivery.ui.components.map

import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/**
 * Replace this with your actual MapTiler API key.
 */
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

    // Initialize MapLibre globally (only happens once)
    remember {
        MapLibre.getInstance(context)
        null
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    // Track the last values to detect changes in the update block
    val lastRouteSize = remember { mutableStateOf(0) }
    val lastResetTrigger = remember { mutableStateOf(resetTrigger) }

    // Lifecycle observer to manage MapView lifecycle safely in Compose
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
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { 
            mapView.apply {
                getMapAsync { map ->
                    // Disable rotation and tilt for a cleaner UX
                    map.uiSettings.isRotateGesturesEnabled = false
                    map.uiSettings.isTiltGesturesEnabled = false
                    
                    map.setStyle(targetStyle) { style ->
                        drawRoute(style, route, orders, pickupColor, deliveryColor)
                        adjustCameraToRoute(map, route, bottomPadding)
                        lastRouteSize.value = route.size
                        lastResetTrigger.value = resetTrigger
                    }
                }
            }
        },
        update = { view ->
            view.getMapAsync { map ->
                val currentStyle = map.style ?: return@getMapAsync
                
                // Detection of theme change
                if (currentStyle.isFullyLoaded && currentStyle.uri != targetStyle) {
                    map.setStyle(targetStyle) { newStyle ->
                        drawRoute(newStyle, route, orders, pickupColor, deliveryColor)
                        adjustCameraToRoute(map, route, bottomPadding)
                    }
                } else if (currentStyle.isFullyLoaded) {
                    // Update data
                    updateRouteInStyle(currentStyle, route, orders)
                    
                    // If route size changed significantly or reset was triggered, auto-zoom
                    if (route.size != lastRouteSize.value || resetTrigger != lastResetTrigger.value) {
                        adjustCameraToRoute(map, route, bottomPadding)
                        lastRouteSize.value = route.size
                        lastResetTrigger.value = resetTrigger
                    }
                }
            }
        }
    )
}

private fun adjustCameraToRoute(
    map: org.maplibre.android.maps.MapLibreMap, 
    route: List<RoutePoint>,
    bottomPadding: Int
) {
    if (route.isEmpty()) return
    
    // Standard padding for sides/top
    val paddingSide = 100
    val paddingTop = 100
    
    // We add the custom bottomPadding (from the sheet) + a safety margin
    val finalBottomPadding = bottomPadding + 50

    if (route.size > 1) {
        val boundsBuilder = LatLngBounds.Builder()
        route.forEach { point ->
            boundsBuilder.include(LatLng(point.lat, point.lng))
        }
        
        map.easeCamera(
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(), 
                paddingSide, 
                paddingTop, 
                paddingSide, 
                finalBottomPadding
            ),
            1200
        )
    } else {
        map.easeCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(route[0].lat, route[0].lng), 15.0),
            1200
        )
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
        val lineString = LineString.fromLngLats(points)
        source?.setGeoJson(Feature.fromGeometry(lineString))

        // Update main markers
        val startPoint = points.first()
        val endPoint = points.last()
        val startFeature = Feature.fromGeometry(startPoint).apply {
            addStringProperty("marker-type", "start")
        }
        val endFeature = Feature.fromGeometry(endPoint).apply {
            addStringProperty("marker-type", "end")
        }
        markerSource?.setGeoJson(FeatureCollection.fromFeatures(arrayOf(startFeature, endFeature)))
    }

    // Update order points
    val orderFeatures = mutableListOf<Feature>()
    orders.forEach { order ->
        order.pickupLocation?.let { 
            orderFeatures.add(Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply {
                addStringProperty("point-type", "pickup")
            })
        }
        order.deliveryLocation?.let { 
            orderFeatures.add(Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply {
                addStringProperty("point-type", "delivery")
            })
        }
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
    // 1. Line and Main Markers (Start/End)
    if (routePoints.size >= 2) {
        val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
        val lineString = LineString.fromLngLats(points)
        val feature = Feature.fromGeometry(lineString)

        val sourceId = "route-source"
        val source = GeoJsonSource(sourceId, FeatureCollection.fromFeature(feature))
        style.addSource(source)

        val lineLayer = LineLayer("route-layer", sourceId).apply {
            setProperties(
                PropertyFactory.lineColor(Color.parseColor("#4B6BFA")),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND)
            )
        }
        style.addLayer(lineLayer)

        val startPoint = points.first()
        val endPoint = points.last()
        val markerSourceId = "marker-source"
        val startFeature = Feature.fromGeometry(startPoint).apply { addStringProperty("marker-type", "start") }
        val endFeature = Feature.fromGeometry(endPoint).apply { addStringProperty("marker-type", "end") }
        val markerSource = GeoJsonSource(markerSourceId, FeatureCollection.fromFeatures(arrayOf(startFeature, endFeature)))
        style.addSource(markerSource)

        style.addLayer(CircleLayer("start-marker-layer", markerSourceId).apply {
            setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("marker-type"), "start"))
            setProperties(
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleColor(Color.WHITE),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(Color.parseColor("#333333"))
            )
        })

        style.addLayer(CircleLayer("end-marker-layer", markerSourceId).apply {
            setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("marker-type"), "end"))
            setProperties(
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleColor(Color.parseColor("#4B6BFA")),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(Color.parseColor("#333333"))
            )
        })
    }

    // 2. Order Specific Points (Pickup/Delivery)
    val orderFeatures = mutableListOf<Feature>()
    orders.forEach { order ->
        order.pickupLocation?.let { 
            orderFeatures.add(Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply {
                addStringProperty("point-type", "pickup")
            })
        }
        order.deliveryLocation?.let { 
            orderFeatures.add(Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)).apply {
                addStringProperty("point-type", "delivery")
            })
        }
    }

    val ordersSourceId = "orders-source"
    val ordersSource = GeoJsonSource(ordersSourceId, FeatureCollection.fromFeatures(orderFeatures))
    style.addSource(ordersSource)

    // Pickup Layer
    style.addLayer(CircleLayer("pickup-layer", ordersSourceId).apply {
        setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("point-type"), "pickup"))
        setProperties(
            PropertyFactory.circleRadius(6f),
            PropertyFactory.circleColor(pickupColor),
            PropertyFactory.circleStrokeWidth(2f),
            PropertyFactory.circleStrokeColor(Color.WHITE)
        )
    })

    // Delivery Layer
    style.addLayer(CircleLayer("delivery-layer", ordersSourceId).apply {
        setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("point-type"), "delivery"))
        setProperties(
            PropertyFactory.circleRadius(6f),
            PropertyFactory.circleColor(deliveryColor),
            PropertyFactory.circleStrokeWidth(2f),
            PropertyFactory.circleStrokeColor(Color.WHITE)
        )
    })
}
