package com.example.datingapp.screens.meets

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.screens.friends.User
import com.example.datingapp.ui.theme.GrayDark
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.viewmodels.UserViewModel


@Composable
fun ReqFriend(
    navController: NavController,
    viewModel: UserViewModel,
    friendId: String,  pageTitle: String

) {
    val spacing = LocalDatingAppSpacing.current

    LaunchedEffect(friendId) {
        viewModel.loadUserById(friendId)
    }

    val mutualFriends by viewModel.mutualFriends.collectAsState()


    LaunchedEffect(friendId) {
        viewModel.loadMutualFriends(friendId)
    }

    LaunchedEffect(friendId) {
        viewModel.loadUserById(friendId)
    }

    val otherUser by viewModel.otherUser.collectAsState()
    val myUser by viewModel.myUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // стутатат
    val friendshipStatus = myUser?.friends?.get(friendId)?.status ?: ""

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {

        Scaffold(
            topBar = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 6.dp)
                        .padding(top = 40.dp, bottom = 20.dp)
                ) {

                    Heading_Arrow(pageTitle, navController)
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



                    UserInfo(otherUser)
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

                    //FavPlace(user1.favPlace) ДОБАВИТЬ

                    Spacer(modifier = Modifier.height(25.dp))

                    //MutPlaces(user1.mutPlaces) ДОБАВИТЬ

                    Spacer(modifier = Modifier.height(25.dp))
                    FriendsHorizontal("Общие друзья",mutualFriends, navController)


                }
                if (friendshipStatus == "deny" || friendshipStatus == "request" ) {
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
                                        viewModel.updateFriendshipStatus(
                                            myUser?.uid ?:  "",
                                            otherUser?.uid ?: "",
                                            FriendStatus.FRIEND,
                                            FriendStatus.FRIEND
                                        )
                                        viewModel.loadUserById(friendId)
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
                                        viewModel.updateFriendshipStatus(
                                            myUser?.uid ?:  "",
                                            otherUser?.uid ?: "",
                                            FriendStatus.DENY,
                                            FriendStatus.MY_APPLICATION
                                        )
                                        viewModel.loadUserById(friendId)
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
                } else if (friendshipStatus.isEmpty() || friendshipStatus == "none")  {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = spacing.large)
                            .padding(bottom = 95.dp)
                            .clickable {
                                viewModel.updateFriendshipStatus(
                                    myUser?.uid ?:  "",
                                    otherUser?.uid ?: "",
                                    FriendStatus.MY_APPLICATION,
                                    FriendStatus.REQUEST
                                )
                                viewModel.loadUserById(friendId)

                            }
                    ) {

                        Text(
                            text = "отправить заявку",
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 24.sp,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 50.dp)
                        )

                    }
                }

            }
        }
    }
}

