package com.meetmap.datingapp.components.map

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.meetmap.datingapp.R
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@SuppressLint("ClickableViewAccessibility")
@Composable
fun OnePointMap(
    text: String,
    latitude: Double,
    longitude: Double,
    onInteractionChange: (Boolean) -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }

    val point = remember(latitude, longitude) {
        Point(latitude, longitude)
    }

    val placemarkRef = remember { mutableStateOf<com.yandex.mapkit.map.PlacemarkMapObject?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {

                map.move(
                    CameraPosition(point, 14f, 0f, 0f)
                )

                val imageProvider = ImageProvider.fromResource(
                    context,
                    R.drawable.png_point
                )

                val placemark = map.mapObjects.addPlacemark(point)
                placemarkRef.value = placemark

                placemark.setIcon(imageProvider, IconStyle().apply {
                    scale = 0.2f
                    anchor = PointF(0.5f, 1f)
                })

                placemark.setText(
                    text,
                    TextStyle().apply {
                        size = 9f
                        placement = TextStyle.Placement.BOTTOM
                        offset = 6f
                    }
                )

                placemark.addTapListener { _, _ ->

                    val currentZoom = map.cameraPosition.zoom

                    map.move(
                        CameraPosition(
                            point,
                            currentZoom + 2f,
                            0f,
                            0f
                        ),
                        Animation(Animation.Type.SMOOTH, 0.4f),
                        null
                    )

                    true
                }


                setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        android.view.MotionEvent.ACTION_DOWN -> {

                            onInteractionChange(true)
                        }

                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> {

                            onInteractionChange(false)
                        }
                    }

                    false
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .height(300.dp)
    )
}