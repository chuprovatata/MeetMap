package com.example.datingapp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.datingapp.BuildConfig
import com.example.datingapp.utils.CloudImageUtils
import com.google.firebase.auth.FirebaseAuth
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
}