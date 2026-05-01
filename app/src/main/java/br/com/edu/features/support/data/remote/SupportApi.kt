package br.com.edu.features.support.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SupportApi {
    @GET("support")
    suspend fun list(): List<SupportMessageDto>

    @POST("support")
    suspend fun send(@Body body: SupportMessageInDto): List<SupportMessageDto>
}
