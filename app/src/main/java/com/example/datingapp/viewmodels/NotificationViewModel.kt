package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.models.Notification
import com.example.datingapp.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.delay

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val TAG = "NotificationViewModel"

    // Список всех уведомлений
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Количество непрочитанных уведомлений
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Сообщение об ошибке
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Флаг, показывающий, есть ли уведомления
    private val _hasNotifications = MutableStateFlow(false)
    val hasNotifications: StateFlow<Boolean> = _hasNotifications.asStateFlow()

    init {
        Log.d(TAG, "Инициализация NotificationViewModel")
        observeNotifications()
        loadUnreadCount()
    }

    /**
     * Наблюдение за уведомлениями в реальном времени
     */
    private fun observeNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "🔄 Начинаем наблюдение за уведомлениями")

            try {
                notificationRepository.getNotificationsFlow().collect { notificationsList ->
                    Log.d(TAG, "📬 Получен список уведомлений размером: ${notificationsList.size}")

                    notificationsList.forEachIndexed { index, notification ->
                        Log.d(TAG, "  Уведомление #$index: id=${notification.id}, title=${notification.title}, isRead=${notification.read}")
                    }

                    _notifications.value = notificationsList
                    _hasNotifications.value = notificationsList.isNotEmpty()

                    // Обновляем количество непрочитанных
                    _unreadCount.value = notificationsList.count { !it.read }

                    Log.d(TAG, "📊 Итого: ${notificationsList.size} уведомлений, непрочитанных: ${_unreadCount.value}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка при получении уведомлений", e)
                _errorMessage.value = "Ошибка загрузки уведомлений: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Загрузить количество непрочитанных уведомлений
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val count = withContext(Dispatchers.IO) {
                    notificationRepository.getUnreadCount()
                }
                _unreadCount.value = count
                Log.d(TAG, "Загружено количество непрочитанных: $count")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке количества непрочитанных", e)
            }
        }
    }

    /**
     * Отметить уведомление как прочитанное
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    notificationRepository.markAsRead(notificationId)
                }
                // Обновим счетчик после отметки
                loadUnreadCount()
                Log.d(TAG, "Уведомление $notificationId отмечено как прочитанное")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при отметке уведомления", e)
            }
        }
    }

    /**
     * Отметить все уведомления как прочитанные
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    notificationRepository.markAllAsRead()
                }
                // Обновим счетчик
                loadUnreadCount()
                Log.d(TAG, "Все уведомления отмечены как прочитанные")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при отметке всех уведомлений", e)
            }
        }
    }

    /**
     * Удалить уведомление
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    notificationRepository.deleteNotification(notificationId)
                }
                // Обновим счетчик
                loadUnreadCount()
                Log.d(TAG, "Уведомление $notificationId удалено")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении уведомления", e)
            }
        }
    }

    /**
     * Удалить все уведомления
     */
    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    notificationRepository.deleteAllNotifications()
                }
                // Обновим счетчик
                loadUnreadCount()
                Log.d(TAG, "Все уведомления удалены")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при удалении всех уведомлений", e)
            }
        }
    }

    /**
     * Получить уведомление по индексу
     */
    fun getNotificationAt(index: Int): Notification? {
        return _notifications.value.getOrNull(index)
    }

    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Обновить список уведомлений (принудительно)
     */
    fun refreshNotifications() {
        // Просто перезапускаем наблюдение
        observeNotifications()
    }

    /**
     * Получить уведомления для отображения в баннере
     * Возвращает последние 3 непрочитанных уведомления
     */
    fun getBannerNotifications(): List<Notification> {
        return _notifications.value
            .filter { !it.read }
            .take(3)
    }

    /**
     * Проверить, нужно ли показывать баннер
     */
    fun shouldShowBanner(): Boolean {
        return _notifications.value.any { !it.read }
    }

    /**
     * Создать тестовые уведомления (только для отладки)
     */
    fun createTestNotifications() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    notificationRepository.createTestNotifications()
                }
                Log.d(TAG, "Тестовые уведомления созданы")
                // Обновляем список уведомлений
                delay(1000)
                refreshNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при создании тестовых уведомлений", e)
            }
        }
    }
}