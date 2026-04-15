package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.AppFeedback
import com.example.datingapp.data.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _hasSubmittedToday = MutableStateFlow(false)
    val hasSubmittedToday: StateFlow<Boolean> = _hasSubmittedToday.asStateFlow()

    fun checkIfAlreadySubmitted() {
        viewModelScope.launch {
            val userId = feedbackRepository.getCurrentUserId()
            if (userId == null) {
                _hasSubmittedToday.value = false
                return@launch
            }

            val today = getTodayDateString()
            val result = feedbackRepository.hasUserSubmittedToday(userId, today)
            _hasSubmittedToday.value = result
            Log.d("FeedbackViewModel", "hasSubmittedToday: $result for user $userId")
        }
    }

    suspend fun checkIfAlreadySubmittedSync(): Boolean {
        val userId = feedbackRepository.getCurrentUserId() ?: return false
        val today = getTodayDateString()
        return feedbackRepository.hasUserSubmittedToday(userId, today)
    }

    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun savePlacesOfDayFeedback(
        rating: Int,
        selectedOptionIndex: Int,
        wantMoreCategories: List<String>,
        source: String,
        onComplete: () -> Unit
    ) {
        saveFeedback(
            action = {
                feedbackRepository.savePlacesOfDayFeedback(
                    rating = rating,
                    selectedOptionIndex = selectedOptionIndex,
                    wantMoreCategories = wantMoreCategories,
                    source = source
                )
            },
            onComplete = {
                _hasSubmittedToday.value = true
                onComplete()
            }
        )
    }

    fun savePlaceAddedFeedback(
        placeId: String,
        placeName: String,
        heardAboutOption: Int,
        onComplete: () -> Unit = {}
    ) {
        saveFeedback(
            action = {
                feedbackRepository.savePlaceAddedFeedback(
                    placeId = placeId,
                    placeName = placeName,
                    heardAboutOption = heardAboutOption
                )
            },
            onComplete = onComplete
        )
    }

    fun savePlaceDeletedFeedback(
        placeId: String,
        placeName: String,
        deletedReasonOption: Int,
        onComplete: () -> Unit = {}
    ) {
        saveFeedback(
            action = {
                feedbackRepository.savePlaceDeletedFeedback(
                    placeId = placeId,
                    placeName = placeName,
                    deletedReasonOption = deletedReasonOption
                )
            },
            onComplete = onComplete
        )
    }

    private fun saveFeedback(
        action: suspend () -> Result<AppFeedback>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            _saveSuccess.value = false

            try {
                val result = action()
                if (result.isSuccess) {
                    _saveSuccess.value = true
                    onComplete()
                } else {
                    _saveError.value = result.exceptionOrNull()?.message ?: "Ошибка сохранения"
                }
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Ошибка сохранения"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearState() {
        _saveError.value = null
        _saveSuccess.value = false
    }
}