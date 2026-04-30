package br.com.edu.features.marketplace.domain

data class Product(
    val id: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val description: String,
    val price: String,
)
