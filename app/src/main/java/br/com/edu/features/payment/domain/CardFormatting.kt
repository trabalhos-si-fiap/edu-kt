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
const val CPF_DIGITS = 11
const val CNPJ_DIGITS = 14
const val MAX_TAX_ID_DIGITS = CNPJ_DIGITS

fun sanitizeCardNumber(input: String): String =
    input.filter { it.isDigit() }.take(MAX_CARD_DIGITS)

fun sanitizeExpiry(input: String): String =
    input.filter { it.isDigit() }.take(EXPIRY_DIGITS)

fun sanitizeCvv(input: String): String =
    input.filter { it.isDigit() }.take(MAX_CVV_DIGITS)

fun sanitizeTaxId(input: String): String =
    input.filter { it.isDigit() }.take(MAX_TAX_ID_DIGITS)

fun formatTaxId(digits: String): String = when {
    digits.length <= CPF_DIGITS -> formatCpf(digits)
    else -> formatCnpj(digits)
}

private fun formatCpf(digits: String): String = buildString {
    digits.forEachIndexed { i, c ->
        when (i) {
            3, 6 -> append('.')
            9 -> append('-')
        }
        append(c)
    }
}

private fun formatCnpj(digits: String): String = buildString {
    digits.forEachIndexed { i, c ->
        when (i) {
            2, 5 -> append('.')
            8 -> append('/')
            12 -> append('-')
        }
        append(c)
    }
}

val TaxIdTransformation = VisualTransformation { text ->
    val digits = text.text
    val formatted = formatTaxId(digits)
    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val safe = offset.coerceIn(0, digits.length)
            val sepsBefore = if (digits.length <= CPF_DIGITS) {
                cpfSeparatorsBefore(safe)
            } else {
                cnpjSeparatorsBefore(safe)
            }
            return (safe + sepsBefore).coerceIn(0, formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val safe = offset.coerceIn(0, formatted.length)
            val seps = formatted.take(safe).count { !it.isDigit() }
            return (safe - seps).coerceIn(0, digits.length)
        }
    }
    TransformedText(AnnotatedString(formatted), mapping)
}

private fun cpfSeparatorsBefore(pos: Int): Int {
    var n = 0
    if (pos > 3) n++
    if (pos > 6) n++
    if (pos > 9) n++
    return n
}

private fun cnpjSeparatorsBefore(pos: Int): Int {
    var n = 0
    if (pos > 2) n++
    if (pos > 5) n++
    if (pos > 8) n++
    if (pos > 12) n++
    return n
}

fun isValidCpf(digits: String): Boolean {
    if (digits.length != CPF_DIGITS) return false
    if (digits.all { it == digits[0] }) return false
    val nums = digits.map { it.digitToInt() }
    val d1 = checkDigit(nums.subList(0, 9), startWeight = 10)
    if (d1 != nums[9]) return false
    val d2 = checkDigit(nums.subList(0, 10), startWeight = 11)
    return d2 == nums[10]
}

fun isValidCnpj(digits: String): Boolean {
    if (digits.length != CNPJ_DIGITS) return false
    if (digits.all { it == digits[0] }) return false
    val nums = digits.map { it.digitToInt() }
    val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    val d1 = checkDigitWeighted(nums.subList(0, 12), weights1)
    if (d1 != nums[12]) return false
    val d2 = checkDigitWeighted(nums.subList(0, 13), weights2)
    return d2 == nums[13]
}

fun isValidTaxId(digits: String): Boolean = when (digits.length) {
    CPF_DIGITS -> isValidCpf(digits)
    CNPJ_DIGITS -> isValidCnpj(digits)
    else -> false
}

private fun checkDigit(nums: List<Int>, startWeight: Int): Int {
    var sum = 0
    for ((i, n) in nums.withIndex()) sum += n * (startWeight - i)
    val rest = sum % 11
    return if (rest < 2) 0 else 11 - rest
}

private fun checkDigitWeighted(nums: List<Int>, weights: IntArray): Int {
    var sum = 0
    for ((i, n) in nums.withIndex()) sum += n * weights[i]
    val rest = sum % 11
    return if (rest < 2) 0 else 11 - rest
}

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
    data object InvalidTaxId : CardFormError
}

fun validateCreditCardForm(
    cardNumber: String,
    cardName: String,
    expiry: String,
    cvv: String,
    taxId: String,
    isEditing: Boolean,
): CardFormError? {
    val numberRequired = !isEditing || cardNumber.isNotEmpty()
    if (numberRequired && cardNumber.length < MIN_CARD_DIGITS) return CardFormError.InvalidNumber
    if (cardName.isBlank()) return CardFormError.MissingName
    if (expiry.length < EXPIRY_DIGITS) return CardFormError.InvalidExpiry
    if (!isEditing && cvv.length < MIN_CVV_DIGITS) return CardFormError.InvalidCvv
    if (!isValidTaxId(taxId)) return CardFormError.InvalidTaxId
    return null
}

fun CardFormError.message(): String = when (this) {
    CardFormError.InvalidNumber -> "Número de cartão inválido"
    CardFormError.MissingName -> "Informe o nome"
    CardFormError.InvalidExpiry -> "Validade inválida"
    CardFormError.InvalidCvv -> "CVV inválido"
    CardFormError.InvalidTaxId -> "CPF/CNPJ inválido"
}
