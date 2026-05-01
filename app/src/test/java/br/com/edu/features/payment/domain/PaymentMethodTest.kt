package br.com.edu.features.payment.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentMethodTest {

    @Test
    fun `brand from number maps first digit to known brands`() {
        assertEquals("Visa", brandFromNumber("4111111111111111"))
        assertEquals("Mastercard", brandFromNumber("5500000000000004"))
        assertEquals("Amex", brandFromNumber("340000000000009"))
        assertEquals("Elo", brandFromNumber("6362970000457013"))
    }

    @Test
    fun `brand from number falls back to generic for unknown prefix`() {
        assertEquals("Cartão", brandFromNumber("0000000000000000"))
        assertEquals("Cartão", brandFromNumber("9999999999999999"))
    }

    @Test
    fun `brand from number falls back when input is empty`() {
        assertEquals("Cartão", brandFromNumber(""))
    }
}
