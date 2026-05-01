package br.com.edu.features.profile.domain

data class UserProfile(
    val id: Int,
    val email: String,
    val name: String,
    val phone: String,
    val birthDate: String?,
    val dateJoined: String,
)
