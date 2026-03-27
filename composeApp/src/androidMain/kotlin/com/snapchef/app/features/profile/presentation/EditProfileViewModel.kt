package com.snapchef.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

data class EditProfileUiState(
    val editedName: String = "",
    val editedEmail: String = "",
    val editedPassword: String = "",
    val editedConfirmPassword: String = "",
    val errorMessage: String? = null,
) {
    val initials: String
        get() {
            val parts = editedName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return "JD"
            if (parts.size == 1) return parts.first().take(2).uppercase()
            return (parts.first().first().toString() + parts.last().first().toString()).uppercase()
        }
}

class EditProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun setInitialValues(name: String, email: String) {
        _uiState.update {
            if (it.editedName == name && it.editedEmail == email) it
            else it.copy(editedName = name, editedEmail = email)
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(editedName = value, errorMessage = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(editedEmail = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(editedPassword = value, errorMessage = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(editedConfirmPassword = value, errorMessage = null) }

    fun validateAndSave(onValidSave: (String, String, String, String) -> Unit) {
        val name = _uiState.value.editedName.trim()
        val email = _uiState.value.editedEmail.trim()
        val password = _uiState.value.editedPassword
        val confirmPassword = _uiState.value.editedConfirmPassword

        if (name.isBlank() || email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name and email cannot be empty.") }
            return
        }

        if (name.length < 2) {
            _uiState.update { it.copy(errorMessage = "Name must be at least 2 characters long.") }
            return
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.matches(emailRegex)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        if (password.isNotEmpty()) {
            if (password.length < 8) {
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters long.") }
                return
            }
            if (password != confirmPassword) {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                return
            }
        }

        _uiState.update { it.copy(errorMessage = null) }
        onValidSave(name, email, password, confirmPassword)
    }

    fun applyBackendJson(json: String) {
        runCatching {
            val obj = JSONObject(json)
            _uiState.update {
                it.copy(
                    editedName = obj.optString("name", it.editedName),
                    editedEmail = obj.optString("email", it.editedEmail),
                )
            }
        }
    }
}

