package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.data.models.UserPlace
import com.example.datingapp.data.repository.UserPlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class MyPlacesViewModel @Inject constructor(
    private val userPlacesRepository: UserPlacesRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _userPlaces = MutableStateFlow<List<UserPlace>>(emptyList())
    val userPlaces: StateFlow<List<UserPlace>> = _userPlaces.asStateFlow()

    private val _placesDetails = MutableStateFlow<Map<String, PlaceInfo>>(emptyMap())
    val placesDetails: StateFlow<Map<String, PlaceInfo>> = _placesDetails.asStateFlow()

    private val _combinedPlaces = MutableStateFlow<List<Pair<UserPlace, PlaceInfo?>>>(emptyList())
    val combinedPlaces: StateFlow<List<Pair<UserPlace, PlaceInfo?>>> = _combinedPlaces.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserPlaces()
    }

    fun loadUserPlaces() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("MY_PLACES_DEBUG", "========== НАЧАЛО ЗАГРУЗКИ МЕСТ ==========")
                Log.d("MY_PLACES_DEBUG", "Загружаем user_places из репозитория...")

                val result = userPlacesRepository.getUserLikedPlaces()

                if (result.isSuccess) {
                    val places = result.getOrNull() ?: emptyList()
                    Log.d("MY_PLACES_DEBUG", "✅ Загружено ${places.size} user_places записей")

                    places.forEachIndexed { index, userPlace ->
                        Log.d("MY_PLACES_DEBUG", "  user_place[$index]: placeId=${userPlace.placeId}, userId=${userPlace.userId}")
                    }

                    _userPlaces.value = places

                    if (places.isNotEmpty()) {
                        val placeIds = places.map { it.placeId }
                        Log.d("MY_PLACES_DEBUG", "Уникальные placeId: $placeIds")
                        loadPlacesDetails(placeIds)
                    } else {
                        Log.d("MY_PLACES_DEBUG", "⚠️ Нет user_places записей")
                        _combinedPlaces.value = emptyList()
                        _isLoading.value = false
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("MY_PLACES_DEBUG", "❌ Ошибка загрузки user_places", error)
                    _errorMessage.value = error?.message ?: "Ошибка загрузки мест"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("MY_PLACES_DEBUG", "❌ Исключение в loadUserPlaces", e)
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadPlacesDetails(placeIds: List<String>) {
        try {
            Log.d("MY_PLACES_DEBUG", "========== ЗАГРУЗКА ДЕТАЛЕЙ МЕСТ ==========")
            Log.d("MY_PLACES_DEBUG", "Загружаем детали для ${placeIds.size} мест")

            val detailsMap = mutableMapOf<String, PlaceInfo>()

            placeIds.forEach { placeId ->
                try {
                    Log.d("MY_PLACES_DEBUG", "--- Загрузка деталей для placeId: $placeId ---")

                    val doc = firestore.collection("places_info")
                        .document(placeId)
                        .get()
                        .await()

                    if (doc.exists()) {
                        Log.d("MY_PLACES_DEBUG", "✅ Документ существует")
                        val data = doc.data

                        if (data != null) {
                            Log.d("MY_PLACES_DEBUG", "Данные документа: $data")

                            // Проверяем оба возможных названия поля для лайков
                            var likesCount = 0

                            // Сначала пробуем likesCount (без подчеркивания)
                            if (data.containsKey("likesCount")) {
                                val rawValue = data["likesCount"]
                                Log.d("MY_PLACES_DEBUG", "Найдено поле 'likesCount': $rawValue (${rawValue?.javaClass?.simpleName})")
                                likesCount = when (rawValue) {
                                    is Long -> rawValue.toInt()
                                    is Int -> rawValue
                                    is Double -> rawValue.toInt()
                                    else -> 0
                                }
                            }
                            // Затем пробуем likes_count (с подчеркиванием)
                            else if (data.containsKey("likes_count")) {
                                val rawValue = data["likes_count"]
                                Log.d("MY_PLACES_DEBUG", "Найдено поле 'likes_count': $rawValue (${rawValue?.javaClass?.simpleName})")
                                likesCount = when (rawValue) {
                                    is Long -> rawValue.toInt()
                                    is Int -> rawValue
                                    is Double -> rawValue.toInt()
                                    else -> 0
                                }
                            } else {
                                Log.d("MY_PLACES_DEBUG", "⚠️ Поле с лайками не найдено в документе")
                            }

                            val place = PlaceInfo(
                                id = (data["id"] as? String ?: doc.id),
                                name = data["name"] as? String ?: "",
                                address = data["address"] as? String ?: "",
                                latitude = (data["latitude"] as? Double) ?: 0.0,
                                longitude = (data["longitude"] as? Double) ?: 0.0,
                                metroStation = data["metroStation"] as? String ?: "",
                                metroLine = data["metroLine"] as? String ?: "",
                                distanceToMetro = (data["distanceToMetro"] as? Double) ?: 0.0,
                                photoUrl = when {
                                    data.containsKey("photoUrl") -> data["photoUrl"] as? String ?: ""
                                    data.containsKey("photo_url") -> data["photo_url"] as? String ?: ""
                                    else -> ""
                                },
                                categories = (data["categories"] as? List<String>) ?: emptyList(),
                                likesCount = likesCount, // Используем вычисленное значение
                                hasFireIcon = data["hasFireIcon"] as? Boolean ?: data["fire_icon"] as? Boolean ?: false,
                                place_ofday = data["place_ofday"] as? Boolean ?: false,
                                uniqueId = data["unique_id"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                rarity = data["rarity"] as? String ?: "common",
                                createdAt = data["created_at"] as? com.google.firebase.Timestamp,
                                updatedAt = data["updated_at"] as? com.google.firebase.Timestamp
                            )

                            Log.d("MY_PLACES_DEBUG", "✅ Создан PlaceInfo: ${place.name}")
                            Log.d("MY_PLACES_DEBUG", "   likesCount = ${place.likesCount}")

                            detailsMap[placeId] = place
                        } else {
                            Log.e("MY_PLACES_DEBUG", "❌ data is null for ID: $placeId")
                        }
                    } else {
                        Log.e("MY_PLACES_DEBUG", "❌ Документ не существует для ID: $placeId")
                    }
                } catch (e: Exception) {
                    Log.e("MY_PLACES_DEBUG", "❌ Ошибка загрузки для placeId: $placeId", e)
                }
            }

            Log.d("MY_PLACES_DEBUG", "========== ИТОГИ ЗАГРУЗКИ ==========")
            Log.d("MY_PLACES_DEBUG", "Загружено деталей: ${detailsMap.size} из ${placeIds.size}")

            detailsMap.forEach { (placeId, place) ->
                Log.d("MY_PLACES_DEBUG", "  $placeId -> ${place.name} (лайков: ${place.likesCount})")
            }

            _placesDetails.value = detailsMap

            // Формируем комбинированный список
            val combined = _userPlaces.value.map { userPlace ->
                val placeInfo = detailsMap[userPlace.placeId]
                Log.d("MY_PLACES_DEBUG", "Комбинируем: userPlace.placeId=${userPlace.placeId} -> placeInfo=${placeInfo?.name} (лайков: ${placeInfo?.likesCount})")
                userPlace to placeInfo
            }
            _combinedPlaces.value = combined
            Log.d("MY_PLACES_DEBUG", "Итоговый combined список размер: ${combined.size}")

        } catch (e: Exception) {
            Log.e("MY_PLACES_DEBUG", "❌ Ошибка в loadPlacesDetails", e)
            _errorMessage.value = "Ошибка загрузки деталей мест: ${e.message}"
        } finally {
            _isLoading.value = false
            Log.d("MY_PLACES_DEBUG", "========== ЗАГРУЗКА ЗАВЕРШЕНА ==========")
        }
    }

    fun refresh() {
        loadUserPlaces()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}