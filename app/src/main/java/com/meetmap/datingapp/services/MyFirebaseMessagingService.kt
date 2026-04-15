package com.meetmap.datingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.meetmap.datingapp.MainActivity
import com.meetmap.datingapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Показываем уведомление
        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Новое уведомление",
                body = notification.body ?: ""
            )
        }

        // Сохраняем в Firestore (чтобы было и внутри приложения)
        saveNotificationToFirestore(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Отправляем новый токен на сервер
        sendTokenToServer(token)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel",
                "Основные уведомления",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "default_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.icon_app_foreground) // Создайте иконку
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveNotificationToFirestore(message: RemoteMessage) {
        // TODO: Сохранить уведомление в Firestore
        // чтобы оно отображалось и внутри приложения
    }

    private fun sendTokenToServer(token: String) {
        // Сохраняем токен в Firestore для текущего пользователя
        CoroutineScope(Dispatchers.IO).launch {
            // Получить текущего пользователя и сохранить token
        }
    }
}