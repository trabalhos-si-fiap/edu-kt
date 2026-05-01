package br.com.edu.features.profile.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AddressApi {
    @GET("auth/addresses")
    suspend fun list(): List<AddressDto>

    @POST("auth/addresses")
    suspend fun create(@Body body: AddressInDto): AddressDto

    @PATCH("auth/addresses/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: AddressPatchDto): AddressDto

    @DELETE("auth/addresses/{id}")
    suspend fun delete(@Path("id") id: Int)
}
