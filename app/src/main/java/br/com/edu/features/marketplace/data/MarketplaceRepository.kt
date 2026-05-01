package br.com.edu.features.marketplace.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.marketplace.data.remote.ProductApi
import br.com.edu.features.marketplace.data.remote.ProductDto
import br.com.edu.features.marketplace.data.remote.ReviewDto
import br.com.edu.features.marketplace.domain.Product
import br.com.edu.features.marketplace.domain.Review

class MarketplaceRepository(
    private val api: ProductApi = ApiClient.create(),
) {
    suspend fun listCategories(): List<String> = api.categories()
        .items
        .map { it.type }
        .filter { it.isNotBlank() }

    suspend fun listProducts(
        query: String? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): List<Product> = api.list(q = query?.takeIf { it.isNotBlank() }, limit = limit, offset = offset)
        .items
        .map { it.toDomain() }

    suspend fun getProduct(productId: Int): Product = api.get(productId).toDomain()

    suspend fun listReviews(
        productId: Int,
        limit: Int? = null,
        offset: Int? = null,
    ): List<Review> = api.reviews(productId = productId, limit = limit, offset = offset)
        .items
        .map { it.toDomain() }
}

private fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    type = type,
    subtype = subtype,
    description = description,
    price = price,
    imageUrl = imageUrl,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
)

private fun ReviewDto.toDomain() = Review(
    id = id,
    author = author,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
)
