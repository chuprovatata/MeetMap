// app/src/main/java/com/example/datingapp/viewmodels/OnboardingViewModel.kt
package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    fun setFirstLaunchComplete() {
        viewModelScope.launch {
            _isFirstLaunch.value = false
            println("🔥 OnboardingViewModel: First launch set to FALSE")
        }
    }

    fun resetFirstLaunch() {
        viewModelScope.launch {
            _isFirstLaunch.value = true
            println("🔥 OnboardingViewModel: First launch reset to TRUE")
        }
    }
}