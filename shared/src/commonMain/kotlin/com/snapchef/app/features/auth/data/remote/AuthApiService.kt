package com.snapchef.app.features.auth.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
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

    suspend fun googleAuth(idToken: String): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleAuthRequest(idToken = idToken))
        }.body()
    }

    suspend fun deleteAccount() {
        client.delete("${AppConfig.BASE_URL}/auth/me") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    suspend fun fetchPantryItems(): List<PantryItemOut> {
        return client.get("${AppConfig.BASE_URL}/pantry") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    suspend fun fetchGroups(): List<GroupOut> {
        return client.get("${AppConfig.BASE_URL}/groups") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    suspend fun createGroup(name: String): GroupOut {
        return client.post("${AppConfig.BASE_URL}/groups") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(GroupCreateRequest(name = name))
        }.body()
    }

    suspend fun joinGroup(code: String): GroupOut {
        return client.post("${AppConfig.BASE_URL}/groups/join") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(JoinGroupRequest(code = code))
        }.body()
    }

    suspend fun deleteGroup(id: Int) {
        client.delete("${AppConfig.BASE_URL}/groups/$id") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    suspend fun leaveGroup(id: Int) {
        client.post("${AppConfig.BASE_URL}/groups/$id/leave") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
    
    suspend fun fetchGroupDetail(id: Int): GroupDetailOut {
        return client.get("${AppConfig.BASE_URL}/groups/$id") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }
}
