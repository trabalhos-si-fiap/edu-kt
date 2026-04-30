package br.com.edu.features.marketplace.data.remote

import retrofit2.http.GET

interface ProductApi {
    @GET("products")
    suspend fun list(): List<ProductDto>
}
