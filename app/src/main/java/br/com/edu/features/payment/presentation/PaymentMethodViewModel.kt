package br.com.edu.features.payment.presentation

import androidx.lifecycle.ViewModel
import br.com.edu.features.payment.data.PaymentMethodRepository
import br.com.edu.features.payment.domain.PaymentMethod
import kotlinx.coroutines.flow.StateFlow

class PaymentMethodViewModel : ViewModel() {

    private val repository = PaymentMethodRepository

    val methods: StateFlow<List<PaymentMethod>> = repository.methods

    fun getById(id: String): PaymentMethod? = repository.getById(id)

    fun add(method: PaymentMethod, makeDefault: Boolean): PaymentMethod =
        repository.add(method, makeDefault)

    fun update(method: PaymentMethod, makeDefault: Boolean) =
        repository.update(method, makeDefault)

    fun delete(id: String) = repository.delete(id)

    fun setDefault(id: String) = repository.setDefault(id)

    companion object {
        @Volatile private var instance: PaymentMethodViewModel? = null
        fun get(): PaymentMethodViewModel = instance ?: synchronized(this) {
            instance ?: PaymentMethodViewModel().also { instance = it }
        }
    }
}
