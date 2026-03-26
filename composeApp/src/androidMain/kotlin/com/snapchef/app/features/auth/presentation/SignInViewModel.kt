package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val rememberMe: Boolean = false,
)

class SignInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun setRememberMe(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            _uiState.value = _uiState.value.copy(
                email = obj.optString("email", _uiState.value.email),
                rememberMe = obj.optBoolean("rememberMe", _uiState.value.rememberMe),
            )
        }
    }
}

