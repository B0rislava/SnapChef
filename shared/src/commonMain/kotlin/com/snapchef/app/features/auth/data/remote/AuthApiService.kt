package com.snapchef.app.features.auth.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import com.snapchef.app.features.home.data.remote.SessionRecipeOut
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(private val client: HttpClient) {

    @Throws(Exception::class)
    suspend fun signup(request: SignupRequest): SignupResponse {
        return client.post("${AppConfig.BASE_URL}/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
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
    suspend fun googleAuth(idToken: String): AuthResponse {
        return client.post("${AppConfig.BASE_URL}/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleAuthRequest(idToken = idToken))
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

    @Throws(Exception::class)
    suspend fun updateMe(request: UserUpdateRequest): UserOut {
        return client.patch("${AppConfig.BASE_URL}/auth/me") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(request)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun fetchPantryItems(): List<PantryItemOut> {
        return client.get("${AppConfig.BASE_URL}/pantry") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun deletePantryItem(id: Int) {
        client.delete("${AppConfig.BASE_URL}/pantry/$id") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun getCurrentUser(): UserOut {
        return client.get("${AppConfig.BASE_URL}/auth/me") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun updateProfile(name: String): UserOut {
        return client.patch("${AppConfig.BASE_URL}/auth/me") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(UpdateProfileRequest(name = name))
        }.body()
    }

    @Throws(Exception::class)
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        client.post("${AppConfig.BASE_URL}/auth/change-password") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                )
            )
        }
    }

    @Throws(Exception::class)
    suspend fun shareRecipe(request: ShareRecipeRequest): SharedRecipeOut {
        return client.post("${AppConfig.BASE_URL}/share/recipe") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(request)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun listGroupSharedRecipes(groupId: Int, limit: Int = 50, offset: Int = 0): List<SharedRecipeOut> {
        return client.get("${AppConfig.BASE_URL}/share/group/$groupId/recipes") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun deleteSharedRecipe(sharedRecipeId: Int) {
        client.delete("${AppConfig.BASE_URL}/share/recipe/$sharedRecipeId") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun getRecommendedRecipes(count: Int = 6): RecommendedRecipesOut {
        return client.get("${AppConfig.BASE_URL}/recipes/recommended") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            parameter("count", count)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun listCatalogFavoriteRecipes(): List<RecipeOut> {
        return client.get("${AppConfig.BASE_URL}/recipes/favorites") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun starCatalogRecipe(recipeId: Int) {
        client.post("${AppConfig.BASE_URL}/recipes/$recipeId/star") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun unstarCatalogRecipe(recipeId: Int) {
        client.delete("${AppConfig.BASE_URL}/recipes/$recipeId/star") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun favoriteSessionRecipe(sessionRecipeId: Int): SessionRecipeOut {
        return client.post("${AppConfig.BASE_URL}/ai/recipes/$sessionRecipeId/favorite") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun unfavoriteSessionRecipe(sessionRecipeId: Int): SessionRecipeOut {
        return client.delete("${AppConfig.BASE_URL}/ai/recipes/$sessionRecipeId/favorite") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun listFavoriteSessionRecipes(limit: Int = 50, offset: Int = 0): List<SessionRecipeOut> {
        return client.get("${AppConfig.BASE_URL}/ai/recipes/favorites") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }

    @Throws(Exception::class)
    suspend fun fetchGroups(): List<GroupOut> {
        return client.get("${AppConfig.BASE_URL}/groups") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    @Throws(Exception::class)
    suspend fun createGroup(name: String): GroupOut {
        return client.post("${AppConfig.BASE_URL}/groups") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(GroupCreateRequest(name = name))
        }.body()
    }

    @Throws(Exception::class)
    suspend fun joinGroup(code: String): GroupOut {
        return client.post("${AppConfig.BASE_URL}/groups/join") {
            contentType(ContentType.Application.Json)
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
            setBody(JoinGroupRequest(code = code))
        }.body()
    }

    @Throws(Exception::class)
    suspend fun deleteGroup(id: Int) {
        client.delete("${AppConfig.BASE_URL}/groups/$id") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun leaveGroup(id: Int) {
        client.post("${AppConfig.BASE_URL}/groups/$id/leave") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun kickMember(groupId: Int, userId: Int) {
        client.post("${AppConfig.BASE_URL}/groups/$groupId/remove-member/$userId") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
    
    @Throws(Exception::class)
    suspend fun fetchGroupDetail(id: Int): GroupDetailOut {
        return client.get("${AppConfig.BASE_URL}/groups/$id") {
            AuthManager.accessToken?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }.body()
    }
}
