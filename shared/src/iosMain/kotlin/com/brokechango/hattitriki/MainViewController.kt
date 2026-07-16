package com.brokechango.hattitriki

import androidx.compose.ui.window.ComposeUIViewController
import com.brokechango.hattitriki.core.auth.AdminAuthRepository
import com.brokechango.hattitriki.core.auth.SupabaseCredentials
import com.brokechango.hattitriki.core.auth.createAdminAuthRepository
import platform.Foundation.NSBundle

fun MainViewController() = createAdminAuthRepositoryOrNull().let { authRepository ->
    val appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: "1.0"
    ComposeUIViewController {
        App(
            adminAuthRepository = authRepository,
            appVersion = appVersion
        )
    }
}

private fun createAdminAuthRepositoryOrNull(): AdminAuthRepository? = runCatching {
    val bundle = NSBundle.mainBundle
    val credentials = SupabaseCredentials(
        url = bundle.objectForInfoDictionaryKey("SUPABASE_URL") as String,
        publishableKey = bundle.objectForInfoDictionaryKey("SUPABASE_PUBLISHABLE_KEY") as String
    )
    createAdminAuthRepository(credentials)
}.getOrNull()
