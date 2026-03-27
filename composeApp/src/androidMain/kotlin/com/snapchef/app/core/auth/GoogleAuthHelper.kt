package com.snapchef.app.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import com.snapchef.app.AppConfig

object GoogleAuthHelper {

    // The Web Client ID from Google Cloud Console
    private val WEB_CLIENT_ID =  AppConfig.GOOGLE_WEB_CLIENT_ID

    suspend fun signInWithGoogle(context: Context): String? {
        return withContext(Dispatchers.IO) {
            val credentialManager = CredentialManager.create(context)

            // Generate a nonce to prevent replay attacks
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    return@withContext credential.idToken
                } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    return@withContext googleIdTokenCredential.idToken
                }

                return@withContext null
            } catch (e: GetCredentialException) {
                Log.e("GoogleAuth", "Failed to get google credential: ${e.message}")
                return@withContext null
            }
        }
    }
}
