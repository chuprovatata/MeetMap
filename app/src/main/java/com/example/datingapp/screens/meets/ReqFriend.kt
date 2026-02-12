package com.example.datingapp.screens.meets

import android.R.attr.spacing
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.stylusHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.GrayBlock
import com.example.datingapp.components.blocks.MutPlaces
import com.example.datingapp.components.blocks.Place
import com.example.datingapp.components.blocks.Sub_Block
import com.example.datingapp.components.blocks.Title_Block
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.navigation.Screen
import com.example.datingapp.screens.friends.User
import com.example.datingapp.ui.theme.GrayDark
import com.example.datingapp.ui.theme.LocalDatingAppSpacing

@Composable
fun ReqFriend(navController: NavController) {
    val spacing = LocalDatingAppSpacing.current

    Scaffold(
        topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Заявки в друзья", navController)
            }
        },


        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp)
                    .padding(bottom = 150.dp)
            ) {
                val user1 = User(
                    name = "Кирилл",
                    username = "kirill_it",
                    icon = R.drawable.profile_male,
                    age = "25",
                    university = "МФТИ",
                    mutFriends = listOf(
                        User(username = "@Nase"),
                        User(username = "@Nase"),
                        User(username = "@Nase")
                    ),
                    mutPlaces = listOf(
                        Place(placeName = "Парк Зарядье"),
                        Place(placeName = "Парк Зарядье"),
                        Place(placeName = "Парк Зарядье")
                    )
                )


                UserInfo(user1)
                Spacer(modifier = Modifier.height(30.dp))
                ProgressLine(0.6f, height = 12)
                Spacer(modifier = Modifier.height(15.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "60" + "%",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "ваших мест совпадают!\nэто больше, чем в среднем",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp

                    )

                }
                Spacer(modifier = Modifier.height(24.dp))

                FavPlace(user1.favPlace)

                Spacer(modifier = Modifier.height(25.dp))

                MutPlaces(user1.mutPlaces)
                Spacer(modifier = Modifier.height(25.dp))
                FriendsHorizontal("Общие друзья", user1.mutFriends, navController)


            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = spacing.large)
                    .padding(bottom = 95.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                //TODO
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Принять",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.surface,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(GrayDark)
                            .clickable {
                                //TODO
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center

                        ) {
                            Text(
                                text = "Отклонить",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

        }
    }
}