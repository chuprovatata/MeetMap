package com.meetmap.datingapp.utils

import android.util.Log
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.meetmap.datingapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CloudImageUtils {

    private const val TAG = "CloudImageUtils"

    // Константы для Яндекс.Облака
    const val BUCKET_NAME = "meetmap"
    const val ENDPOINT = "storage.yandexcloud.net"
    private const val REGION = "ru-central1"
    private const val SERVICE = "s3"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, "Request: ${request.url}")
            val response = chain.proceed(request)
            Log.d(TAG, "Response: ${response.code} ${response.message}")
            response
        }
        .build()

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
     * Загрузить файл с подписью AWS Signature V4
     */
    suspend fun uploadFile(
        file: File,
        fileName: String,
        accessKey: String,
        secretKey: String
    ): String = withContext(Dispatchers.IO) {
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        val url = "https://$BUCKET_NAME.$ENDPOINT/$encodedFileName"
        Log.d(TAG, "Uploading to: $url")

        val contentType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }

        val date = getCurrentDate()
        val dateTime = getCurrentDateTime()

        val payloadHash = file.inputStream().use { input ->
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
        Log.d(TAG, "Payload hash: $payloadHash")

        val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Host", "$BUCKET_NAME.$ENDPOINT")
            .addHeader("Content-Type", contentType)
            .addHeader("x-amz-acl", "public-read")
            .addHeader("x-amz-date", dateTime)
            .addHeader("x-amz-content-sha256", payloadHash) // ⬅️ ВАЖНО!
            .addHeader("Authorization", buildSignature(accessKey, secretKey, date, dateTime, fileName, contentType, payloadHash))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            Log.e(TAG, "Upload failed: ${response.code} - $errorBody")
            throw Exception("HTTP ${response.code}: $errorBody")
        }

        Log.d(TAG, "Upload successful: $url")
        return@withContext url
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

    private fun buildSignature(
        accessKey: String,
        secretKey: String,
        date: String,
        dateTime: String,
        fileName: String,
        contentType: String,
        payloadHash: String
    ): String {
        val canonicalRequest = """
    PUT
    /$fileName
    
    content-type:$contentType
    host:$BUCKET_NAME.$ENDPOINT
    x-amz-acl:public-read
    x-amz-content-sha256:$payloadHash
    x-amz-date:$dateTime
    
    content-type;host;x-amz-acl;x-amz-content-sha256;x-amz-date
    $payloadHash
""".trimIndent().replace("\n", "\n")

        Log.d(TAG, "CanonicalRequest: $canonicalRequest")

        val canonicalRequestHash = hashSha256(canonicalRequest)
        Log.d(TAG, "CanonicalRequestHash: $canonicalRequestHash")

        val stringToSign = """
        AWS4-HMAC-SHA256
        $dateTime
        $date/$REGION/$SERVICE/aws4_request
        $canonicalRequestHash
    """.trimIndent()

        Log.d(TAG, "StringToSign: $stringToSign")

        val signingKey = deriveSigningKey(secretKey, date, REGION, SERVICE)
        val signature = hmacSha256Hex(signingKey, stringToSign)

        Log.d(TAG, "Signature: $signature")

        return "AWS4-HMAC-SHA256 Credential=$accessKey/$date/$REGION/$SERVICE/aws4_request, SignedHeaders=content-type;host;x-amz-acl;x-amz-content-sha256;x-amz-date, Signature=$signature"
    }

    private fun deriveSigningKey(secretKey: String, date: String, region: String, service: String): ByteArray {
        val kSecret = ("AWS4$secretKey").toByteArray()
        val kDate = hmacSha256(kSecret, date)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).joinToString("") { "%02x".format(it) }
    }

    private fun hashSha256(data: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return dateFormat.format(java.util.Date())
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", java.util.Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val now = java.util.Date()
        val result = dateFormat.format(now)
        Log.d(TAG, "Current UTC time: $result")
        return result
    }
}