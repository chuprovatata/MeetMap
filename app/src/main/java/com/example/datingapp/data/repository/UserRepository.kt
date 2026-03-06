package com.example.datingapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.datingapp.BuildConfig
import com.example.datingapp.utils.CloudImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
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
    private val okHttpClient: OkHttpClient
) {

    suspend fun uploadProfileImage(uri: Uri, contentResolver: ContentResolver): String = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        val userId = currentUser.uid

        var file: File? = null

        try {
            file = uriToFile(uri, contentResolver)
                ?: throw Exception("Не удалось обработать изображение")

            val extension = getFileExtension(uri, contentResolver) ?: "jpg"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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
    suspend fun uploadFavoritePlaceImage(uri: Uri, contentResolver: ContentResolver): String = withContext(Dispatchers.IO) {
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

    /**
     * Получить текущего пользователя как data class MyUser
     */
    suspend fun getCurrentUser(): MyUser? {
        val currentUser = auth.currentUser ?: return null
        Log.d("UserRepository", "Current user UID: ${currentUser.uid}")

        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            Log.d("UserRepository", "Document exists: ${document.exists()}")

            if (!document.exists()) {
                Log.e("UserRepository", "Document does not exist for user: ${currentUser.uid}")
                return null
            }

            Log.d("UserRepository", "Document data: ${document.data}")

            val user = document.toObject(MyUser::class.java)
            Log.d("UserRepository", "Converted MyUser before copy: $user")

            val userWithId = user?.copy(uid = document.id)
            Log.d("UserRepository", "Final MyUser with uid: $userWithId")

            userWithId
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current user", e)
            null
        }
    }

    /**
     * Получить пользователя по ID
     */
    suspend fun getUserById(userId: String): MyUser? {
        Log.d("UserRepository", "Getting user by ID: $userId")

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            Log.d("UserRepository", "Document exists: ${document.exists()} for userId: $userId")

            if (!document.exists()) {
                Log.e("UserRepository", "Document does not exist for userId: $userId")
                return null
            }

            Log.d("UserRepository", "Document data for userId $userId: ${document.data}")

            val user = document.toObject(MyUser::class.java)
            Log.d("UserRepository", "Converted MyUser before copy: $user")

            val userWithId = user?.copy(uid = document.id)
            Log.d("UserRepository", "Final MyUser with uid: $userWithId")

            userWithId
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by id: $userId", e)
            null
        }
    }

    /**
     * Обновить данные пользователя по ID
     */
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

    /**
     * Обновить статус друга для пользователя
     */
    suspend fun updateFriendStatusForUser(userId: String, friendId: String, newStatus: String) {
        try {
            if (userId.isEmpty() || friendId.isEmpty()) {
                throw Exception("userId или friendId пустые: userId='$userId', friendId='$friendId'")
            }

            val fieldPath = "friends.$friendId.status"
            Log.d("UserRepository", "Updating: users/$userId/$fieldPath = $newStatus")

            firestore.collection("users")
                .document(userId)
                .update(fieldPath, newStatus)
                .await()

        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating friend status. userId='$userId', friendId='$friendId'", e)
            throw Exception("Ошибка обновления статуса друга: ${e.message}")
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

            val currentUserFriends = currentUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
                ?: emptyMap()

            val otherUserFriends = otherUserDoc.data?.get("friends") as? Map<String, Map<String, Any>>
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