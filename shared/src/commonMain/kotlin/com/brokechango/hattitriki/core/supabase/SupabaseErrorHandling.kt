package com.brokechango.hattitriki.core.supabase

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

/**
 * The categories that are useful to someone using the app. Technical error
 * details stay in the diagnostic log and never reach the UI.
 */
internal enum class SupabaseErrorKind {
    SETUP,
    UNAUTHORIZED,
    RATE_LIMITED,
    CONFLICT,
    UNAVAILABLE,
    CONNECTION,
    UNKNOWN
}

internal data class SupabaseErrorMessages(
    val setupMessage: String,
    val permissionMessage: String,
    val connectionMessage: String,
    val fallbackMessage: String,
    val rateLimitMessage: String = "Hay demasiadas solicitudes. Espera unos segundos antes de volver a intentarlo.",
    val unavailableMessage: String = "Supabase no está disponible temporalmente. Inténtalo de nuevo en unos segundos.",
    val conflictMessage: String? = null
)

/**
 * Converts the structured errors returned by Auth and PostgREST into a small,
 * stable set of actions. Codes are preferred over exception text because the
 * wording returned by the API is not a contract.
 */
internal fun classifySupabaseError(
    statusCode: Int? = null,
    errorCode: String? = null,
    details: String? = null
): SupabaseErrorKind {
    val normalizedCode = errorCode.orEmpty().uppercase()
    val normalizedDetails = details.orEmpty().lowercase()

    return when {
        normalizedCode in setupErrorCodes ||
            normalizedDetails.contains("pgrst202") ||
            normalizedDetails.contains("pgrst204") ||
            normalizedDetails.contains("pgrst205") -> SupabaseErrorKind.SETUP

        normalizedCode in authorizationErrorCodes ||
            statusCode == 401 ||
            statusCode == 403 ||
            normalizedDetails.contains("permission denied") ||
            normalizedDetails.contains("not authorized") ||
            normalizedDetails.contains("invalid jwt") -> SupabaseErrorKind.UNAUTHORIZED

        statusCode == 429 ||
            normalizedCode in rateLimitErrorCodes ||
            normalizedDetails.contains("over_request_rate_limit") ||
            normalizedDetails.contains("rate limit") -> SupabaseErrorKind.RATE_LIMITED

        statusCode == 409 ||
            normalizedCode == "23505" ||
            normalizedDetails.contains("duplicate key") -> SupabaseErrorKind.CONFLICT

        statusCode in 500..599 ||
            normalizedCode in temporaryServiceErrorCodes ||
            normalizedDetails.contains("pgrst000") ||
            normalizedDetails.contains("pgrst001") ||
            normalizedDetails.contains("pgrst002") ||
            normalizedDetails.contains("pgrst003") -> SupabaseErrorKind.UNAVAILABLE

        normalizedDetails.contains("network") ||
            normalizedDetails.contains("failed to fetch") ||
            normalizedDetails.contains("fetch failed") ||
            normalizedDetails.contains("timeout") ||
            normalizedDetails.contains("timed out") ||
            normalizedDetails.contains("connection") ||
            normalizedDetails.contains("connect") ||
            normalizedDetails.contains("offline") -> SupabaseErrorKind.CONNECTION

        else -> SupabaseErrorKind.UNKNOWN
    }
}

internal fun Throwable.supabaseErrorKind(): SupabaseErrorKind = classifySupabaseError(
    statusCode = restExceptionOrNull()?.statusCode,
    errorCode = supabaseErrorCode(),
    details = throwableDetails()
)

internal fun Throwable.supabaseErrorCode(): String? = when (val exception = restExceptionOrNull()) {
    is PostgrestRestException -> exception.code
    is AuthRestException -> exception.error
    else -> null
}

internal fun Throwable.toSupabaseUserMessage(messages: SupabaseErrorMessages): String = when (supabaseErrorKind()) {
    SupabaseErrorKind.SETUP -> messages.setupMessage
    SupabaseErrorKind.UNAUTHORIZED -> messages.permissionMessage
    SupabaseErrorKind.RATE_LIMITED -> messages.rateLimitMessage
    SupabaseErrorKind.CONFLICT -> messages.conflictMessage ?: messages.fallbackMessage
    SupabaseErrorKind.UNAVAILABLE -> messages.unavailableMessage
    SupabaseErrorKind.CONNECTION -> messages.connectionMessage
    SupabaseErrorKind.UNKNOWN -> messages.fallbackMessage
}

private fun Throwable.restExceptionOrNull(): RestException? {
    var current: Throwable? = this
    while (current != null) {
        if (current is RestException) return current
        current = current.cause
    }
    return null
}

private fun Throwable.throwableDetails(): String = buildString {
    var current: Throwable? = this@throwableDetails
    while (current != null) {
        current.message?.let(::appendLine)
        current = current.cause
    }
}

private val setupErrorCodes = setOf("PGRST202", "PGRST204", "PGRST205")

private val authorizationErrorCodes = setOf(
    "42501",
    "28000",
    "28P01",
    "PGRST301",
    "PGRST302",
    "PGRST303",
    "BAD_JWT",
    "NO_AUTHORIZATION",
    "SESSION_NOT_FOUND",
    "SESSION_EXPIRED",
    "REFRESH_TOKEN_NOT_FOUND",
    "REFRESH_TOKEN_ALREADY_USED"
)

private val rateLimitErrorCodes = setOf("OVER_REQUEST_RATE_LIMIT", "OVER_EMAIL_SEND_RATE_LIMIT", "OVER_SMS_SEND_RATE_LIMIT")

private val temporaryServiceErrorCodes = setOf("REQUEST_TIMEOUT", "UNEXPECTED_FAILURE", "HOOK_TIMEOUT", "HOOK_TIMEOUT_AFTER_RETRY")
