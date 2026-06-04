package com.meetmap.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.repository.EventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _approvedEvents = MutableStateFlow<List<EventInfo>>(emptyList())
    val approvedEvents: StateFlow<List<EventInfo>> = _approvedEvents.asStateFlow()

    private val _myEvents = MutableStateFlow<List<EventInfo>>(emptyList())
    val myEvents: StateFlow<List<EventInfo>> = _myEvents.asStateFlow()

    private val _myUnpublishedEvents = MutableStateFlow<List<EventInfo>>(emptyList())
    val myUnpublishedEvents: StateFlow<List<EventInfo>> = _myUnpublishedEvents.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            _currentUserId.value = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            val approvedResult = eventsRepository.getApprovedEvents()
            val myResult = eventsRepository.getMyEvents()
            val unpublishedResult = eventsRepository.getMyUnpublishedEvents()

            approvedResult
                .onSuccess { events ->
                    _approvedEvents.value = events
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось загрузить мероприятия"
                }

            myResult
                .onSuccess { events ->
                    _myEvents.value = events
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось загрузить мои мероприятия"
                }

            unpublishedResult
                .onSuccess { events ->
                    _myUnpublishedEvents.value = events
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось загрузить черновики"
                }

            _isLoading.value = false
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}