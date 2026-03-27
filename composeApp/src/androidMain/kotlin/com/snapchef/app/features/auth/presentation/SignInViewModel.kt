package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.di.SnapChefServiceLocator
import com.snapchef.app.features.auth.data.remote.LoginRequest
import io.ktor.client.plugins.ClientRequestException
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

    private val apiService = SnapChefServiceLocator.authApiService

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun signIn(onSuccess: () -> Unit, onVerifyRequired: (String) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Call actual backend
                val response = apiService.login(LoginRequest(email, password))
                
                // Save session properly
                AuthManager.accessToken = response.accessToken
                AuthManager.currentUser = response.user
                
                onSuccess()
            } catch (e: Exception) {
                if (e is ClientRequestException && e.response.status.value == 403) {
                    onVerifyRequired(email)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed. Check your credentials."
                    )
                }
            }
        }
    }

    fun googleSignIn(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.googleAuth(idToken)
                AuthManager.accessToken = response.accessToken
                AuthManager.currentUser = response.user
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In failed on the server."
                )
            }
        }
    }
}
