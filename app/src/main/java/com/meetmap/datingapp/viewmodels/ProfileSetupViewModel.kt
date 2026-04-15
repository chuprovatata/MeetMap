package com.meetmap.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileSetupViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    sealed class SetupEvent {
        object NavigateToTargets : SetupEvent()
        object NavigateToCategories : SetupEvent()

        object NavigateToTutorial : SetupEvent()
        object NavigateToMain : SetupEvent()
        object RegistrationStarted : SetupEvent()

        data class ShowError(val message: String) : SetupEvent()
        object ShowSuccessMessage : SetupEvent()
    }

    private val _events = MutableSharedFlow<SetupEvent>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val events: SharedFlow<SetupEvent> = _events.asSharedFlow()

    private suspend fun emitEvent(event: SetupEvent) {
        _events.emit(event)
    }

    fun saveUserProfile(userProfile: Map<String, Any?>) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                println("DEBUG: Начало сохранения профиля")
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    println("DEBUG: Пользователь не авторизован")
                    emitEvent(SetupEvent.ShowError("Пользователь не авторизован"))
                    return@launch
                }

                val userId = currentUser.uid
                val username = userProfile["username"] as? String ?: ""

                if (username.isNotBlank()) {
                    val existingUser = firestore.collection("users")
                        .whereEqualTo("username", username)
                        .limit(1)
                        .get()
                        .await()

                    if (!existingUser.isEmpty &&
                        existingUser.documents.isNotEmpty() &&
                        existingUser.documents[0].id != userId) {
                        println("DEBUG: Никнейм уже занят")
                        emitEvent(SetupEvent.ShowError("Этот никнейм уже занят"))
                        return@launch
                    }
                }

                val userData = mutableMapOf<String, Any>(
                    "name" to (userProfile["name"] ?: ""),
                    "username" to username,
                    "email" to (currentUser.email ?: ""),
                    "id" to userId,
                    "profileComplete" to false,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                userProfile["university"]?.takeIf { it.toString().isNotBlank() }?.let {
                    userData["university"] = it
                }

                userProfile["bio"]?.takeIf { it.toString().isNotBlank() }?.let {
                    userData["bio"] = it
                }

                userProfile["gender"]?.let { userData["gender"] = it }
                userProfile["birthYear"]?.let { userData["birthYear"] = it }
                userProfile["age"]?.let { userData["age"] = it }

                println("DEBUG: Сохранение данных в Firestore: $userData")
                firestore.collection("users")
                    .document(userId)
                    .set(userData, SetOptions.merge())
                    .await()

                println("DEBUG: Профиль успешно сохранен, переход к целям")
                emitEvent(SetupEvent.NavigateToTargets)

            } catch (e: Exception) {
                println("DEBUG: Ошибка при сохранении: ${e.message}")
                emitEvent(SetupEvent.ShowError(e.message ?: "Неизвестная ошибка при сохранении"))
            } finally {
                _isLoading.value = false
                println("DEBUG: Загрузка завершена")
            }
        }
    }

    fun saveUserTargets(targets: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                println("DEBUG: Начало сохранения целей")
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    emitEvent(SetupEvent.ShowError("Пользователь не авторизован"))
                    return@launch
                }

                val userId = currentUser.uid

                val userData = mapOf(
                    "targets" to targets,
                    "profileComplete" to false,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                println("DEBUG: Сохранение целей в Firestore: $userData")
                firestore.collection("users")
                    .document(userId)
                    .set(userData, SetOptions.merge())
                    .await()

                println("DEBUG: Цели успешно сохранены, переход к категориям")
                emitEvent(SetupEvent.NavigateToCategories)

            } catch (e: Exception) {
                println("DEBUG: Ошибка при сохранении целей: ${e.message}")
                emitEvent(SetupEvent.ShowError(e.message ?: "Неизвестная ошибка при сохранении целей"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveUserCategories(categories: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                println("DEBUG: Начало сохранения категорий")
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    emitEvent(SetupEvent.ShowError("Пользователь не авторизован"))
                    return@launch
                }

                val userId = currentUser.uid

                val userData = mapOf(
                    "categories" to categories,
                    "profileComplete" to true, // Теперь профиль полностью заполнен
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                println("DEBUG: Сохранение категорий в Firestore: $userData")
                firestore.collection("users")
                    .document(userId)
                    .set(userData, SetOptions.merge())
                    .await()

                println("DEBUG: Категории успешно сохранены, переход на главный экран")
                emitEvent(SetupEvent.NavigateToTutorial)

            } catch (e: Exception) {
                println("DEBUG: Ошибка при сохранении категорий: ${e.message}")
                emitEvent(SetupEvent.ShowError(e.message ?: "Неизвестная ошибка при сохранении категорий"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Метод для сохранения с коллбэками
    fun saveUserProfileWithCallback(
        userProfile: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    onError("Пользователь не авторизован")
                    return@launch
                }

                val userId = currentUser.uid
                val username = userProfile["username"] as? String ?: ""

                if (username.isNotBlank()) {
                    val existingUser = firestore.collection("users")
                        .whereEqualTo("username", username)
                        .limit(1)
                        .get()
                        .await()

                    if (!existingUser.isEmpty &&
                        existingUser.documents.isNotEmpty() &&
                        existingUser.documents[0].id != userId) {
                        onError("Этот никнейм уже занят")
                        return@launch
                    }
                }

                val userData = mutableMapOf<String, Any>(
                    "name" to (userProfile["name"] ?: ""),
                    "username" to username,
                    "email" to (currentUser.email ?: ""),
                    "id" to userId,
                    "profileComplete" to false,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                userProfile["university"]?.takeIf { it.toString().isNotBlank() }?.let {
                    userData["university"] = it
                }

                userProfile["bio"]?.takeIf { it.toString().isNotBlank() }?.let {
                    userData["bio"] = it
                }

                userProfile["gender"]?.let { userData["gender"] = it }
                userProfile["birthYear"]?.let { userData["birthYear"] = it }
                userProfile["age"]?.let { userData["age"] = it }

                userProfile["telegram"]?.takeIf { it.toString().isNotBlank() }?.let {
                    userData["telegram"] = it
                }

                firestore.collection("users")
                    .document(userId)
                    .set(userData, SetOptions.merge())
                    .await()
                emitEvent(SetupEvent.RegistrationStarted)

                onSuccess()

            } catch (e: Exception) {
                onError(e.message ?: "Неизвестная ошибка при сохранении")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
