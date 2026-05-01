package br.com.edu.features.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String = "",
    val phone: String = "",
    val birth_date: String = "",
)

@Serializable
data class TokenPairResponse(
    val access: String,
    val refresh: String,
    val email: String,
)

@Serializable
data class RefreshRequest(val refresh: String)

@Serializable
data class RefreshResponse(val access: String, val refresh: String)

@Serializable
data class LogoutRequest(val refresh: String)

@Serializable
data class ErrorResponse(val detail: String? = null)
