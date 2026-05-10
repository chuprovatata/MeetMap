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
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetmap.datingapp.data.models.Notification
import com.meetmap.datingapp.data.models.NotificationType
import com.google.firebase.Timestamp

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // OneSignal отправляет данные в remoteMessage.data
        val data = remoteMessage.data
        val title = data["headings"] ?: data["title"] ?: "Новое уведомление"
        val body = data["contents"] ?: data["body"] ?: ""
        val type = data["type"] ?: "PLACES_OF_DAY_UPDATED"
        val buttonText = data["buttonText"] ?: "Смотреть"

        // Показываем пуш в панели уведомлений телефона
        showNotification(title, body)

        // Сохраняем в Firestore (внутреннее уведомление для приложения)
        saveNotificationToFirestore(title, body, type, buttonText)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // OneSignal сам управляет токенами, но сохраним для резерва
        android.util.Log.d("FCM_TOKEN", "New token: $token")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_digest_channel",
                "Ежедневная подборка",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о новых подборках и событиях"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "daily_digest_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.icon_app_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveNotificationToFirestore(
        title: String,
        body: String,
        type: String,
        buttonText: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    println("❌ Пользователь не авторизован, уведомление не сохранено")
                    return@launch
                }

                val userId = currentUser.uid
                val notification = mapOf(
                    "id" to "", // будет сгенерирован
                    "title" to title,
                    "description" to body,
                    "type" to type,
                    "buttonText" to buttonText,
                    "read" to false,
                    "createdAt" to Timestamp.now(),
                    "data" to mapOf("source" to "push")
                )

                val notificationsRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document()

                val notificationWithId = notification.toMutableMap().apply {
                    this["id"] = notificationsRef.id
                }

                notificationsRef.set(notificationWithId).await()
                println("✅ Внутреннее уведомление сохранено в Firestore")

            } catch (e: Exception) {
                println("❌ Ошибка сохранения: ${e.message}")
            }
        }
    }
}