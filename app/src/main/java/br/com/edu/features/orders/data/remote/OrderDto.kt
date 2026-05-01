package br.com.edu.features.orders.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    @SerialName("product_id") val productId: Int,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: String,
    val quantity: Int,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
)

@Serializable
data class OrderDto(
    val id: Int,
    val total: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("created_at") val createdAt: String,
    val items: List<OrderItemDto>,
)
