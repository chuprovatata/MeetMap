package com.example.datingapp.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.components.blocks.GrayBlock
import com.example.datingapp.components.blocks.SimpleBlock
import com.example.datingapp.navigation.Screen
import com.example.datingapp.ui.theme.LocalDatingAppSpacing
import com.example.datingapp.ui.theme.Pink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController
) {
    val spacing = LocalDatingAppSpacing.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = spacing.large)
            ) {
                // Шапка с иконками уведомлений и профиля
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
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    navController.navigate("notification")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_bell),
                                contentDescription = "Уведомления",
                                modifier = Modifier.size(35.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    navController.navigate("my_profile")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_person),
                                contentDescription = "Профиль",
                                modifier = Modifier.size(35.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.PlacesAdmin.route) },
                modifier = Modifier.padding(bottom = 72.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Админка")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleBlock(
                title = "Места дня",
                subtitle = "Смотри, что нового мы нашли специально для тебя!",
                imageResId = R.mipmap.picture_places_of_the_day_foreground,
                onClick = { navController.navigate(Screen.PlacesOfDay.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large),
                height = 160.dp,
                showImage = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GrayBlock(
                    title = "Мне нравится",
                    subtitle = "Отмечай любимое, а мы подберем людей с похожими местами!",
                    onClick = {
                        navController.navigate("main_bottom_menu/screen_2") {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    height = 220.dp
                )

                GrayBlock(
                    title = "В планах",
                    subtitle = "Те места, куда ты когда-то хотел сходить.\n\nМожет быть самое время?",
                    onClick = { /* ... */ },
                    modifier = Modifier.weight(1f),
                    height = 220.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SimpleBlock(
                title = "Знакомства",
                subtitle = "Кажется, самое время написать кому-то!",
                imageResId = null,
                onClick = {
                    navController.navigate("main_bottom_menu/screen_1") {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large),
                height = 120.dp,
                showImage = false,
                containerColor = Pink
            )

            Spacer(modifier = Modifier.height(12.dp))

            SimpleBlock(
                title = "Мои друзья",
                subtitle = "Смотри, любимые места своих друзей и планируйте встречи вместе!",
                imageResId = null,
                onClick = {
                    //тут я поставила запрет на возврат на главный экран по стрелке
                    //потом можно будет исправить
                    navController.navigate("main_bottom_menu/screen_3") {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.large),
                height = 140.dp,
                showImage = false,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}