package com.snapchef.app.features.home.data.remote

import com.snapchef.app.AppConfig
import com.snapchef.app.core.auth.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class HomeApiService(private val client: HttpClient) {
    
    /**
     * Uploads an image to the backend's /ai/scan endpoint using Multipart Form Data.
     * Returns exactly what the AI model detected.
     */
    suspend fun scanFridgeImage(imageBytes: ByteArray): ImageScanResponse {
        return client.post("${AppConfig.BASE_URL}/ai/scan") {
            AuthManager.accessToken?.let { token -> 
                header("Authorization", "Bearer $token")
            }
            
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg") 
                        append(HttpHeaders.ContentDisposition, "filename=\"fridge.jpg\"")
                    })
                }
            ))
        }.body()
    }

    /**
     * Hits the /recipes/suggest endpoint.
     * Takes a list of strings and returns a list of backend-generated recipes.
     */
    @Throws(Exception::class)
    suspend fun suggestRecipes(ingredients: List<String>): List<RecipeOut> {
        return client.post("${AppConfig.BASE_URL}/recipes/suggest") {
            AuthManager.accessToken?.let { token -> 
                header("Authorization", "Bearer $token")
            }
            
            contentType(ContentType.Application.Json)
            setBody(RecipeSuggestRequest(items = ingredients))
        }.body()
    }
}
