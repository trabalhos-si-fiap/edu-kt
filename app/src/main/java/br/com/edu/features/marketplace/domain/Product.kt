package br.com.edu.features.marketplace.domain

data class Product(
    val id: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val description: String,
    val price: String,
    val imageUrl: String = "",
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
)

data class Review(
    val id: Int,
    val author: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
)
