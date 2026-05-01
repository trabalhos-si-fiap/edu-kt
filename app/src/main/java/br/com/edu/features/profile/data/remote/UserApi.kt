package br.com.edu.features.profile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApi {
    @GET("auth/me")
    suspend fun me(): UserDto

    @PATCH("auth/me")
    suspend fun update(@Body body: UserPatchDto): UserDto
}
