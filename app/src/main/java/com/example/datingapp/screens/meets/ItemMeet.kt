package com.example.datingapp.screens.meets

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.navigation.NavController
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.User

@Composable
fun ItemMeets(user: User, navController: NavController, title:String="meet") {
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 17.dp)
                .clickable {
                    //для бд что-то такое
                    // navController.navigate("cur_friend/${user.username}")
                    if (title=="meet")
                    {
                    navController.navigate(Screen.ReqMeet.route)}
                    else{
                        navController.navigate(Screen.ReqFriend.route)

                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(user.icon),
                    contentDescription = "icon",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .padding(end = 12.dp)
                )

                Column() {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall

                    )
                    Text(
                        text = user.age,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,

                        )
                    Text(
                        text = user.university,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }

        }


    }
}






