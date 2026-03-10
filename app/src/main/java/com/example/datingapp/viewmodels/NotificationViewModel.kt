package com.example.datingapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.Notification
import com.example.datingapp.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                notificationRepository.getNotificationsFlow()
                    .catch { exception ->
                        Log.e("NotificationVM", "Ошибка получения уведомлений", exception)
                        _errorMessage.value = "Ошибка загрузки: ${exception.message}"
                        _isLoading.value = false
                    }
                    .collect { notifications ->
                        _notifications.value = notifications
                        _isLoading.value = false
                        // Обновляем счетчик при получении новых уведомлений
                        loadUnreadCount()
                    }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Ошибка при запуске потока", e)
                _errorMessage.value = "Ошибка: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun refreshNotifications() {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val count = notificationRepository.getUnreadCount()
                _unreadCount.value = count
                Log.d("NotificationVM", "Счетчик непрочитанных обновлен: $count")
            } catch (e: Exception) {
                Log.e("NotificationVM", "Ошибка получения количества непрочитанных", e)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            // Обновляем счетчик
            loadUnreadCount()
            // Обновляем список, чтобы изменился статус прочтения
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(read = true) else it
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadUnreadCount()
            _notifications.value = _notifications.value.map { it.copy(read = true) }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            _notifications.value = _notifications.value.filter { it.id != notificationId }
            loadUnreadCount()
        }
    }

    fun createTestNotifications() {
        viewModelScope.launch {
            notificationRepository.createTestNotifications()
            refreshNotifications()
        }
    }

    /**
     * Создать уведомление о новой заявке в друзья
     */
    fun createFriendRequestNotification(fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.createFriendRequestNotification(fromUserId, toUserId)
                Log.d("NotificationVM", "Уведомление о заявке создано: from=$fromUserId, to=$toUserId")
                // Обновляем счетчик для получателя
                if (toUserId == notificationRepository.getCurrentUserId()) {
                    loadUnreadCount()
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Ошибка создания уведомления о заявке", e)
            }
        }
    }

    /**
     * Создать уведомление о принятии заявки в друзья
     */
    fun createFriendAcceptedNotification(fromUserId: String, toUserId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.createFriendAcceptedNotification(fromUserId, toUserId)
                Log.d("NotificationVM", "Уведомление о принятии заявки создано: from=$fromUserId, to=$toUserId")
                // Обновляем счетчик для получателя
                if (toUserId == notificationRepository.getCurrentUserId()) {
                    loadUnreadCount()
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "Ошибка создания уведомления о принятии заявки", e)
            }
        }
    }
}