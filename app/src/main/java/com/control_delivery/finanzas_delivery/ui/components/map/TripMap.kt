package com.control_delivery.finanzas_delivery.ui.components.map

import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
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
 * The style URL uses "Dataviz Dark" or "Dataviz Light" to emulate the Cabify look.
 */
private const val MAPTILER_API_KEY = "WHxrBddYD6ejeLg3Udkr"
private const val DARK_STYLE_URL = "https://api.maptiler.com/maps/dataviz-dark/style.json?key=$MAPTILER_API_KEY"
private const val LIGHT_STYLE_URL = "https://api.maptiler.com/maps/dataviz-light/style.json?key=$MAPTILER_API_KEY"

@Composable
fun TripMap(
    route: List<RoutePoint>,
    modifier: Modifier = Modifier,
    bottomPadding: Int = 0
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

    // Track the last route size to decide if we should auto-zoom on update
    val lastRouteSize = remember { mutableStateOf(0) }

    // Lifecycle observer...
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
                    map.setStyle(targetStyle) { style ->
                        drawRoute(style, route)
                        adjustCameraToRoute(map, route, bottomPadding)
                        lastRouteSize.value = route.size
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
                        drawRoute(newStyle, route)
                        adjustCameraToRoute(map, route, bottomPadding)
                    }
                } else if (currentStyle.isFullyLoaded) {
                    // Update data
                    updateRouteInStyle(currentStyle, route)
                    
                    // If route size changed significantly (e.g. from raw trace to snapped trace), auto-zoom
                    if (route.size != lastRouteSize.value) {
                        adjustCameraToRoute(map, route, bottomPadding)
                        lastRouteSize.value = route.size
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
        // For single point, we zoom in but also offset it slightly up if padding is large
        map.easeCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(route[0].lat, route[0].lng), 15.0),
            1200
        )
    }
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
