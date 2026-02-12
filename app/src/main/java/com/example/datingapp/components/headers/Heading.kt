package com.example.datingapp.components.headers

import android.net.sip.SipProfile
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.boundedFamily


@Composable
fun Heading_Arrow(heading: String, navController: NavController) {

    Row(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        IconButton(
            onClick = {
                navController.popBackStack()
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
            fontWeight = FontWeight.Normal
        )


    }
}

@Composable
fun Heading(
    heading: String,
    settings: Boolean = true,
    profile: Boolean = true,
    navController: NavController
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (settings) {
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
            if (profile) {
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
