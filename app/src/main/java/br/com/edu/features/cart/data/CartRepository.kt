package br.com.edu.features.cart.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.cart.data.remote.CartApi
import br.com.edu.features.cart.data.remote.CartDto
import br.com.edu.features.cart.data.remote.CartItemDto
import br.com.edu.features.cart.data.remote.CartItemInDto
import br.com.edu.features.cart.domain.Cart
import br.com.edu.features.cart.domain.CartItem

class CartRepository(
    private val api: CartApi = ApiClient.create(),
) {
    suspend fun getCart(): Cart = api.get().toDomain()

    suspend fun addItem(productId: Int, quantity: Int = 1): Cart =
        api.addItem(CartItemInDto(productId = productId, quantity = quantity)).toDomain()

    suspend fun removeItem(productId: Int, quantity: Int? = null): Cart =
        api.removeItem(productId = productId, quantity = quantity).toDomain()
}

private fun CartDto.toDomain() = Cart(
    items = items.map { it.toDomain() },
    total = total,
)

private fun CartItemDto.toDomain() = CartItem(
    productId = productId,
    name = name,
    type = type,
    subtype = subtype,
    price = price,
    quantity = quantity,
    subtotal = subtotal,
    imageUrl = imageUrl,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
)
