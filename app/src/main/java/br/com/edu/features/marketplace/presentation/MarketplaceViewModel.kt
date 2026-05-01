package br.com.edu.features.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.marketplace.data.MarketplaceRepository
import br.com.edu.features.marketplace.domain.Product
import br.com.edu.features.marketplace.domain.Review
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface MarketplaceUiState {
    data object Loading : MarketplaceUiState
    data class Ready(val products: List<Product>) : MarketplaceUiState
    data class Error(val message: String) : MarketplaceUiState
}

sealed interface ReviewsUiState {
    data object Hidden : ReviewsUiState
    data class Loading(val product: Product) : ReviewsUiState
    data class Ready(val product: Product, val reviews: List<Review>) : ReviewsUiState
    data class Error(val product: Product, val message: String) : ReviewsUiState
}

@OptIn(FlowPreview::class)
class MarketplaceViewModel(
    private val repository: MarketplaceRepository = MarketplaceRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<MarketplaceUiState>(MarketplaceUiState.Loading)
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _reviews = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Hidden)
    val reviews: StateFlow<ReviewsUiState> = _reviews.asStateFlow()

    init {
        _query
            .debounce { if (it.isEmpty()) 0L else 300L }
            .distinctUntilChanged()
            .onEach { load(it) }
            .launchIn(viewModelScope)
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            runCatching { repository.listCategories() }
                .onSuccess { _categories.value = it }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onTypeSelected(type: String?) {
        _selectedType.value = type
    }

    fun retry() {
        load(_query.value)
    }

    fun load(query: String = _query.value) {
        _state.value = MarketplaceUiState.Loading
        _selectedType.value = null
        viewModelScope.launch {
            _state.value = runCatching { repository.listProducts(query = query) }
                .fold(
                    onSuccess = { MarketplaceUiState.Ready(it) },
                    onFailure = { MarketplaceUiState.Error(it.message ?: "Erro ao carregar produtos") },
                )
        }
    }

    fun showReviews(product: Product) {
        _reviews.value = ReviewsUiState.Loading(product)
        viewModelScope.launch {
            _reviews.value = runCatching { repository.listReviews(product.id, limit = 50) }
                .fold(
                    onSuccess = { ReviewsUiState.Ready(product, it) },
                    onFailure = { ReviewsUiState.Error(product, it.message ?: "Erro ao carregar avaliações") },
                )
        }
    }

    fun hideReviews() {
        _reviews.value = ReviewsUiState.Hidden
    }
}
