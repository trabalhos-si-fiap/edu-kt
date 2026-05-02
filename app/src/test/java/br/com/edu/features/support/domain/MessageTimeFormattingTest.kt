package br.com.edu.features.support.domain

import org.junit.Assert.assertEquals
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Test

class MessageTimeFormattingTest {

    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    @Test
    fun `converts UTC instant to local zone wall clock`() {
        // 14:20Z is 11:20 in São Paulo (UTC-3, no DST in 2026).
        assertEquals("11:20", formatMessageTime("2026-04-30T14:20:00Z", saoPaulo))
    }

    @Test
    fun `keeps wall clock when zone matches UTC`() {
        assertEquals("14:20", formatMessageTime("2026-04-30T14:20:00Z", ZoneOffset.UTC))
    }

    @Test
    fun `handles ISO with milliseconds`() {
        assertEquals("11:20", formatMessageTime("2026-04-30T14:20:00.000Z", saoPaulo))
    }

    @Test
    fun `handles ISO with explicit offset, not just Z`() {
        // 14:20-03:00 is the same instant as 17:20Z, which is 14:20 in São Paulo.
        assertEquals("14:20", formatMessageTime("2026-04-30T14:20:00-03:00", saoPaulo))
    }

    @Test
    fun `pads single-digit hours and minutes`() {
        assertEquals("01:05", formatMessageTime("2026-04-30T01:05:00Z", ZoneOffset.UTC))
    }

    @Test
    fun `returns empty string for blank input`() {
        assertEquals("", formatMessageTime("", saoPaulo))
        assertEquals("", formatMessageTime("   ", saoPaulo))
    }

    @Test
    fun `returns empty string for unparseable input`() {
        assertEquals("", formatMessageTime("not-a-timestamp", saoPaulo))
        assertEquals("", formatMessageTime("2026-04-30 14:20:00", saoPaulo))
    }

    @Test
    fun `crosses midnight when zone offset pushes time to previous or next day`() {
        // 02:30Z is 23:30 the previous day in São Paulo.
        assertEquals("23:30", formatMessageTime("2026-05-01T02:30:00Z", saoPaulo))
    }
}
