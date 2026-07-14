package com.brokechango.hattitriki.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.auth.AdminAuthRepository
import com.brokechango.hattitriki.core.auth.AdminLoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val authRepository: AdminAuthRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AdminUiState(isAuthConfigured = authRepository != null)
    )
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.EmailChanged -> updateForm(email = event.value)
            is AdminEvent.PasswordChanged -> updateForm(password = event.value)
            AdminEvent.SubmitLogin -> login()
            AdminEvent.LogoutClicked -> logout()
            AdminEvent.NewMatchClicked,
            AdminEvent.AddPlayerClicked -> Unit
        }
    }

    private fun updateForm(email: String = _uiState.value.email, password: String = _uiState.value.password) {
        _uiState.value = _uiState.value.copy(
            email = email,
            password = password,
            errorMessage = null
        )
    }

    private fun restoreSession() {
        val repository = authRepository ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAdmin = repository.hasActiveAdminSession(),
                isLoading = false
            )
        }
    }

    private fun login() {
        val repository = authRepository
        if (repository == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Falta configurar Supabase en este dispositivo."
            )
            return
        }

        if (!_uiState.value.canSubmitLogin) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = repository.login(_uiState.value.email, _uiState.value.password)) {
                AdminLoginResult.Authorized -> {
                    _uiState.value = _uiState.value.copy(
                        isAdmin = true,
                        password = "",
                        isLoading = false
                    )
                }

                AdminLoginResult.NotAdministrator -> {
                    _uiState.value = _uiState.value.copy(
                        password = "",
                        isLoading = false,
                        errorMessage = "Esta cuenta no tiene permisos de administrador."
                    )
                }

                is AdminLoginResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        password = "",
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun logout() {
        val repository = authRepository ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            runCatching { repository.logout() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isAdmin = false,
                        isLoading = false,
                        password = ""
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se ha podido cerrar la sesión."
                    )
                }
        }
    }
}
