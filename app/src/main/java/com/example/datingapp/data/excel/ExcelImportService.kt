// app/src/main/java/com/example/datingapp/data/excel/ExcelImportService.kt
package com.example.datingapp.data.excel

import android.content.Context
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.Timestamp
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ExcelImportService(private val context: Context) {

    suspend fun importPlacesFromExcel(inputStream: InputStream): List<PlaceInfo> = withContext(Dispatchers.IO) {
        val places = mutableListOf<PlaceInfo>()

        try {
            // Используем более простой парсер для XLSX
            val xlsxData = parseSimpleXlsx(inputStream)

            xlsxData.forEach { rowData ->
                if (rowData.size >= 6) {
                    val place = PlaceInfo(
                        id = "",
                        name = rowData[1].toString().trim(),
                        address = rowData[2].toString().trim(),
                        categories = parseCategories(rowData[3].toString()),
                        latitude = rowData[5].toString().toDoubleOrNull() ?: 0.0,
                        longitude = rowData[6].toString().toDoubleOrNull() ?: 0.0,
                        metroStation = "",
                        photoUrl = "https://storage.yandexcloud.net/meetmap/NO%20Picture.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=YCAJEwyujEU34SnldwmEhCSvI%2F20260208%2Fru-central1%2Fs3%2Faws4_request&X-Amz-Date=20260208T230012Z&X-Amz-Expires=60&X-Amz-Signature=8870e2af9d160cc4754a93cdbdb99b8a4531f6dbe7f884da01aacddf18dcf60c&X-Amz-SignedHeaders=host",
                        likesCount = 0,
                        hasFireIcon = false,
                        isPlaceOfDay = false,
                        uniqueId = "",
                        rarity = parseRarity(rowData[4].toString()),
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )

                    places.add(place.copy(uniqueId = place.generateUniqueId()))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext places
    }

    private fun parseSimpleXlsx(inputStream: InputStream): List<List<Any>> {
        // Простой парсинг CSV-like данных
        val lines = inputStream.bufferedReader().use { it.readLines() }
        val data = mutableListOf<List<Any>>()

        lines.forEachIndexed { index, line ->
            if (index == 0) return@forEachIndexed // Пропускаем заголовок

            // Разделяем строку по табуляции или запятой
            val row = if (line.contains('\t')) {
                line.split('\t').map { it.trim() }
            } else if (line.contains(',')) {
                line.split(',').map { it.trim() }
            } else {
                listOf(line.trim())
            }

            if (row.isNotEmpty()) {
                data.add(row)
            }
        }

        return data
    }

    private fun parseCategories(categoryString: String): List<String> {
        return categoryString.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }

    private fun parseRarity(rarityString: String): String {
        return when (rarityString.trim().lowercase()) {
            "базовое" -> PlaceInfo.RARITY_COMMON
            "среднее" -> PlaceInfo.RARITY_UNCOMMON
            "редкое" -> PlaceInfo.RARITY_RARE
            "эпическое" -> PlaceInfo.RARITY_EPIC
            "уникальное" -> PlaceInfo.RARITY_UNIQUE
            else -> PlaceInfo.RARITY_COMMON
        }
    }
}