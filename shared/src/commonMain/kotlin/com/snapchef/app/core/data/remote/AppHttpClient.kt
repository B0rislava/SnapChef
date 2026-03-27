package com.snapchef.app.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Shared Ktor timeouts (Darwin + OkHttp). Signup can override per-request in [AuthApiService]. */
internal const val HTTP_CONNECT_TIMEOUT_MS = 60_000L
internal const val HTTP_REQUEST_TIMEOUT_MS = 120_000L

fun createHttpClient(): HttpClient {
    return HttpClient {
        expectSuccess = true
        install(HttpTimeout) {
            connectTimeoutMillis = HTTP_CONNECT_TIMEOUT_MS
            requestTimeoutMillis = HTTP_REQUEST_TIMEOUT_MS
            socketTimeoutMillis = HTTP_REQUEST_TIMEOUT_MS
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }
}
