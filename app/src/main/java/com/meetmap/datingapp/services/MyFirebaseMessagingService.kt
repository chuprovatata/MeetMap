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
import java.util.Date
import com.google.firebase.Timestamp

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Получаем данные из уведомления
        val title = remoteMessage.notification?.title ?: "Новое уведомление"
        val body = remoteMessage.notification?.body ?: ""

        // Получаем дополнительные данные (если есть)
        val type = remoteMessage.data["type"] ?: "GENERAL"
        val buttonText = remoteMessage.data["buttonText"] ?: "Открыть"
        val actionId = remoteMessage.data["actionId"] ?: ""

        // Показываем уведомление в системной панели
        showNotification(title, body)

        // Сохраняем уведомление в Firestore (чтобы появилось внутри приложения)
        saveNotificationToFirestore(title, body, type, buttonText, actionId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM_TOKEN", "Новый токен: $token")
        saveTokenToFirestore(token)
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
            .setSmallIcon(R.drawable.icon_app_foreground) // Убедитесь, что иконка есть
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
        buttonText: String,
        actionId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    println("❌ Не удалось сохранить уведомление: пользователь не авторизован")
                    return@launch
                }

                val notification = Notification(
                    id = "", // Будет сгенерирован Firestore
                    userId = currentUser.uid,
                    type = mapStringToNotificationType(type),
                    title = title,
                    description = body,
                    data = mapOf(
                        "actionId" to actionId,
                        "source" to "push"
                    ),
                    buttonText = buttonText,
                    read = false,
                    createdAt = Timestamp(Date())
                )

                val notificationsCollection = FirebaseFirestore.getInstance()
                    .collection("notifications")

                val docRef = notificationsCollection.document()
                val notificationWithId = notification.copy(id = docRef.id)
                docRef.set(notificationWithId).await()

                println("✅ Уведомление сохранено в Firestore: $title")

            } catch (e: Exception) {
                println("❌ Ошибка сохранения уведомления в Firestore: ${e.message}")
            }
        }
    }

    private fun saveTokenToFirestore(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    println("❌ Не удалось сохранить токен: пользователь не авторизован")
                    return@launch
                }

                val tokenData = hashMapOf(
                    "token" to token,
                    "updatedAt" to Timestamp(Date()),
                    "deviceType" to "android"
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .collection("fcm_tokens")
                    .document(token)
                    .set(tokenData)
                    .await()

                println("✅ FCM токен сохранен в Firestore")

            } catch (e: Exception) {
                println("❌ Ошибка сохранения FCM токена: ${e.message}")
            }
        }
    }

    private fun mapStringToNotificationType(type: String): NotificationType {
        return when (type.uppercase()) {
            "PLACES_OF_DAY_UPDATED" -> NotificationType.PLACES_OF_DAY_UPDATED
            "FRIEND_REQUEST" -> NotificationType.FRIEND_REQUEST
            "FRIEND_ACCEPTED" -> NotificationType.FRIEND_ACCEPTED
            "NEW_PLACE_FROM_FRIEND" -> NotificationType.NEW_PLACE_FROM_FRIEND
            else -> NotificationType.PLACES_OF_DAY_UPDATED // По умолчанию
        }
    }
}