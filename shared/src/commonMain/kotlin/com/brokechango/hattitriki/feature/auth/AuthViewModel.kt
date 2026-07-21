package com.brokechango.hattitriki.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.auth.LeagueAccessResult
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository? = null,
    initialEmail: String = "",
    initialPassword: String = "",
    submitInitialLogin: Boolean = false
) : ViewModel() {
    private var pendingSignedOutMessage: String? = null
    private var isCompletingInvitation = false
    private var pendingInitialLogin = if (
        submitInitialLogin && initialEmail.isNotBlank() && initialPassword.isNotBlank()
    ) {
        initialEmail to initialPassword
    } else {
        null
    }

    private val _uiState = MutableStateFlow(
        AuthUiState(
            gateState = if (authRepository == null) {
                AuthGateState.SignedOut
            } else {
                AuthGateState.Loading
            },
            isAuthConfigured = authRepository != null,
            email = initialEmail,
            password = initialPassword,
            errorMessage = if (authRepository == null) {
                "Falta configurar Supabase en este dispositivo."
            } else {
                null
            }
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> updateForm(email = event.value)
            is AuthEvent.PasswordChanged -> updateForm(password = event.value)
            is AuthEvent.InvitationPasswordChanged -> updateInvitationForm(newPassword = event.value)
            is AuthEvent.InvitationPasswordConfirmationChanged ->
                updateInvitationForm(confirmPassword = event.value)
            AuthEvent.SubmitLogin -> login()
            AuthEvent.SubmitInvitation -> completeInvitation()
            AuthEvent.CancelInvitation -> cancelInvitation()
            AuthEvent.RetryAccess -> retryAccess()
            AuthEvent.Logout -> logout()
        }
    }

    private fun observeSession() {
        val repository = authRepository ?: return
        viewModelScope.launch {
            repository.sessionStatus.collectLatest { status ->
                when (status) {
                    SessionStatus.Initializing -> {
                        _uiState.value = _uiState.value.copy(
                            gateState = AuthGateState.Loading,
                            isSubmitting = false,
                            errorMessage = null
                        )
                    }

                    is SessionStatus.NotAuthenticated -> {
                        if (repository.requiresPasswordSetup) {
                            repository.discardInvitation()
                            pendingSignedOutMessage =
                                "El enlace de invitación no es válido o ha caducado. Solicita uno nuevo."
                        }
                        showSignedOut()
                    }
                    is SessionStatus.Authenticated -> when {
                        isCompletingInvitation -> Unit
                        repository.requiresPasswordSetup -> showInvitationSetup(repository)
                        else -> checkAccess(repository)
                    }
                    is SessionStatus.RefreshFailure -> {
                        pendingSignedOutMessage =
                            "Tu sesión ha caducado. Vuelve a iniciar sesión."
                        runCatching { repository.clearSession() }
                        showSignedOut()
                    }
                }
            }
        }
    }

    private fun updateForm(
        email: String = _uiState.value.email,
        password: String = _uiState.value.password
    ) {
        _uiState.value = _uiState.value.copy(
            email = email,
            password = password,
            errorMessage = null
        )
    }

    private fun updateInvitationForm(
        newPassword: String = _uiState.value.newPassword,
        confirmPassword: String = _uiState.value.confirmPassword
    ) {
        _uiState.value = _uiState.value.copy(
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            errorMessage = null
        )
    }

    private fun login() {
        val repository = authRepository ?: return
        val state = _uiState.value
        if (!state.canSubmitLogin) return

        pendingSignedOutMessage = null
        _uiState.value = state.copy(
            gateState = AuthGateState.Loading,
            isSubmitting = true,
            errorMessage = null
        )
        viewModelScope.launch {
            runCatching { repository.signIn(state.email, state.password) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        password = "",
                        isSubmitting = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        gateState = AuthGateState.SignedOut,
                        password = "",
                        isSubmitting = false,
                        errorMessage = AuthRepository.loginErrorMessage(exception)
                    )
                }
        }
    }

    private fun retryAccess() {
        val repository = authRepository ?: return
        viewModelScope.launch { checkAccess(repository) }
    }

    private fun completeInvitation() {
        val repository = authRepository ?: return
        val state = _uiState.value
        if (!state.canSubmitInvitation) return

        isCompletingInvitation = true
        _uiState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                repository.completeInvitation(state.newPassword)
                isCompletingInvitation = false
                _uiState.value = _uiState.value.copy(
                    gateState = AuthGateState.Loading,
                    newPassword = "",
                    confirmPassword = "",
                    isSubmitting = false,
                    errorMessage = null
                )
                checkAccess(repository)
            } catch (exception: Exception) {
                isCompletingInvitation = false
                _uiState.value = _uiState.value.copy(
                    gateState = AuthGateState.InvitationSetup,
                    isSubmitting = false,
                    errorMessage = AuthRepository.invitationErrorMessage(exception)
                )
            }
        }
    }

    private fun cancelInvitation() {
        val repository = authRepository ?: return
        isCompletingInvitation = false
        pendingSignedOutMessage = null
        repository.discardInvitation()
        _uiState.value = _uiState.value.copy(
            gateState = AuthGateState.Loading,
            newPassword = "",
            confirmPassword = "",
            isSubmitting = false,
            errorMessage = null
        )
        viewModelScope.launch {
            runCatching { repository.signOut() }
            showSignedOut()
        }
    }

    private fun showInvitationSetup(repository: AuthRepository) {
        _uiState.value = _uiState.value.copy(
            gateState = AuthGateState.InvitationSetup,
            email = repository.currentUserEmail,
            password = "",
            isSubmitting = false,
            errorMessage = null
        )
    }

    private suspend fun checkAccess(repository: AuthRepository) {
        _uiState.value = _uiState.value.copy(
            gateState = AuthGateState.Loading,
            isSubmitting = false,
            errorMessage = null
        )

        when (val result = repository.loadCurrentAccess()) {
            is LeagueAccessResult.Authorized -> {
                _uiState.value = _uiState.value.copy(
                    gateState = AuthGateState.Authenticated(result.access),
                    password = "",
                    newPassword = "",
                    confirmPassword = "",
                    isSubmitting = false,
                    errorMessage = null
                )
            }

            LeagueAccessResult.NotLeagueMember -> {
                pendingSignedOutMessage =
                    "Esta cuenta no pertenece a la liga o todavía no está activa."
                runCatching { repository.signOut() }
                showSignedOut()
            }

            is LeagueAccessResult.Failure -> {
                _uiState.value = _uiState.value.copy(
                    gateState = AuthGateState.AccessError(result.message),
                    isSubmitting = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun logout() {
        val repository = authRepository ?: return
        pendingSignedOutMessage = null
        _uiState.value = _uiState.value.copy(
            gateState = AuthGateState.Loading,
            password = "",
            newPassword = "",
            confirmPassword = "",
            errorMessage = null
        )

        viewModelScope.launch {
            runCatching { repository.signOut() }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        gateState = AuthGateState.AccessError(
                            exception.message ?: "No se ha podido cerrar la sesión."
                        )
                    )
                }
        }
    }

    private fun showSignedOut() {
        val message = pendingSignedOutMessage
        pendingSignedOutMessage = null
        _uiState.value = _uiState.value.copy(
            gateState = AuthGateState.SignedOut,
            password = "",
            newPassword = "",
            confirmPassword = "",
            isSubmitting = false,
            errorMessage = message
        )

        val initialLogin = pendingInitialLogin ?: return
        pendingInitialLogin = null
        updateForm(
            email = initialLogin.first,
            password = initialLogin.second
        )
        login()
    }
}
