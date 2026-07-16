package com.brokechango.hattitriki.core.auth

import com.brokechango.hattitriki.core.logging.logSupabaseFailure
import com.brokechango.hattitriki.core.logging.logSupabaseRequest
import com.brokechango.hattitriki.core.logging.logSupabaseSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class AdminProfile(
    val id: String,
    val role: String
)

sealed interface AdminLoginResult {
    data object Authorized : AdminLoginResult
    data object NotAdministrator : AdminLoginResult
    data class Failure(val message: String) : AdminLoginResult
}

/**
 * Authenticates with Supabase Auth and verifies the app-specific admin role
 * stored in public.profiles. Database RLS remains the final authority for
 * writes; this check only determines what the app displays.
 */
class AdminAuthRepository internal constructor(
    internal val client: SupabaseClient
) {
    suspend fun login(email: String, password: String): AdminLoginResult {
        logSupabaseRequest("Iniciar sesión de administrador")
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = client.auth.currentUserOrNull()
                ?: return AdminLoginResult.Failure("No se pudo recuperar la sesión iniciada.")

            val profile = findProfile(user.id)

            if (profile.role == ADMIN_ROLE) {
                logSupabaseSuccess("Iniciar sesión de administrador")
                AdminLoginResult.Authorized
            } else {
                logout()
                AdminLoginResult.NotAdministrator
            }
        } catch (exception: Exception) {
            logSupabaseFailure("Iniciar sesión de administrador", exception)
            AdminLoginResult.Failure(loginErrorMessage(exception))
        }
    }

    suspend fun logout() {
        logSupabaseRequest("Cerrar sesión de administrador")
        try {
            client.auth.signOut()
            logSupabaseSuccess("Cerrar sesión de administrador")
        } catch (exception: Exception) {
            logSupabaseFailure("Cerrar sesión de administrador", exception)
            throw exception
        }
    }

    suspend fun hasActiveAdminSession(): Boolean {
        val user = client.auth.currentUserOrNull() ?: return false
        return runCatching {
            findProfile(user.id).role == ADMIN_ROLE
        }.getOrDefault(false)
    }

    private suspend fun findProfile(userId: String): AdminProfile {
        logSupabaseRequest("Consultar perfil de administrador")
        return try {
            val profile = client
                .from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<AdminProfile>()
            logSupabaseSuccess("Consultar perfil de administrador")
            profile
        } catch (exception: Exception) {
            logSupabaseFailure("Consultar perfil de administrador", exception)
            throw exception
        }
    }

    companion object {
        private const val ADMIN_ROLE = "admin"

        private fun loginErrorMessage(exception: Exception): String {
            val isInvalidCredentials = exception.message.orEmpty().contains(
                "invalid_credentials",
                ignoreCase = true
            )

            return if (isInvalidCredentials) {
                "Usuario o contraseña inválidos."
            } else {
                "No se ha podido iniciar sesión. Comprueba tu conexión e inténtalo de nuevo."
            }
        }
    }
}
