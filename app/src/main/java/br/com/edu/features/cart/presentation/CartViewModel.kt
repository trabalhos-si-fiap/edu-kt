package br.com.edu.features.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.cart.data.CartRepository
import br.com.edu.features.cart.domain.Cart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface CartUiState {
    data object Idle : CartUiState
    data object Loading : CartUiState
    data class Ready(val cart: Cart) : CartUiState
    data class Error(val message: String) : CartUiState
}

class CartViewModel(
    private val repository: CartRepository = CartRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<CartUiState>(CartUiState.Idle)
    val state: StateFlow<CartUiState> = _state.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val mutex = Mutex()

    fun load() {
        _state.value = CartUiState.Loading
        runOp { _state.value = CartUiState.Ready(repository.getCart()) }
    }

    fun addItem(productId: Int, quantity: Int = 1) = runOp {
        _state.value = CartUiState.Ready(repository.addItem(productId, quantity))
    }

    fun decrementItem(productId: Int) = runOp {
        _state.value = CartUiState.Ready(repository.removeItem(productId, quantity = 1))
    }

    fun removeAll(productId: Int) = runOp {
        _state.value = CartUiState.Ready(repository.removeItem(productId, quantity = null))
    }

    fun clear() = runOp {
        val current = (_state.value as? CartUiState.Ready)?.cart ?: repository.getCart()
        var cart = current
        current.items.forEach { item ->
            cart = repository.removeItem(item.productId, quantity = null)
        }
        _state.value = CartUiState.Ready(cart)
    }

    private fun runOp(block: suspend () -> Unit) {
        viewModelScope.launch {
            mutex.withLock {
                _busy.value = true
                try {
                    block()
                } catch (t: Throwable) {
                    _state.value = CartUiState.Error(t.message ?: "Erro no carrinho")
                } finally {
                    _busy.value = false
                }
            }
        }
    }

    companion object {
        @Volatile private var instance: CartViewModel? = null
        fun get(): CartViewModel = instance ?: synchronized(this) {
            instance ?: CartViewModel().also { instance = it }
        }
    }
}
