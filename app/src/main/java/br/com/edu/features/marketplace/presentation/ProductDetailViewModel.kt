package br.com.edu.features.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.marketplace.data.MarketplaceRepository
import br.com.edu.features.marketplace.domain.Product
import br.com.edu.features.marketplace.domain.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Ready(
        val product: Product,
        val reviews: List<Review>,
        val reviewsLoading: Boolean,
        val reviewsError: String?,
    ) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val productId: Int,
    private val repository: MarketplaceRepository = MarketplaceRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = ProductDetailUiState.Loading
        viewModelScope.launch {
            runCatching { repository.getProduct(productId) }.fold(
                onSuccess = { product ->
                    _state.value = ProductDetailUiState.Ready(
                        product = product,
                        reviews = emptyList(),
                        reviewsLoading = true,
                        reviewsError = null,
                    )
                    loadReviews()
                },
                onFailure = { err ->
                    _state.value = ProductDetailUiState.Error(
                        err.message ?: "Erro ao carregar produto",
                    )
                },
            )
        }
    }

    fun retryReviews() {
        val current = _state.value as? ProductDetailUiState.Ready ?: return
        _state.value = current.copy(reviewsLoading = true, reviewsError = null)
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            runCatching { repository.listReviews(productId, limit = 50) }
                .fold(
                    onSuccess = { reviews ->
                        _state.update { current ->
                            if (current is ProductDetailUiState.Ready) {
                                current.copy(reviews = reviews, reviewsLoading = false, reviewsError = null)
                            } else current
                        }
                    },
                    onFailure = { err ->
                        _state.update { current ->
                            if (current is ProductDetailUiState.Ready) {
                                current.copy(
                                    reviewsLoading = false,
                                    reviewsError = err.message ?: "Erro ao carregar avaliações",
                                )
                            } else current
                        }
                    },
                )
        }
    }
}
