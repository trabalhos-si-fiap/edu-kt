package br.com.edu.features.marketplace.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.marketplace.data.remote.ProductApi
import br.com.edu.features.marketplace.data.remote.ProductDto
import br.com.edu.features.marketplace.domain.Product

class MarketplaceRepository(
    private val api: ProductApi = ApiClient.create(),
) {
    suspend fun listProducts(): List<Product> = api.list().map { it.toDomain() }
}

private fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    type = type,
    subtype = subtype,
    description = description,
    price = price,
)
