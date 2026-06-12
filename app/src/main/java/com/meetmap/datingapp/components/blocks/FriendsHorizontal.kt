package com.meetmap.datingapp.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.data.repository.MyUser
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.ui.theme.boundedFamily

@Composable
fun FriendsHorizontal(
    header: String, friends: List<MyUser>, navController: NavController,
    fontSize: TextUnit = 25.sp,size: Dp = 78.dp

    ) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = header,
            style = MaterialTheme.typography.displayMedium,
            fontSize = fontSize
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (friends.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Text(
                    text = "Находи единомышленников по любимым местам в разделе ",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Знакомства",
                    color = PurpleCard,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {

                        navController.navigate(Screen.PeopleOfDay.route)
                    }
                )

                Text(
                    text = "или добавляй в друзья, тех кого уже знаешь, в разделе ",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Друзья",
                    color = PurpleCard,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {

                        navController.navigate("main_bottom_menu/screen_1")
                    }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                friends.forEach { friend ->
                    ItemFriendsHorizontal(friend, navController, size)
                    Spacer(modifier = Modifier.width(23.dp))
                }
            }
        }
    }
}

@Composable
fun ItemFriendsHorizontal(
    user: MyUser,
    navController: NavController,
    size: Dp = 78.dp
) {
    Column(modifier = Modifier.clickable {
        navController.navigate(Screen.CurFriend.passFriendId(user.uid))
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.profile_male),
            contentDescription = "icon",
            modifier = Modifier.size(size)
        )
        Text(
            text = user.username,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraLight,
            fontFamily = boundedFamily
        )
    }
}