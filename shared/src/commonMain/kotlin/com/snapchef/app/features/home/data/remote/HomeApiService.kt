package com.snapchef.app.features.home.data.remote

import com.snapchef.app.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class HomeApiService(private val client: HttpClient) {
    
    suspend fun analyzeIngredients(imageBase64: String): IngredientAnalysisResponse {
        return client.post("${AppConfig.BASE_URL}/analyze") {
            contentType(ContentType.Application.Json)
            setBody(IngredientAnalysisRequest(imageBase64))
        }.body()
    }

    suspend fun getRecipes(ingredients: List<String>): RecipeResponse {
        return client.post("${AppConfig.BASE_URL}/recipes") {
            contentType(ContentType.Application.Json)
            setBody(RecipeGenerationRequest(ingredients))
        }.body()
    }
}
