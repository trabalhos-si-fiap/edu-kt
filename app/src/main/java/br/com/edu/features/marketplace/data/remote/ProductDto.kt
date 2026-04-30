package br.com.edu.features.marketplace.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val description: String,
    val price: String,
)
