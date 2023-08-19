package com.jwk.jerry.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform