package br.com.edu.features.marketplace.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun list(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): ProductListDto

    @GET("products/{id}/reviews")
    suspend fun reviews(
        @Path("id") productId: Int,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): ReviewListDto

    @POST("products/{id}/reviews")
    suspend fun createReview(
        @Path("id") productId: Int,
        @Body body: ReviewInDto,
    ): ReviewDto
}
