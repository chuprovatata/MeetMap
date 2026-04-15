package com.example.datingapp.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.SimpleBlock
import com.example.datingapp.components.notifications.NotificationBanner
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.GrayLight
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.datingapp.viewmodels.NotificationViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,  // Глобальный контроллер для внешних переходов
    localNavController: NavController // Локальный контроллер для переходов внутри меню
) {
    val spacing = LocalDatingAppSpacing.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Получаем ViewModel для уведомлений
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val isLandscape = screenWidth > screenHeight

    val topBarPadding = if (isLandscape) 8.dp else screenHeight * 0.05f
    val iconSize = if (isLandscape) 28.dp else screenWidth * 0.08f
    val maxIconSize = 36.dp
    val finalIconSize = minOf(iconSize, maxIconSize)

    val blockHeight = if (isLandscape) 100.dp else screenHeight * 0.2f
    val peopleBlockHeight = if (isLandscape) 80.dp else screenHeight * 0.15f
    val maxBlockHeight = 180.dp
    val finalBlockHeight = minOf(blockHeight, maxBlockHeight)
    val finalPeopleBlockHeight = minOf(peopleBlockHeight, maxBlockHeight - 20.dp)

    val bottomPadding = if (isLandscape) 16.dp else screenHeight * 0.06f
    val notificationTopPadding = if (isLandscape) 20.dp else screenHeight * 0.15f
    val notificationOffset = if (isLandscape) 0.dp else screenWidth * 0.08f
    val bottomNavHeight = 80.dp

    // Высота уведомления
    val notificationHeight = 60.dp

    // Верхняя граница для скроллируемой области (после уведомлений)
    val scrollAreaTopOffset = notificationTopPadding + notificationHeight + spacing.medium + 20.dp

    // Нижняя граница для скроллируемой области (до нижней навигации)
    val scrollAreaBottomOffset = bottomNavHeight + spacing.large

    // Проверка для админки
    val ADMIN_EMAILS = listOf(
        "meetmap.team@gmail.com",
        "chuprova_tata@mail.ru",
        "vmbaizdrenko@gmail.com",
        "liliadyrnina7464@gmail.com"
    )
    var currentUserEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentUserEmail = Firebase.auth.currentUser?.email
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = topBarPadding, horizontal = spacing.large)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Привет!",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Кнопка профиля
                        Box(
                            modifier = Modifier
                                .size(finalIconSize)
                                .clip(RoundedCornerShape(30.dp))
                                .clickable {
                                    navController.navigate("my_profile")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_person),
                                contentDescription = "Профиль",
                                modifier = Modifier.size(finalIconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

//                        // Временная кнопка для создания тестовых уведомлений
//                        Box(
//                            modifier = Modifier
//                                .size(finalIconSize)
//                                .clip(RoundedCornerShape(30.dp))
//                                .clickable {
//                                    scope.launch {
//                                        notificationViewModel.createTestNotifications()
//                                    }
//                                },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.icon_bell),
//                                contentDescription = "Тест уведомлений",
//                                modifier = Modifier.size(finalIconSize),
//                                tint = MaterialTheme.colorScheme.primary
//                            )
//                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentUserEmail in ADMIN_EMAILS) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.PlacesAdmin.route) },
                    modifier = Modifier.padding(bottom = if (isLandscape) 16.dp else screenHeight * 0.08f)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Админка")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.picture_main_screen),
                contentDescription = "Фоновое изображение",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isLandscape) 0.6f else 0.7f)
                    .offset(
                        x = if (isLandscape) screenWidth * (-0.1f) else screenWidth * (-0.25f),
                        y = if (isLandscape) screenHeight * (-0.1f) else screenHeight * (-0.25f)
                    ),
                contentScale = ContentScale.Crop
            )

            // Уведомления - поверх всего
            NotificationBanner(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = notificationTopPadding)
                    .offset(x = notificationOffset)
                    .zIndex(10f)
            )

            // Основной контент с ограниченной областью для скролла
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = scrollAreaTopOffset,
                        bottom = scrollAreaBottomOffset
                    )
            ) {
                // Скроллируемая область с блоками
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Занимает все доступное пространство между верхним и нижним отступами
                        .verticalScroll(scrollState)
                        .padding(horizontal = spacing.large)
                ) {
                    SimpleBlock(
                        title = "Места дня",
                        subtitle = "Смотри, что нового мы нашли специально для тебя!",
                        imageResId = R.mipmap.picture_places_of_the_day_foreground,
                        onClick = {
                            navController.navigate("places_of_day?fromOnboarding=false") {
                                popUpTo(Screen.Main.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = finalBlockHeight,
                        showImage = true,
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else screenHeight * 0.02f))

                    SimpleBlock(
                        title = "Люди дня",
                        subtitle = "Посмотри, сколько похожих на тебя людей!",
                        imageResId = null,
                        onClick = {
                            navController.navigate(Screen.PeopleOfDay.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = finalPeopleBlockHeight,
                        showImage = false,
                        containerColor = GrayLight
                    )
                }
            }
        }
    }
}