package com.brokechango.hattitriki.feature.admin

data class AdminUiState(
    val isAdmin: Boolean = false,
    val pendingFirebaseSetup: Boolean = true
)
