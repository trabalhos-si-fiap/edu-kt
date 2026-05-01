package br.com.edu.features.profile.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.profile.data.remote.AddressApi
import br.com.edu.features.profile.data.remote.AddressDto
import br.com.edu.features.profile.data.remote.AddressInDto
import br.com.edu.features.profile.data.remote.AddressPatchDto
import br.com.edu.features.profile.domain.Address

class AddressRepository(
    private val api: AddressApi = ApiClient.create(),
) {
    suspend fun list(): List<Address> = api.list().map { it.toDomain() }

    suspend fun create(input: AddressInDto): Address = api.create(input).toDomain()

    suspend fun update(id: Int, patch: AddressPatchDto): Address =
        api.update(id, patch).toDomain()

    suspend fun delete(id: Int) {
        api.delete(id)
    }
}

private fun AddressDto.toDomain() = Address(
    id = id,
    label = label,
    zipCode = zipCode,
    street = street,
    number = number,
    complement = complement,
    neighborhood = neighborhood,
    city = city,
    state = state,
    isFavorite = isFavorite,
)
