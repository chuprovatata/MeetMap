// app/src/main/java/com/example/datingapp/data/yandex/YandexCloudService.kt
package com.example.datingapp.data.yandex

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object YandexCloudService {

    private const val TAG = "YandexCloud"
    private const val ENDPOINT = "storage.yandexcloud.net"
    private const val BUCKET_NAME = "meetmap"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "Request: ${request.url}")

            val response = chain.proceed(request)
            Log.d(TAG, "Response: ${response.code} ${response.message}")

            response
        }
        .build()

    /**
     * Получить список всех файлов в бакете
     */
    suspend fun listFiles(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://$BUCKET_NAME.$ENDPOINT/"
            Log.d(TAG, "Fetching files from: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "MeetMap-Android-App")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                Log.d(TAG, "Response body length: ${body.length}")
                Log.d(TAG, "Response body first 500 chars: ${body.take(500)}")

                parseXmlFileList(body)
            } else {
                Log.e(TAG, "Failed to list files: ${response.code} - ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files", e)
            emptyList()
        }
    }

    /**
     * Получить публичный URL для файла
     */
    fun getPublicUrl(fileName: String): String {
        return "https://$BUCKET_NAME.$ENDPOINT/$fileName"
    }

    /**
     * Проверить доступность файла
     */
    suspend fun checkFileExists(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = getPublicUrl(fileName)
            Log.d(TAG, "Checking file: $url")

            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = client.newCall(request).execute()
            val exists = response.isSuccessful

            Log.d(TAG, "File $fileName exists: $exists (${response.code})")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking file $fileName", e)
            false
        }
    }

    /**
     * Получить базовую информацию о бакете
     */
    suspend fun getBucketInfo(): String = withContext(Dispatchers.IO) {
        try {
            val url = "https://$BUCKET_NAME.$ENDPOINT/"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            return@withContext when {
                response.isSuccessful -> {
                    "Бакет доступен. Код: ${response.code}"
                }
                response.code == 404 -> {
                    "Бакет не найден (404). Проверьте название: $BUCKET_NAME"
                }
                else -> {
                    "Ошибка доступа к бакету. Код: ${response.code}, Сообщение: ${response.message}"
                }
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun parseXmlFileList(xml: String): List<String> {
        val files = mutableListOf<String>()

        try {
            if (xml.isBlank()) {
                Log.w(TAG, "Empty XML response")
                return emptyList()
            }

            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var isKey = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "Key") {
                            isKey = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (isKey) {
                            val fileName = parser.text.trim()
                            if (fileName.isNotBlank() && !fileName.endsWith("/")) {
                                files.add(fileName)
                                Log.d(TAG, "Found file: $fileName")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Key") {
                            isKey = false
                        }
                    }
                }
                eventType = parser.next()
            }

            Log.d(TAG, "Total files parsed: ${files.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML", e)

            // Попробуем простой regex парсинг как fallback
            val keyPattern = "<Key>([^<]+)</Key>".toRegex()
            val matches = keyPattern.findAll(xml)

            matches.forEach { matchResult ->
                val fileName = matchResult.groupValues[1].trim()
                if (fileName.isNotBlank() && !fileName.endsWith("/")) {
                    files.add(fileName)
                }
            }

            Log.d(TAG, "Regex parsed files: ${files.size}")
        }

        return files
    }
}