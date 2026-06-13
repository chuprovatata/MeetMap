package com.meetmap.datingapp.viewmodels

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meetmap.datingapp.data.models.Event
import com.meetmap.datingapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _currentEvent = MutableStateFlow<Event?>(null)
    val currentEvent: StateFlow<Event?> = _currentEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUserJoined = MutableStateFlow(false)
    val isUserJoined: StateFlow<Boolean> = _isUserJoined.asStateFlow()

    private val _joinLoading = MutableStateFlow(false)
    val joinLoading: StateFlow<Boolean> = _joinLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _events.value = eventRepository.getAllEvents()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки мероприятий"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _currentEvent.value = eventRepository.getEventById(eventId)
                if (_currentEvent.value == null) {
                    _errorMessage.value = "Мероприятие не найдено"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinEvent(eventId: String, userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _joinLoading.value = true
            _errorMessage.value = null
            try {
                val success = eventRepository.joinEvent(eventId, userId)
                if (success) {
                    loadEventById(eventId)
                    checkUserJoined(eventId, userId)
                }
                onComplete(success)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка при записи"
                onComplete(false)
            } finally {
                _joinLoading.value = false
            }
        }
    }

    fun leaveEvent(eventId: String, userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _joinLoading.value = true
            _errorMessage.value = null
            try {
                val success = eventRepository.leaveEvent(eventId, userId)
                if (success) {
                    loadEventById(eventId)
                    checkUserJoined(eventId, userId)
                }
                onComplete(success)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка при отписке"
                onComplete(false)
            } finally {
                _joinLoading.value = false
            }
        }
    }

    fun checkUserJoined(eventId: String, userId: String) {
        viewModelScope.launch {
            _isUserJoined.value = eventRepository.isUserJoined(eventId, userId)
        }
    }

    fun createEvent(event: Event, onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val eventId = eventRepository.createEvent(event)
                onComplete(eventId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка создания мероприятия"
                onComplete(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(event: Event, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {


                val data = mutableMapOf<String, Any>(
                    "title" to event.title,
                    "place" to event.place,
                    "description" to event.description,
                    "date" to event.date,
                    "time" to event.time,
                    "university" to event.university,
                    "isForAll" to event.isForAll
                )

                event.imageUrl?.let { data["imageUrl"] = it }

                eventRepository.updateEvent(event.id, data)

                onComplete(true)
            } catch (e: Exception) {

                _errorMessage.value = e.message ?: "Ошибка обновления мероприятия"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }

    }

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

    fun loadMyEvents(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val all = eventRepository.getAllEvents()

                _myEvents.value = all.filter { event ->
                    event.organizerId == userId || event.participantsList.contains(userId)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    fun uploadEventImage(uri: Uri, contentResolver: ContentResolver, eventId: String, onComplete: (String?) -> Unit = {}) {
        Log.d("EventViewModel", "=== uploadEventImage СТАРТ ===")
        Log.d("EventViewModel", "uri: $uri")
        Log.d("EventViewModel", "eventId: $eventId")

        viewModelScope.launch {
            _isUploadingImage.value = true
            _uploadError.value = null
            try {
                Log.d("EventViewModel", "Вызываем eventRepository.uploadEventImage...")
                val imageUrl = eventRepository.uploadEventImage(uri, contentResolver, eventId)
                Log.d("EventViewModel", "РЕЗУЛЬТАТ: $imageUrl")
                onComplete(imageUrl)
            } catch (e: Exception) {
                Log.e("EventViewModel", "ОШИБКА: ${e.message}", e)
                onComplete(null)
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun clearUploadError() {
        _uploadError.value = null
    }





}