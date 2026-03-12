package com.example.datingapp.viewmodels

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.R
import com.example.datingapp.data.models.PlaceInfo
import com.example.datingapp.data.models.UserPlace
import com.example.datingapp.data.repository.FriendStatus
import com.example.datingapp.data.repository.MyUser
import com.example.datingapp.data.repository.UserPlacesRepository
import com.example.datingapp.data.repository.UserRepository
import com.example.datingapp.utils.CloudImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPlacesRepository: UserPlacesRepository
) : ViewModel() {
    private val _userPlacesCount = MutableStateFlow(0)
    val userPlacesCount: StateFlow<Int> = _userPlacesCount.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    private val _userData = MutableStateFlow<Map<String, Any>?>(null)
    val userData: StateFlow<Map<String, Any>?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dataLoadError = MutableStateFlow<String?>(null)
    val dataLoadError: StateFlow<String?> = _dataLoadError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _myUser = MutableStateFlow<MyUser?>(null)
    val myUser: StateFlow<MyUser?> = _myUser.asStateFlow()

    private val _otherUser = MutableStateFlow<MyUser?>(null)
    val otherUser: StateFlow<MyUser?> = _otherUser.asStateFlow()

    private val _friendsList = MutableStateFlow<List<MyUser>>(emptyList())
    val friendsList: StateFlow<List<MyUser>> = _friendsList.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<MyUser>>(emptyList())
    val incomingRequests: StateFlow<List<MyUser>> = _incomingRequests.asStateFlow()

    private val _deniedList = MutableStateFlow<List<MyUser>>(emptyList())
    val deniedList: StateFlow<List<MyUser>> = _deniedList.asStateFlow()

    private val _outgoingRequests = MutableStateFlow<List<MyUser>>(emptyList())
    val outgoingRequests: StateFlow<List<MyUser>> = _outgoingRequests.asStateFlow()

    private val _mutualFriends = MutableStateFlow<List<MyUser>>(emptyList())
    val mutualFriends: StateFlow<List<MyUser>> = _mutualFriends.asStateFlow()
    private val _mutualPlaces = MutableStateFlow<List<PlaceInfo>>(emptyList())
    val mutualPlaces: StateFlow<List<PlaceInfo>> = _mutualPlaces.asStateFlow()
    val PROFILE_PLACEHOLDER = R.drawable.picture_defaullt_profile

    init {
        loadUserData()
        loadMyUser()
        loadUserPlacesCount()
    }
    private val _isUploadingFavoritePlace = MutableStateFlow(false)
    val isUploadingFavoritePlace: StateFlow<Boolean> = _isUploadingFavoritePlace.asStateFlow()

    private val _favoritePlacePhotoUrl = MutableStateFlow<String?>(null)
    val favoritePlacePhotoUrl: StateFlow<String?> = _favoritePlacePhotoUrl.asStateFlow()
    private val _compatibilityPercent = MutableStateFlow(0)
    val compatibilityPercent: StateFlow<Int> = _compatibilityPercent.asStateFlow()
    fun refreshUserPlacesCount() {
        viewModelScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            val count = userPlacesRepository.getUserPlacesCount(userId)
            _userPlacesCount.value = count
            Log.d("🔥🔥🔥", "ОБНОВЛЕНО количество мест: $count")
        }
    }

    fun loadCompatibility(otherUserId: String) {
        viewModelScope.launch {
            try {
                val myPlaces = userPlacesRepository.getUserLikedPlaces().getOrNull() ?: emptyList()
                val otherPlaces = userPlacesRepository.getUserLikedPlaces(otherUserId).getOrNull() ?: emptyList()

                // Загружаем детали мест для категорий
                val myPlaceIds = myPlaces.map { it.placeId }
                val otherPlaceIds = otherPlaces.map { it.placeId }

                val myPlaceDetails = if (myPlaceIds.isNotEmpty()) {
                    userPlacesRepository.getPlacesDetails(myPlaceIds).getOrNull() ?: emptyList()
                } else emptyList()

                val otherPlaceDetails = if (otherPlaceIds.isNotEmpty()) {
                    userPlacesRepository.getPlacesDetails(otherPlaceIds).getOrNull() ?: emptyList()
                } else emptyList()

                // Получаем количество общих друзей
                val mutualFriendsCount = userRepository.getMutualFriendsCount(otherUserId)

                // Получаем общее количество друзей текущего пользователя
                val currentUser = _myUser.value
                val totalFriendsCount = currentUser?.friends?.count { it.value.status == "friend" }?.coerceAtMost(10) ?: 0

                // Используем улучшенную формулу со всеми параметрами
                val percent = calculateEnhancedCompatibility(
                    myPlaces = myPlaces,
                    otherUserPlaces = otherPlaces,
                    myPlaceDetails = myPlaceDetails,
                    otherUserPlaceDetails = otherPlaceDetails,
                    mutualFriendsCount = mutualFriendsCount,
                    totalFriendsCount = totalFriendsCount
                )

                _compatibilityPercent.value = percent

                Log.d("COMPATIBILITY", "Compatibility with $otherUserId: $percent%")
            } catch (e: Exception) {
                Log.e("COMPATIBILITY", "Error calculating compatibility", e)
                _compatibilityPercent.value = 0
            }
        }
    }
    fun loadUserPlacesCount() {
        viewModelScope.launch {
            // Ждем загрузки пользователя, если нужно
            val userId = getCurrentUserId()
            if (userId == null) {
                delay(500) // Даем время на загрузку
                loadUserPlacesCount() // Пробуем снова
                return@launch
            }
            val count = userPlacesRepository.getUserPlacesCount(userId)
            _userPlacesCount.value = count
            Log.d("PlacesCount", "✅ Итоговое количество: $count")
        }
    }

    fun uploadFavoritePlacePhoto(uri: Uri, contentResolver: ContentResolver) {
        _isUploadingFavoritePlace.value = true
        _uploadError.value = null

        viewModelScope.launch {
            try {
                val imageUrl = userRepository.uploadFavoritePlaceImage(uri, contentResolver)

                _favoritePlacePhotoUrl.value = imageUrl

                updateUserField("favoritePlacePhoto", imageUrl)

                _saveSuccess.value = true
                Log.d("UserViewModel", "Favorite place photo uploaded: $imageUrl")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error uploading favorite place photo", e)
                _uploadError.value = e.message ?: "Ошибка загрузки фото места"
            } finally {
                _isUploadingFavoritePlace.value = false
            }
        }
    }
    fun deleteFavoritePlacePhoto() {
        viewModelScope.launch {
            _isUploadingFavoritePlace.value = true
            _uploadError.value = null

            try {
                userRepository.updateUserData(mapOf("favoritePlacePhoto" to com.google.firebase.firestore.FieldValue.delete()))

                _favoritePlacePhotoUrl.value = null

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                currentData.remove("favoritePlacePhoto")
                _userData.value = currentData

                _saveSuccess.value = true
                Log.d("UserViewModel", "Favorite place photo deleted")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error deleting favorite place photo", e)
                _uploadError.value = e.message ?: "Ошибка удаления фото места"
            } finally {
                _isUploadingFavoritePlace.value = false
            }
        }
    }
    suspend fun getFavoritePlaceImageUrl(imageUrl: String?): Any {
        return if (imageUrl.isNullOrBlank() || imageUrl == CloudImageUtils.NO_PICTURE_URL) {
            Log.d("UserViewModel", "No favorite place image, using placeholder")
            R.drawable.picture_museum_background
        } else {
            Log.d("UserViewModel", "Processing favorite place URL: $imageUrl")
            imageUrl
        }
    }
    fun getCurrentUserId(): String? {
        return _myUser.value?.uid
    }

    fun loadUserData() {
        _isLoading.value = true
        _dataLoadError.value = null
        _saveSuccess.value = false

        viewModelScope.launch {
            try {
                val data = userRepository.getUserData()
                _userData.value = data

                val imageUrl = userRepository.getUserProfileImageUrl()
                _profileImageUrl.value = imageUrl

                val favoritePhoto = data["favoritePlacePhoto"] as? String
                _favoritePlacePhotoUrl.value = favoritePhoto

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading user data", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки данных"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfileImage(uri: Uri, contentResolver: ContentResolver) {
        _isUploadingImage.value = true
        _uploadError.value = null

        viewModelScope.launch {
            try {
                val imageUrl = userRepository.uploadProfileImage(uri, contentResolver)

                _profileImageUrl.value = imageUrl

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                currentData["profileImageUrl"] = imageUrl
                _userData.value = currentData

                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e("UserViewModel", "Upload error", e)
                _uploadError.value = e.message ?: "Неизвестная ошибка при загрузке фото"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }
    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _isUploadingImage.value = true
            _uploadError.value = null

            try {
                val noPhotoUrl = CloudImageUtils.NO_PICTURE_URL

                userRepository.updateUserData(mapOf("profileImageUrl" to noPhotoUrl))

                _profileImageUrl.value = noPhotoUrl
                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                currentData["profileImageUrl"] = noPhotoUrl
                _userData.value = currentData

                _saveSuccess.value = true

                Log.d("UserViewModel", "Profile photo deleted successfully")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error deleting profile photo", e)
                _uploadError.value = e.message ?: "Ошибка удаления фото"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }
    suspend fun getProfileImageUrl(imageUrl: String?): Any {
        return if (imageUrl.isNullOrBlank() || imageUrl == CloudImageUtils.NO_PICTURE_URL) {
            Log.d("UserViewModel", "No profile image, using profile placeholder")
            PROFILE_PLACEHOLDER
        } else {
            Log.d("UserViewModel", "Processing profile URL: $imageUrl")
            imageUrl
        }
    }

    fun updateUserData(data: Map<String, Any?>) {
        val updateData = mutableMapOf<String, Any>()
        val deleteFields = mutableListOf<String>()

        data.forEach { (key, value) ->
            if (value != null) {
                updateData[key] = value
            } else {
                deleteFields.add(key)
            }
        }

        if (updateData.isEmpty() && deleteFields.isEmpty()) {
            Log.d("UserViewModel", "No data to update")
            return
        }

        _isSaving.value = true
        _saveError.value = null
        _saveSuccess.value = false

        viewModelScope.launch {
            try {
                if (updateData.isNotEmpty()) {
                    userRepository.updateUserData(updateData)
                }

                for (field in deleteFields) {
                    userRepository.updateUserData(mapOf(field to com.google.firebase.firestore.FieldValue.delete()))
                }

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                updateData.forEach { (key, value) -> currentData[key] = value }
                deleteFields.forEach { currentData.remove(it) }
                _userData.value = currentData

                if (updateData.containsKey("name") || updateData.containsKey("username") ||
                    updateData.containsKey("telegram") || updateData.containsKey("bio") ||
                    deleteFields.isNotEmpty()) {
                    loadMyUser()
                }

                _saveSuccess.value = true

                Log.d("UserViewModel", "User data updated successfully. Updated: $updateData, Deleted: $deleteFields")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user data", e)
                _saveError.value = e.message ?: "Ошибка обновления данных"
            } finally {
                _isSaving.value = false
            }
        }
    }
    fun deleteField(field: String) {
        updateUserData(mapOf(field to null))
    }

    fun updateUserField(field: String, value: Any?) {
        if (value == null) return
        updateUserData(mapOf(field to value))
    }

    suspend fun getProcessedImageUrl(originalUrl: String?): Any {
        return CloudImageUtils.getFixedImageUrl(originalUrl)
    }

    fun clearUploadError() {
        _uploadError.value = null
    }

    fun clearDataLoadError() {
        _dataLoadError.value = null
    }

    fun clearSaveError() {
        _saveError.value = null
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    fun refreshUserData() {
        loadUserData()
        loadMyUser()
    }

    fun hasUnsavedChanges(currentData: Map<String, Any?>): Boolean {
        val originalData = _userData.value ?: return false

        return currentData.any { (key, value) ->
            when (val originalValue = originalData[key]) {
                is Long -> value?.toString()?.toLongOrNull() != originalValue
                else -> value != originalValue
            }
        }
    }

    fun loadMyUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _dataLoadError.value = null

            try {
                Log.d("UserViewModel", "Loading my user...")
                val user = userRepository.getCurrentUser()
                Log.d("UserViewModel", "Loaded user: $user")

                _myUser.value = user

                if (user == null) {
                    _dataLoadError.value = "Пользователь не найден в базе"
                } else {
                    _userData.value = mapOf(
                        "uid" to user.uid,
                        "name" to user.name,
                        "username" to user.username,
                        "email" to user.email,
                        "telegram" to user.telegram,
                        "age" to user.age,
                        "birthYear" to user.birthYear,
                        "bio" to user.bio,
                        "gender" to user.gender,
                        "university" to user.university,
                        "profileComplete" to user.profileComplete,
                        "targets" to user.targets,
                        "categories" to user.categories,
                        "friends" to user.friends
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading my user", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserById(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _dataLoadError.value = null

            try {
                Log.d("UserViewModel", "Loading user by ID: $userId")
                val user = userRepository.getUserById(userId)
                _otherUser.value = user

                if (user == null) {
                    _dataLoadError.value = "Пользователь не найден"
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading user by id", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки пользователя"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOtherUser() {
        _otherUser.value = null
    }

    suspend fun getUsersByFriendStatus(status: FriendStatus): List<MyUser> {
        val currentUser = _myUser.value ?: return emptyList()

        val friendIds = currentUser.friends
            .filter { it.value.status == status.value }
            .keys
            .toList()

        if (friendIds.isEmpty()) return emptyList()

        val friendsList = mutableListOf<MyUser>()
        for (friendId in friendIds) {
            val friend = userRepository.getUserById(friendId)
            if (friend != null) {
                friendsList.add(friend)
            }
        }

        return friendsList
    }

    fun loadAllFriendData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val freshUser = userRepository.getCurrentUser()
                _myUser.value = freshUser

                _friendsList.value = getUsersByFriendStatus(FriendStatus.FRIEND)
                _incomingRequests.value = getUsersByFriendStatus(FriendStatus.REQUEST)
                _deniedList.value = getUsersByFriendStatus(FriendStatus.DENY)
                _outgoingRequests.value = getUsersByFriendStatus(FriendStatus.MY_APPLICATION)

                Log.d("UserViewModel", "Все данные загружены: друзей=${_friendsList.value.size}, заявок=${_incomingRequests.value.size}")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Ошибка загрузки данных", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshFriendData() {
        loadAllFriendData()
    }

    fun updateUserById(userId: String, data: Map<String, Any?>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val firestoreData = data.filterValues { it != null }
                    .mapValues { (_, value) -> value!! }

                userRepository.updateUserById(userId, firestoreData)

                // Если обновляем текущего пользователя - обновляем и локальные данные
                if (userId == _myUser.value?.uid) {
                    loadMyUser()
                }

                // Если обновляем того, кого смотрим на экране
                if (userId == _otherUser.value?.uid) {
                    loadUserById(userId)
                }

                Log.d("UserViewModel", "User $userId updated")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user $userId", e)
                _dataLoadError.value = e.message ?: "Ошибка обновления"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFriendshipStatus(
        myUserId: String,
        friendId: String,
        newStatusForMe: FriendStatus,
        newStatusForFriend: FriendStatus
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("UserViewModel", "Updating friendship: me=$myUserId, friend=$friendId")
                Log.d("UserViewModel", "My new status: ${newStatusForMe.value}, Friend new status: ${newStatusForFriend.value}")

                // Обновляем статус у текущего пользователя по отношению к другу
                userRepository.updateFriendStatusForUser(myUserId, friendId, newStatusForMe.value)

                // Обновляем статус у друга по отношению к текущему пользователю
                userRepository.updateFriendStatusForUser(friendId, myUserId, newStatusForFriend.value)

                // Обновляем данные текущего пользователя
                loadMyUser()

                // Если это был экран друга, обновляем и его данные
                if (friendId == _otherUser.value?.uid) {
                    loadUserById(friendId)
                }

                Log.d("UserViewModel", "Friendship updated successfully")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating friendship", e)
                _dataLoadError.value = e.message ?: "Ошибка обновления статуса"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMutualFriends(otherUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("UserViewModel", "Loading mutual friends for user: $otherUserId")
                val friends = userRepository.getMutualFriends(otherUserId)
                _mutualFriends.value = friends
                Log.d("UserViewModel", "Found ${friends.size} mutual friends")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading mutual friends", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки общих друзей"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun loadMutualPlaces(otherUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Loading mutual places for user: $otherUserId")
                val places = userPlacesRepository.getMutualPlaces(otherUserId)
                _mutualPlaces.value = places
                Log.d("UserViewModel", "Found ${places.size} mutual places")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading mutual places", e)
                _mutualPlaces.value = emptyList()
            }
        }
    }

    /**
     * Улучшенная формула расчета совместимости
     * Учитывает: общие места (70%), общие категории мест (20%), общих друзей (10%)
     */
    fun calculateEnhancedCompatibility(
        myPlaces: List<UserPlace>,
        otherUserPlaces: List<UserPlace>,
        myPlaceDetails: List<PlaceInfo>,
        otherUserPlaceDetails: List<PlaceInfo>,
        mutualFriendsCount: Int,
        totalFriendsCount: Int
    ): Int {
        if (myPlaces.isEmpty() || otherUserPlaces.isEmpty()) return 0

        // 1. Общие места (70% веса)
        val myPlaceIds = myPlaces.map { it.placeId }.toSet()
        val otherPlaceIds = otherUserPlaces.map { it.placeId }.toSet()

        val mutualPlacesCount = myPlaceIds.intersect(otherPlaceIds).size
        val totalUniquePlaces = (myPlaceIds + otherPlaceIds).size

        val placesScore = if (totalUniquePlaces > 0) {
            (mutualPlacesCount * 100 / totalUniquePlaces) * 0.7
        } else 0.0

        // 2. Общие категории мест (20% веса)
        val myCategories = myPlaceDetails.flatMap { it.categories }.toSet()
        val otherCategories = otherUserPlaceDetails.flatMap { it.categories }.toSet()

        val mutualCategoriesCount = myCategories.intersect(otherCategories).size
        val totalUniqueCategories = (myCategories + otherCategories).size

        val categoriesScore = if (totalUniqueCategories > 0) {
            (mutualCategoriesCount * 100 / totalUniqueCategories) * 0.2
        } else 0.0

        // 3. Общие друзья (10% веса)
        val friendsScore = if (totalFriendsCount > 0) {
            (mutualFriendsCount * 100 / totalFriendsCount) * 0.1
        } else 0.0

        // Суммируем и округляем
        return (placesScore + categoriesScore + friendsScore).toInt().coerceIn(0, 100)
    }

    fun clearMutualFriends() {
        _mutualFriends.value = emptyList()
    }
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _myUser.value = null
                _userData.value = null
                _profileImageUrl.value = null
                _favoritePlacePhotoUrl.value = null
                _friendsList.value = emptyList()
                _incomingRequests.value = emptyList()
                _outgoingRequests.value = emptyList()
                _deniedList.value = emptyList()
                _mutualFriends.value = emptyList()
                _otherUser.value = null

                _isLoading.value = false
                _isSaving.value = false
                _saveSuccess.value = false
                _saveError.value = null
                _uploadError.value = null
                _dataLoadError.value = null

                userRepository.logout()

                Log.d("UserViewModel", "User logged out successfully")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error during logout", e)
                _dataLoadError.value = e.message ?: "Ошибка при выходе"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Добавить новые поля
    private val _recommendedUsers = MutableStateFlow<List<MyUser>>(emptyList())
    val recommendedUsers: StateFlow<List<MyUser>> = _recommendedUsers.asStateFlow()

    private val _usersCompatibility = MutableStateFlow<Map<String, Int>>(emptyMap())
    val usersCompatibility: StateFlow<Map<String, Int>> = _usersCompatibility.asStateFlow()

    fun loadRecommendedUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("UserViewModel", "=== НАЧАЛО ЗАГРУЗКИ РЕКОМЕНДАЦИЙ С УЛУЧШЕННОЙ ФОРМУЛОЙ ===")

                val currentUser = _myUser.value
                if (currentUser == null) {
                    loadMyUser()
                    delay(500)
                }

                val freshCurrentUser = _myUser.value ?: return@launch
                Log.d("UserViewModel", "Текущий пользователь: ${freshCurrentUser.name} (${freshCurrentUser.uid})")

                // Получаем всех пользователей
                val allUsers = userRepository.getAllUsers()
                Log.d("UserViewModel", "Всего пользователей в БД: ${allUsers.size}")

                // Исключаем текущего пользователя и тех, с кем уже есть взаимодействие
                val excludedUserIds = freshCurrentUser.friends.keys.toSet() + freshCurrentUser.uid
                val potentialUsers = allUsers.filter { it.uid !in excludedUserIds }
                Log.d("UserViewModel", "Потенциальных пользователей после исключения: ${potentialUsers.size}")

                // Загружаем места текущего пользователя
                val myPlacesResult = userPlacesRepository.getUserLikedPlaces()
                val myPlaces = if (myPlacesResult.isSuccess) myPlacesResult.getOrNull() ?: emptyList() else emptyList()
                Log.d("UserViewModel", "Мои лайкнутые места: ${myPlaces.size}")

                if (myPlaces.isEmpty()) {
                    Log.d("UserViewModel", "У текущего пользователя нет мест, рекомендации не загружаются")
                    _recommendedUsers.value = emptyList()
                    _usersCompatibility.value = emptyMap()
                    return@launch
                }

                // Загружаем детали мест текущего пользователя для категорий
                val myPlaceIds = myPlaces.map { it.placeId }
                val myPlaceDetailsResult = if (myPlaceIds.isNotEmpty()) {
                    userPlacesRepository.getPlacesDetails(myPlaceIds)
                } else {
                    Result.success(emptyList())
                }
                val myPlaceDetails = if (myPlaceDetailsResult.isSuccess) myPlaceDetailsResult.getOrNull() ?: emptyList() else emptyList()
                Log.d("UserViewModel", "Детали моих мест: ${myPlaceDetails.size}")

                // Общее количество друзей для нормализации (максимум 10)
                val totalFriendsCount = freshCurrentUser.friends.count { it.value.status == "friend" }.coerceAtMost(10)
                Log.d("UserViewModel", "Общее количество друзей (cap 10): $totalFriendsCount")

                val validUsers = mutableListOf<Pair<MyUser, Int>>()

                for (user in potentialUsers) {
                    Log.d("UserViewModel", "--- Обработка пользователя: ${user.name} (${user.uid}) ---")

                    // Загружаем места другого пользователя
                    val otherPlacesResult = userPlacesRepository.getUserLikedPlaces(user.uid)
                    val otherPlaces = if (otherPlacesResult.isSuccess) otherPlacesResult.getOrNull() ?: emptyList() else emptyList()
                    Log.d("UserViewModel", "Лайкнутые места пользователя: ${otherPlaces.size}")

                    if (otherPlaces.isNotEmpty()) {
                        // Загружаем детали мест другого пользователя
                        val otherPlaceIds = otherPlaces.map { it.placeId }
                        val otherPlaceDetailsResult = if (otherPlaceIds.isNotEmpty()) {
                            userPlacesRepository.getPlacesDetails(otherPlaceIds)
                        } else {
                            Result.success(emptyList())
                        }
                        val otherPlaceDetails = if (otherPlaceDetailsResult.isSuccess) otherPlaceDetailsResult.getOrNull() ?: emptyList() else emptyList()
                        Log.d("UserViewModel", "Детали мест пользователя: ${otherPlaceDetails.size}")

                        // Получаем количество общих друзей
                        val mutualFriendsCount = userRepository.getMutualFriendsCount(user.uid)
                        Log.d("UserViewModel", "Общих друзей: $mutualFriendsCount")

                        // ИСПОЛЬЗУЕМ УЛУЧШЕННУЮ ФОРМУЛУ (не временную)
                        val percent = calculateEnhancedCompatibility(
                            myPlaces = myPlaces,
                            otherUserPlaces = otherPlaces,
                            myPlaceDetails = myPlaceDetails,
                            otherUserPlaceDetails = otherPlaceDetails,
                            mutualFriendsCount = mutualFriendsCount,
                            totalFriendsCount = totalFriendsCount
                        )

                        Log.d("UserViewModel", "Процент совместимости (улучшенная формула): $percent%")

                        if (percent > 0) {
                            validUsers.add(user to percent)
                            Log.d("UserViewModel", "✅ ДОБАВЛЕН: ${user.name} с $percent%")
                        } else {
                            Log.d("UserViewModel", "❌ ПОЛЬЗОВАТЕЛЬ НЕ ДОБАВЛЕН (0%)")
                        }
                    } else {
                        Log.d("UserViewModel", "❌ У ПОЛЬЗОВАТЕЛЯ НЕТ ЛАЙКНУТЫХ МЕСТ")
                    }
                }

                // Сортируем по убыванию совместимости
                val sortedUsers = validUsers.sortedByDescending { it.second }

                Log.d("UserViewModel", "=== ИТОГ: найдено ${sortedUsers.size} пользователей с улучшенной формулой ===")
                sortedUsers.forEachIndexed { index, (user, percent) ->
                    Log.d("UserViewModel", "$index. ${user.name}: $percent%")
                }

                _recommendedUsers.value = sortedUsers.map { it.first }
                _usersCompatibility.value = sortedUsers.associate { it.first.uid to it.second }

            } catch (e: Exception) {
                Log.e("UserViewModel", "Ошибка загрузки рекомендаций", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки рекомендаций"
            } finally {
                _isLoading.value = false
                Log.d("UserViewModel", "=== ЗАГРУЗКА ЗАВЕРШЕНА ===")
            }
        }
    }

    fun refreshRecommendedUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("PeopleOfDay", "🔄 ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ РЕКОМЕНДАЦИЙ")

                // ШАГ 1: Загружаем СВЕЖИЕ данные текущего пользователя (НЕ ИЗ КЕША!)
                val freshCurrentUser = userRepository.getCurrentUser()

                if (freshCurrentUser == null) {
                    Log.e("PeopleOfDay", "❌ Не удалось загрузить текущего пользователя")
                    _recommendedUsers.value = emptyList()
                    _usersCompatibility.value = emptyMap()
                    return@launch
                }

                // Обновляем кеш в ViewModel
                _myUser.value = freshCurrentUser
                Log.d("PeopleOfDay", "✅ Загружен свежий пользователь: ${freshCurrentUser.name}")
                Log.d("PeopleOfDay", "📊 Количество друзей: ${freshCurrentUser.friends.size}")

                // ШАГ 2: Получаем всех пользователей
                val allUsers = userRepository.getAllUsers()

                // ШАГ 3: Исключаем текущего и друзей
                val excludedUserIds = freshCurrentUser.friends.keys.toSet() + freshCurrentUser.uid
                val potentialUsers = allUsers.filter { it.uid !in excludedUserIds }

                Log.d("PeopleOfDay", "👥 Потенциальных пользователей: ${potentialUsers.size}")

                // ШАГ 4: Загружаем места текущего пользователя
                val myPlacesResult = userPlacesRepository.getUserLikedPlaces()
                val myPlaces = myPlacesResult.getOrNull() ?: emptyList()

                if (myPlaces.isEmpty()) {
                    Log.d("PeopleOfDay", "⚠️ У пользователя нет мест - пустые рекомендации")
                    _recommendedUsers.value = emptyList()
                    _usersCompatibility.value = emptyMap()
                    return@launch
                }

                // ШАГ 5: Загружаем детали мест для категорий
                val myPlaceIds = myPlaces.map { it.placeId }
                val myPlaceDetailsResult = userPlacesRepository.getPlacesDetails(myPlaceIds)
                val myPlaceDetails = myPlaceDetailsResult.getOrNull() ?: emptyList()

                // ШАГ 6: Общее количество друзей для нормализации
                val totalFriendsCount = freshCurrentUser.friends.count { it.value.status == "friend" }.coerceAtMost(10)

                val validUsers = mutableListOf<Pair<MyUser, Int>>()

                // ШАГ 7: Обрабатываем каждого потенциального пользователя
                for (user in potentialUsers) {
                    // Загружаем СВЕЖИЕ данные о местах пользователя
                    val otherPlacesResult = userPlacesRepository.getUserLikedPlaces(user.uid)
                    val otherPlaces = otherPlacesResult.getOrNull() ?: emptyList()

                    if (otherPlaces.isNotEmpty()) {
                        val otherPlaceIds = otherPlaces.map { it.placeId }
                        val otherPlaceDetailsResult = userPlacesRepository.getPlacesDetails(otherPlaceIds)
                        val otherPlaceDetails = otherPlaceDetailsResult.getOrNull() ?: emptyList()

                        // Загружаем СВЕЖИЕ данные об общих друзьях
                        val mutualFriendsCount = userRepository.getMutualFriendsCount(user.uid)

                        val percent = calculateEnhancedCompatibility(
                            myPlaces = myPlaces,
                            otherUserPlaces = otherPlaces,
                            myPlaceDetails = myPlaceDetails,
                            otherUserPlaceDetails = otherPlaceDetails,
                            mutualFriendsCount = mutualFriendsCount,
                            totalFriendsCount = totalFriendsCount
                        )

                        if (percent > 0) {
                            validUsers.add(user to percent)
                            Log.d("PeopleOfDay", "✅ ${user.name}: $percent%")
                        }
                    }
                }

                // ШАГ 8: Сортируем и сохраняем
                val sortedUsers = validUsers.sortedByDescending { it.second }

                _recommendedUsers.value = sortedUsers.map { it.first }
                _usersCompatibility.value = sortedUsers.associate { it.first.uid to it.second }

                Log.d("PeopleOfDay", "🎯 ИТОГО: ${sortedUsers.size} рекомендаций")

            } catch (e: Exception) {
                Log.e("PeopleOfDay", "❌ Ошибка: ${e.message}", e)
                _dataLoadError.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Удалить статус дружбы (отменить заявку)
     */
    fun removeFriendshipStatus(
        myUserId: String,
        friendId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("UserViewModel", "Removing friendship status: me=$myUserId, friend=$friendId")

                // Удаляем запись у текущего пользователя
                userRepository.removeFriendField(myUserId, friendId)

                // Удаляем запись у друга
                userRepository.removeFriendField(friendId, myUserId)

                // Обновляем данные текущего пользователя
                loadMyUser()

                // Если это был экран друга, обновляем и его данные
                if (friendId == _otherUser.value?.uid) {
                    loadUserById(friendId)
                }

                Log.d("UserViewModel", "Friendship status removed successfully")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error removing friendship status", e)
                _dataLoadError.value = e.message ?: "Ошибка отмены заявки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _friendshipStatus = MutableStateFlow<String?>(null)
    val friendshipStatus: StateFlow<String?> = _friendshipStatus.asStateFlow()

    private var friendshipListener: (() -> Unit)? = null

    /**
     * Подписаться на изменения статуса дружбы с конкретным пользователем
     */
    fun observeFriendshipStatus(friendId: String) {
        val currentUserId = _myUser.value?.uid ?: return

        // Отписываемся от предыдущего слушателя, если он был
        friendshipListener?.invoke()

        Log.d("UserViewModel", "Starting to observe friendship status with $friendId")

        friendshipListener = userRepository.observeFriendStatus(
            userId = currentUserId,
            friendId = friendId,
            onFriendStatusChanged = { status ->
                viewModelScope.launch {
                    Log.d("UserViewModel", "Friendship status updated: $status")
                    _friendshipStatus.value = status

                    // Обновляем данные пользователя, чтобы синхронизировать остальные поля
                    if (status != null) {
                        // Обновляем otherUser, если это тот же друг
                        if (_otherUser.value?.uid == friendId) {
                            loadUserById(friendId)
                        }

                        // Обновляем myUser, чтобы актуализировать friends map
                        loadMyUser()
                    }
                }
            }
        )
    }

    /**
     * Отписаться от изменений статуса дружбы
     */
    fun stopObservingFriendshipStatus() {
        Log.d("UserViewModel", "Stopping friendship status observation")
        friendshipListener?.invoke()
        friendshipListener = null
        _friendshipStatus.value = null
    }

    /**
     * ПРИНУДИТЕЛЬНО загрузить свежие данные о пользователе из БД (не из кеша)
     * Аналогично refreshRecommendedUsers()
     */
    fun forceLoadUserData(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "🔄 ПРИНУДИТЕЛЬНАЯ ЗАГРУЗКА пользователя $userId из БД")

                // Загружаем СВЕЖИЕ данные напрямую из репозитория
                val freshUser = userRepository.getUserById(userId)

                if (freshUser != null) {
                    // Обновляем otherUser
                    _otherUser.value = freshUser

                    Log.d("UserViewModel", "✅ Пользователь $userId загружен: ${freshUser.name}")
                    Log.d("UserViewModel", "📊 Статус дружбы: ${freshUser.friends}")
                } else {
                    Log.e("UserViewModel", "❌ Пользователь $userId не найден в БД")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ Ошибка при загрузке пользователя $userId", e)
            }
        }
    }

    // Добавьте в onCleared() для очистки ресурсов
    override fun onCleared() {
        super.onCleared()
        stopObservingFriendshipStatus()
    }
}