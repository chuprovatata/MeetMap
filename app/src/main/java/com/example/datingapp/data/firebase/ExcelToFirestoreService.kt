// app/src/main/java/com/example/datingapp/data/firebase/ExcelToFirestoreService.kt
package com.example.datingapp.data.firebase

import android.content.Context
import com.example.datingapp.data.csv.CsvImportService
import com.example.datingapp.data.csv.CsvParseResult
import com.example.datingapp.data.csv.SkippedRowInfo
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.InputStream

data class ImportStatistics(
    val totalInFirebase: Int,
    val totalInFile: Int,
    val added: Int,
    val updated: Int,
    val skipped: List<SkippedRowInfo>
)

class ExcelToFirestoreService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val csvImportService = CsvImportService(context)

    suspend fun importCsvToFirestore(
        inputStream: InputStream,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: (Boolean, String?, ImportStatistics?) -> Unit
    ) {
        try {
            // 1. Получаем общее количество мест в Firebase
            val totalInFirebase = getTotalPlacesCount()

            // 2. Читаем данные из CSV
            val parseResult = csvImportService.importPlacesFromCsv(inputStream)
            val placesFromFile = parseResult.places

            if (placesFromFile.isEmpty()) {
                val stats = ImportStatistics(
                    totalInFirebase = totalInFirebase,
                    totalInFile = 0,
                    added = 0,
                    updated = 0,
                    skipped = parseResult.skippedRows
                )
                onComplete(false, "CSV файл не содержит валидных мест для импорта", stats)
                return
            }

            // 3. Загружаем в Firestore
            var addedCount = 0
            var updatedCount = 0
            val processedIds = mutableSetOf<String>()

            for ((index, placeFromFile) in placesFromFile.withIndex()) {
                try {
                    val existingPlace = findExistingPlace(placeFromFile)

                    if (existingPlace == null) {
                        // Место не существует - создаем новое
                        addNewPlace(placeFromFile)
                        addedCount++
                    } else {
                        // Место существует - проверяем и обновляем при необходимости
                        if (shouldUpdatePlace(existingPlace, placeFromFile)) {
                            updateExistingPlace(existingPlace.id, placeFromFile)
                            updatedCount++
                        }
                    }

                    processedIds.add(placeFromFile.uniqueId)
                    onProgress(index + 1, placesFromFile.size)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val statistics = ImportStatistics(
                totalInFirebase = totalInFirebase,
                totalInFile = placesFromFile.size,
                added = addedCount,
                updated = updatedCount,
                skipped = parseResult.skippedRows
            )

            val message = buildResultMessage(statistics)
            onComplete(true, message, statistics)

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, "Ошибка при обработке файла: ${e.message}", null)
        }
    }

    private suspend fun getTotalPlacesCount(): Int {
        return try {
            val snapshot = firestore.collection("places_info")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun findExistingPlace(placeFromFile: PlaceInfo): PlaceInfo? {
        // Поиск по ID из файла (если есть)
        if (placeFromFile.id.isNotEmpty()) {
            try {
                val docSnapshot = firestore.collection("places_info")
                    .document(placeFromFile.id)
                    .get()
                    .await()

                if (docSnapshot.exists()) {
                    return docSnapshot.toObject(PlaceInfo::class.java)
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, пробуем искать по unique_id
            }
        }

        // Поиск по unique_id
        val querySnapshot = firestore.collection("places_info")
            .whereEqualTo("unique_id", placeFromFile.uniqueId)
            .limit(1)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].toObject(PlaceInfo::class.java)
        } else {
            null
        }
    }

    private fun shouldUpdatePlace(existing: PlaceInfo, new: PlaceInfo): Boolean {
        // Проверяем description
        if (existing.description != new.description) {
            return true
        }

        // Проверяем photoUrl (используем правильное имя поля из модели)
        if (existing.photoUrl != new.photoUrl) {
            return true
        }

        return false
    }

    private suspend fun addNewPlace(place: PlaceInfo) {
        // Создаем новый документ
        val documentRef = firestore.collection("places_info")
            .add(place)
            .await()

        // Обновляем ID документа
        documentRef.update("id", documentRef.id).await()
    }

    private suspend fun updateExistingPlace(docId: String, updatedPlace: PlaceInfo) {
        val updates = mutableMapOf<String, Any>()

        // Обновляем description если есть
        if (updatedPlace.description.isNotEmpty()) {
            updates["description"] = updatedPlace.description
        }

        // ВАЖНО: Используем "photoUrl" как в модели данных
        updates["photoUrl"] = updatedPlace.photoUrl

        // Удаляем старое поле photo_url
        updates["photo_url"] = FieldValue.delete()

        // Используем текущее время для updatedAt, если updatedPlace.updatedAt == null
        val timestamp = updatedPlace.updatedAt ?: Timestamp.now()
        updates["updatedAt"] = timestamp

        firestore.collection("places_info")
            .document(docId)
            .update(updates)
            .await()
    }

    private fun buildResultMessage(stats: ImportStatistics): String {
        return buildString {
            appendLine("Результаты импорта:")
            appendLine("• Всего в Firebase: ${stats.totalInFirebase}")
            appendLine("• Найдено в файле: ${stats.totalInFile}")
            appendLine("• Добавлено: ${stats.added}")
            appendLine("• Обновлено: ${stats.updated}")
            appendLine("• Пропущено: ${stats.skipped.size}")

            if (stats.skipped.isNotEmpty()) {
                appendLine("\nПропущенные места:")
                stats.skipped.forEach {
                    appendLine("  • Строка ${it.rowNumber}: ${it.placeName} - ${it.reason}")
                }
            }
        }
    }
}