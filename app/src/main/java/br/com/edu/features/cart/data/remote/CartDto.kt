package br.com.edu.features.cart.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    @SerialName("product_id") val productId: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val price: String,
    val quantity: Int,
    val subtotal: String,
)

@Serializable
data class CartDto(
    val items: List<CartItemDto>,
    val total: String,
)

@Serializable
data class CartItemInDto(
    @SerialName("product_id") val productId: Int,
    val quantity: Int = 1,
)
