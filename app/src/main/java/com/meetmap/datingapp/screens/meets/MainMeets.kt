package com.meetmap.datingapp.screens.meets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.Title_Block
import com.meetmap.datingapp.components.headers.Heading
import com.meetmap.datingapp.components.segmentedButton.CustomTabsComponent
import com.meetmap.datingapp.data.repository.FriendStatus
import com.meetmap.datingapp.navigation.GlobalNavController
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.NotificationViewModel
import com.meetmap.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@Composable
fun MainMeets(navController: NavController, viewModel: UserViewModel) {
    val spacing = LocalDatingAppSpacing.current
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val friendsList by viewModel.friendsList.collectAsState()
    val incomingRequests by viewModel.incomingRequests.collectAsState()
    val deniedList by viewModel.deniedList.collectAsState()
    val outgoingRequests by viewModel.outgoingRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val myUser by viewModel.myUser.collectAsState()
    val curId = myUser?.uid ?: ""

    // Получаем NotificationViewModel
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.loadAllFriendData()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)


            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 9.dp, end = 19.dp)
                ) {
                    Heading(
                        heading = "Знакомства",
                        showBackButton = false,
                        showSettings = false,
                        showProfile = true,
                        onBackClick = {
                            GlobalNavController.navController?.navigate(Screen.Main.route) {
                                popUpTo(0)
                            }
                        },
                        navController = navController
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp)
                ) {
                    CustomTabsComponent(
                        "Заявки в друзья",
                        "Мои друзья",
                        0, 0,
                        selectedTab = selectedTab,
                        onTabSelected = { tabIndex -> selectedTab = tabIndex }
                    )
                }


            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .padding(top = 30.dp)
            ) {

                when (selectedTab) {

                    0 -> {
                        viewModel.loadAllFriendData()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Title_Block(
                                    navController,
                                    "С тобой хотят познакомиться",
                                    "Посмотри, может быть ваши пути сойдутся?",
                                    R.drawable.person_on_board,
                                    false
                                )
                            }


                            if (isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else {

                                if (incomingRequests.isEmpty() &&
                                    outgoingRequests.isEmpty() &&
                                    deniedList.isEmpty()
                                ) {

                                    item {
                                        EmptyNotFriend()
                                    }
                                } else {

                                    if (incomingRequests.isNotEmpty()) {

                                        items(incomingRequests) { user ->
                                            ItemMeets(
                                                user,
                                                navController,
                                                status = FriendStatus.REQUEST,

                                                onClickButton = {
                                                    scope.launch {
                                                        viewModel.updateFriendshipStatus(
                                                            curId,
                                                            user.uid,
                                                            FriendStatus.FRIEND,
                                                            FriendStatus.FRIEND
                                                        )

                                                        // Отправляем уведомление о принятии заявки
                                                        notificationViewModel.createFriendAcceptedNotification(
                                                            fromUserId = curId,
                                                            toUserId = user.uid
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                    }
                                    if (deniedList.isNotEmpty()) {

                                        items(deniedList) { user ->
                                            ItemMeets(
                                                user,
                                                navController,
                                                status = FriendStatus.DENY,
                                                onClickButton = {
                                                    viewModel.loadAllFriendData()

                                                })
                                        }


                                    }

                                    // Исходящие заявки
                                    if (outgoingRequests.isNotEmpty()) {

                                        items(outgoingRequests) { user ->
                                            ItemMeets(
                                                user,
                                                navController,
                                                status = FriendStatus.MY_APPLICATION,
                                                onClickButton = {
                                                    viewModel.loadAllFriendData()

                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        viewModel.loadAllFriendData()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Title_Block(
                                    navController,
                                    "Список друзей",
                                    "Посмотри, может быть ты кому-то давно не писал?",
                                    R.drawable.person_on_board,
                                    false
                                )
                            }


                            if (isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            } else if (friendsList.isEmpty()) {
                                item {
                                    EmptyFriend()

                                }
                            } else {
                                item { Spacer(modifier = Modifier.height(20.dp)) }
                                items(friendsList) { user ->
                                    ItemMeetsFriend(user, navController, "notmeet")
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0 || friendsList.isEmpty()) {

                Box(
                    modifier = Modifier

                        .align(Alignment.BottomCenter)
                        .padding(bottom = 150.dp)

                        .background(shape = RoundedCornerShape(12.dp), color = PurpleCard)
                        .clickable {
                            if (friendsList.isEmpty() && selectedTab == 1 ) {
                                selectedTab = 0
                            } else {

                                navController.navigate(Screen.PeopleOfDay.route)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (incomingRequests.isEmpty() &&
                            outgoingRequests.isEmpty() &&
                            deniedList.isEmpty() && selectedTab == 0
                        ) {
                            "рекомендации"
                        } else if (friendsList.isEmpty() && selectedTab == 1) {
                            "к заявкам"

                        } else {
                            "искать ещё"
                        },
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


@Composable
fun EmptyFriend() {

    Box(
        modifier = Modifier

            .fillMaxSize()

    ) {
        Column(
            modifier = Modifier

                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.cloud),
                contentDescription = "",
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = "У тебя пока нет друзей(",
                color = Color.Gray
            )

            Text(
                text = buildAnnotatedString {
                    append("Переходи в подборку ")
                    withStyle(style = SpanStyle(color = PurpleCard, fontWeight = FontWeight.Bold)) {
                        append("Люди дня")
                    }
                    append(" и находи \nновых людей!")
                },
                color = Color.Gray, textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun EmptyNotFriend() {
    Box(
        modifier = Modifier

            .fillMaxSize()

    ) {
        Column(
            modifier = Modifier

                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.cloud),
                contentDescription = "",
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = "У тебя пока нет заявок(",
                color = Color.Gray,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = buildAnnotatedString {
                    append("Переходи в подборку ")
                    withStyle(style = SpanStyle(color = PurpleCard, fontWeight = FontWeight.Bold)) {
                        append("Люди дня")
                    }
                    append(", чтобы посмотреть, кто посещает те же места, что и ты!")
                },
                color = Color.Gray, textAlign = TextAlign.Center
            )
        }
    }
}