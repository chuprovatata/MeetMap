package com.example.datingapp.components.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.R

@Composable
fun PlaceGridItem(
    placeName: String,
    imageRes: Int = R.drawable.picture_museum_background,
    likesCount: Int = 42,
    hasFireIcon: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Изображение места
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = placeName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Огненная иконка только если hasFireIcon = true
                if (hasFireIcon) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-8).dp, y = (-8).dp)  // Вылезает за границы
                            .size(36.dp)
                            .clip(CircleShape)  // Кружочек
                            .background(Color(0xFFA75CC6)),
                        contentAlignment = Alignment.Center
                    ) {
                        // SVG файл огонька (импортированный)
                        Icon(
                            painter = painterResource(id = R.drawable.icon_fire),
                            contentDescription = "Популярное",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White  // Если нужно изменить цвет
                        )
                    }
                }

                // Количество лайков справа снизу
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(width = 30.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFA75CC6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = likesCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Название места под картинкой
        Text(
            text = placeName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}