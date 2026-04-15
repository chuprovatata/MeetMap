package com.example.datingapp.data.models

/**
 * Типы уведомлений в приложении
 * При добавлении нового типа уведомления:
 * 1. Добавьте значение в этот enum
 * 2. Добавьте обработку в NotificationRepository.createNotification()
 * 3. Добавьте обработку в NotificationScreen.handleNotificationClick()
 * 4. Добавьте шаблон текста в NotificationRepository.getNotificationText()
 */
enum class NotificationType {
    NEW_PLACE_FROM_FRIEND,  // Друг добавил новое место
    PLACES_OF_DAY_UPDATED,  // Обновилась подборка "Места дня"
    FRIEND_REQUEST,         // Запрос в друзья (на будущее)
    FRIEND_ACCEPTED,        // Запрос принят (на будущее)
    NEW_MESSAGE,            // Новое сообщение (на будущее)
    PLACE_LIKED,            // Кто-то лайкнул твое место (на будущее)
    SYSTEM                  // Системные уведомления
}