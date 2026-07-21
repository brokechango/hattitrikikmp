package com.brokechango.hattitriki.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.brokechango.hattitriki.App
import com.brokechango.hattitriki.core.auth.AuthRepository
import com.brokechango.hattitriki.core.auth.SupabaseCredentials
import com.brokechango.hattitriki.core.auth.createAuthRepository
import com.russhwolf.settings.StorageSettings
import io.github.jan.supabase.auth.SettingsSessionManager
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.jetbrains.skiko.wasm.onWasmReady

private const val WEB_AUTH_SESSION_KEY = "hattitriki-session"
private const val LEGACY_WEB_AUTH_SESSION_KEY = "hattitriki-admin-session"
private const val WEB_PENDING_INVITATION_KEY = "hattitriki-pending-invitation"

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {
    onWasmReady {
        whenBootstrapMountRequested {
            window.setTimeout({
                mountComposeApp()
                null
            }, 0)
            null
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun mountComposeApp() {
    val initialEmail = readBootstrapEmail()
    val initialPassword = readBootstrapPassword()
    val submitInitialLogin = shouldSubmitBootstrapLogin()
    val authRepository = createWebAuthRepositoryOrNull()
    clearBootstrapCredentials()

    ComposeViewport("composeApp") {
        App(
            authRepository = authRepository,
            appVersion = "Web",
            initialAuthEmail = initialEmail,
            initialAuthPassword = initialPassword,
            submitInitialAuth = submitInitialLogin
        )
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(callback) => globalThis.HATTITRIKI_BOOTSTRAP?.whenMountRequested(callback)")
private external fun whenBootstrapMountRequested(callback: () -> JsAny?)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_BOOTSTRAP?.email ?? ''")
private external fun readBootstrapEmail(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_BOOTSTRAP?.password ?? ''")
private external fun readBootstrapPassword(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_BOOTSTRAP?.submitLogin === true")
private external fun shouldSubmitBootstrapLogin(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_BOOTSTRAP?.clearCredentials()")
private external fun clearBootstrapCredentials()

private fun createWebAuthRepositoryOrNull(): AuthRepository? {
    val repository = runCatching {
        val url = readSupabaseUrl().trim()
        val publishableKey = readSupabasePublishableKey().trim()

        if (url.isBlank() || publishableKey.isBlank()) {
            null
        } else {
            removeLegacyPersistentSession(WEB_AUTH_SESSION_KEY)
            removeLegacyPersistentSession(LEGACY_WEB_AUTH_SESSION_KEY)
            val sessionSettings = StorageSettings(sessionStorage)
            if (isInvitationCallback()) {
                markPendingInvitation(WEB_PENDING_INVITATION_KEY)
                sessionStorage.removeItem(WEB_AUTH_SESSION_KEY)
            }
            val hasSavedSession = sessionSettings.getStringOrNull(WEB_AUTH_SESSION_KEY) != null
            val passwordSetupPending = hasPendingInvitation(WEB_PENDING_INVITATION_KEY)

            createAuthRepository(
                SupabaseCredentials(
                    url = url,
                    publishableKey = publishableKey
                ),
                passwordSetupPending = passwordSetupPending,
                onPasswordSetupResolved = {
                    clearPendingInvitation(WEB_PENDING_INVITATION_KEY)
                }
            ) {
                sessionManager = SettingsSessionManager(
                    settings = sessionSettings,
                    key = WEB_AUTH_SESSION_KEY
                )
                autoLoadFromStorage = hasSavedSession
            }
        }
    }.getOrElse {
        println("HattitrikiWeb: no se pudo inicializar la configuración remota.")
        null
    }

    clearWebRuntimeConfig()
    return repository
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_CONFIG?.supabaseUrl ?? ''")
private external fun readSupabaseUrl(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.HATTITRIKI_CONFIG?.supabasePublishableKey ?? ''")
private external fun readSupabasePublishableKey(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { try { const hash = (globalThis.location?.hash ?? '').replace(/^#/, ''); return new URLSearchParams(hash).get('type') === 'invite'; } catch (_) { return false; } }")
private external fun isInvitationCallback(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => { try { globalThis.sessionStorage?.setItem(key, 'true'); } catch (_) {} }")
private external fun markPendingInvitation(key: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => { try { return globalThis.sessionStorage?.getItem(key) === 'true'; } catch (_) { return false; } }")
private external fun hasPendingInvitation(key: String): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => { try { globalThis.sessionStorage?.removeItem(key); } catch (_) {} }")
private external fun clearPendingInvitation(key: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => { try { globalThis.localStorage?.removeItem(key); } catch (_) {} }")
private external fun removeLegacyPersistentSession(key: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { try { delete globalThis.HATTITRIKI_CONFIG; } catch (_) {} }")
private external fun clearWebRuntimeConfig()
