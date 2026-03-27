package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.data.remote.createHttpClient
import com.snapchef.app.features.auth.data.remote.AuthApiService
import com.snapchef.app.features.auth.data.remote.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val apiService = AuthApiService(createHttpClient())

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun signIn(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password.")
            return
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.matches(emailRegex)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address.")
            return
        }

        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 8 characters long.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.login(LoginRequest(email, password))
                AuthManager.accessToken = response.accessToken
                AuthManager.currentUser = response.user
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: Incorrect credentials or server error."
                )
            }
        }
    }
}

