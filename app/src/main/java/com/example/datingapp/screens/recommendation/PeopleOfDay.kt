package com.example.datingapp.screens.recommendation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.Title_Block
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.PurpleCard
import com.example.datingapp.viewmodels.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun PeopleOfDay(navController: NavController, viewModel: UserViewModel) {
    val recommendedUsers by viewModel.recommendedUsers.collectAsState()
    val usersCompatibility by viewModel.usersCompatibility.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔥 КЛЮЧЕВОЕ РЕШЕНИЕ: отслеживаем жизненный цикл экрана
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Когда экран становится активным (возврат на экран)
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("PeopleOfDay", "📱 Экран стал активным - обновляем данные")
                    viewModel.refreshRecommendedUsers()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Также обновляем при первом открытии
    LaunchedEffect(Unit) {
        Log.d("PeopleOfDay", "🚀 Первое открытие экрана")
        viewModel.refreshRecommendedUsers()
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
                    // Изменено позиционирование блока Title_Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.25f) // Блок занимает 25% высоты экрана
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 12.dp)
                    ) {
                        Title_Block(
                            navController,
                            "У вас схожие интересы",
                            "Вы часто посещаете одни и те же места, может быть это знак?",
                            R.drawable.person_on_board,
                            false
                        )
                    }

                    if (recommendedUsers.isEmpty()) {
                        EmptyRecommendations()
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
            onClick = { /* Переход на экран с местами */ },
            colors = ButtonDefaults.buttonColors(containerColor = PurpleCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Найти места",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}