package com.example.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.ui.theme.PurpleLight
import com.example.datingapp.ui.theme.PurpleMedium

@Composable
fun SimpleBlock(
    title: String,
    subtitle: String,
    imageResId: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    imageScale: Float = 1.1f,
    showImage: Boolean = true,
    containerColor: Color = PurpleMedium,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 16.dp,
                        end = if (showImage && imageResId != null) 8.dp else 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    ),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = titleColor,
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = subtitleColor,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Фото показываем только если есть и showImage = true
            if (showImage && imageResId != null) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(percent = 32))
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(imageScale),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}