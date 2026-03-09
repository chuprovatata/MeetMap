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
            onComplete = onComplete
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