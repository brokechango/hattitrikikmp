package com.brokechango.hattitriki.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Public connection values for one Supabase project.
 *
 * The publishable (or legacy anon) key is intended for client applications;
 * a service-role or secret key must never be passed here.
 */
data class SupabaseCredentials(
    val url: String,
    val publishableKey: String
) {
    init {
        require(url.startsWith("https://")) {
            "Supabase URL must start with https://"
        }
        require(publishableKey.isNotBlank()) {
            "Supabase publishable key cannot be blank"
        }
    }
}

/**
 * Owns the single shared client used by the application.
 *
 * Credentials are injected by the app's platform configuration rather than
 * being committed in common source code.
 */
internal class SupabaseProvider(credentials: SupabaseCredentials) {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = credentials.url,
        supabaseKey = credentials.publishableKey
    ) {
        install(Auth)
        install(Postgrest)
    }
}

fun createAdminAuthRepository(credentials: SupabaseCredentials): AdminAuthRepository =
    AdminAuthRepository(SupabaseProvider(credentials).client)
