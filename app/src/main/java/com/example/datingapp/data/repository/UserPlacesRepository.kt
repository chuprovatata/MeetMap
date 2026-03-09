package com.example.datingapp.data.repository

import com.example.datingapp.data.models.UserPlace
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.datingapp.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class UserPlacesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) {
    private val collection = firestore.collection("user_places")
    private val TAG = "UserPlacesRepository"

    suspend fun likePlace(placeId: String, source: String = "places_of_day"): Result<UserPlace> {
        return try {
            val userId = auth.currentUser?.uid
            Log.d("LIKE_DEBUG", "likePlace - userId: $userId, placeId: $placeId")

            if (userId == null) {
                Log.e("LIKE_DEBUG", "User not authenticated")
                return Result.failure(Exception("Пользователь не авторизован"))
            }

            // Проверяем, существует ли уже такой лайк
            val existing = checkIfUserLikedPlace(userId, placeId)
            Log.d("LIKE_DEBUG", "Existing like: $existing")

            if (existing != null) {
                Log.d("LIKE_DEBUG", "Place already liked, returning existing")
                return Result.success(existing)
            }

            val userPlace = UserPlace(
                userId = userId,
                placeId = placeId,
                status = "liked",
                addedTime = com.google.firebase.Timestamp(Date()),
                source = source
            )

            Log.d("LIKE_DEBUG", "Creating new UserPlace: $userPlace")

            val docRef = collection.document()
            val placeWithId = userPlace.copy(id = docRef.id)

            Log.d("LIKE_DEBUG", "Saving to Firestore: ${docRef.path}")

            docRef.set(placeWithId).await()

            Log.d("LIKE_DEBUG", "Successfully saved place with id: ${placeWithId.id}")

            // Запускаем фоновую задачу для уведомлений друзей (не блокируем основной поток)
            CoroutineScope(Dispatchers.IO).launch {
                notifyFriendsAboutNewPlace(userId, placeId)
            }

            Result.success(placeWithId)
        } catch (e: Exception) {
            Log.e("LIKE_DEBUG", "Error in likePlace", e)
            Result.failure(e)
        }
    }

    suspend fun unlikePlace(placeId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("Пользователь не авторизован")

            val existing = checkIfUserLikedPlace(userId, placeId)
            if (existing != null) {
                collection.document(existing.id).delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkIfUserLikedPlace(userId: String, placeId: String): UserPlace? {
        return try {
            Log.d("LIKE_DEBUG", "Checking if user $userId liked place $placeId")

            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("status", "liked")
                .limit(1)
                .get()
                .await()

            Log.d("LIKE_DEBUG", "Query result size: ${snapshot.size()}")

            val result = snapshot.documents.firstOrNull()?.toObject<UserPlace>()
            Log.d("LIKE_DEBUG", "Found existing like: $result")

            result
        } catch (e: Exception) {
            Log.e("LIKE_DEBUG", "Error checking if user liked place", e)
            null
        }
    }

    // В UserPlacesRepository.kt добавьте логирование
    suspend fun getUserLikedPlaces(userId: String = auth.currentUser?.uid ?: ""): Result<List<UserPlace>> {
        Log.d("UserPlacesRepository", "Получение мест для пользователя: $userId")

        return try {
            val snapshot = firestore.collection("user_places")  // ИСПРАВЛЕНО: было "userPlaces"
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "liked")
                .get()
                .await()

            val places = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserPlace::class.java)
            }

            Log.d("UserPlacesRepository", "Найдено мест для пользователя $userId: ${places.size}")
            Result.success(places)
        } catch (e: Exception) {
            Log.e("UserPlacesRepository", "Ошибка получения мест для пользователя $userId", e)
            Result.failure(e)
        }
    }
    /**
     * Получить общие места с другим пользователем
     */
    suspend fun getMutualPlaces(otherUserId: String): List<PlaceInfo> {
        val currentUser = auth.currentUser ?: return emptyList()
        val currentUserId = currentUser.uid

        return try {
            val currentUserPlaces = collection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "liked")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(UserPlace::class.java) }

            val otherUserPlaces = collection
                .whereEqualTo("userId", otherUserId)
                .whereEqualTo("status", "liked")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(UserPlace::class.java) }

            Log.d("UserPlacesRepo", "Current user places: ${currentUserPlaces.size}, Other user places: ${otherUserPlaces.size}")

            val currentUserPlaceIds = currentUserPlaces.map { it.placeId }.toSet()
            val otherUserPlaceIds = otherUserPlaces.map { it.placeId }.toSet()
            val mutualPlaceIds = otherUserPlaceIds.intersect(currentUserPlaceIds)

            if (mutualPlaceIds.isEmpty()) {
                return emptyList()
            }

            val placesCollection = firestore.collection("places_info")
            val mutualPlacesInfo = mutableListOf<PlaceInfo>()

            for (placeId in mutualPlaceIds) {
                val placeDoc = placesCollection.document(placeId).get().await()
                if (placeDoc.exists()) {
                    val placeInfo = placeDoc.toObject(PlaceInfo::class.java)?.copy(id = placeDoc.id)
                    if (placeInfo != null) {
                        mutualPlacesInfo.add(placeInfo)
                    }
                }
            }

            Log.d("UserPlacesRepo", "Found ${mutualPlacesInfo.size} mutual places")
            mutualPlacesInfo

        } catch (e: Exception) {
            Log.e("UserPlacesRepo", "Error getting mutual places", e)
            emptyList()
        }
    }

    suspend fun getPlaceLikesCount(placeId: String): Int {
        return try {
            val snapshot = collection
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("status", "liked")
                .get()
                .await()

            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun notifyFriendsAboutNewPlace(userId: String, placeId: String) {
        try {
            Log.d(TAG, "🔔 Checking friends for user $userId about new place $placeId")

            // Получаем документ пользователя, чтобы извлечь список друзей
            val userDoc = firestore.collection("users").document(userId).get().await()
            val friendsMap = userDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                ?: return

            // Фильтруем только друзей со статусом "friend"
            val friendIds = friendsMap.filter { it.value["status"] == "friend" }.keys
            if (friendIds.isEmpty()) {
                Log.d(TAG, "No friends found for user $userId")
                return
            }

            Log.d(TAG, "Found ${friendIds.size} friends: $friendIds")

            // Для каждого друга проверяем, есть ли у него уже это место
            for (friendId in friendIds) {
                val existing = firestore.collection("user_places")
                    .whereEqualTo("userId", friendId)
                    .whereEqualTo("placeId", placeId)
                    .whereEqualTo("status", "liked")
                    .get()
                    .await()

                if (existing.isEmpty) {
                    // У друга нет этого места — создаём уведомление
                    Log.d(TAG, "Friend $friendId doesn't have this place, sending notification")
                    notificationRepository.createNewPlaceFromFriendNotification(
                        friendId = userId,
                        placeId = placeId,
                        targetUserId = friendId
                    )
                } else {
                    Log.d(TAG, "Friend $friendId already has this place, skipping notification")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in notifyFriendsAboutNewPlace", e)
        }
    }

    // В UserPlacesRepository.kt добавить:

    /**
     * Получить детальную информацию о местах по их ID
     */
    suspend fun getPlacesDetails(placeIds: List<String>): Result<List<PlaceInfo>> {
        return try {
            if (placeIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val allPlaces = mutableListOf<PlaceInfo>()

            for (placeId in placeIds) {
                try {
                    val doc = firestore.collection("places")  // Здесь коллекция "places", это правильно
                        .document(placeId)
                        .get()
                        .await()

                    if (doc.exists()) {
                        val place = doc.toObject(PlaceInfo::class.java)
                        if (place != null) {
                            allPlaces.add(place.copy(id = doc.id))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserPlacesRepository", "Error loading place $placeId", e)
                }
            }

            Result.success(allPlaces)
        } catch (e: Exception) {
            Log.e("UserPlacesRepository", "Error getting places details", e)
            Result.failure(e)
        }
    }

    suspend fun debugGetAllUserPlaces(userId: String): Result<List<UserPlace>> {
        Log.d("UserPlacesRepository", "DEBUG: Получение ВСЕХ мест для пользователя: $userId")

        return try {
            val snapshot = firestore.collection("user_places")  // ИСПРАВЛЕНО
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val places = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserPlace::class.java)
            }

            Log.d("UserPlacesRepository", "DEBUG: Найдено ВСЕГО мест: ${places.size}")
            places.forEachIndexed { index, place ->
                Log.d("UserPlacesRepository", "DEBUG: Место $index: placeId=${place.placeId}, status='${place.status}'")
            }

            Result.success(places)
        } catch (e: Exception) {
            Log.e("UserPlacesRepository", "DEBUG: Ошибка", e)
            Result.failure(e)
        }
    }
}