package com.brokechango.hattitriki.core.logging

internal actual fun logSupabaseRequest(operation: String) {
    println("HattitrikiSupabase: $operation → inicio")
}

internal actual fun logSupabaseSuccess(operation: String) {
    println("HattitrikiSupabase: $operation → correcto")
}

internal actual fun logSupabaseFailure(operation: String, error: Throwable) {
    println("HattitrikiSupabase: $operation → error")
}
