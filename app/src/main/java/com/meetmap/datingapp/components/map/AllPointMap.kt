package com.meetmap.datingapp.components.map

import android.graphics.PointF
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.meetmap.datingapp.R
import com.meetmap.datingapp.data.models.PlaceInfo
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.collections.forEach
import kotlin.collections.set

@Composable
fun AllPointMap(
    places: List<PlaceInfo>,
    onPlaceClick: (PlaceInfo) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    // принудительная перерисовка!!!!
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // ссылки
    val placemarksRef =
        remember { mutableStateMapOf<String, com.yandex.mapkit.map.PlacemarkMapObject>() }
    val listenersRef =
        remember { mutableStateMapOf<String, com.yandex.mapkit.map.MapObjectTapListener>() }

    val centerPoint = remember(places) {
        if (places.isEmpty()) Point(55.751574, 37.573856)
        else Point(places.map { it.latitude }.average(), places.map { it.longitude }.average())
    }

    val zoomLevel = remember(places.size) {
        when (places.size) {
            0 -> 10f
            1 -> 14f
            in 2..5 -> 12f
            else -> 10f
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("MAP_DEBUG", "ON_RESUME - принудительная перерисовка")
                mapViewRef?.post {
                    mapViewRef?.requestLayout()
                    mapViewRef?.invalidate()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

            // очистка при уничтожении
            placemarksRef.values.forEach { placemark ->
                listenersRef.values.forEach { listener ->
                    placemark.removeTapListener(listener)
                }
            }
            placemarksRef.clear()
            listenersRef.clear()
            mapView.map.mapObjects.clear()
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    // пересоздание маркерво
    LaunchedEffect(places) {
        android.util.Log.d("MAP_DEBUG", " ${places.size} мест")

        // очистка старых слушателей
        placemarksRef.values.forEach { placemark ->
            listenersRef.values.forEach { listener ->
                placemark.removeTapListener(listener)
            }
        }
        placemarksRef.clear()
        listenersRef.clear()

        // очистка
        mapView.map.mapObjects.clear()

        // позиция
        mapView.map.move(CameraPosition(centerPoint, zoomLevel, 0f, 0f))

        val imageProvider = ImageProvider.fromResource(context, R.drawable.png_point)

        places.forEach { place ->
            val point = Point(place.latitude, place.longitude)
            val placemark = mapView.map.mapObjects.addPlacemark(point)

            placemark.setIcon(
                imageProvider,
                IconStyle().apply {
                    scale = 0.2f
                    anchor = PointF(0.5f, 1f)
                }
            )

            placemark.setText(
                place.name,
                TextStyle().apply {
                    placement = TextStyle.Placement.BOTTOM
                    offset = 10f
                }
            )

            val listener = com.yandex.mapkit.map.MapObjectTapListener { _, _ ->
                mapView.map.move(
                    CameraPosition(point, 16f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.4f),
                    null
                )
                onPlaceClick(place)
                true
            }

            placemark.addTapListener(listener)

            placemarksRef[place.id] = placemark
            listenersRef[place.id] = listener
        }


    }

    AndroidView(
        factory = {
            mapViewRef = mapView
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )
}