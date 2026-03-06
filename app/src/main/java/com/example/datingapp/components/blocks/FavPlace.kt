package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datingapp.R
import com.example.datingapp.ui.theme.PurpleMedium
import com.example.datingapp.ui.theme.montserratFamily
import com.example.datingapp.utils.CloudImageUtils

@Composable
fun FavPlace(
    placeName: String?,
    photoUrl: String? = null
) {
    if (placeName.isNullOrBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .clip(RoundedCornerShape(16.dp))
            .background(color = PurpleMedium)
            .height(200.dp)
            .padding(top = 15.dp)
            .padding(horizontal = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Любимое место",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!photoUrl.isNullOrBlank() && photoUrl != CloudImageUtils.NO_PICTURE_URL) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Фото любимого места",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.picture_museum_background),
                        placeholder = painterResource(id = R.drawable.picture_museum_background)
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        painter = painterResource(id = R.drawable.picture_museum_background),
                        contentDescription = "Фото любимого места",
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = placeName,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}