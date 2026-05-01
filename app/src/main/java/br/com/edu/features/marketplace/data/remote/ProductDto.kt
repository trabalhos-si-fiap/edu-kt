package br.com.edu.features.marketplace.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val description: String,
    val price: String,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
)

@Serializable
data class ProductListDto(
    val items: List<ProductDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)

@Serializable
data class ReviewDto(
    val id: Int,
    val author: String,
    val rating: Int,
    val comment: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ReviewInDto(
    val rating: Int,
    val comment: String = "",
)

@Serializable
data class ReviewListDto(
    val items: List<ReviewDto>,
    val total: Int,
    @SerialName("rating_avg") val ratingAvg: Double,
    @SerialName("rating_count") val ratingCount: Int,
)
