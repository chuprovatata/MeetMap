package com.meetmap.datingapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.meetmap.datingapp.BuildConfig
import com.meetmap.datingapp.data.models.PlaceInfo
import com.meetmap.datingapp.utils.CloudImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val okHttpClient: OkHttpClient,
    private val notificationRepository: NotificationRepository
) {

    // ==================== ЗАГРУЗКА ФОТО ====================

    suspend fun uploadProfileImage(uri: Uri, contentResolver: ContentResolver): String =
        withContext(Dispatchers.IO) {
            val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
            val userId = currentUser.uid

            var file: File? = null

            try {
                file = uriToFile(uri, contentResolver)
                    ?: throw Exception("Не удалось обработать изображение")

                val extension = getFileExtension(uri, contentResolver) ?: "jpg"

                val utcFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val timestamp = utcFormat.format(Date())

                val fileName = "profile-photo/$userId-$timestamp.$extension"

                val imageUrl = CloudImageUtils.uploadFile(
                    file = file,
                    fileName = fileName,
                    accessKey = BuildConfig.YANDEX_ACCESS_KEY_ID,
                    secretKey = BuildConfig.YANDEX_SECRET_ACCESS_KEY
                )

                withContext(Dispatchers.Main) {
                    firestore.collection("users")
                        .document(userId)
                        .update("profileImageUrl", imageUrl)
                        .await()
                }

                return@withContext imageUrl

            } catch (e: Exception) {
                Log.e("UserRepository", "Upload error", e)
                throw Exception("Ошибка загрузки: ${e.message}")
            } finally {
                file?.delete()
            }
        }

    suspend fun uploadFavoritePlaceImage(uri: Uri, contentResolver: ContentResolver): String =
        withContext(Dispatchers.IO) {
            val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
            val userId = currentUser.uid

            var file: File? = null

            try {
                file = uriToFile(uri, contentResolver)
                    ?: throw Exception("Не удалось обработать изображение")

                val extension = getFileExtension(uri, contentResolver) ?: "jpg"
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "favoriteplace/$userId-$timestamp.$extension"

                val imageUrl = CloudImageUtils.uploadFile(
                    file = file,
                    fileName = fileName,
                    accessKey = BuildConfig.YANDEX_ACCESS_KEY_ID,
                    secretKey = BuildConfig.YANDEX_SECRET_ACCESS_KEY
                )

                withContext(Dispatchers.Main) {
                    firestore.collection("users")
                        .document(userId)
                        .update("favoritePlacePhoto", imageUrl)
                        .await()
                }

                Log.d("UserRepository", "Favorite place photo uploaded successfully: $imageUrl")
                return@withContext imageUrl

            } catch (e: Exception) {
                Log.e("UserRepository", "Error uploading favorite place photo", e)
                throw Exception("Ошибка загрузки фото места: ${e.message}")
            } finally {
                file?.delete()
            }
        }

    // ==================== ПОЛУЧЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ ====================

    suspend fun getUserData(): Map<String, Any> {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            document.data ?: emptyMap()
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки данных: ${e.message}")
        }
    }

    suspend fun getUserProfileImageUrl(): String? {
        val currentUser = auth.currentUser ?: return null
        return try {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
                .getString("profileImageUrl")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserData(data: Map<String, Any>) {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        try {
            firestore.collection("users")
                .document(currentUser.uid)
                .update(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Ошибка обновления данных: ${e.message}")
        }
    }

    suspend fun getCurrentUser(): MyUser? {
        val currentUser = auth.currentUser ?: return null
        Log.d("UserRepository", "Current user UID: ${currentUser.uid}")

        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (!document.exists()) {
                Log.e("UserRepository", "Document does not exist for user: ${currentUser.uid}")
                return null
            }

            val user = document.toObject(MyUser::class.java)
            user?.copy(uid = document.id)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current user", e)
            null
        }
    }

    suspend fun getUserById(userId: String): MyUser? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!document.exists()) return null

            document.toObject(MyUser::class.java)?.copy(uid = document.id)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by id: $userId", e)
            null
        }
    }

    suspend fun updateUserById(userId: String, data: Map<String, Any>) {
        try {
            firestore.collection("users")
                .document(userId)
                .update(data)
                .await()
            Log.d("UserRepository", "User $userId updated successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user $userId", e)
            throw Exception("Ошибка обновления пользователя: ${e.message}")
        }
    }

    // ==================== УПРАВЛЕНИЕ ДРУЗЬЯМИ ====================
    /**
     * Наблюдать за статусом друга в реальном времени
     * @param userId ID пользователя, у которого смотрим статус друга
     * @param friendId ID друга
     * @param onFriendStatusChanged колбэк, вызываемый при изменении статуса
     * @return функцию для отписки от наблюдения
     */
    fun observeFriendStatus(
        userId: String,
        friendId: String,
        onFriendStatusChanged: (String?) -> Unit
    ): () -> Unit {
        Log.d("UserRepository", "Setting up friend status observer for user=$userId, friend=$friendId")

        val listenerRegistration = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Error observing friend status", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        // Получаем поле friends.friendId.status
                        val friendStatus = snapshot.get("friends.$friendId.status") as? String
                        Log.d("UserRepository", "Friend status changed: $friendStatus for friend=$friendId")
                        onFriendStatusChanged(friendStatus)
                    } catch (e: Exception) {
                        Log.e("UserRepository", "Error parsing friend status", e)
                        onFriendStatusChanged(null)
                    }
                } else {
                    onFriendStatusChanged(null)
                }
            }

        return {
            Log.d("UserRepository", "Removing friend status observer for user=$userId, friend=$friendId")
            listenerRegistration.remove()
        }
    }

    suspend fun updateFriendStatusForUser(userId: String, friendId: String, newStatus: String) {
        try {
            val friendData = mapOf(
                "status" to newStatus,
                "since" to if (newStatus == "friend") com.google.firebase.Timestamp.now() else null
            )

            val updates = mapOf("friends.$friendId" to friendData)

            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            Log.d("UserRepository", "Successfully updated friend status for user $userId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating friend status", e)
            throw Exception("Ошибка обновления статуса друга: ${e.message}")
        }
    }

    /**
     * ОТПРАВИТЬ ЗАЯВКУ В ДРУЗЬЯ
     * (создаёт внутреннее уведомление → GitHub Actions отправит пуш)
     */
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String, fromUserName: String) {
        try {
            updateFriendStatusForUser(fromUserId, toUserId, "pending")
            updateFriendStatusForUser(toUserId, fromUserId, "incoming")

            // ✅ Внутреннее уведомление (pushSent = false → триггер для GitHub Actions)
            notificationRepository.createFriendRequestNotification(fromUserId, toUserId)

            Log.d("UserRepository", "Friend request sent from $fromUserId to $toUserId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending friend request", e)
            throw e
        }
    }

    /**
     * ПРИНЯТЬ ЗАЯВКУ В ДРУЗЬЯ
     * (создаёт внутреннее уведомление → GitHub Actions отправит пуш)
     */
    suspend fun acceptFriendRequest(currentUserId: String, friendId: String, friendName: String) {
        try {
            updateFriendStatusForUser(currentUserId, friendId, "friend")
            updateFriendStatusForUser(friendId, currentUserId, "friend")

            // ✅ Внутреннее уведомление (pushSent = false → триггер для GitHub Actions)
            notificationRepository.createFriendAcceptedNotification(friendId, currentUserId)

            Log.d("UserRepository", "Friend request accepted: $currentUserId accepted $friendId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error accepting friend request", e)
            throw e
        }
    }

    suspend fun rejectFriendRequest(currentUserId: String, friendId: String) {
        try {
            removeFriendField(currentUserId, friendId)
            removeFriendField(friendId, currentUserId)
            Log.d("UserRepository", "Friend request rejected: $currentUserId rejected $friendId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error rejecting friend request", e)
            throw e
        }
    }

    suspend fun removeFriendField(userId: String, friendId: String) {
        try {
            val updates = mapOf("friends.$friendId" to com.google.firebase.firestore.FieldValue.delete())
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error removing friend field", e)
            throw Exception("Ошибка удаления статуса друга: ${e.message}")
        }
    }

    /**
     * Получить общих друзей с другим пользователем
     */
    suspend fun getMutualFriends(otherUserId: String): List<MyUser> {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        val currentUserId = currentUser.uid

        Log.d("UserRepository", "Getting mutual friends between $currentUserId and $otherUserId")

        try {
            val currentUserDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val otherUserDoc = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()

            if (!currentUserDoc.exists() || !otherUserDoc.exists()) {
                Log.e("UserRepository", "One of users doesn't exist")
                return emptyList()
            }

            val currentUserFriends =
                currentUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val otherUserFriends =
                otherUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val currentUserFriendIds = currentUserFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            val otherUserFriendIds = otherUserFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            val mutualFriendIds = currentUserFriendIds.intersect(otherUserFriendIds)

            Log.d("UserRepository", "Mutual friend IDs: $mutualFriendIds")

            if (mutualFriendIds.isEmpty()) {
                return emptyList()
            }

            val mutualFriends = mutableListOf<MyUser>()
            for (friendId in mutualFriendIds) {
                val friendDoc = firestore.collection("users")
                    .document(friendId)
                    .get()
                    .await()

                if (friendDoc.exists()) {
                    val friend = friendDoc.toObject(MyUser::class.java)?.copy(uid = friendDoc.id)
                    if (friend != null) {
                        mutualFriends.add(friend)
                        Log.d("UserRepository", "Added mutual friend: ${friend.name}")
                    }
                }
            }

            Log.d("UserRepository", "Found ${mutualFriends.size} mutual friends")
            return mutualFriends

        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting mutual friends", e)
            return emptyList()
        }
    }

    /**
     * Получить количество общих друзей с другим пользователем
     */
    suspend fun getMutualFriendsCount(otherUserId: String): Int {
        val currentUser = auth.currentUser ?: return 0
        val currentUserId = currentUser.uid

        return try {
            val currentUserDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val otherUserDoc = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()

            if (!currentUserDoc.exists() || !otherUserDoc.exists()) {
                return 0
            }

            val currentUserFriends =
                currentUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val otherUserFriends =
                otherUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val currentUserFriendIds = currentUserFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            val otherUserFriendIds = otherUserFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            currentUserFriendIds.intersect(otherUserFriendIds).size
        } catch (e: Exception) {
            Log.e("UserRepository", "Error counting mutual friends", e)
            0
        }
    }

    /**
     * Получить количество общих друзей для конкретных двух пользователей (без авторизации)
     */
    suspend fun getMutualFriendsCountForUser(userId: String, otherUserId: String): Int {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val otherUserDoc = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()

            if (!userDoc.exists() || !otherUserDoc.exists()) {
                return 0
            }

            val userFriends =
                userDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val otherUserFriends =
                otherUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                    ?: emptyMap()

            val userFriendIds = userFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            val otherUserFriendIds = otherUserFriends
                .filter { it.value["status"] == "friend" }
                .keys
                .toSet()

            userFriendIds.intersect(otherUserFriendIds).size
        } catch (e: Exception) {
            Log.e("UserRepository", "Error counting mutual friends for users", e)
            0
        }
    }

    suspend fun getAllUsers(): List<MyUser> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MyUser::class.java)?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting all users", e)
            emptyList()
        }
    }

    suspend fun getPlacesDetails(placeIds: List<String>): Result<List<PlaceInfo>> =
        withContext(Dispatchers.IO) {
            try {
                if (placeIds.isEmpty()) return@withContext Result.success(emptyList())

                val chunkedIds = placeIds.chunked(10)
                val allPlaces = mutableListOf<PlaceInfo>()

                for (chunk in chunkedIds) {
                    val snapshot = firestore.collection("places")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    val places = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PlaceInfo::class.java)?.copy(id = doc.id)
                    }
                    allPlaces.addAll(places)
                }

                Result.success(allPlaces)
            } catch (e: Exception) {
                Log.e("UserRepository", "Error getting places details", e)
                Result.failure(e)
            }
        }

    suspend fun getPossiblePlaces() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("possible_places").get().await()
            snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val address = doc.getString("address")
                val categories = doc.get("categories") as? List<String> ?: emptyList()
                val photoUrl = doc.getString("photoUrl")
                Triple(name, address, Pair(categories, photoUrl))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting possible places", e)
            emptyList()
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private fun uriToFile(uri: Uri, contentResolver: ContentResolver): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("profile_", getFileExtension(uri, contentResolver) ?: "jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        return when {
            uri.scheme == ContentResolver.SCHEME_CONTENT -> {
                val mime = android.webkit.MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(contentResolver.getType(uri))
            }
            else -> {
                val path = uri.path ?: return null
                path.substringAfterLast(".", "").takeIf { it.isNotEmpty() }
            }
        }?.lowercase()
    }

    fun logout() {
        try {
            auth.signOut()
            Log.d("UserRepository", "User signed out successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error during sign out", e)
            throw Exception("Ошибка при выходе: ${e.message}")
        }
    }
}