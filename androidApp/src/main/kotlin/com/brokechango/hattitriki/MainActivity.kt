package com.brokechango.hattitriki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brokechango.hattitriki.core.auth.AdminAuthRepository
import com.brokechango.hattitriki.core.auth.SupabaseCredentials
import com.brokechango.hattitriki.core.auth.createAdminAuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val adminAuthRepository = createAdminAuthRepository()

        setContent {
            App(
                adminAuthRepository = adminAuthRepository,
                appVersion = BuildConfig.VERSION_NAME
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

private fun createAdminAuthRepository(): AdminAuthRepository? = runCatching {
    val credentials = SupabaseCredentials(
        url = BuildConfig.SUPABASE_URL,
        publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    )
    createAdminAuthRepository(credentials)
}.getOrNull()
