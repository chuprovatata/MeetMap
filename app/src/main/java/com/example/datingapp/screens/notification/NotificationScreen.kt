package com.example.datingapp.screens.notification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.datingapp.components.notification.NotificationItem
import com.example.datingapp.navigation.Screen
import com.example.datingapp.R
import com.example.datingapp.components.notification.Notification
import com.example.datingapp.components.notification.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Уведомления",
                        style = MaterialTheme.typography.displaySmall.copy()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_back),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (sampleNotifications.isEmpty()) {
            EmptyNotificationsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = sampleNotifications,
                    key = { it.id }
                ) { notification ->
                    NotificationItem(
                        title = notification.title,
                        description = notification.description,
                        time = notification.time,
                        buttonText = notification.buttonText,
                        onButtonClick = {
                            handleNotificationClick(
                                notification.type,
                                navController,
                                notification.id
                            )
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = "Нет уведомлений",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Нет уведомлений",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Здесь будут появляться ваши уведомления",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun handleNotificationClick(
    type: NotificationType,
    navController: NavController,
    notificationId: Int
) {
    when (type) {
        NotificationType.NEW_PLACE -> {
            navController.navigate("${Screen.Main.route}/$notificationId")
        }
        NotificationType.PROMOTION -> {
            navController.navigate(Screen.PlacesOfDay.route)
        }
        NotificationType.REMINDER -> {
            println("Нажато напоминание с id: $notificationId")
        }
        NotificationType.NEW_PERSON -> {
            navController.navigate(Screen.Main.route)
        }
    }
}

private val sampleNotifications = listOf(
    Notification(
        id = 1,
        title = "@anna",
        description = "отметил(а) новое место!",
        time = "10:30",
        buttonText = "перейти к месту",
        type = NotificationType.NEW_PLACE
    ),
    Notification(
        id = 2,
        title = "Stars Coffee",
        description = "новый человек отметил твое любимое место!",
        time = "вчера, 15:45",
        buttonText = "перейти в профиль",
        type = NotificationType.NEW_PERSON
    ),
    Notification(
        id = 3,
        title = "Места дня",
        description = "скорее смотри места, которые мы нашли специально для тебя!",
        time = "пн, 09:15",
        buttonText = "перейти к подборке",
        type = NotificationType.REMINDER
    )
)