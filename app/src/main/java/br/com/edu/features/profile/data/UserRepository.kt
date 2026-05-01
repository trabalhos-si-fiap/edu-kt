package br.com.edu.features.profile.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.profile.data.remote.UserApi
import br.com.edu.features.profile.data.remote.UserDto
import br.com.edu.features.profile.data.remote.UserPatchDto
import br.com.edu.features.profile.domain.UserProfile

class UserRepository(
    private val api: UserApi = ApiClient.create(),
) {
    suspend fun getProfile(): UserProfile = api.me().toDomain()

    suspend fun updateProfile(
        name: String? = null,
        phone: String? = null,
        birthDate: String? = null,
    ): UserProfile = api.update(UserPatchDto(name = name, phone = phone, birthDate = birthDate)).toDomain()
}

private fun UserDto.toDomain() = UserProfile(
    id = id,
    email = email,
    name = name,
    phone = phone,
    birthDate = birthDate,
    dateJoined = dateJoined,
)
