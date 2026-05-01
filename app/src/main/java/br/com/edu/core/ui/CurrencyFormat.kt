package br.com.edu.core.ui

import java.text.NumberFormat
import java.util.Locale

private val BrazilLocale = Locale("pt", "BR")

private val brlFormatter: NumberFormat
    get() = NumberFormat.getCurrencyInstance(BrazilLocale)

fun formatBRL(raw: String): String {
    val value = raw.toDoubleOrNull() ?: return "R$ $raw"
    return formatBRL(value)
}

fun formatBRL(value: Double): String = brlFormatter.format(value)
