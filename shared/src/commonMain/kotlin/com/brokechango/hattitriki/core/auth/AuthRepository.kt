package com.brokechango.hattitriki.core.auth

import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class LeagueRole {
    MEMBER,
    ADMIN;

    companion object {
        internal fun fromDatabaseValue(value: String?): LeagueRole? = when (value?.lowercase()) {
            "member" -> MEMBER
            "admin" -> ADMIN
            else -> null
        }
    }
}

data class LeagueAccess(
    val email: String,
    val role: LeagueRole
)

sealed interface LeagueAccessResult {
    data class Authorized(val access: LeagueAccess) : LeagueAccessResult
    data object NotLeagueMember : LeagueAccessResult
    data class Failure(val message: String) : LeagueAccessResult
}

@Serializable
private data class CurrentUserAccess(
    @SerialName("is_member") val isMember: Boolean,
    val role: String? = null
)

/**
 * Owns authentication for every league member. PostgreSQL remains the authority
 * for membership and administrator access; the role returned here only drives UI.
 */
class AuthRepository internal constructor(
    internal val client: SupabaseClient,
    passwordSetupPending: Boolean = false,
    private val onPasswordSetupResolved: () -> Unit = {}
) {
    private var isPasswordSetupPending = passwordSetupPending

    val sessionStatus: StateFlow<SessionStatus>
        get() = client.auth.sessionStatus

    val requiresPasswordSetup: Boolean
        get() = isPasswordSetupPending

    val currentUserEmail: String
        get() = client.auth.currentUserOrNull()?.email.orEmpty()

    suspend fun signIn(email: String, password: String) {
        logSupabaseRequest("Iniciar sesión")
        try {
            client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            logSupabaseSuccess("Iniciar sesión")
        } catch (exception: Exception) {
            logSupabaseFailure("Iniciar sesión", exception)
            throw exception
        }
    }

    suspend fun signOut() {
        logSupabaseRequest("Cerrar sesión")
        try {
            client.auth.signOut()
            logSupabaseSuccess("Cerrar sesión")
        } catch (exception: Exception) {
            logSupabaseFailure("Cerrar sesión", exception)
            throw exception
        }
    }

    suspend fun completeInvitation(password: String) {
        logSupabaseRequest("Completar invitación")
        try {
            client.auth.updateUser {
                this.password = password
            }
            resolvePasswordSetup()
            logSupabaseSuccess("Completar invitación")
        } catch (exception: Exception) {
            logSupabaseFailure("Completar invitación", exception)
            throw exception
        }
    }

    fun discardInvitation() {
        resolvePasswordSetup()
    }

    suspend fun clearSession() {
        client.auth.clearSession()
    }

    suspend fun loadCurrentAccess(): LeagueAccessResult {
        val user = client.auth.currentUserOrNull() ?: return LeagueAccessResult.NotLeagueMember

        logSupabaseRequest("Comprobar acceso a la liga")
        return try {
            val access = client.postgrest
                .rpc("get_current_user_access")
                .decodeSingle<CurrentUserAccess>()
            val role = LeagueRole.fromDatabaseValue(access.role)

            if (access.isMember && role != null) {
                logSupabaseSuccess("Comprobar acceso a la liga")
                LeagueAccessResult.Authorized(
                    LeagueAccess(
                        email = user.email.orEmpty(),
                        role = role
                    )
                )
            } else {
                LeagueAccessResult.NotLeagueMember
            }
        } catch (exception: Exception) {
            logSupabaseFailure("Comprobar acceso a la liga", exception)
            LeagueAccessResult.Failure(accessErrorMessage(exception.message))
        }
    }

    suspend fun hasActiveAdminSession(): Boolean =
        (loadCurrentAccess() as? LeagueAccessResult.Authorized)?.access?.role == LeagueRole.ADMIN

    companion object {
        fun loginErrorMessage(exception: Throwable): String {
            val details = exception.message.orEmpty()
            val invalidCredentials = details.contains("invalid_credentials", ignoreCase = true) ||
                details.contains("invalid login credentials", ignoreCase = true)

            return if (invalidCredentials) {
                "Usuario o contraseña inválidos."
            } else {
                "No se ha podido iniciar sesión. Comprueba tu conexión e inténtalo de nuevo."
            }
        }

        fun invitationErrorMessage(exception: Throwable): String {
            val details = exception.message.orEmpty()
            val weakPassword = details.contains("weak_password", ignoreCase = true) ||
                details.contains("password", ignoreCase = true) &&
                details.contains("weak", ignoreCase = true)

            return if (weakPassword) {
                "La contraseña no cumple la política de seguridad de la liga."
            } else {
                "No se ha podido guardar la contraseña. Comprueba tu conexión e inténtalo de nuevo."
            }
        }

        internal fun accessErrorMessage(details: String?): String {
            val normalized = details.orEmpty().lowercase()
            return when {
                "pgrst202" in normalized || "get_current_user_access" in normalized ->
                    "La protección de miembros todavía no está configurada. Aplica la última migración de Supabase."
                "network" in normalized || "timeout" in normalized || "connect" in normalized ->
                    "No se ha podido comprobar tu acceso. Revisa la conexión e inténtalo de nuevo."
                else -> "No se ha podido comprobar tu acceso a la liga. Inténtalo de nuevo."
            }
        }
    }

    private fun resolvePasswordSetup() {
        if (!isPasswordSetupPending) return
        isPasswordSetupPending = false
        onPasswordSetupResolved()
    }
}
