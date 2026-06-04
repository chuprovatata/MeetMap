package com.meetmap.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meetmap.datingapp.data.models.EventInfo
import com.meetmap.datingapp.data.models.EventParticipant
import com.meetmap.datingapp.data.models.EventStatus
import com.meetmap.datingapp.data.repository.EventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _event = MutableStateFlow<EventInfo?>(null)
    val event: StateFlow<EventInfo?> = _event.asStateFlow()

    private val _isGoing = MutableStateFlow(false)
    val isGoing: StateFlow<Boolean> = _isGoing.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()

    private val _friendsGoing = MutableStateFlow<List<EventParticipant>>(emptyList())
    val friendsGoing: StateFlow<List<EventParticipant>> = _friendsGoing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            eventsRepository.getEventById(eventId)
                .onSuccess { loadedEvent ->
                    _event.value = loadedEvent

                    val currentUserId = auth.currentUser?.uid
                    _isOwner.value = loadedEvent.createdByUserId == currentUserId

                    if (!_isOwner.value && loadedEvent.status != EventStatus.ARCHIVE.value) {
                        eventsRepository.isCurrentUserGoingToEvent(eventId)
                            .onSuccess { going ->
                                _isGoing.value = going
                            }
                    } else {
                        _isGoing.value = false
                    }

                    loadFriendsGoing(eventId)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось загрузить мероприятие"
                }

            _isLoading.value = false
        }
    }

    fun toggleGoing() {
        val currentEvent = _event.value ?: return

        if (_isOwner.value) {
            // TODO следующий этап: переход на экран редактирования мероприятия
            return
        }

        if (currentEvent.status == EventStatus.ARCHIVE.value) {
            _errorMessage.value = "Архивное мероприятие нельзя добавить в мои"
            return
        }

        viewModelScope.launch {
            _isActionLoading.value = true
            _errorMessage.value = null

            val result = if (_isGoing.value) {
                eventsRepository.removeCurrentUserFromEvent(currentEvent.id)
            } else {
                eventsRepository.addCurrentUserToEvent(currentEvent)
            }

            result
                .onSuccess {
                    _isGoing.value = !_isGoing.value
                    loadFriendsGoing(currentEvent.id)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось обновить мероприятие"
                }

            _isActionLoading.value = false
        }
    }

    private fun loadFriendsGoing(eventId: String) {
        viewModelScope.launch {
            eventsRepository.getFriendsGoingToEvent(eventId)
                .onSuccess { friends ->
                    _friendsGoing.value = friends
                }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun moveEventToDraft(
        onSuccess: (String) -> Unit
    ) {
        val currentEvent = _event.value ?: return

        if (!_isOwner.value) {
            _errorMessage.value = "Редактировать может только создатель мероприятия"
            return
        }

        viewModelScope.launch {
            _isActionLoading.value = true
            _errorMessage.value = null

            eventsRepository.moveEventToDraft(currentEvent.id)
                .onSuccess {
                    _event.value = currentEvent.copy(status = EventStatus.DRAFT.value)
                    onSuccess(currentEvent.id)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Не удалось открыть редактирование"
                }

            _isActionLoading.value = false
        }
    }
}