package com.meetmap.datingapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object EmailNotVerified : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        checkCurrentUser()

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            updateAuthState(user)
            Log.d("AuthViewModel", "Auth state changed: ${user?.email}")
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                val isValid = checkIfUserExists(user)
                if (isValid) {
                    _currentUser.value = user
                    updateAuthState(user)
                } else {
                    forceSignOut()
                }
            } else {
                _currentUser.value = null
                updateAuthState(null)
            }
            Log.d("AuthViewModel", "Initial auth check: ${user?.email}")
        }
    }

    private suspend fun checkIfUserExists(user: FirebaseUser): Boolean {
        return try {
            user.getIdToken(true).await()
            true
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("AuthViewModel", "User no longer exists: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking user existence: ${e.message}")
            true
        }
    }

    private fun updateAuthState(user: FirebaseUser?) {
        _authState.value = when {
            user == null -> AuthState.Unauthenticated
            !user.isEmailVerified -> AuthState.EmailNotVerified
            else -> AuthState.Authenticated
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    if (user.isEmailVerified) {
                        onSuccess()
                    } else {
                        _errorMessage.value = "Подтверди email для входа. Проверь свою почту"
                        onError("Подтверди email для входа. Проверь свою почту")
                    }
                }
            } catch (e: Exception) {
                // Общая ошибка для всех случаев: неверный email или неверный пароль
                val error = "Неправильный email или пароль"
                _errorMessage.value = error
                onError(error)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    Log.d("AuthViewModel", "Google sign in success: ${user.email}")
                    onSuccess()
                } else {
                    val error = "Ошибка входа через Google"
                    _errorMessage.value = error
                    onError(error)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign in error", e)
                val error = when (e.message) {
                    else -> "Ошибка входа через Google: ${e.message}"
                }
                _errorMessage.value = error
                onError(error)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun register(email: String, password: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                user?.sendEmailVerification()?.await()
                _successMessage.value = "Письмо с подтверждением отправлено на $email"
                onSuccess()
            } catch (e: Exception) {
                val error = when (e.message) {
                    "The email address is already in use by another account." ->
                        "Этот email уже используется"
                    "The email address is badly formatted." ->
                        "Некорректный формат email"
                    "Password should be at least 6 characters" ->
                        "Пароль должен содержать минимум 6 символов"
                    else -> "Ошибка регистрации: ${e.message}"
                }
                _errorMessage.value = error
                onError(error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        clearLocalData()
    }

    fun forceSignOut() {
        auth.signOut()
        clearLocalData()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
        Log.d("AuthViewModel", "Force sign out executed")
    }

    private fun clearLocalData() {
    }

    suspend fun checkSessionValidity(): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            user.getIdToken(true).await()
            true
        } catch (e: FirebaseAuthInvalidUserException) {
            forceSignOut()
            false
        } catch (e: Exception) {
            true
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.sendPasswordResetEmail(email).await()
                _successMessage.value = "Письмо для сброса пароля отправлено на $email"
                onSuccess()
            } catch (e: FirebaseAuthInvalidUserException) {
                _successMessage.value = "Письмо для сброса пароля отправлено на $email"
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
                onError(e.message ?: "Неизвестная ошибка")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun clearErrors() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun resendVerificationEmail(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val user = auth.currentUser
                user?.sendEmailVerification()?.await()
                _successMessage.value = "Новое письмо отправлено на ${user?.email}"
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка отправки письма")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkEmailVerification(onVerified: () -> Unit = {}, onNotVerified: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val user = auth.currentUser
                user?.reload()?.await()

                if (user?.isEmailVerified == true) {
                    onVerified()
                } else {
                    onNotVerified()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка проверки email"
                onNotVerified()
            } finally {
                _isLoading.value = false
            }
        }
    }
}