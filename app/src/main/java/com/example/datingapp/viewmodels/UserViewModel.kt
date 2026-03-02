package com.example.datingapp.viewmodels

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: UserRepository
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

    init {
        loadUserData()
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

    fun updateUserData(data: Map<String, Any?>) {
        val firestoreData = data.filterValues { it != null }
            .mapValues { (_, value) -> value!! }

        if (firestoreData.isEmpty()) {
            Log.d("UserViewModel", "No data to update")
            return
        }

        _isSaving.value = true
        _saveError.value = null
        _saveSuccess.value = false

        viewModelScope.launch {
            try {
                userRepository.updateUserData(firestoreData)

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                firestoreData.forEach { (key, value) -> currentData[key] = value }
                _userData.value = currentData

                _saveSuccess.value = true

                Log.d("UserViewModel", "User data updated successfully: $firestoreData")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user data", e)
                _saveError.value = e.message ?: "Ошибка обновления данных"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Обновить одно поле пользователя
     */
    fun updateUserField(field: String, value: Any?) {
        if (value == null) return

        updateUserData(mapOf(field to value))
    }

    /**
     * Получить обработанный URL изображения для отображения
     */
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
    }

    /**
     * Проверить, есть ли несохраненные изменения
     */
    fun hasUnsavedChanges(currentData: Map<String, Any?>): Boolean {
        val originalData = _userData.value ?: return false

        return currentData.any { (key, value) ->
            when (val originalValue = originalData[key]) {
                is Long -> value?.toString()?.toLongOrNull() != originalValue
                else -> value != originalValue
            }
        }
    }
}