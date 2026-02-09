// app/src/main/java/com/example/datingapp/data/firebase/ExcelToFirestoreService.kt
package com.example.datingapp.data.firebase

import android.content.Context
import com.example.datingapp.data.csv.CsvImportService
import com.example.datingapp.data.models.PlaceInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.InputStream

class ExcelToFirestoreService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val csvImportService = CsvImportService(context)

    // ИЗМЕНЕНО: функция теперь публичная
    suspend fun importCsvToFirestore(
        inputStream: InputStream,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            // 1. Читаем данные из CSV
            val places = csvImportService.importPlacesFromCsv(inputStream)

            if (places.isEmpty()) {
                onComplete(false, "CSV файл пустой или неверного формата")
                return
            }

            // 2. Загружаем в Firestore
            var successCount = 0
            var errorCount = 0

            for ((index, place) in places.withIndex()) {
                try {
                    // Генерируем уникальный ID
                    val uniqueId = place.generateUniqueId()

                    // Проверяем, существует ли уже такое место
                    val existingPlace = firestore.collection("places_info")
                        .whereEqualTo("unique_id", uniqueId)
                        .get()
                        .await()

                    if (existingPlace.isEmpty) {
                        // Создаем новое место
                        val placeWithUniqueId = place.copy(uniqueId = uniqueId)
                        firestore.collection("places_info")
                            .add(placeWithUniqueId)
                            .await()
                        successCount++
                    } else {
                        // Место уже существует - можно обновить
                        val docId = existingPlace.documents[0].id
                        firestore.collection("places_info")
                            .document(docId)
                            .set(place.copy(id = docId, uniqueId = uniqueId))
                            .await()
                        successCount++
                    }

                    // Обновляем прогресс
                    onProgress(index + 1, places.size)

                } catch (e: Exception) {
                    e.printStackTrace()
                    errorCount++
                }
            }

            val message = when {
                successCount == places.size -> "Успешно загружено $successCount мест"
                successCount > 0 -> "Загружено $successCount мест, ошибок: $errorCount"
                else -> "Не удалось загрузить ни одного места"
            }

            onComplete(successCount > 0, message)

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, "Ошибка при обработке файла: ${e.message}")
        }
    }

    // Остальные методы могут остаться private
}