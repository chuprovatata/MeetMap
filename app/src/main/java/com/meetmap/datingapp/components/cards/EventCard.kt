package com.meetmap.datingapp.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.buttons.PrimaryButton
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.GrayDark
import com.meetmap.datingapp.ui.theme.GrayLight
import com.meetmap.datingapp.ui.theme.GrayMedium
import com.meetmap.datingapp.ui.theme.GrayMedium2
import com.meetmap.datingapp.ui.theme.GrayPerson
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.PurpleLight


@Composable
fun EventCard(
    title: String = "Название",
    date: String = "дата",
    description: String = "description",
    imageUrl: String? = null,
    imageRes: Int? = R.drawable.place1,
    isUserJoined: Boolean = false,
    isOrganizer: Boolean = false,
    onJoinClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = GrayLight.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Event image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.place1)
                    )
                } else {
                    Image(
                        painter = painterResource(imageRes ?: R.drawable.place1),
                        contentDescription = "Event image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 20.sp
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                // пользователь — организатор, то редактировать кнопка
                if (isOrganizer) {
                    PrimaryButton(
                        text = "Редактировать",
                        onClick = onEditClick,
                        modifier = Modifier.padding(top = 4.dp),
                        textSize = 13.sp,
                        containerColor = PurpleCard,
                        contentColor = Color.White
                    )
                } else {
                    PrimaryButton(
                        text = if (isUserJoined) "Я иду" else "Я пойду",
                        onClick = onJoinClick,
                        modifier = Modifier.padding(top = 4.dp),
                        textSize = 13.sp,
                        containerColor = if (isUserJoined) GrayPerson else PurpleCard,
                        contentColor = Color.White
                    )
                }
            }
        }
    }
}