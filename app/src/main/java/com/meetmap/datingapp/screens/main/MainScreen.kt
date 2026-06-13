package com.meetmap.datingapp.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.meetmap.datingapp.R
import com.meetmap.datingapp.components.blocks.SimpleBlock
import com.meetmap.datingapp.components.notifications.NotificationBanner
import com.meetmap.datingapp.navigation.Screen
import com.meetmap.datingapp.ui.theme.GrayLight
import com.meetmap.datingapp.ui.theme.LocalDatingAppSpacing
import com.meetmap.datingapp.ui.theme.PurpleCard
import com.meetmap.datingapp.viewmodels.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    localNavController: NavController
) {
    val spacing = LocalDatingAppSpacing.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val isLandscape = screenWidth > screenHeight

    val blockHeight = if (isLandscape) 100.dp else screenHeight * 0.2f
    val peopleBlockHeight = if (isLandscape) 80.dp else screenHeight * 0.15f
    val maxBlockHeight = 180.dp
    val finalBlockHeight = minOf(blockHeight, maxBlockHeight)
    val finalPeopleBlockHeight = minOf(peopleBlockHeight, maxBlockHeight - 20.dp)

    val notificationTopPadding = if (isLandscape) 20.dp else screenHeight * 0.15f
    val notificationOffset = if (isLandscape) 0.dp else screenWidth * 0.08f
    val bottomNavHeight = 80.dp

    val notificationHeight = 60.dp

    val scrollAreaTopOffset = notificationTopPadding + notificationHeight + spacing.medium + 20.dp
    val scrollAreaBottomOffset = bottomNavHeight + spacing.large

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(horizontal = spacing.large)
                    .padding(bottom = 20.dp)
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .clickable {
                                    navController.navigate("my_profile")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_person),
                                contentDescription = "Профиль",
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.FavoritePlace.route) },
                modifier = Modifier.padding(bottom = if (isLandscape) 16.dp else screenHeight * 0.12f),
                containerColor = PurpleCard,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить любимое место",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(28.dp)
                )
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

            NotificationBanner(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = notificationTopPadding)
                    .offset(x = notificationOffset)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = scrollAreaTopOffset,
                        bottom = scrollAreaBottomOffset
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else screenHeight * 0.02f))
                    SimpleBlock(
                        title = "Мероприятия",
                        subtitle = "Некуда сходить? Мы поможем!",
                        imageResId = null,
                        onClick = {
                            navController.navigate(Screen.EventMain.route)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        height = finalPeopleBlockHeight,
                        showImage = false,

                    )
                }
            }
        }
    }
}