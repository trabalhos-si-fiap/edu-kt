package br.com.edu.features.payment.domain

import kotlinx.serialization.Serializable

enum class PaymentMethodType { CREDIT_CARD, PIX, BOLETO }

@Serializable
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val isDefault: Boolean = false,
    val cardLast4: String? = null,
    val cardBrand: String? = null,
    val cardholderName: String? = null,
    val cardExpiry: String? = null,
    val pixKey: String? = null,
)

fun brandFromNumber(digits: String): String = when (digits.firstOrNull()) {
    '4' -> "Visa"
    '5' -> "Mastercard"
    '3' -> "Amex"
    '6' -> "Elo"
    else -> "Cartão"
}
