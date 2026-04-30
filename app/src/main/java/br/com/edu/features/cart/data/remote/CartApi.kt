package br.com.edu.features.cart.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CartApi {
    @GET("cart")
    suspend fun get(): CartDto

    @POST("cart/items")
    suspend fun addItem(@Body body: CartItemInDto): CartDto

    @DELETE("cart/items/{productId}")
    suspend fun removeItem(
        @Path("productId") productId: Int,
        @Query("quantity") quantity: Int? = null,
    ): CartDto
}
