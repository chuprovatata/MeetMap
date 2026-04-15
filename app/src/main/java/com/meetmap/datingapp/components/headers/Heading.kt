package com.meetmap.datingapp.components.headers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.ui.theme.boundedFamily

@Composable
fun Heading_Arrow(
    heading: String,
    navController: NavController,
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                if (onBackClick != null) {
                    onBackClick()
                } else {
                    navController.popBackStack()
                }
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_left),
                contentDescription = "arrow_left",
                tint = Color.Black
            )
        }

        Text(
            text = heading,
            fontSize = 35.sp,
            fontFamily = boundedFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun Heading(
    heading: String,
    showBackButton: Boolean = false,
    showSettings: Boolean = true,
    showProfile: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    navController: NavController
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(
                onClick = {
                    if (onBackClick != null) {
                        onBackClick()
                    } else {
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = "Назад",
                    tint = Color.Black
                )
            }
        } else {
            // Ничего не ставим
        }

        Text(
            text = heading,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (showSettings) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            navController.navigate("settings")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_settings),
                        contentDescription = "Настройки",
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
            if (showProfile) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            navController.navigate("my_profile")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_person),
                        contentDescription = "Профиль",
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }
}