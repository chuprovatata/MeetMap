package com.meetmap.datingapp.screens.meets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.data.repository.FriendStatus
import com.meetmap.datingapp.data.repository.MyUser
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.GrayMedium2
import com.meetmap.datingapp.ui.theme.PurpleCard

@Composable
fun ItemMeets(
    user: MyUser,
    navController: NavController,

    status: FriendStatus,
    onClickButton: () -> Unit,

    ) {
    Row(modifier = Modifier.fillMaxWidth()) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 17.dp)
                .clickable {
                    navController.navigate(
                        Screen.ReqFriend.passParams(
                            friendId = user.uid,
                            pageTitle = "Заявки в друзья"
                        )
                    )

                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_male),
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
                        text = user.age.toString(),
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
            if (status.value == "request") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = PurpleCard, shape = RoundedCornerShape(12.dp))
                        .clickable {
                            onClickButton()

                        },
                    contentAlignment = Alignment.Center
                ) {


                    Text(
                        "принять",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
            if (status.value == "deny") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = GrayMedium2, shape = RoundedCornerShape(12.dp))
                    ,
                    contentAlignment = Alignment.Center
                ) {


                    Text(
                        "отклонено",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
            }
            if (status.value == "my_application") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.Transparent, shape = RoundedCornerShape(12.dp))
                        .clickable {

                        },
                    contentAlignment = Alignment.Center
                ) {


                    Text(
                        "запрос отправлен",
                        style = MaterialTheme.typography.displayMedium,
                        lineHeight = 17.sp,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }

        }

    }



}


@Composable
fun ItemMeetsFriend(user: MyUser, navController: NavController, title: String = "meet") {
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 17.dp)
                .clickable {
                    navController.navigate(Screen.CurFriend.passFriendId(user.uid))
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(R.drawable.profile_male),
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
                        text = user.telegram,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,

                        )

                }


            }
            Image(
                painter = painterResource(R.drawable.person),
                contentDescription = "icon",
                modifier = Modifier
                    .size(55.dp)

            )

        }


    }
}