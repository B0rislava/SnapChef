package com.snapchef.app.features.auth.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.core.data.remote.HTTP_REQUEST_TIMEOUT_MS
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class AuthApiService(private val client: HttpClient) {

    private val signupJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Signup may be slower than other calls (cold Railway, SMTP). Allow longer than [HTTP_REQUEST_TIMEOUT_MS]
     * so the client outlives slow responses if the backend ever blocks on email again.
     */
    private val signupRequestTimeoutMs = maxOf(HTTP_REQUEST_TIMEOUT_MS, 240_000L)

    @Throws(Exception::class)
    suspend fun signup(request: SignupRequest): SignupEndpointResult {
        val response = client.post("${AppConfig.BASE_URL}/auth/signup") {
            timeout {
                requestTimeoutMillis = signupRequestTimeoutMs
                socketTimeoutMillis = signupRequestTimeoutMs
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val text = response.bodyAsText()
        val root = signupJson.parseToJsonElement(text).jsonObject
        return if ("access_token" in root) {
            SignupEndpointResult(immediateAuth = signupJson.decodeFromString<AuthResponse>(text))
        } else {
            SignupEndpointResult(pendingVerification = signupJson.decodeFromString<SignupResponse>(text))
        }
    }

    @Throws(Exception::class)
    suspend fun verify(request: VerifyRequest): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun deleteAccount() {
        client.delete("${AppConfig.BASE_URL}/auth/me") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
}
