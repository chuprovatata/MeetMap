package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.data.models.UserPlace
import com.example.datingapp.data.models.AppUser
import com.example.datingapp.data.repository.UserPlacesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class MyPlaceDetailViewModel @Inject constructor(
    private val userPlacesRepository: UserPlacesRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _placeInfo = MutableStateFlow<PlaceInfo?>(null)
    val placeInfo: StateFlow<PlaceInfo?> = _placeInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLiking = MutableStateFlow(false)
    val isLiking: StateFlow<Boolean> = _isLiking.asStateFlow()

    private val _isLiked = MutableStateFlow(true)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Данные для блока с пользователями
    private val _usersCount = MutableStateFlow(0)
    val usersCount: StateFlow<Int> = _usersCount.asStateFlow()

    private val _users = MutableStateFlow<List<AppUser>>(emptyList())
    val users: StateFlow<List<AppUser>> = _users.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _hasMoreUsers = MutableStateFlow(false)
    val hasMoreUsers: StateFlow<Boolean> = _hasMoreUsers.asStateFlow()

    private var lastVisible: com.google.firebase.firestore.DocumentSnapshot? = null
    private val pageSize = 10

    fun loadPlaceDetails(placeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("MY_PLACE_DETAIL", "Loading place details for ID: $placeId")

                val doc = firestore.collection("places_info")
                    .document(placeId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val data = doc.data
                    if (data != null) {
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
                            likesCount = (data["likesCount"] as? Long)?.toInt() ?: (data["likes_count"] as? Long)?.toInt() ?: 0,
                            hasFireIcon = data["hasFireIcon"] as? Boolean ?: data["fire_icon"] as? Boolean ?: false,
                            place_ofday = data["place_ofday"] as? Boolean ?: false,
                            uniqueId = data["unique_id"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            rarity = data["rarity"] as? String ?: "common",
                            createdAt = data["created_at"] as? com.google.firebase.Timestamp,
                            updatedAt = data["updated_at"] as? com.google.firebase.Timestamp
                        )
                        _placeInfo.value = place
                        Log.d("MY_PLACE_DETAIL", "Successfully loaded place: ${place.name} with likes: ${place.likesCount}")

                        // Загружаем количество пользователей, добавивших это место
                        loadUsersCountForPlace(placeId)
                        // Загружаем первых пользователей
                        loadUsersForPlace(placeId)
                    }
                } else {
                    _errorMessage.value = "Место не найдено"
                    Log.e("MY_PLACE_DETAIL", "Document does not exist for ID: $placeId")
                }
            } catch (e: Exception) {
                Log.e("MY_PLACE_DETAIL", "Error loading place details", e)
                _errorMessage.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUsersCountForPlace(placeId: String) {
        viewModelScope.launch {
            try {
                Log.d("MY_PLACE_DETAIL", "Loading users count for place: $placeId")

                val currentUserId = auth.currentUser?.uid

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val thirtyDaysAgo = com.google.firebase.Timestamp(calendar.time)

                val snapshot = firestore.collection("user_places")
                    .whereEqualTo("placeId", placeId)
                    .whereEqualTo("status", "liked")
                    .whereGreaterThanOrEqualTo("addedTime", thirtyDaysAgo)
                    .get()
                    .await()

                // Считаем количество уникальных пользователей, исключая текущего
                val userIds = snapshot.documents.mapNotNull { doc ->
                    doc.getString("userId")
                }.toSet()

                val count = if (currentUserId != null) {
                    userIds.count { it != currentUserId }
                } else {
                    userIds.size
                }

                _usersCount.value = count
                Log.d("MY_PLACE_DETAIL", "Users count for place $placeId: $count")

            } catch (e: Exception) {
                Log.e("MY_PLACE_DETAIL", "Error loading users count", e)
                _usersCount.value = 0
            }
        }
    }

    fun loadUsersForPlace(placeId: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (!loadMore) {
                _isLoadingUsers.value = true
                lastVisible = null
            }

            try {
                val currentUserId = auth.currentUser?.uid
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val thirtyDaysAgo = com.google.firebase.Timestamp(calendar.time)

                var query = firestore.collection("user_places")
                    .whereEqualTo("placeId", placeId)
                    .whereEqualTo("status", "liked")
                    .whereGreaterThanOrEqualTo("addedTime", thirtyDaysAgo)
                    .orderBy("addedTime", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())

                if (loadMore && lastVisible != null) {
                    query = query.startAfter(lastVisible!!)
                }

                val snapshot = query.get().await()

                if (snapshot.documents.isNotEmpty()) {
                    lastVisible = snapshot.documents.last()
                    _hasMoreUsers.value = snapshot.documents.size == pageSize
                } else {
                    _hasMoreUsers.value = false
                }

                val userIds = snapshot.documents.mapNotNull { doc ->
                    doc.getString("userId")
                }.filter { it != currentUserId }
                    .distinct()

                if (userIds.isNotEmpty()) {
                    val users = mutableListOf<AppUser>()

                    userIds.chunked(10).forEach { chunk ->
                        val usersSnapshot = firestore.collection("users")
                            .whereIn("id", chunk)
                            .get()
                            .await()

                        usersSnapshot.documents.forEach { doc ->
                            val data = doc.data
                            if (data != null) {
                                try {
                                    val user = AppUser(
                                        id = data["id"] as? String ?: doc.id,
                                        name = data["name"] as? String ?: "",
                                        username = data["username"] as? String ?: "",
                                        email = data["email"] as? String ?: "",
                                        profileImageUrl = data["profileImageUrl"] as? String ?: "",
                                        bio = data["bio"] as? String ?: "",
                                        gender = data["gender"] as? String ?: "",
                                        age = (data["age"] as? Long)?.toInt() ?: 0,
                                        birthYear = (data["birthYear"] as? Long)?.toInt(),
                                        university = data["university"] as? String ?: "",
                                        targets = (data["targets"] as? List<Int>) ?: emptyList(),
                                        categories = (data["categories"] as? List<Int>) ?: emptyList(),
                                        profileComplete = data["profileComplete"] as? Boolean ?: false
                                    )
                                    users.add(user)
                                } catch (e: Exception) {
                                    Log.e("MY_PLACE_DETAIL", "Error parsing user data", e)
                                }
                            }
                        }
                    }

                    if (loadMore) {
                        _users.value = _users.value + users
                    } else {
                        _users.value = users
                    }
                } else {
                    if (!loadMore) {
                        _users.value = emptyList()
                    }
                }

            } catch (e: Exception) {
                Log.e("MY_PLACE_DETAIL", "Error loading users for place", e)
            } finally {
                _isLoadingUsers.value = false
            }
        }
    }

    fun loadMoreUsers(placeId: String) {
        if (_hasMoreUsers.value && !_isLoadingUsers.value) {
            loadUsersForPlace(placeId, loadMore = true)
        }
    }

    private suspend fun updatePlaceLikesCount(placeId: String, increment: Boolean) {
        try {
            val placeRef = firestore.collection("places_info").document(placeId)

            if (increment) {
                // Увеличиваем счетчик лайков на 1
                placeRef.update("likesCount", FieldValue.increment(1)).await()
                Log.d("MY_PLACE_DETAIL", "Incremented likesCount for place $placeId")
            } else {
                // Уменьшаем счетчик лайков на 1
                placeRef.update("likesCount", FieldValue.increment(-1)).await()
                Log.d("MY_PLACE_DETAIL", "Decremented likesCount for place $placeId")
            }

            // Обновляем локальное состояние PlaceInfo
            _placeInfo.value = _placeInfo.value?.copy(
                likesCount = _placeInfo.value!!.likesCount + (if (increment) 1 else -1)
            )

        } catch (e: Exception) {
            Log.e("MY_PLACE_DETAIL", "Error updating likesCount", e)
        }
    }

    fun toggleLike(placeId: String) {
        viewModelScope.launch {
            _isLiking.value = true
            _errorMessage.value = null

            try {
                if (_isLiked.value) {
                    // Сейчас в избранном -> удаляем
                    val result = userPlacesRepository.unlikePlace(placeId)
                    if (result.isSuccess) {
                        _isLiked.value = false
                        // Уменьшаем счетчик лайков
                        updatePlaceLikesCount(placeId, false)
                        Log.d("MY_PLACE_DETAIL", "Successfully unliked place")
                        // Обновляем счетчик пользователей
                        loadUsersCountForPlace(placeId)
                        // Перезагружаем список пользователей
                        loadUsersForPlace(placeId)
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Ошибка при удалении из избранного"
                    }
                } else {
                    // Сейчас не в избранном -> добавляем
                    val result = userPlacesRepository.likePlace(placeId, "my_places")
                    if (result.isSuccess) {
                        _isLiked.value = true
                        // Увеличиваем счетчик лайков
                        updatePlaceLikesCount(placeId, true)
                        Log.d("MY_PLACE_DETAIL", "Successfully liked place")
                        // Обновляем счетчик пользователей
                        loadUsersCountForPlace(placeId)
                        // Перезагружаем список пользователей
                        loadUsersForPlace(placeId)
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Ошибка при добавлении в избранное"
                    }
                }
            } catch (e: Exception) {
                Log.e("MY_PLACE_DETAIL", "Error toggling like", e)
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLiking.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}