package br.com.edu.features.profile.domain

data class Address(
    val id: Int,
    val label: String,
    val zipCode: String,
    val street: String,
    val number: String,
    val complement: String,
    val neighborhood: String,
    val city: String,
    val state: String,
    val isFavorite: Boolean,
)
