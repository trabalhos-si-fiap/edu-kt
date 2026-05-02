package br.com.edu.features.auth.domain

import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Test

class BirthDateFormattingTest {

    // --- epochMillisUtcToIsoDate ---

    @Test
    fun `epochMillisUtcToIsoDate returns ISO yyyy-MM-dd`() {
        val millis = LocalDate.of(2026, 5, 2).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("2026-05-02", epochMillisUtcToIsoDate(millis))
    }

    @Test
    fun `epochMillisUtcToIsoDate uses UTC, ignoring sub-day offsets`() {
        // Material3 DatePicker emits midnight UTC, but verify we don't drift on the same day.
        val base = LocalDate.of(1990, 1, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        // Add 23h59m — still the same UTC date.
        val late = base + (23L * 3600 + 59 * 60) * 1000
        assertEquals("1990-01-15", epochMillisUtcToIsoDate(base))
        assertEquals("1990-01-15", epochMillisUtcToIsoDate(late))
    }

    @Test
    fun `epochMillisUtcToIsoDate handles epoch zero`() {
        assertEquals("1970-01-01", epochMillisUtcToIsoDate(0L))
    }

    @Test
    fun `epochMillisUtcToIsoDate pads month and day with leading zeros`() {
        val millis = LocalDate.of(2001, 3, 4).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("2001-03-04", epochMillisUtcToIsoDate(millis))
    }

    // --- isoDateToDisplay ---

    @Test
    fun `isoDateToDisplay converts ISO to pt-BR format`() {
        assertEquals("02/05/2026", isoDateToDisplay("2026-05-02"))
        assertEquals("15/01/1990", isoDateToDisplay("1990-01-15"))
    }

    @Test
    fun `isoDateToDisplay pads single-digit day and month`() {
        assertEquals("04/03/2001", isoDateToDisplay("2001-03-04"))
    }

    @Test
    fun `isoDateToDisplay returns empty string for blank input`() {
        assertEquals("", isoDateToDisplay(""))
        assertEquals("", isoDateToDisplay("   "))
    }

    @Test
    fun `isoDateToDisplay returns input untouched when unparseable`() {
        assertEquals("not-a-date", isoDateToDisplay("not-a-date"))
        assertEquals("02/05/2026", isoDateToDisplay("02/05/2026"))
    }

    // --- round trip: picker -> ISO -> display ---

    @Test
    fun `picker millis round trip yields the original calendar date in pt-BR`() {
        val millis = LocalDate.of(2000, 12, 31).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val iso = epochMillisUtcToIsoDate(millis)
        assertEquals("2000-12-31", iso)
        assertEquals("31/12/2000", isoDateToDisplay(iso))
    }
}
