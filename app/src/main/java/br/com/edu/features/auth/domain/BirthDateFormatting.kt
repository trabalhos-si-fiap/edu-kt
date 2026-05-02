package br.com.edu.features.auth.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/**
 * Converts a UTC epoch-millis value (as produced by Material3 DatePicker) to an
 * ISO-8601 date string (`yyyy-MM-dd`) — the format expected by the backend.
 */
fun epochMillisUtcToIsoDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

/**
 * Converts an ISO-8601 date (`yyyy-MM-dd`) to the pt-BR display format
 * (`dd/MM/yyyy`). Returns an empty string for blank input and the original
 * value for unparseable input, so the UI degrades gracefully.
 */
fun isoDateToDisplay(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val date = LocalDate.parse(iso)
        "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)
    } catch (_: DateTimeParseException) {
        iso
    }
}
