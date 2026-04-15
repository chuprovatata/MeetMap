package com.example.datingapp.screens.notification

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.datingapp.components.notification.NotificationItem
import com.example.datingapp.navigation.Screen
import com.example.datingapp.R
import com.example.datingapp.components.headers.Heading_Arrow
import com.example.datingapp.data.models.Notification
import com.example.datingapp.data.models.NotificationType
import com.example.datingapp.viewmodels.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Log.d("NotificationScreen", "📱 Экран уведомлений: notifications.size=${notifications.size}, isLoading=$isLoading")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 6.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                Heading_Arrow("Уведомления", navController)

                // Кнопка "Отметить все" (если есть непрочитанные)
                if (notifications.any { !it.read }) {
                    TextButton(
                        onClick = { viewModel.markAllAsRead() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Отметить все")
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refreshNotifications() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            notifications.isEmpty() -> {
                EmptyNotificationsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            title = notification.title,
                            description = notification.description,
                            time = formatNotificationTime(notification.createdAt),
                            buttonText = notification.buttonText,
                            isRead = notification.read,
                            onButtonClick = {
                                // Отмечаем как прочитанное при клике
                                viewModel.markAsRead(notification.id)
                                handleNotificationClick(
                                    notification.type,
                                    navController,
                                    notification
                                )
                            },
                            onNotificationClick = {
                                viewModel.markAsRead(notification.id)
                            },
                            onDeleteClick = {
                                viewModel.deleteNotification(notification.id)
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

/**
 * Обработка клика по кнопке в уведомлении
 *
 * КАК ДОБАВИТЬ НОВЫЙ ТИП УВЕДОМЛЕНИЯ:
 * 1. Добавить новый case в when для обработки навигации
 * 2. Определить, на какой экран нужно перейти
 * 3. Извлечь необходимые данные из notification.data
 */
private fun handleNotificationClick(
    type: NotificationType,
    navController: NavController,
    notification: Notification
) {
    when (type) {
        NotificationType.NEW_PLACE_FROM_FRIEND -> {
            val placeId = notification.getPlaceId()
            if (placeId != null) {
                navController.navigate(Screen.MyPlaceDetail.passPlaceId(placeId))
            }
        }

        NotificationType.PLACES_OF_DAY_UPDATED -> {
            navController.navigate("places_of_day?fromOnboarding=false")
        }

        NotificationType.FRIEND_REQUEST -> {
            val friendId = notification.getFriendId()
            if (friendId != null) {
                // Исправлено: переходим на экран ReqFriend с параметрами
                navController.navigate(
                    Screen.ReqFriend.passParams(
                        friendId = friendId,
                        pageTitle = "Заявка в друзья"
                    )
                )
            }
        }

        NotificationType.FRIEND_ACCEPTED -> {
            val friendId = notification.getFriendId()
            if (friendId != null) {
                navController.navigate(Screen.CurFriend.passFriendId(friendId))
            }
        }

        NotificationType.NEW_MESSAGE -> {
            // TODO: реализовать переход к чату
        }

        NotificationType.PLACE_LIKED -> {
            val placeId = notification.getPlaceId()
            if (placeId != null) {
                navController.navigate(Screen.MyPlaceDetail.passPlaceId(placeId))
            }
        }

        NotificationType.SYSTEM -> {
            // Системные уведомления могут не иметь действия
        }
    }
}

/**
 * Форматирование времени для отображения
 */
private fun formatNotificationTime(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "только что"

    val now = System.currentTimeMillis()
    val diff = now - timestamp.seconds * 1000

    return when {
        diff < 60 * 1000 -> "только что"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} мин назад"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ч назад"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} дн назад"
        else -> {
            val date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            date.format(java.util.Date(timestamp.seconds * 1000))
        }
    }
}