// app/src/main/java/com/example/datingapp/data/csv/CsvImportService.kt
package com.example.datingapp.data.csv

import android.content.Context
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class CsvImportService(private val context: Context) {

    suspend fun importPlacesFromCsv(inputStream: InputStream): List<PlaceInfo> = withContext(Dispatchers.IO) {
        val places = mutableListOf<PlaceInfo>()

        try {
            val lines = inputStream.bufferedReader().readLines()

            lines.forEachIndexed { index, line ->
                if (index == 0) return@forEachIndexed // Пропускаем заголовок
                if (line.trim().isEmpty() || line.startsWith(";;;;")) return@forEachIndexed // Пропускаем пустые строки

                val columns = parseSemicolonCsvLine(line)

                if (columns.size >= 7) { // Нужно минимум 7 колонок
                    try {
                        val place = createPlaceFromCsvColumns(columns)
                        places.add(place)
                    } catch (e: Exception) {
                        e.printStackTrace() // Логируем ошибку, но продолжаем обработку
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext places
    }

    private fun createPlaceFromCsvColumns(columns: List<String>): PlaceInfo {
        val name = cleanString(columns.getOrNull(1) ?: "")
        val address = cleanString(columns.getOrNull(2) ?: "")
        val latitude = columns.getOrNull(5)?.toDoubleOrNull() ?: 0.0
        val longitude = columns.getOrNull(6)?.toDoubleOrNull() ?: 0.0

        // Генерируем уникальный ID
        val uniqueId = "${name.lowercase().replace(" ", "_")}_${latitude}_${longitude}"

        return PlaceInfo(
            // id оставляем пустым - Firestore сам сгенерирует
            id = "",
            name = name,
            address = address,
            categories = parseCategories(columns.getOrNull(3) ?: ""),
            latitude = latitude,
            longitude = longitude,
            rarity = parseRarity(columns.getOrNull(4) ?: ""),
            photoUrl = "https://storage.yandexcloud.net/meetmap/NO%20Picture.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=YCAJEwyujEU34SnldwmEhCSvI%2F20260208%2Fru-central1%2Fs3%2Faws4_request&X-Amz-Date=20260208T230012Z&X-Amz-Expires=60&X-Amz-Signature=8870e2af9d160cc4754a93cdbdb99b8a4531f6dbe7f884da01aacddf18dcf60c&X-Amz-SignedHeaders=host",
            uniqueId = uniqueId, // Добавляем uniqueId
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    }

    private fun parseSemicolonCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> {
                    // Обработка двойных кавычек (экранирование "")
                    if (current.endsWith('"')) {
                        current.deleteCharAt(current.length - 1) // Удаляем предыдущую кавычку
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ';' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }

        result.add(current.toString().trim())
        return result
    }

    private fun cleanString(text: String): String {
        // Убираем двойные кавычки из начала и конца, заменяем экранированные кавычки
        return text.trim()
            .removePrefix("\"")
            .removeSuffix("\"")
            .replace("\"\"", "\"") // Заменяем "" на "
            .trim()
    }

    private fun parseCategories(categoryString: String): List<String> {
        return categoryString.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }

    private fun parseRarity(rarityString: String): String {
        val cleanRarity = cleanString(rarityString).trim()
        return when (cleanRarity.lowercase()) {
            "базовое" -> PlaceInfo.RARITY_COMMON
            "среднее" -> PlaceInfo.RARITY_UNCOMMON
            "редкое" -> PlaceInfo.RARITY_RARE
            "эпическое" -> PlaceInfo.RARITY_EPIC
            "уникальное" -> PlaceInfo.RARITY_UNIQUE
            else -> PlaceInfo.RARITY_COMMON
        }
    }
}