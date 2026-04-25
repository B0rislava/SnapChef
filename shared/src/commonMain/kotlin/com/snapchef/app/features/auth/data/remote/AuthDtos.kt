package com.snapchef.app.features.auth.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class SignupRequest(
    val email: String,
    val name: String,
    val password: String
)

@Serializable
data class SignupResponse(
    val message: String,
    val email: String
)

@Serializable
data class VerifyRequest(
    val email: String,
    val code: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserOut(
    val id: Int,
    val email: String,
    val name: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    val user: UserOut
)

@Serializable
data class PantryItemOut(
    val id: Int,
    val name: String,
    val quantity: Int,
    val unit: String? = null,
    val source: String = "manual",
    @SerialName("session_id") val sessionId: Int? = null,
    @SerialName("image_id") val imageId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class GroupOut(
    val id: Int,
    val name: String,
    @SerialName("created_by_user_id") val createdByUserId: Int,
    @SerialName("created_at") val createdAt: String? = null,
    val code: String? = null
)

@Serializable
data class GroupMemberOut(
    val user: UserOut,
    val role: String,
    @SerialName("joined_at") val joinedAt: String? = null
)

@Serializable
data class GroupDetailOut(
    val id: Int,
    val name: String,
    @SerialName("created_by_user_id") val createdByUserId: Int,
    @SerialName("created_at") val createdAt: String? = null,
    val members: List<GroupMemberOut> = emptyList(),
    val code: String? = null
)

@Serializable
data class GroupCreateRequest(
    val name: String
)

@Serializable
data class JoinGroupRequest(
    val code: String
)

@Serializable
data class UserUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null
)
