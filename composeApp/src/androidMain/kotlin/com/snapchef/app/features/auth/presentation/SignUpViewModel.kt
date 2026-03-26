package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val agreeTerms: Boolean = false,
)

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun toggleShowPassword() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun setAgreeTerms(value: Boolean) {
        _uiState.value = _uiState.value.copy(agreeTerms = value)
    }

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            _uiState.value = _uiState.value.copy(
                name = obj.optString("name", _uiState.value.name),
                email = obj.optString("email", _uiState.value.email),
                agreeTerms = obj.optBoolean("agreeTerms", _uiState.value.agreeTerms),
            )
        }
    }
}

