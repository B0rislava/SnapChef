package com.snapchef.app.features.auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthNavViewModel : ViewModel() {
    private val _current = MutableStateFlow(AuthDestination.WELCOME)
    val current: StateFlow<AuthDestination> = _current.asStateFlow()

    fun goTo(destination: AuthDestination) {
        _current.value = destination
    }
}

