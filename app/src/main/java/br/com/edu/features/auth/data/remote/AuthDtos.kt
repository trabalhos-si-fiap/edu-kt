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
data class TokenResponse(val token: String, val email: String)

@Serializable
data class ErrorResponse(val detail: String? = null)
