package com.brokechango.hattitriki.core.logging

import android.util.Log

private const val SUPABASE_LOG_TAG = "HattitrikiSupabase"

internal actual fun logSupabaseRequest(operation: String) {
    Log.d(SUPABASE_LOG_TAG, "$operation → inicio")
}

internal actual fun logSupabaseSuccess(operation: String) {
    Log.d(SUPABASE_LOG_TAG, "$operation → correcto")
}

internal actual fun logSupabaseFailure(operation: String, error: Throwable) {
    Log.e(SUPABASE_LOG_TAG, "$operation → error: ${safeSupabaseErrorDetail(error)}")
}
