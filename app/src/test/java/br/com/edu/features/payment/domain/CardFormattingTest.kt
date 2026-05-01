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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
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
                taxId = "39053344705",
                isEditing = true,
            ),
        )
    }

    @Test
    fun `validate rejects invalid taxId when adding`() {
        assertEquals(
            CardFormError.InvalidTaxId,
            validateCreditCardForm(
                cardNumber = "4".repeat(16),
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "123",
                taxId = "12345678900",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `validate rejects empty taxId when editing`() {
        assertEquals(
            CardFormError.InvalidTaxId,
            validateCreditCardForm(
                cardNumber = "",
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "",
                taxId = "",
                isEditing = true,
            ),
        )
    }

    @Test
    fun `validate accepts a valid CNPJ`() {
        assertNull(
            validateCreditCardForm(
                cardNumber = "4".repeat(16),
                cardName = "JOHN DOE",
                expiry = "1230",
                cvv = "123",
                taxId = "11222333000181",
                isEditing = false,
            ),
        )
    }

    @Test
    fun `error messages are user friendly Portuguese strings`() {
        assertEquals("Número de cartão inválido", CardFormError.InvalidNumber.message())
        assertEquals("Informe o nome", CardFormError.MissingName.message())
        assertEquals("Validade inválida", CardFormError.InvalidExpiry.message())
        assertEquals("CVV inválido", CardFormError.InvalidCvv.message())
        assertEquals("CPF/CNPJ inválido", CardFormError.InvalidTaxId.message())
    }

    // --- tax id sanitize / format ---

    @Test
    fun `sanitizeTaxId strips non-digits and caps at 14`() {
        assertEquals("39053344705", sanitizeTaxId("390.533.447-05"))
        assertEquals("11222333000181", sanitizeTaxId("11.222.333/0001-81"))
        assertEquals("12345678901234", sanitizeTaxId("1234567890123456"))
    }

    @Test
    fun `formatTaxId masks as CPF up to 11 digits and as CNPJ from 12 onwards`() {
        assertEquals("", formatTaxId(""))
        assertEquals("123", formatTaxId("123"))
        assertEquals("123.456", formatTaxId("123456"))
        assertEquals("123.456.789", formatTaxId("123456789"))
        assertEquals("123.456.789-01", formatTaxId("12345678901"))
        // 12 digits → CNPJ partial
        assertEquals("12.345.678/9012", formatTaxId("123456789012"))
        assertEquals("12.345.678/9012-34", formatTaxId("12345678901234"))
    }

    private fun assertTaxIdMappingInvariants(input: String) {
        val tt = TaxIdTransformation.applyTo(input)
        val mapping = tt.offsetMapping
        val origLen = input.length
        val transLen = tt.text.length
        assertEquals(transLen, mapping.originalToTransformed(origLen))
        assertEquals(origLen, mapping.transformedToOriginal(transLen))
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
    fun `tax id mapping holds invariants for all valid lengths`() {
        for (len in 0..MAX_TAX_ID_DIGITS) {
            assertTaxIdMappingInvariants("1".repeat(len))
        }
    }

    // --- CPF validators ---

    @Test
    fun `valid CPFs are accepted`() {
        assertEquals(true, isValidCpf("39053344705"))
        assertEquals(true, isValidCpf("11144477735"))
    }

    @Test
    fun `CPFs with wrong check digits are rejected`() {
        assertEquals(false, isValidCpf("39053344700"))
        assertEquals(false, isValidCpf("11144477700"))
    }

    @Test
    fun `CPFs with all repeated digits are rejected`() {
        for (d in '0'..'9') assertEquals(false, isValidCpf(d.toString().repeat(11)))
    }

    @Test
    fun `CPFs with wrong length are rejected`() {
        assertEquals(false, isValidCpf(""))
        assertEquals(false, isValidCpf("3905334470"))
        assertEquals(false, isValidCpf("390533447055"))
    }

    @Test
    fun `CPFs with non digits would not pass length check`() {
        // sanitize strips non-digits before reaching validator, but defend the invariant
        assertEquals(false, isValidCpf("390.533.447-05"))
    }

    // --- CNPJ validators ---

    @Test
    fun `valid CNPJs are accepted`() {
        assertEquals(true, isValidCnpj("11222333000181"))
        assertEquals(true, isValidCnpj("19131243000197"))
    }

    @Test
    fun `CNPJs with wrong check digits are rejected`() {
        assertEquals(false, isValidCnpj("11222333000180"))
        assertEquals(false, isValidCnpj("19131243000100"))
    }

    @Test
    fun `CNPJs with all repeated digits are rejected`() {
        for (d in '0'..'9') assertEquals(false, isValidCnpj(d.toString().repeat(14)))
    }

    @Test
    fun `CNPJs with wrong length are rejected`() {
        assertEquals(false, isValidCnpj(""))
        assertEquals(false, isValidCnpj("1122233300018"))
        assertEquals(false, isValidCnpj("112223330001811"))
    }

    // --- isValidTaxId dispatcher ---

    @Test
    fun `isValidTaxId routes by length`() {
        assertEquals(true, isValidTaxId("39053344705"))
        assertEquals(true, isValidTaxId("11222333000181"))
        assertEquals(false, isValidTaxId("12345678901"))
        assertEquals(false, isValidTaxId("12345678901234"))
        assertEquals(false, isValidTaxId(""))
        assertEquals(false, isValidTaxId("1234567"))
    }
}
