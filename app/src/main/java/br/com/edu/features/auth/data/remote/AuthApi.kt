package br.com.edu.features.auth.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenPairResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<TokenPairResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<Unit>
}
