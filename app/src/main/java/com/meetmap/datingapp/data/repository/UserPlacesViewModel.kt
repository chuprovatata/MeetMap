package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.UserPlace
import com.example.datingapp.data.repository.UserPlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class UserPlacesViewModel @Inject constructor(
    private val userPlacesRepository: UserPlacesRepository
) : ViewModel() {

    private val _isLiking = MutableStateFlow(false)
    val isLiking: StateFlow<Boolean> = _isLiking.asStateFlow()

    private val _likeResult = MutableStateFlow<Result<UserPlace>?>(null)
    val likeResult: StateFlow<Result<UserPlace>?> = _likeResult.asStateFlow()

    private val _likedPlaces = MutableStateFlow<List<UserPlace>>(emptyList())
    val likedPlaces: StateFlow<List<UserPlace>> = _likedPlaces.asStateFlow()

    private val _isLoadingLikedPlaces = MutableStateFlow(false)
    val isLoadingLikedPlaces: StateFlow<Boolean> = _isLoadingLikedPlaces.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun likePlace(placeId: String, source: String = "places_of_day") {
        viewModelScope.launch {
            _isLiking.value = true
            _errorMessage.value = null

            try {
                val result = userPlacesRepository.likePlace(placeId, source)
                Log.d("LIKE_DEBUG", "ViewModel like result: $result")

                _likeResult.value = result

                if (result.isSuccess) {
                    Log.d("LIKE_DEBUG", "Like successful, loading liked places")
                    loadLikedPlaces()
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("LIKE_DEBUG", "Like failed", error)
                    _errorMessage.value = error?.message ?: "Ошибка при добавлении в избранное"
                }
            } catch (e: Exception) {
                Log.e("LIKE_DEBUG", "Exception in likePlace", e)
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLiking.value = false
            }
        }
    }

    fun unlikePlace(placeId: String) {
        viewModelScope.launch {
            _isLiking.value = true
            _errorMessage.value = null

            try {
                val result = userPlacesRepository.unlikePlace(placeId)
                if (result.isSuccess) {
                    loadLikedPlaces()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Ошибка при удалении из избранного"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLiking.value = false
            }
        }
    }

    fun loadLikedPlaces() {
        viewModelScope.launch {
            _isLoadingLikedPlaces.value = true

            try {
                val result = userPlacesRepository.getUserLikedPlaces()
                if (result.isSuccess) {
                    _likedPlaces.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Ошибка загрузки избранных мест"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoadingLikedPlaces.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearLikeResult() {
        _likeResult.value = null
    }
}