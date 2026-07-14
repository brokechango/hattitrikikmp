package com.brokechango.hattitriki.feature.admin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun onEvent(event: AdminEvent) {
        when (event) {
            AdminEvent.LoginClicked,
            AdminEvent.NewMatchClicked,
            AdminEvent.AddPlayerClicked -> Unit
        }
    }
}
