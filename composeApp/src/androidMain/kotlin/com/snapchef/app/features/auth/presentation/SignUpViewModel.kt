package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.di.SnapChefServiceLocator
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val agreeTerms: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val apiService = SnapChefServiceLocator.authApiService

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun setAgreeTerms(value: Boolean) {
        _uiState.value = _uiState.value.copy(agreeTerms = value)
    }

    fun signUp(onVerifyRequired: (String) -> Unit, onSuccess: () -> Unit) {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill out all fields.")
            return
        }

        if (name.length < 2) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name must be at least 2 characters long.")
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

        if (!_uiState.value.agreeTerms) {
            _uiState.value = _uiState.value.copy(errorMessage = "You must agree to the Terms of Service.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.signup(com.snapchef.app.features.auth.data.remote.SignupRequest(email, name, password))
                onVerifyRequired(response.email)
            } catch (e: ClientRequestException) {
                val status = e.response.status.value
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (status) {
                        400, 409 -> "This email is already registered. Please sign in."
                        403      -> { onVerifyRequired(email); return@launch }
                        else     -> "An error occurred during account creation."
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please check your connection."
                )
            }
        }
    }
}

