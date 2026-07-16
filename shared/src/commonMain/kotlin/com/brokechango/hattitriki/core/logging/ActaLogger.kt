package com.brokechango.hattitriki.core.logging

/** Diagnostic logging for Supabase operations without credentials or session data. */
internal expect fun logSupabaseRequest(operation: String)
internal expect fun logSupabaseSuccess(operation: String)
internal expect fun logSupabaseFailure(operation: String, error: Throwable)

internal fun safeSupabaseErrorDetail(error: Throwable): String {
    val detail = error.message?.takeIf { it.isNotBlank() } ?: error::class.simpleName.orEmpty()
    val diagnosticLines = detail.lineSequence()
        .takeWhile { line ->
            val prefix = line.trimStart().lowercase()
            !prefix.startsWith("url:") &&
                !prefix.startsWith("headers:") &&
                !prefix.startsWith("http method:")
        }
        .joinToString(" ")

    return diagnosticLines
        .replace(
            Regex("(?i)(access_?token|refresh_?token|apikey|authorization|password)\\s*[:=]\\s*[^,\\s}]+"),
            "\$1=[redacted]"
        )
        .take(500)
}
