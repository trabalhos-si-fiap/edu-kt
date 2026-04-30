package br.com.edu.features.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<TokenResponse>
}
