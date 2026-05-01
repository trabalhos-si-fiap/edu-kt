package br.com.edu.features.cart.domain

data class CartItem(
    val productId: Int,
    val name: String,
    val type: String,
    val subtype: String,
    val price: String,
    val quantity: Int,
    val subtotal: String,
    val imageUrl: String,
    val ratingAvg: Double,
    val ratingCount: Int,
)

data class Cart(
    val items: List<CartItem>,
    val total: String,
) {
    val totalQuantity: Int get() = items.sumOf { it.quantity }

    companion object {
        val EMPTY = Cart(items = emptyList(), total = "0")
    }
}
