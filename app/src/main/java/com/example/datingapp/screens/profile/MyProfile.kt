package com.example.datingapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.FavPlace
import com.example.datingapp.components.blocks.FriendsHorizontal
import com.example.datingapp.components.blocks.MutPlaces
import com.example.datingapp.components.blocks.Place
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.screens.friends.User

@Composable
fun MyProfile(navController: NavController) {

    Scaffold(
        topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Профиль", navController)
            }
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 25.dp)
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
                    User(username = "@Nase"),
                    User(username = "@Nase"),
                    User(username = "@Nase"),
                    User(username = "@Nase"),
                    User(username = "@Nase"),
                    User(username = "@Nase"),
                    User(username = "@Nase"),
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


            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "места отмечено",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp

                )

            }
            Spacer(modifier = Modifier.height(21.dp))
            ProgressLine(0.6f, height = 12)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("15 ")
                        }
                        append("посещённые")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 17.sp
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("9 ")
                        }
                        append("хочу посетить")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 17.sp
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            FavPlace(user1.favPlace)

            Spacer(modifier = Modifier.height(25.dp))



            FriendsHorizontal("Общие друзья", user1.mutFriends, navController)


            Spacer(modifier = Modifier.height(100.dp))


        }
    }
}