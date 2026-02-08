package com.example.datingapp.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {  // ← БЕЗ @HiltViewModel

    // Создаем зависимости напрямую (БЕЗ @Inject)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Состояния для загрузки фото
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    // Загрузка данных пользователя
    private val _userData = MutableStateFlow<Map<String, Any>?>(null)
    val userData: StateFlow<Map<String, Any>?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Загрузить данные пользователя
    fun loadUserData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val document = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    if (document.exists()) {
                        val data = document.data ?: emptyMap()
                        _userData.value = data

                        // Извлекаем URL фото профиля
                        val imageUrl = data["profileImageUrl"] as? String
                        _profileImageUrl.value = imageUrl
                    }
                }
            } catch (e: Exception) {
                // Логируем ошибку
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загрузить фото профиля
    fun uploadProfileImage(uri: Uri) {
        _isUploadingImage.value = true
        _uploadError.value = null

        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uploadError.value = "Пользователь не авторизован"
                    return@launch
                }

                val userId = currentUser.uid

                // 1. Загружаем в Storage
                val storageRef = storage.reference.child("profile_images/$userId.jpg")
                val uploadTask = storageRef.putFile(uri).await()

                // 2. Получаем URL
                val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await()
                    ?: throw Exception("Не удалось получить URL изображения")

                val imageUrl = downloadUrl.toString()

                // 3. Сохраняем URL в Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("profileImageUrl", imageUrl)
                    .await()

                _profileImageUrl.value = imageUrl

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                currentData["profileImageUrl"] = imageUrl
                _userData.value = currentData

            } catch (e: Exception) {
                _uploadError.value = e.message ?: "Неизвестная ошибка"
                e.printStackTrace()
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun updateUserData(data: Map<String, Any?>) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    return@launch
                }

                val firestoreData = data.filterValues { it != null }
                    .mapValues { (_, value) -> value!! }

                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(firestoreData)
                    .await()

                val currentData = _userData.value?.toMutableMap() ?: mutableMapOf()
                firestoreData.forEach { (key, value) -> currentData[key] = value }
                _userData.value = currentData

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}