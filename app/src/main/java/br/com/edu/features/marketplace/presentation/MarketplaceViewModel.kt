package br.com.edu.features.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.marketplace.data.MarketplaceRepository
import br.com.edu.features.marketplace.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MarketplaceUiState {
    data object Loading : MarketplaceUiState
    data class Ready(val products: List<Product>) : MarketplaceUiState
    data class Error(val message: String) : MarketplaceUiState
}

class MarketplaceViewModel(
    private val repository: MarketplaceRepository = MarketplaceRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<MarketplaceUiState>(MarketplaceUiState.Loading)
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = MarketplaceUiState.Loading
        viewModelScope.launch {
            _state.value = runCatching { repository.listProducts() }
                .fold(
                    onSuccess = { MarketplaceUiState.Ready(it) },
                    onFailure = { MarketplaceUiState.Error(it.message ?: "Erro ao carregar produtos") },
                )
        }
    }
}
