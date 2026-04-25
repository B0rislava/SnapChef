package com.snapchef.app.features.groups.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.features.home.data.remote.GroqRecipeSuggestResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GroupSharingApiService(private val client: HttpClient) {

    @Throws(Exception::class)
    suspend fun shareRecipeToGroup(request: ShareRecipeToGroupRequest) {
        client.post("${AppConfig.BASE_URL}/share/recipe") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token -> header("Authorization", "Bearer $token") }
            setBody(request)
        }
    }

    @Throws(Exception::class)
    suspend fun listSharedRecipesForGroup(groupId: Int): List<GroupSharedRecipeOut> {
        return client.get("${AppConfig.BASE_URL}/share/group/$groupId/recipes") {
            AuthManager.accessToken?.let { token -> header("Authorization", "Bearer $token") }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun fetchCombinedGroupPantry(): CombinedPantryResponse {
        return client.get("${AppConfig.BASE_URL}/ai/groups/combined-pantry") {
            AuthManager.accessToken?.let { token -> header("Authorization", "Bearer $token") }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun requestCombinedGroupMeal(count: Int = 6): GroqRecipeSuggestResponse {
        val safeCount = count.coerceIn(1, 8)
        return client.post("${AppConfig.BASE_URL}/ai/groups/combined-meal") {
            parameter("count", safeCount)
            AuthManager.accessToken?.let { token -> header("Authorization", "Bearer $token") }
        }.body()
    }
}
