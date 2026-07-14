package com.brokechango.hattitriki.core.auth

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
    private val client: SupabaseClient
) {
    suspend fun login(email: String, password: String): AdminLoginResult = try {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val user = client.auth.currentUserOrNull()
            ?: return AdminLoginResult.Failure("No se pudo recuperar la sesión iniciada.")

        val profile = findProfile(user.id)

        if (profile.role == ADMIN_ROLE) {
            AdminLoginResult.Authorized
        } else {
            client.auth.signOut()
            AdminLoginResult.NotAdministrator
        }
    } catch (exception: Exception) {
        AdminLoginResult.Failure(loginErrorMessage(exception))
    }

    suspend fun logout() {
        client.auth.signOut()
    }

    suspend fun hasActiveAdminSession(): Boolean {
        val user = client.auth.currentUserOrNull() ?: return false
        return runCatching {
            findProfile(user.id).role == ADMIN_ROLE
        }.getOrDefault(false)
    }

    private suspend fun findProfile(userId: String): AdminProfile = client
        .from("profiles")
        .select {
            filter {
                eq("id", userId)
            }
        }
        .decodeSingle()

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
