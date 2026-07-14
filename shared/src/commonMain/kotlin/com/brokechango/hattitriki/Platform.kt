package com.brokechango.hattitriki

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform