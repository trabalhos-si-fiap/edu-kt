package br.com.edu.features.auth.data

import br.com.edu.EduApplication
import br.com.edu.core.network.ApiClient
import br.com.edu.features.auth.data.remote.AuthApi
import br.com.edu.features.auth.data.remote.ErrorResponse
import br.com.edu.features.auth.data.remote.LoginRequest
import br.com.edu.features.auth.data.remote.LogoutRequest
import br.com.edu.features.auth.data.remote.RegisterRequest
import br.com.edu.features.auth.data.remote.TokenPairResponse
import kotlinx.serialization.json.Json
import retrofit2.Response

class AuthRepository(
    private val api: AuthApi = ApiClient.create(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = api.login(LoginRequest(email = email.trim(), password = password))
        handleTokenPair(response)
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
        handleTokenPair(response)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val refresh = EduApplication.tokenStore.currentRefresh()
        if (!refresh.isNullOrBlank()) {
            runCatching { api.logout(LogoutRequest(refresh = refresh)) }
        }
        EduApplication.tokenStore.clear()
    }

    private fun handleTokenPair(response: Response<TokenPairResponse>): String {
        if (!response.isSuccessful) {
            val raw = response.errorBody()?.string().orEmpty()
            val message = runCatching { json.decodeFromString<ErrorResponse>(raw).detail }
                .getOrNull()
                ?: "Falha (${response.code()})"
            throw IllegalStateException(message)
        }
        val body = response.body() ?: throw IllegalStateException("Resposta vazia do servidor")
        EduApplication.tokenStore.setPair(body.access, body.refresh)
        return body.email
    }
}
