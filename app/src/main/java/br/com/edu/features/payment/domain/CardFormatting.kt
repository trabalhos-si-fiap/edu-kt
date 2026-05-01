package br.com.edu.features.payment.domain

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

const val MAX_CARD_DIGITS = 19
const val MIN_CARD_DIGITS = 13
const val EXPIRY_DIGITS = 4
const val MIN_CVV_DIGITS = 3
const val MAX_CVV_DIGITS = 4

fun sanitizeCardNumber(input: String): String =
    input.filter { it.isDigit() }.take(MAX_CARD_DIGITS)

fun sanitizeExpiry(input: String): String =
    input.filter { it.isDigit() }.take(EXPIRY_DIGITS)

fun sanitizeCvv(input: String): String =
    input.filter { it.isDigit() }.take(MAX_CVV_DIGITS)

fun formatCardNumber(digits: String): String = buildString {
    digits.forEachIndexed { i, c ->
        if (i > 0 && i % 4 == 0) append(' ')
        append(c)
    }
}

fun formatExpiry(digits: String): String =
    if (digits.length >= 3) digits.substring(0, 2) + "/" + digits.substring(2) else digits

val CardNumberTransformation = VisualTransformation { text ->
    val digits = text.text
    val formatted = formatCardNumber(digits)
    // one space is inserted before each new 4-digit group after the first
    val totalSpaces = if (digits.length <= 1) 0 else (digits.length - 1) / 4
    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val safe = offset.coerceIn(0, digits.length)
            // advance the visual cursor past every space that has been inserted up to `safe`,
            // never claiming more spaces than actually exist in the formatted string
            val spaces = minOf(safe / 4, totalSpaces)
            return (safe + spaces).coerceIn(0, formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val safe = offset.coerceIn(0, formatted.length)
            // count separator characters that fall before this transformed offset
            val spaces = formatted.take(safe).count { it == ' ' }
            return (safe - spaces).coerceIn(0, digits.length)
        }
    }
    TransformedText(AnnotatedString(formatted), mapping)
}

val ExpiryTransformation = VisualTransformation { text ->
    val digits = text.text
    val formatted = formatExpiry(digits)
    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val safe = offset.coerceIn(0, digits.length)
            val shift = if (safe >= 3) 1 else 0
            return (safe + shift).coerceIn(0, formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val safe = offset.coerceIn(0, formatted.length)
            val shift = if (safe >= 3) 1 else 0
            return (safe - shift).coerceIn(0, digits.length)
        }
    }
    TransformedText(AnnotatedString(formatted), mapping)
}

sealed interface CardFormError {
    data object InvalidNumber : CardFormError
    data object MissingName : CardFormError
    data object InvalidExpiry : CardFormError
    data object InvalidCvv : CardFormError
}

fun validateCreditCardForm(
    cardNumber: String,
    cardName: String,
    expiry: String,
    cvv: String,
    isEditing: Boolean,
): CardFormError? {
    val numberRequired = !isEditing || cardNumber.isNotEmpty()
    if (numberRequired && cardNumber.length < MIN_CARD_DIGITS) return CardFormError.InvalidNumber
    if (cardName.isBlank()) return CardFormError.MissingName
    if (expiry.length < EXPIRY_DIGITS) return CardFormError.InvalidExpiry
    if (!isEditing && cvv.length < MIN_CVV_DIGITS) return CardFormError.InvalidCvv
    return null
}

fun CardFormError.message(): String = when (this) {
    CardFormError.InvalidNumber -> "Número de cartão inválido"
    CardFormError.MissingName -> "Informe o nome"
    CardFormError.InvalidExpiry -> "Validade inválida"
    CardFormError.InvalidCvv -> "CVV inválido"
}
