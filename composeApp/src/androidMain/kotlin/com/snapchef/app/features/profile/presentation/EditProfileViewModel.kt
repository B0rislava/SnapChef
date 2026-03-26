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

    fun updateName(value: String) = _uiState.update { it.copy(editedName = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(editedEmail = value) }

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

