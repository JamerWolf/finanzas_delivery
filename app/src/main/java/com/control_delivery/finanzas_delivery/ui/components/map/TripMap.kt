package com.control_delivery.finanzas_delivery.ui.components.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
 * The style URL uses "Dataviz Dark" to emulate the Cabify dark mode look.
 */
private const val MAPTILER_API_KEY = "WHxrBddYD6ejeLg3Udkr"
private const val STYLE_URL = "https://api.maptiler.com/maps/dataviz-dark/style.json?key=$MAPTILER_API_KEY"

@Composable
fun TripMap(
    route: List<RoutePoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                    map.setStyle(STYLE_URL) { style ->
                        drawRoute(style, route)
                        
                        // Fit camera bounds to route
                        if (route.size > 1) {
                            val boundsBuilder = LatLngBounds.Builder()
                            route.forEach { point ->
                                boundsBuilder.include(LatLng(point.lat, point.lng))
                            }
                            // Add some padding (e.g. 100 pixels) around the bounds
                            map.easeCamera(
                                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
                                1000 // 1 second animation
                            )
                        } else if (route.size == 1) {
                            map.easeCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(route.first().lat, route.first().lng), 14.0),
                                1000
                            )
                        }
                    }
                }
            }
        },
        update = { view ->
            view.getMapAsync { map ->
                val style = map.style
                if (style != null && style.isFullyLoaded) {
                    updateRouteInStyle(style, route)
                }
            }
        }
    )
}

private fun updateRouteInStyle(style: Style, routePoints: List<RoutePoint>) {
    val source = style.getSourceAs<GeoJsonSource>("route-source")
    val markerSource = style.getSourceAs<GeoJsonSource>("marker-source")

    if (routePoints.size < 2) return

    val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
    
    // Update the line
    val lineString = LineString.fromLngLats(points)
    source?.setGeoJson(Feature.fromGeometry(lineString))

    // Update markers
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

private fun drawRoute(style: Style, routePoints: List<RoutePoint>) {
    if (routePoints.size < 2) return

    val points = routePoints.map { Point.fromLngLat(it.lng, it.lat) }
    val lineString = LineString.fromLngLats(points)
    val feature = Feature.fromGeometry(lineString)

    // 1. Add Source
    val sourceId = "route-source"
    val source = GeoJsonSource(sourceId, FeatureCollection.fromFeature(feature))
    style.addSource(source)

    // 2. Add Line Layer (The polyline itself)
    val lineLayerId = "route-layer"
    val lineLayer = LineLayer(lineLayerId, sourceId).apply {
        setProperties(
            PropertyFactory.lineColor(Color.parseColor("#4B6BFA")), // Cabify-like blue/purple
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
            PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND)
        )
    }
    style.addLayer(lineLayer)

    // 3. Add Origin and Destination Markers
    val startPoint = points.first()
    val endPoint = points.last()

    val markerSourceId = "marker-source"
    val startFeature = Feature.fromGeometry(startPoint).apply {
        addStringProperty("marker-type", "start")
    }
    val endFeature = Feature.fromGeometry(endPoint).apply {
        addStringProperty("marker-type", "end")
    }
    val markerSource = GeoJsonSource(markerSourceId, FeatureCollection.fromFeatures(arrayOf(startFeature, endFeature)))
    style.addSource(markerSource)

    // Start Marker (White/Light gray dot)
    style.addLayer(CircleLayer("start-marker-layer", markerSourceId).apply {
        setFilter(org.maplibre.android.style.expressions.Expression.eq(org.maplibre.android.style.expressions.Expression.get("marker-type"), "start"))
        setProperties(
            PropertyFactory.circleRadius(8f),
            PropertyFactory.circleColor(Color.WHITE),
            PropertyFactory.circleStrokeWidth(2f),
            PropertyFactory.circleStrokeColor(Color.parseColor("#333333"))
        )
    })

    // End Marker (Blue/Purple dot)
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
