package br.com.edu.features.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.core.network.ApiClient
import br.com.edu.features.marketplace.data.remote.ProductApi
import br.com.edu.features.marketplace.data.remote.ReviewInDto
import br.com.edu.features.orders.data.OrdersRepository
import br.com.edu.features.orders.domain.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data class Ready(val orders: List<Order>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

class OrdersViewModel(
    private val repository: OrdersRepository = OrdersRepository(),
    private val productApi: ProductApi = ApiClient.create(),
) : ViewModel() {

    private val _state = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val state: StateFlow<OrdersUiState> = _state.asStateFlow()

    private val _action = MutableStateFlow<OrdersAction?>(null)
    val action: StateFlow<OrdersAction?> = _action.asStateFlow()

    fun load() {
        _state.value = OrdersUiState.Loading
        viewModelScope.launch {
            runCatching { repository.listOrders() }
                .onSuccess { _state.value = OrdersUiState.Ready(it) }
                .onFailure { _state.value = OrdersUiState.Error(it.message ?: "Erro ao carregar pedidos") }
        }
    }

    fun rebuy(orderId: Int) {
        viewModelScope.launch {
            runCatching { repository.rebuy(orderId) }
                .onSuccess { _action.value = OrdersAction.RebuySuccess }
                .onFailure { _action.value = OrdersAction.Error(it.message ?: "Não foi possível adicionar ao carrinho") }
        }
    }

    fun submitReviews(ratings: Map<Int, Int>, comment: String = "") {
        val cleaned = ratings.filterValues { it in 1..5 }
        if (cleaned.isEmpty()) {
            _action.value = OrdersAction.Error("Selecione uma nota antes de enviar")
            return
        }
        viewModelScope.launch {
            val failures = mutableListOf<Throwable>()
            cleaned.forEach { (productId, rating) ->
                runCatching {
                    productApi.createReview(productId, ReviewInDto(rating = rating, comment = comment))
                }.onFailure { failures += it }
            }
            if (failures.isEmpty()) {
                _action.value = OrdersAction.ReviewsSubmitted
                load()
            } else {
                _action.value = OrdersAction.Error(
                    failures.first().message ?: "Falha ao enviar avaliações",
                )
            }
        }
    }

    fun consumeAction() { _action.value = null }
}

sealed interface OrdersAction {
    data object RebuySuccess : OrdersAction
    data object ReviewsSubmitted : OrdersAction
    data class Error(val message: String) : OrdersAction
}
