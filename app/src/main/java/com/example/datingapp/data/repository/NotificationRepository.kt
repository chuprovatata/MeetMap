package com.example.datingapp.data.repository

import android.util.Log
import com.example.datingapp.data.models.Notification
import com.example.datingapp.data.models.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")
    private val usersCollection = firestore.collection("users")
    private val placesCollection = firestore.collection("places_info")

    companion object {
        private const val TAG = "NotificationRepository"
    }

    /**
     * ПОЛУЧЕНИЕ УВЕДОМЛЕНИЙ
     */

    /**
     * Получить уведомления текущего пользователя в реальном времени
     * Используется для отображения в NotificationScreen
     */
    /**
     * Получить уведомления текущего пользователя в реальном времени
     */
    fun getNotificationsFlow(): Flow<List<Notification>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "❌ Пользователь не авторизован")
            close()
            return@callbackFlow
        }

        Log.d(TAG, "🔍 Начинаем прослушивание уведомлений для пользователя: ${currentUser.uid}")

        val registration = notificationsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Ошибка получения уведомлений", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e(TAG, "❌ Snapshot is null")
                    return@addSnapshotListener
                }

                Log.d(TAG, "📊 Получен snapshot, размер: ${snapshot.size()} документов")

                // Логируем каждый документ
                snapshot.documents.forEachIndexed { index, doc ->
                    Log.d(TAG, "  Документ #$index: id=${doc.id}, data=${doc.data}")
                }

                val notifications = snapshot.documents.mapNotNull { doc ->
                    try {
                        val notification = doc.toObject<Notification>()?.copy(id = doc.id)
                        Log.d(TAG, "✅ Преобразован в Notification: ${notification?.title}")
                        notification
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Ошибка преобразования документа ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "📨 Итого уведомлений после преобразования: ${notifications.size}")

                trySend(notifications).isSuccess
            }

        awaitClose {
            Log.d(TAG, "👋 Закрываем поток уведомлений")
            registration.remove()
        }
    }

    /**
     * Получить количество непрочитанных уведомлений
     * Используется для бейджа на иконке
     */
    suspend fun getUnreadCount(): Int {
        val currentUser = auth.currentUser ?: return 0
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("is_read", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения количества непрочитанных", e)
            0
        }
    }

    /**
     * ОБНОВЛЕНИЕ УВЕДОМЛЕНИЙ
     */

    /**
     * Отметить уведомление как прочитанное
     */
    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection
                .document(notificationId)
                .update("is_read", true)
                .await()
            Log.d(TAG, "Уведомление $notificationId отмечено как прочитанное")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отметке уведомления как прочитанного", e)
        }
    }

    /**
     * Отметить все уведомления пользователя как прочитанные
     */
    suspend fun markAllAsRead() {
        val currentUser = auth.currentUser ?: return
        try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("is_read", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "is_read", true)
            }
            batch.commit().await()
            Log.d(TAG, "Все уведомления отмечены как прочитанные")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отметке всех уведомлений", e)
        }
    }

    /**
     * Удалить уведомление
     */
    suspend fun deleteNotification(notificationId: String) {
        try {
            notificationsCollection.document(notificationId).delete().await()
            Log.d(TAG, "Уведомление $notificationId удалено")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении уведомления", e)
        }
    }

    /**
     * Удалить все уведомления пользователя (для очистки)
     */
    suspend fun deleteAllNotifications() {
        val currentUser = auth.currentUser ?: return
        try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Log.d(TAG, "Все уведомления удалены")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении всех уведомлений", e)
        }
    }

    /**
     * СОЗДАНИЕ УВЕДОМЛЕНИЙ
     *
     * КАК ДОБАВИТЬ НОВЫЙ ТИП УВЕДОМЛЕНИЯ:
     * 1. Добавить новый тип в NotificationType
     * 2. Создать функцию create[Название]Notification
     * 3. В функции определить:
     *    - title: заголовок
     *    - description: описание
     *    - buttonText: текст кнопки
     *    - data: дополнительные данные для навигации
     * 4. Вызвать эту функцию в нужном месте приложения
     */

    /**
     * Уведомление: друг добавил новое место
     * Событие: когда друг пользователя добавляет место в избранное
     */
    suspend fun createNewPlaceFromFriendNotification(
        friendId: String,
        placeId: String,
        targetUserId: String // кому отправляем уведомление
    ) {
        try {
            // Получаем информацию о друге
            val friendDoc = usersCollection.document(friendId).get().await()
            val friendName = friendDoc.getString("name") ?: "Пользователь"

            // Получаем информацию о месте
            val placeDoc = placesCollection.document(placeId).get().await()
            val placeName = placeDoc.getString("name") ?: "новое место"

            val notification = Notification(
                userId = targetUserId,
                type = NotificationType.NEW_PLACE_FROM_FRIEND,
                title = "Новое место от друга ✨",
                description = "$friendName добавил(а) новое место: $placeName",
                data = mapOf(
                    "friendId" to friendId,
                    "placeId" to placeId,
                    "friendName" to friendName,
                    "placeName" to placeName
                ),
                buttonText = "Посмотреть место",
                read = false,
                createdAt = com.google.firebase.Timestamp(Date())
            )

            val docRef = notificationsCollection.document()
            docRef.set(notification.copy(id = docRef.id)).await()

            Log.d(TAG, "Создано уведомление NEW_PLACE_FROM_FRIEND для пользователя $targetUserId")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при создании уведомления NEW_PLACE_FROM_FRIEND", e)
        }
    }

    /**
     * Уведомление: обновилась подборка "Места дня"
     * Событие: при обновлении коллекции places_of_day (например, раз в день)
     */
    suspend fun createPlacesOfDayUpdatedNotification() {
        val notification = Notification(
            userId = "", // будет установлено для каждого пользователя отдельно
            type = NotificationType.PLACES_OF_DAY_UPDATED,
            title = "Свежие места дня 🔥",
            description = "Подборка мест дня обновилась! Смотри, что нового мы для тебя нашли.",
            data = emptyMap(),
            buttonText = "Смотреть подборку",
            read = false,
            createdAt = com.google.firebase.Timestamp(Date())
        )

        // Отправляем всем пользователям
        try {
            val users = usersCollection.get().await()
            val batch = firestore.batch()

            users.documents.forEach { userDoc ->
                val userId = userDoc.id
                val docRef = notificationsCollection.document()
                val userNotification = notification.copy(
                    id = docRef.id,
                    userId = userId
                )
                batch.set(docRef, userNotification)
            }

            batch.commit().await()
            Log.d(TAG, "Создано уведомление PLACES_OF_DAY_UPDATED для всех пользователей")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при создании уведомления PLACES_OF_DAY_UPDATED", e)
        }
    }

    /**
     * Пример: уведомление о новом запросе в друзья
     * (для демонстрации расширяемости)
     */
    suspend fun createFriendRequestNotification(
        fromUserId: String,
        toUserId: String
    ) {
        try {
            val userDoc = usersCollection.document(fromUserId).get().await()
            val userName = userDoc.getString("name") ?: "Пользователь"

            val notification = Notification(
                userId = toUserId,
                type = NotificationType.FRIEND_REQUEST,
                title = "Запрос в друзья 👋",
                description = "$userName хочет добавить вас в друзья",
                data = mapOf(
                    "friendId" to fromUserId,
                    "friendName" to userName
                ),
                buttonText = "Посмотреть",
                read = false,
                createdAt = com.google.firebase.Timestamp(Date())
            )

            val docRef = notificationsCollection.document()
            docRef.set(notification.copy(id = docRef.id)).await()

            Log.d(TAG, "Создано уведомление FRIEND_REQUEST для пользователя $toUserId")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при создании уведомления FRIEND_REQUEST", e)
        }
    }

    /**
     * ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
     */

    /**
     * Проверить, есть ли уже такое уведомление (чтобы не дублировать)
     */
    private suspend fun notificationExists(
        userId: String,
        type: NotificationType,
        data: Map<String, String>
    ): Boolean {
        return try {
            var query = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)

            // Добавляем условия для данных
            data.forEach { (key, value) ->
                query = query.whereEqualTo("data.$key", value)
            }

            val snapshot = query
                .whereGreaterThan("created_at", Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)) // последние 24 часа
                .get()
                .await()

            snapshot.size() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке существования уведомления", e)
            false
        }
    }

    /**
     * СОЗДАНИЕ ТЕСТОВЫХ УВЕДОМЛЕНИЙ
     * Используется только для отладки!
     */
    suspend fun createTestNotifications() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        Log.d(TAG, "Создание тестовых уведомлений для пользователя $userId")

        // Удаляем старые тестовые уведомления
        try {
            val oldNotifications = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("title", "Тестовое уведомление")
                .get()
                .await()

            val batch = firestore.batch()
            oldNotifications.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении старых тестовых уведомлений", e)
        }

        // Создаем новые тестовые уведомления
        val testNotifications = listOf(
            Notification(
                userId = userId,
                type = NotificationType.NEW_PLACE_FROM_FRIEND,
                title = "Анна добавила новое место ✨",
                description = "Анна добавила кафе 'Кофе и Книги' в избранное",
                data = mapOf(
                    "friendId" to "test_friend_1",
                    "placeId" to "test_place_1",
                    "friendName" to "Анна",
                    "placeName" to "Кофе и Книги"
                ),
                buttonText = "Посмотреть место",
                read = false,
                createdAt = com.google.firebase.Timestamp(java.util.Date())
            ),
            Notification(
                userId = userId,
                type = NotificationType.NEW_PLACE_FROM_FRIEND,
                title = "Михаил добавил новое место 🌟",
                description = "Михаил добавил ресторан 'Итальянский дворик' в избранное",
                data = mapOf(
                    "friendId" to "test_friend_2",
                    "placeId" to "test_place_2",
                    "friendName" to "Михаил",
                    "placeName" to "Итальянский дворик"
                ),
                buttonText = "Посмотреть место",
                read = false,
                createdAt = com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() - 3600000))
            ),
            Notification(
                userId = userId,
                type = NotificationType.PLACES_OF_DAY_UPDATED,
                title = "Свежие места дня 🔥",
                description = "Подборка мест дня обновилась! Смотри, что нового мы для тебя нашли.",
                data = emptyMap(),
                buttonText = "Смотреть подборку",
                read = true,
                createdAt = com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() - 86400000))
            )
        )

        testNotifications.forEach { notification ->
            val docRef = notificationsCollection.document()
            docRef.set(notification.copy(id = docRef.id)).await()
            Log.d(TAG, "Создано тестовое уведомление: ${notification.title}")
        }

        Log.d(TAG, "Все тестовые уведомления созданы")
    }
}