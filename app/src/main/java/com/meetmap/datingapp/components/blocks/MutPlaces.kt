package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.R
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.ui.theme.GrayMedium2

@Composable
fun MutPlaces(
    places: List<PlaceInfo>,
    onPlaceClick: (PlaceInfo) -> Unit = {}
) {
    if (places.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = GrayMedium2)
            .padding(top = 10.dp)
            .padding(horizontal = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Общие места",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            places.forEach { place ->
                MutItemPlaces(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
fun MutItemPlaces(
    place: PlaceInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Фото места
        if (place.photoUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(place.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = place.name,
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.picture_museum_background),
                placeholder = painterResource(id = R.drawable.picture_museum_background)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.picture_museum_background),
                contentDescription = place.name,
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = place.categories.firstOrNull() ?: "Место",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}