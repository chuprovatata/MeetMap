package com.example.datingapp.components.blocks

import android.R
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datingapp.screens.friends.User
import com.example.datingapp.ui.theme.boundedFamily
import com.example.datingapp.ui.theme.montserratFamily

@Composable
fun UserInfo(user: User) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .size(144.dp),
            painter = painterResource(id = user.icon),
            contentDescription = "icon"
        )
        Spacer(modifier = Modifier.width(21.dp))
        Column(modifier = Modifier.fillMaxHeight(),
            ) {
            Text(
                text = "@" + user.username,
                fontFamily = boundedFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 25.sp
            )
            Text(
                text = user.name,
                fontFamily = montserratFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.age + "лет",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = user.university,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
