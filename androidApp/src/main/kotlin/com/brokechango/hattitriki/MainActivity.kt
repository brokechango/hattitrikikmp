package com.brokechango.hattitriki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.auth.SupabaseCredentials
import com.brokechango.hattitriki.core.auth.createAuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepository = createPlatformAuthRepository()

        setContent {
            App(
                authRepository = authRepository,
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

private fun createPlatformAuthRepository(): AuthRepository? = runCatching {
    val credentials = SupabaseCredentials(
        url = BuildConfig.SUPABASE_URL,
        publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    )
    createAuthRepository(credentials)
}.getOrNull()
