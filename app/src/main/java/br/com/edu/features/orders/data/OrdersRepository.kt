package br.com.edu.features.orders.data

import br.com.edu.core.network.ApiClient
import br.com.edu.features.orders.data.remote.OrderApi
import br.com.edu.features.orders.data.remote.OrderDto
import br.com.edu.features.orders.data.remote.OrderItemDto
import br.com.edu.features.orders.domain.Order
import br.com.edu.features.orders.domain.OrderItem

class OrdersRepository(
    private val api: OrderApi = ApiClient.create(),
) {
    suspend fun listOrders(): List<Order> = api.list().map { it.toDomain() }

    suspend fun placeOrder(): Order = api.create().toDomain()

    suspend fun rebuy(orderId: Int) {
        api.rebuy(orderId)
    }
}

private fun OrderDto.toDomain() = Order(
    id = id,
    total = total,
    paymentMethod = paymentMethod,
    createdAt = createdAt,
    items = items.map { it.toDomain() },
)

private fun OrderItemDto.toDomain() = OrderItem(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    imageUrl = imageUrl,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
)
