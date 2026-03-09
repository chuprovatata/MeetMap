package com.example.datingapp.screens.recommendation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.Title_Block
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.viewmodels.UserViewModel

@Composable
fun PeopleOfDay(navController: NavController, viewModel: UserViewModel) {
    val recommendedUsers by viewModel.recommendedUsers.collectAsState()
    val usersCompatibility by viewModel.usersCompatibility.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecommendedUsers()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Люди дня", navController)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    // Заголовок (оставляем существующий Title_Block)
                    Title_Block(
                        navController,
                        "У вас схожие интересы",
                        "Вы часто посещаете одни и те же места, может быть это знак?",
                        R.drawable.person_on_board,
                        false
                    )

                    if (recommendedUsers.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "😕 Пока нет подходящих людей",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Добавляйте больше мест в избранное,\nчтобы мы могли найти людей с похожими интересами!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(recommendedUsers) { user ->
                                RecommendedUserItem(
                                    user = user,
                                    compatibilityPercent = usersCompatibility[user.uid] ?: 0,
                                    onUserClick = {
                                        navController.navigate(
                                            Screen.ReqFriend.passParams(
                                                friendId = user.uid,
                                                pageTitle = "Люди дня"
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecommendations() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.cloud),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Здесь пока никого нет",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Чтобы получать рекомендации,\nдобавляйте места в избранное",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Переход на экран с местами дня
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleCard
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Найти места",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}