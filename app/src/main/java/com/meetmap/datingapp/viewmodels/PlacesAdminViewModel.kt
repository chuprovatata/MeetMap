package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class PlacesAdminViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    fun sendPlacesOfDayNotification() {
        viewModelScope.launch {
            try {
                notificationRepository.createPlacesOfDayUpdatedNotification()
                Log.d("PlacesAdminViewModel", "Уведомления о местах дня разосланы")
            } catch (e: Exception) {
                Log.e("PlacesAdminViewModel", "Ошибка рассылки", e)
            }
        }
    }
}