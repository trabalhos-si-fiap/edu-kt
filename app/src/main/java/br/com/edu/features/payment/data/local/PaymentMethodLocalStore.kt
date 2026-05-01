package br.com.edu.features.payment.data.local

import android.content.Context
import br.com.edu.EduApplication
import br.com.edu.features.payment.domain.PaymentMethod
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PaymentMethodLocalStore {
    private const val PREFS = "edu_payments"
    private const val KEY = "payment_methods"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val prefs by lazy {
        EduApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun load(): List<PaymentMethod> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<PaymentMethod>>(raw) }
            .getOrElse { emptyList() }
    }

    fun save(list: List<PaymentMethod>) {
        prefs.edit().putString(KEY, json.encodeToString(list)).apply()
    }
}
