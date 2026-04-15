package com.meetmap.datingapp.screens.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.ui.theme.montserratFamily
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.Place
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.GrayPerson


data class User(
    val name: String="",
    val username: String="",
    val icon: Int=R.drawable.profile_female,
    val age: String="",
    val university: String="",
    val mutFriends: List<User> = listOf(),
    val favPlace: Place=Place(
        "123",
        placeName = "Surf Coffee",
        placeAddress = "Проспект Вернадского, 41",
        placeMetro = "Проспект Вернадского"
    ),
    val mutPlaces: List<Place> = listOf()
)

@Composable
fun UserItem(user: User, navController: NavController? = null,) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 25.dp)
            .clickable {
                //для бд что-то такое
                // navController.navigate("cur_friend/${user.username}")
                    navController?.navigate(Screen.CurFriend.route)
                }


            ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.75f),
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
                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Text(
                    text = user.username,
                    fontFamily = montserratFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
            }
        }

        Icon(
            modifier = Modifier

                .size(55.dp),
            painter = painterResource(R.drawable.person),
            contentDescription = "person",
            tint = GrayPerson,
        )


    }


}