package br.com.edu.features.orders.domain

data class OrderItem(
    val productId: Int,
    val productName: String,
    val unitPrice: String,
    val quantity: Int,
    val imageUrl: String,
    val ratingAvg: Double,
    val ratingCount: Int,
)

data class Order(
    val id: Int,
    val total: String,
    val paymentMethod: String,
    val createdAt: String,
    val items: List<OrderItem>,
) {
    val itemsCount: Int get() = items.sumOf { it.quantity }
}
