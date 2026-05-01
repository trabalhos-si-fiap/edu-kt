package br.com.edu.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatTest {

    private fun normalize(s: String): String =
        s.replace(' ', ' ').replace(' ', ' ')

    @Test
    fun `formatBRL formats double with two decimals`() {
        assertEquals("R$ 0,00", normalize(formatBRL(0.0)))
        assertEquals("R$ 1,50", normalize(formatBRL(1.5)))
        assertEquals("R$ 1.234,56", normalize(formatBRL(1234.56)))
    }

    @Test
    fun `formatBRL accepts numeric strings`() {
        assertEquals("R$ 9,90", normalize(formatBRL("9.9")))
        assertEquals("R$ 1.000,00", normalize(formatBRL("1000")))
    }

    @Test
    fun `formatBRL falls back to raw prefix for non-numeric strings`() {
        assertEquals("R$ abc", formatBRL("abc"))
        assertEquals("R$ ", formatBRL(""))
    }

    @Test
    fun `formatBRL handles negatives`() {
        val result = normalize(formatBRL(-12.30))
        // Locale renders as "-R$ 12,30"
        assertTrue(result.contains("12,30"))
        assertTrue(result.contains("R$"))
        assertTrue(result.startsWith("-"))
    }
}
