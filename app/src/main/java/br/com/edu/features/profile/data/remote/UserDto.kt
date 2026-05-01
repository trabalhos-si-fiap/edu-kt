package br.com.edu.features.profile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val phone: String = "",
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("date_joined") val dateJoined: String,
)

@Serializable
data class UserPatchDto(
    val name: String? = null,
    val phone: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
)
