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
     * Uploads images to the backend's /ai/sessions endpoint using Multipart Form Data.
     * The backend returns NDJSON. We read the full string, split by newline, and parse the final JSON.
     */
    suspend fun scanFridgeImages(imagesBytes: List<ByteArray>): ScanSessionResponse {
        val responseText: String = client.post("${AppConfig.BASE_URL}/ai/sessions") {
            AuthManager.accessToken?.let { token -> 
                header("Authorization", "Bearer $token")
            }
            
            setBody(MultiPartFormDataContent(
                formData {
                    imagesBytes.forEachIndexed { index, bytes ->
                        append("files", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg") 
                            append(HttpHeaders.ContentDisposition, "filename=\"fridge_$index.jpg\"")
                        })
                    }
                }
            ))
        }.body()

        // It returns JSON lines (NDJSON). The last line with status_msg == "done" has the full items array.
        val lines = responseText.split("\n").filter { it.isNotBlank() }
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }
        
        var bestSessionResponse = ScanSessionResponse()
        for (line in lines) {
            try {
                val parsed = json.decodeFromString<ScanSessionResponse>(line)
                if (parsed.status_msg == "done" || parsed.items.isNotEmpty()) {
                    bestSessionResponse = parsed
                }
            } catch (e: Exception) {
                // Ignore parsing errors of intermediate lines
            }
        }
        return bestSessionResponse
    }

    /**
     * Confirms the session before recipes can be generated.
     */
    suspend fun confirmSession(sessionId: Int): ScanSessionResponse {
        return client.post("${AppConfig.BASE_URL}/ai/sessions/$sessionId/confirm") {
            AuthManager.accessToken?.let { token -> 
                header("Authorization", "Bearer $token")
            }
        }.body()
    }

    /**
     * Hits the /ai/sessions/{id}/groq-recipes endpoint.
     */
    suspend fun suggestRecipes(sessionId: Int): GroqRecipeSuggestResponse {
        return client.post("${AppConfig.BASE_URL}/ai/sessions/$sessionId/groq-recipes") {
            AuthManager.accessToken?.let { token -> 
                header("Authorization", "Bearer $token")
            }
        }.body()
    }
}
