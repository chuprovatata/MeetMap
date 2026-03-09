package com.example.datingapp.components.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.datingapp.R
import com.example.datingapp.navigation.Screen
import com.example.datingapp.viewmodels.NotificationViewModel
import android.util.Log

@Composable
fun NotificationBanner(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by viewModel.unreadCount.collectAsState()

    Log.d("NotificationBanner", "Unread count: $unreadCount")

    // Всегда показываем баннер, даже если 0 уведомлений (для отладки)
    // В продакшене можно убрать комментарий с условием
    // if (unreadCount > 0) {
    Box(
        modifier = modifier
            .width(168.dp)
            .height(80.dp)
            .clickable {
                Log.d("NotificationBanner", "Баннер нажат, переход на экран уведомлений")
                navController.navigate(Screen.Notification.route)
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.picture_dialogue),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_bell),
                contentDescription = "Уведомления",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$unreadCount")
                    }
                    append(" ${getNotificationWord(unreadCount)}")
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp,
                maxLines = 2
            )
        }
    }
    // }
}

private fun getNotificationWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "новое уведомление"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "новых уведомления"
        else -> "новых уведомлений"
    }
}