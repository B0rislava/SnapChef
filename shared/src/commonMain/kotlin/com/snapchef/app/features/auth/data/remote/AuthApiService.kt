package com.snapchef.app.features.auth.data.remote

import com.snapchef.app.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(private val client: HttpClient) {

    suspend fun signup(request: SignupRequest): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/signup") {
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
}
