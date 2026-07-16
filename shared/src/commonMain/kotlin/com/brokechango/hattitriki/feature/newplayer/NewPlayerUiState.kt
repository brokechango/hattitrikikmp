package com.brokechango.hattitriki.feature.newplayer

data class NewPlayerUiState(
    val editingPlayerId: String? = null,
    val isCheckingAccess: Boolean = true,
    val isAdmin: Boolean = false,
    val name: String = "",
    val hasCardio: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditing: Boolean
        get() = editingPlayerId != null

    val canSubmit: Boolean
        get() = isAdmin && !isSaving && name.trim().isNotEmpty()
}
