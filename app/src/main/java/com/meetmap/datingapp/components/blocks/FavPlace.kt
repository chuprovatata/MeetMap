package com.meetmap.datingapp.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import com.meetmap.datingapp.ui.theme.PurpleMedium
import com.meetmap.datingapp.ui.theme.montserratFamily
import com.meetmap.datingapp.utils.CloudImageUtils

@Composable
fun FavPlace(
    placeName: String?,
    placeAddress: String? = null,
    placeComment: String? = null,
    photoUrl: String? = null,
    isUploading: Boolean = false,
    isEditable: Boolean = false,
    onCardClick: (() -> Unit)? = null
) {
    if (placeName.isNullOrBlank() && !isEditable) return

    var isExpanded by remember { mutableStateOf(false) }
    val hasComment = !placeComment.isNullOrBlank()
    val showExpandButton = hasComment

    val commentHeight = if (isExpanded && hasComment) {
        val lines = (placeComment?.length ?: 0) / 30 + 2
        (lines * 24).dp + 24.dp
    } else 0.dp

    val cardHeight = 200.dp + commentHeight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(color = PurpleMedium)
            .padding(top = 15.dp)
            .padding(horizontal = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Любимое место",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 25.sp
                )

                if (showExpandButton) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isExpanded = !isExpanded }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded) R.drawable.icon_arrow_up else R.drawable.icon_arrow_down
                            ),
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                val hasValidPhoto = !photoUrl.isNullOrBlank() &&
                        photoUrl != CloudImageUtils.NO_PICTURE_URL &&
                        photoUrl != "https://storage.yandexcloud.net/meetmap/photoplace/NO%20PICTURE.jpg"

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (hasValidPhoto) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Фото любимого места",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.picture_museum_background),
                            placeholder = painterResource(id = R.drawable.picture_museum_background)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_camera),
                                contentDescription = "Нет фото",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (!placeName.isNullOrBlank()) {
                        Text(
                            text = placeName,
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!placeAddress.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = placeAddress,
                                fontFamily = montserratFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                color = Color.Gray,
                                maxLines = 2
                            )
                        }
                    } else if (isEditable) {
                        Text(
                            text = buildAnnotatedString {
                                append("Добавь свое любимое место в ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append("Настройках")
                                }
                                append(", чтобы твои друзья знали, что тебе нравится!")
                            },
                            fontFamily = montserratFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (isExpanded && hasComment) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = placeComment,
                        fontFamily = montserratFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}