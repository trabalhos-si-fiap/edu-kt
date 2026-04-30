package br.com.edu.features.auth.data

import br.com.edu.core.auth.TokenStore
import br.com.edu.core.network.ApiClient
import br.com.edu.features.auth.data.remote.AuthApi
import br.com.edu.features.auth.data.remote.ErrorResponse
import br.com.edu.features.auth.data.remote.LoginRequest
import br.com.edu.features.auth.data.remote.RegisterRequest
import kotlinx.serialization.json.Json
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi = ApiClient.create(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = api.login(LoginRequest(email = email.trim(), password = password))
        handleToken(response)
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthDate: String,
    ): Result<String> = runCatching {
        val response = api.register(
            RegisterRequest(
                email = email.trim(),
                password = password,
                name = name.trim(),
                phone = phone,
                birth_date = birthDate,
            ),
        )
        handleToken(response)
    }

    private fun handleToken(response: Response<br.com.edu.features.auth.data.remote.TokenResponse>): String {
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string().orEmpty()
            val message = runCatching { json.decodeFromString<ErrorResponse>(raw).detail }
                .getOrNull()
                ?: "Falha (${response.code()})"
            throw IllegalStateException(message)
        }
        val body = response.body() ?: throw IllegalStateException("Resposta vazia do servidor")
        TokenStore.set(body.token)
        return body.email
    }
}
