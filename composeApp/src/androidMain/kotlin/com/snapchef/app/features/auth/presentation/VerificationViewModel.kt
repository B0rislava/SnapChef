package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.data.remote.createHttpClient
import com.snapchef.app.features.auth.data.remote.AuthApiService
import com.snapchef.app.features.auth.data.remote.VerifyRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerificationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VerificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val apiService = AuthApiService(createHttpClient())

    fun updateCode(newCode: String) {
        if (newCode.length <= 6) {
            _uiState.value = _uiState.value.copy(code = newCode, errorMessage = null)
        }
    }

    fun verify(email: String, onSuccess: () -> Unit) {
        val currentCode = _uiState.value.code
        if (currentCode.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter the 6-digit code.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.verify(VerifyRequest(email = email, code = currentCode))
                
                AuthManager.accessToken = response.accessToken
                AuthManager.currentUser = response.user
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Verification failed. The code might be invalid or expired."
                )
            }
        }
    }
}
