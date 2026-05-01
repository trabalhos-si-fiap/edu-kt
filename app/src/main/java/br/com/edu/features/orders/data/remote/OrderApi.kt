package br.com.edu.features.orders.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @GET("orders")
    suspend fun list(): List<OrderDto>

    @POST("orders")
    suspend fun create(): OrderDto

    @POST("orders/{orderId}/rebuy")
    suspend fun rebuy(@Path("orderId") orderId: Int): br.com.edu.features.cart.data.remote.CartDto
}
