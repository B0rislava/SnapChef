package com.snapchef.app.features.auth.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(private val client: HttpClient) {

    suspend fun signup(request: SignupRequest): SignupResponse {
        return client.post("${AppConfig.BASE_URL}/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun verify(request: VerifyRequest): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteAccount() {
        client.delete("${AppConfig.BASE_URL}/auth/me") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
}
