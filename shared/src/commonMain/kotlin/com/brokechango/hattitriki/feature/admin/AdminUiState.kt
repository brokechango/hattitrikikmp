package com.brokechango.hattitriki.feature.admin

data class AdminUiState(
    val isAdmin: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthConfigured: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmitLogin: Boolean
        get() = isAuthConfigured && !isLoading && email.isNotBlank() && password.isNotBlank()
}
