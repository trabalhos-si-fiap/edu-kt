package br.com.edu.features.support.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Formats an ISO-8601 instant (e.g. `2026-04-30T14:20:00Z`) as `HH:mm` in the
 * given zone. Defaults to the device's zone so support chat timestamps reflect
 * the user's wall clock instead of UTC.
 *
 * Returns an empty string for blank or unparseable input.
 */
fun formatMessageTime(iso: String, zone: ZoneId = ZoneId.systemDefault()): String {
    if (iso.isBlank()) return ""
    return try {
        Instant.parse(iso).atZone(zone).format(HOUR_MINUTE)
    } catch (_: Exception) {
        ""
    }
}
