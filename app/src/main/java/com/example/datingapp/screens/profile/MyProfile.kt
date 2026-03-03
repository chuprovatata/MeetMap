package com.example.datingapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.datingapp.components.blocks.UserInfo
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.components.progress.ProgressLine
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.navigation.Screen

import com.example.datingapp.viewmodels.UserViewModel

@Composable
fun MyProfile(navController: NavController, viewModel: UserViewModel) {

    val name = viewModel.userData.value?.get("name") as? String ?: ""


    val age = viewModel.userData.value?.get("age") as? Long ?: 0
    val gender = viewModel.userData.value?.get("gender") as? String ?: ""
    val telegram = viewModel.userData.value?.get("telegram") as? String ?: ""

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 19.dp)
                ) {
                    Heading_Arrow("Профиль", navController)

                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_settings),
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
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
            val user by viewModel.myUser.collectAsState()




            UserInfo(user)
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
            //FavPlace(user1.favPlace) когда в бд будут места

            Spacer(modifier = Modifier.height(25.dp))

            var curFriends by remember { mutableStateOf<List<MyUser>>(emptyList()) }

            LaunchedEffect(Unit) {
                curFriends = viewModel.getUsersByFriendStatus(FriendStatus.FRIEND)

            }

            FriendsHorizontal("Мои друзья",  curFriends , navController)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}