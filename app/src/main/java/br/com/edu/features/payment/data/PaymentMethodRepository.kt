package br.com.edu.features.payment.data

import br.com.edu.features.payment.data.local.PaymentMethodLocalStore
import br.com.edu.features.payment.domain.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object PaymentMethodRepository {

    private val _methods = MutableStateFlow(PaymentMethodLocalStore.load())
    val methods: StateFlow<List<PaymentMethod>> = _methods.asStateFlow()

    fun getById(id: String): PaymentMethod? = _methods.value.firstOrNull { it.id == id }

    fun add(method: PaymentMethod, makeDefault: Boolean): PaymentMethod {
        val withId = method.copy(id = method.id.ifBlank { UUID.randomUUID().toString() })
        val next = _methods.value + withId
        commit(if (makeDefault || next.size == 1) applyDefault(next, withId.id) else next)
        return withId
    }

    fun update(method: PaymentMethod, makeDefault: Boolean) {
        val next = _methods.value.map { if (it.id == method.id) method else it }
        commit(if (makeDefault) applyDefault(next, method.id) else next)
    }

    fun delete(id: String) {
        val next = _methods.value.filterNot { it.id == id }
        val needsNewDefault = next.isNotEmpty() && next.none { it.isDefault }
        commit(if (needsNewDefault) applyDefault(next, next.first().id) else next)
    }

    fun setDefault(id: String) {
        commit(applyDefault(_methods.value, id))
    }

    private fun applyDefault(list: List<PaymentMethod>, defaultId: String): List<PaymentMethod> =
        list.map { it.copy(isDefault = it.id == defaultId) }

    private fun commit(list: List<PaymentMethod>) {
        _methods.value = list
        PaymentMethodLocalStore.save(list)
    }
}
