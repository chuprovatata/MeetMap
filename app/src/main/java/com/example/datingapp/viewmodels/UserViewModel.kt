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
    }
    private val _isUploadingFavoritePlace = MutableStateFlow(false)
    val isUploadingFavoritePlace: StateFlow<Boolean> = _isUploadingFavoritePlace.asStateFlow()

    private val _favoritePlacePhotoUrl = MutableStateFlow<String?>(null)
    val favoritePlacePhotoUrl: StateFlow<String?> = _favoritePlacePhotoUrl.asStateFlow()
    private val _compatibilityPercent = MutableStateFlow(0)
    val compatibilityPercent: StateFlow<Int> = _compatibilityPercent.asStateFlow()

    fun loadCompatibility(otherUserId: String) {
        viewModelScope.launch {
            try {
                val myPlaces = userPlacesRepository.getUserLikedPlaces().getOrNull() ?: emptyList()
                val otherPlaces = userPlacesRepository.getUserLikedPlaces(otherUserId).getOrNull() ?: emptyList()

                val percent = calculateCompatibilityPercentage(myPlaces, otherPlaces)
                _compatibilityPercent.value = percent

                Log.d("COMPATIBILITY", "Compatibility with $otherUserId: $percent%")
            } catch (e: Exception) {
                Log.e("COMPATIBILITY", "Error calculating compatibility", e)
                _compatibilityPercent.value = 0
            }
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
        myId: String,
        friendId: String,
        newStatusForMe: FriendStatus,
        newStatusForFriend: FriendStatus
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("UserViewModel", "Updating friendship: me=$myId, friend=$friendId")

                userRepository.updateFriendStatusForUser(myId, friendId, newStatusForMe.value)
                userRepository.updateFriendStatusForUser(friendId, myId, newStatusForFriend.value)

                loadAllFriendData()

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
    fun calculateCompatibilityPercentage(
        myPlaces: List<UserPlace>,
        otherUserPlaces: List<UserPlace>
    ): Int {
        if (myPlaces.isEmpty() || otherUserPlaces.isEmpty()) return 0

        val myPlaceIds = myPlaces.map { it.placeId }.toSet()
        val otherPlaceIds = otherUserPlaces.map { it.placeId }.toSet()

        val mutualCount = myPlaceIds.intersect(otherPlaceIds).size
        val totalUniquePlaces = (myPlaceIds + otherPlaceIds).size

        return (mutualCount * 100 / totalUniquePlaces).coerceIn(0, 100)
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
}