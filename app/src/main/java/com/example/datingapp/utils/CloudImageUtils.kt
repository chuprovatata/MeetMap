// app/src/main/java/com/example/datingapp/utils/CloudImageUtils.kt
package com.example.datingapp.utils

import android.util.Log
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.datingapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CloudImageUtils {

    private const val TAG = "CloudImageUtils"

    // Константы для Яндекс.Облака
    const val BUCKET_NAME = "meetmap"
    const val ENDPOINT = "storage.yandexcloud.net"

    // Публичный URL для NO Picture
    val NO_PICTURE_URL = "https://$ENDPOINT/$BUCKET_NAME/NO%20Picture.png"

    /**
     * Проверить и исправить URL изображения
     */
    suspend fun getFixedImageUrl(originalUrl: String?): Any = withContext(Dispatchers.IO) {
        if (originalUrl.isNullOrBlank()) {
            Log.d(TAG, "URL is null or blank, using placeholder")
            return@withContext R.drawable.picture_museum_background
        }

        Log.d(TAG, "Processing URL: $originalUrl")

        return@withContext when {
            // Если это URL Яндекс.Облака
            originalUrl.contains("storage.yandexcloud.net") -> {
                // Убираем параметры подписи если они есть
                val cleanUrl = if (originalUrl.contains("?X-Amz-")) {
                    originalUrl.substringBefore("?X-Amz-")
                } else {
                    originalUrl
                }

                Log.d(TAG, "Cleaned Yandex URL: $cleanUrl")
                cleanUrl
            }

            // Другие HTTP URL
            originalUrl.startsWith("http") -> {
                Log.d(TAG, "Using HTTP URL: $originalUrl")
                originalUrl
            }

            // Локальный ресурс
            else -> {
                Log.d(TAG, "Using placeholder resource")
                R.drawable.picture_museum_background
            }
        }
    }

    /**
     * Создать painter для изображения с поддержкой облака
     */
    @androidx.compose.runtime.Composable
    fun createCloudImagePainter(
        imageUrl: String?,
        placeholderRes: Int = R.drawable.picture_museum_background
    ): AsyncImagePainter {
        val context = androidx.compose.ui.platform.LocalContext.current

        return rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .apply {
                    // Настройки для загрузки из облака
                    crossfade(true)
                    placeholder(placeholderRes)
                    error(placeholderRes)

                    // Добавляем заголовки для Яндекс.Облака
                    if (imageUrl?.contains("storage.yandexcloud.net") == true) {
                        // Убираем параметры подписи для лучшей совместимости
                        val cleanUrl = imageUrl.substringBefore("?X-Amz-")
                        data(cleanUrl)

                        // Добавляем User-Agent
                        addHeader("User-Agent", "MeetMap-Android-App/1.0")
                        addHeader("Accept", "image/*")
                    }
                }
                .build()
        )
    }
}