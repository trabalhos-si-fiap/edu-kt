package br.com.edu.features.profile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
    val id: Int,
    val label: String = "",
    @SerialName("zip_code") val zipCode: String,
    val street: String,
    val number: String,
    val complement: String = "",
    val neighborhood: String,
    val city: String,
    val state: String,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
)

@Serializable
data class AddressInDto(
    val label: String = "",
    @SerialName("zip_code") val zipCode: String,
    val street: String,
    val number: String,
    val complement: String = "",
    val neighborhood: String,
    val city: String,
    val state: String,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
)

@Serializable
data class AddressPatchDto(
    val label: String? = null,
    @SerialName("zip_code") val zipCode: String? = null,
    val street: String? = null,
    val number: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean? = null,
)
