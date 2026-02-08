package com.example.datingapp.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun uploadProfileImage(uri: Uri): String {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        val userId = currentUser.uid

        try {
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            val uploadTask = storageRef.putFile(uri).await()

            val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await()
                ?: throw Exception("Не удалось получить URL изображения")

            val imageUrl = downloadUrl.toString()

            firestore.collection("users")
                .document(userId)
                .update("profileImageUrl", imageUrl)
                .await()

            return imageUrl

        } catch (e: Exception) {
            throw Exception("Ошибка загрузки фото: ${e.message}")
        }
    }

    suspend fun getUserProfileImageUrl(): String? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            document.getString("profileImageUrl")
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

    suspend fun getUserData(): Map<String, Any> {
        val currentUser = auth.currentUser ?: throw Exception("Пользователь не авторизован")

        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (document.exists()) {
                document.data ?: emptyMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки данных: ${e.message}")
        }
    }
}