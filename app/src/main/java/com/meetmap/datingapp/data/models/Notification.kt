package com.example.datingapp.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Модель уведомления для Firestore
 * Коллекция: notifications
 *
 * Поля:
 * - id: автоматический ID документа
 * - userId: ID пользователя-получателя
 * - type: тип уведомления (см. NotificationType)
 * - title: заголовок уведомления
 * - description: текст уведомления
 * - data: дополнительные данные (placeId, friendId и т.д.)
 * - buttonText: текст на кнопке действия
 * - isRead: прочитано ли уведомление
 * - createdAt: время создания
 * - expiresAt: время истечения (опционально)
 */
data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val title: String = "",
    val description: String = "",
    val data: Map<String, String> = emptyMap(),
    val buttonText: String = "Открыть",
    val read: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
) {
    /**
     * Вспомогательные функции для получения данных
     */
    fun getPlaceId(): String? = data["placeId"]
    fun getFriendId(): String? = data["friendId"]
    fun getFriendName(): String? = data["friendName"]
}