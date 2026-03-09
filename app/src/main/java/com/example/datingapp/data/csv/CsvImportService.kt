// app/src/main/java/com/example/datingapp/data/csv/CsvImportService.kt
package com.example.datingapp.data.csv

import android.content.Context
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.BufferedReader

data class CsvParseResult(
    val places: List<PlaceInfo>,
    val skippedRows: List<SkippedRowInfo>
)

data class SkippedRowInfo(
    val rowNumber: Int,
    val placeName: String,
    val reason: String
)

class CsvImportService(private val context: Context) {

    suspend fun importPlacesFromCsv(inputStream: InputStream): CsvParseResult = withContext(Dispatchers.IO) {
        val places = mutableListOf<PlaceInfo>()
        val skippedRows = mutableListOf<SkippedRowInfo>()
        var lineNumber = 0

        try {
            val reader = inputStream.bufferedReader()
            val allLines = mutableListOf<String>()
            var currentLine = StringBuilder()
            var insideQuotes = false

            // Читаем файл построчно, но собираем многострочные поля
            reader.forEachLine { line ->
                lineNumber++

                for (char in line) {
                    currentLine.append(char)
                    if (char == '"') {
                        insideQuotes = !insideQuotes
                    }
                }

                // Если мы не внутри кавычек, значит строка завершена
                if (!insideQuotes) {
                    allLines.add(currentLine.toString())
                    currentLine.clear()
                } else {
                    // Добавляем перевод строки, так как мы внутри кавычек
                    currentLine.append('\n')
                }
            }

            // Добавляем последнюю строку, если она есть
            if (currentLine.isNotEmpty()) {
                allLines.add(currentLine.toString())
            }

            // Проверяем, что файл не пустой
            if (allLines.isEmpty()) {
                return@withContext CsvParseResult(emptyList(), skippedRows)
            }

            // Пропускаем заголовок (первая строка)
            for ((index, line) in allLines.withIndex()) {
                val currentRowNumber = index + 1

                if (index == 0) continue // Пропускаем заголовок
                if (line.trim().isEmpty()) continue // Пропускаем пустые строки

                val columns = parseCsvLine(line)

                // Проверяем минимальное количество колонок
                if (columns.size < 15) {
                    val placeName = columns.getOrNull(2)?.trim() ?: "Неизвестно"
                    skippedRows.add(SkippedRowInfo(
                        rowNumber = currentRowNumber,
                        placeName = placeName,
                        reason = "Недостаточно колонок: ${columns.size} (нужно минимум 15)"
                    ))
                    continue
                }

                try {
                    val place = createPlaceFromCsvColumns(columns, currentRowNumber, skippedRows)
                    if (place != null) {
                        places.add(place)
                    }
                } catch (e: Exception) {
                    val placeName = columns.getOrNull(2)?.trim() ?: "Неизвестно"
                    skippedRows.add(SkippedRowInfo(
                        rowNumber = currentRowNumber,
                        placeName = placeName,
                        reason = "Ошибка обработки: ${e.message}"
                    ))
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            println("Ошибка при чтении CSV: ${e.message}")
            e.printStackTrace()
        }

        return@withContext CsvParseResult(places, skippedRows)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Экранированная кавычка ("")
                        current.append('"')
                        i += 2
                    } else {
                        // Открывающая или закрывающая кавычка
                        insideQuotes = !insideQuotes
                        i++
                    }
                }
                char == ';' && !insideQuotes -> {
                    // Разделитель колонок
                    result.add(current.toString().trim())
                    current.clear()
                    i++
                }
                else -> {
                    current.append(char)
                    i++
                }
            }
        }

        // Добавляем последнюю колонку
        result.add(current.toString().trim())

        return result
    }

    private fun createPlaceFromCsvColumns(
        columns: List<String>,
        rowNumber: Int,
        skippedRows: MutableList<SkippedRowInfo>
    ): PlaceInfo? {
        // Индексы колонок (0-based):
        // 0 - ID
        // 1 - Номер
        // 2 - Название
        // 3 - Адрес
        // 4 - Категория (основная)
        // 5 - Категория - 2 (редкость)
        // 6 - Широта
        // 7 - Долгота
        // 8 - Текст (описание)
        // 9 - Ссылка на фото
        // 10 - Источник
        // 11 - Ближайшее метро
        // 12 - Расстояние
        // 13 - Линия
        // 14 - SVG метро

        val existingId = columns.getOrNull(0)?.trim() ?: ""
        val name = columns.getOrNull(2)?.trim() ?: ""
        val address = columns.getOrNull(3)?.trim() ?: ""
        val mainCategory = columns.getOrNull(4)?.trim()?.lowercase() ?: ""
        val rarityString = columns.getOrNull(5)?.trim() ?: ""
        val latitude = parseCoordinate(columns.getOrNull(6) ?: "0")
        val longitude = parseCoordinate(columns.getOrNull(7) ?: "0")
        val description = columns.getOrNull(8)?.trim() ?: ""
        val metroStation = columns.getOrNull(11)?.trim() ?: ""
        val distanceStr = columns.getOrNull(12)?.trim() ?: "0"
        val metroLine = columns.getOrNull(13)?.trim() ?: ""

        // Валидация обязательных полей
        if (name.isEmpty()) {
            skippedRows.add(SkippedRowInfo(
                rowNumber = rowNumber,
                placeName = "Неизвестно",
                reason = "Отсутствует название места"
            ))
            return null
        }

        if (latitude == 0.0 || longitude == 0.0) {
            skippedRows.add(SkippedRowInfo(
                rowNumber = rowNumber,
                placeName = name,
                reason = "Некорректные координаты (широта: $latitude, долгота: $longitude)"
            ))
            return null
        }

        // Парсим расстояние
        val distanceToMetro = try {
            distanceStr.replace(",", ".").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }

        // Формируем категории
        val categories = if (mainCategory.isNotEmpty()) {
            listOf(mainCategory)
        } else {
            emptyList()
        }

        // Генерируем уникальный ID
        val uniqueId = generateUniqueId(name, latitude, longitude)

        // Формируем photoUrl
        val photoUrl = "https://storage.yandexcloud.net/meetmap/photoplace/${uniqueId}.jpg"

        val now = Timestamp.now()

        return PlaceInfo(
            id = existingId,
            name = name,
            address = address,
            categories = categories,
            latitude = latitude,
            longitude = longitude,
            metroStation = metroStation,
            metroLine = metroLine,
            distanceToMetro = distanceToMetro,
            rarity = parseRarity(rarityString),
            description = description,
            photoUrl = photoUrl,
            likesCount = 0,
            hasFireIcon = false,
            place_ofday = false,
            uniqueId = uniqueId,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun generateUniqueId(name: String, latitude: Double, longitude: Double): String {
        val cleanName = name.lowercase()
            .replace(" ", "_")
            .replace("\"", "")
            .replace("'", "")
            .replace(Regex("[^a-zа-я0-9_]"), "")
        return "${cleanName}_${latitude}_${longitude}"
    }

    private fun parseCoordinate(coord: String): Double {
        return try {
            coord.replace("\"", "")
                .replace(",", ".")
                .trim()
                .toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    private fun parseRarity(rarityString: String): String {
        val cleanRarity = rarityString.trim().lowercase()
            .replace("\"", "")

        return when {
            cleanRarity.contains("базовое") -> PlaceInfo.RARITY_COMMON
            cleanRarity.contains("среднее") -> PlaceInfo.RARITY_UNCOMMON
            cleanRarity.contains("редкое") -> PlaceInfo.RARITY_RARE
            cleanRarity.contains("эпическое") -> PlaceInfo.RARITY_EPIC
            cleanRarity.contains("уникальное") -> PlaceInfo.RARITY_UNIQUE
            else -> PlaceInfo.RARITY_COMMON
        }
    }
}