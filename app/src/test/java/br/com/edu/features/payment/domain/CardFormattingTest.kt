package br.com.edu.features.payment.domain

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardFormattingTest {

    private fun VisualTransformation.applyTo(input: String): TransformedText =
        filter(AnnotatedString(input))

    // --- sanitize ---

    @Test
    fun `sanitizeCardNumber strips non-digits and caps at 19`() {
        assertEquals("4444444444444444", sanitizeCardNumber("4444 4444 4444 4444"))
        assertEquals("123456789012", sanitizeCardNumber("1234-5678-9012"))
        assertEquals("1".repeat(19), sanitizeCardNumber("1".repeat(25)))
    }

    @Test
    fun `sanitizeExpiry caps at 4 digits`() {
        assertEquals("1230", sanitizeExpiry("12/30"))
        assertEquals("1234", sanitizeExpiry("12345"))
    }

    @Test
    fun `sanitizeCvv caps at 4 digits and strips non-digits`() {
        assertEquals("123", sanitizeCvv("1a2b3"))
        assertEquals("1234", sanitizeCvv("12345"))
    }

    // --- format ---

    @Test
    fun `formatCardNumber inserts spaces every 4 digits`() {
        assertEquals("", formatCardNumber(""))
        assertEquals("4444", formatCardNumber("4444"))
        assertEquals("4444 4", formatCardNumber("44444"))
        assertEquals("4444 4444 4444", formatCardNumber("444444444444"))
        assertEquals("4444 4444 4444 4444", formatCardNumber("4".repeat(16)))
        assertEquals("4444 4444 4444 4444 444", formatCardNumber("4".repeat(19)))
    }

    @Test
    fun `formatExpiry inserts slash after second digit`() {
        assertEquals("", formatExpiry(""))
        assertEquals("1", formatExpiry("1"))
        assertEquals("12", formatExpiry("12"))
        assertEquals("12/3", formatExpiry("123"))
        assertEquals("12/34", formatExpiry("1234"))
    }

    // --- OffsetMapping invariants ---
    //
    // Compose requires:
    //   originalToTransformed(originalLength) == transformedLength
    //   transformedToOriginal(transformedLength) == originalLength
    // and both must remain in [0, length] for any input in their respective ranges.
    // Violating these crashes BasicTextField with IllegalStateException,
    // which is the bug that caused the AddPaymentMethod screen to crash on
    // long card numbers (17–19 digits accepted by sanitizeCardNumber).

    private fun assertCardMappingInvariants(input: String) {
        val tt = CardNumberTransformation.applyTo(input)
        val mapping = tt.offsetMapping
        val origLen = input.length
        val transLen = tt.text.length

        assertEquals(
            "originalToTransformed(originalLength) must equal transformedLength for input length=$origLen",
            transLen,
            mapping.originalToTransformed(origLen),
        )
        assertEquals(
            "transformedToOriginal(transformedLength) must equal originalLength for input length=$origLen",
            origLen,
            mapping.transformedToOriginal(transLen),
        )

        for (off in 0..origLen) {
            val t = mapping.originalToTransformed(off)
            require(t in 0..transLen) { "originalToTransformed($off)=$t out of [0,$transLen] (len=$origLen)" }
        }
        for (off in 0..transLen) {
            val o = mapping.transformedToOriginal(off)
            require(o in 0..origLen) { "transformedToOriginal($off)=$o out of [0,$origLen] (len=$origLen)" }
        }
    }

    @Test
    fun `card mapping holds invariants for empty input`() {
        assertCardMappingInvariants("")
    }

    @Test
    fun `card mapping holds invariants for 12-digit user-reported case`() {
        // Reported crash repro: "4444 4444 4444" → 12 digits.
        assertCardMappingInvariants("444444444444")
    }

    @Test
    fun `card mapping holds invariants across all relevant lengths`() {
        // Every length the screen can produce (sanitizeCardNumber caps at 19).
        for (len in 0..MAX_CARD_DIGITS) {
            assertCardMappingInvariants("4".repeat(len))
        }
    }

    @Test
    fun `card mapping holds invariants at boundary lengths 16 17 18 19`() {
        // Old implementation capped the space count at 3 — broke on 17+.
        assertCardMappingInvariants("4".repeat(16))
        assertCardMappingInvariants("4".repeat(17))
        assertCardMappingInvariants("4".repeat(18))
        assertCardMappingInvariants("4".repeat(19))
    }

    @Test
    fun `card mapping cursor positions are sensible for 12 digits`() {
        val tt = CardNumberTransformation.applyTo("444444444444")
        val m = tt.offsetMapping
        // "4444 4444 4444" — after-space convention
        assertEquals(0, m.originalToTransformed(0))
        assertEquals(5, m.originalToTransformed(4))   // after first space, before 5th digit
        assertEquals(10, m.originalToTransformed(8))  // after second space
        assertEquals(14, m.originalToTransformed(12)) // end (no trailing space at length 12)
        // inverse
        assertEquals(0, m.transformedToOriginal(0))
        assertEquals(4, m.transformedToOriginal(5))   // just past first space → original 4
        assertEquals(12, m.transformedToOriginal(14)) // end
    }

    @Test
    fun `card mapping cursor positions are sensible for 16 digits`() {
        val tt = CardNumberTransformation.applyTo("4".repeat(16))
        val m = tt.offsetMapping
        assertEquals(19, m.originalToTransformed(16))
        assertEquals(16, m.transformedToOriginal(19))
        assertEquals(5, m.originalToTransformed(4))   // position right after 1st space
        assertEquals(4, m.transformedToOriginal(4))   // before 1st space → original 4
    }

    @Test
    fun `card mapping cursor positions are sensible for 19 digits`() {
        val tt = CardNumberTransformation.applyTo("4".repeat(19))
        val m = tt.offsetMapping
        // formatted: "4444 4444 4444 4444 444" length 23
        assertEquals(23, tt.text.length)
        assertEquals(23, m.originalToTransformed(19))
        assertEquals(19, m.transformedToOriginal(23))
    }

    private fun assertExpiryMappingInvariants(input: String) {
        val tt = ExpiryTransformation.applyTo(input)
        val mapping = tt.offsetMapping
        val origLen = input.length
        val transLen = tt.text.length
        assertEquals(transLen, mapping.originalToTransformed(origLen))
        assertEquals(origLen, mapping.transformedToOriginal(transLen))
        for (off in 0..origLen) {
            val t = mapping.originalToTransformed(off)
            require(t in 0..transLen)
        }
        for (off in 0..transLen) {
            val o = mapping.transformedToOriginal(off)
            require(o in 0..origLen)
        }
    }

    @Test
    fun `expiry mapping holds invariants for all valid lengths`() {
        for (len in 0..EXPIRY_DIGITS) {
            assertExpiryMappingInvariants("1".repeat(len))
        }
    }

    // --- validation ---

    @Test
    fun `validate rejects card number under 13 digits when adding`() {
        assertEquals(
            CardFormError.InvalidNumber,
            validateCreditCardForm(
                cardNumber = "444444444444", // 12 digits — user repro
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "123",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate accepts 13 digit card with all fields filled`() {
        assertNull(
            validateCreditCardForm(
                cardNumber = "4".repeat(13),
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "123",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate accepts 19 digit card (Maestro)`() {
        assertNull(
            validateCreditCardForm(
                cardNumber = "4".repeat(19),
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "1234",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate allows empty card number when editing`() {
        assertNull(
            validateCreditCardForm(
                cardNumber = "",
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "",
                isEditing = true,
            ),
        )
    }

    @Test
    fun `validate rejects partial card number when editing`() {
        assertEquals(
            CardFormError.InvalidNumber,
            validateCreditCardForm(
                cardNumber = "12345", // not blank, but < 13
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "",
                isEditing = true,
            ),
        )
    }

    @Test
    fun `validate rejects blank cardholder name`() {
        assertEquals(
            CardFormError.MissingName,
            validateCreditCardForm(
                cardNumber = "4".repeat(16),
                cardName = "   ",
                expiry = "1230",
                cvv = "123",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate rejects expiry shorter than 4 digits`() {
        assertEquals(
            CardFormError.InvalidExpiry,
            validateCreditCardForm(
                cardNumber = "4".repeat(16),
                cardName = "JOHN DOE",
                expiry = "12",
                cvv = "123",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate rejects cvv shorter than 3 when adding`() {
        assertEquals(
            CardFormError.InvalidCvv,
            validateCreditCardForm(
                cardNumber = "4".repeat(16),
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "12",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate does not require cvv when editing`() {
        assertNull(
            validateCreditCardForm(
                cardNumber = "",
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "",
                isEditing = true,
            ),
        )
    }

    @Test
    fun `error messages are user friendly Portuguese strings`() {
        assertEquals("Número de cartão inválido", CardFormError.InvalidNumber.message())
        assertEquals("Informe o nome", CardFormError.MissingName.message())
        assertEquals("Validade inválida", CardFormError.InvalidExpiry.message())
        assertEquals("CVV inválido", CardFormError.InvalidCvv.message())
    }
}
