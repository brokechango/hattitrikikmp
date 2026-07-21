package com.brokechango.hattitriki

import androidx.compose.ui.window.ComposeUIViewController
import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.auth.SupabaseCredentials
import com.brokechango.hattitriki.core.auth.createAuthRepository
import platform.Foundation.NSBundle

fun MainViewController() = createAuthRepositoryOrNull().let { authRepository ->
    val appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "1.0"
    ComposeUIViewController {
        App(
            authRepository = authRepository,
            appVersion = appVersion
        )
    }
}

private fun createAuthRepositoryOrNull(): AuthRepository? = runCatching {
    val bundle = NSBundle.mainBundle
    val credentials = SupabaseCredentials(
        url = bundle.objectForInfoDictionaryKey("SUPABASE_URL") as String,
        publishableKey = bundle.objectForInfoDictionaryKey("SUPABASE_PUBLISHABLE_KEY") as String
    )
    createAuthRepository(credentials)
}.getOrNull()
