package br.com.edu.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun submit(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthDate: String,
    ) {
        _state.value = RegisterUiState(loading = true)
        viewModelScope.launch {
            repository.register(
                email = email,
                password = password,
                name = name,
                phone = phone,
                birthDate = birthDate,
            )
                .onSuccess { _state.value = RegisterUiState(success = true) }
                .onFailure { _state.value = RegisterUiState(error = it.message ?: "Erro ao cadastrar") }
        }
    }

    fun consumed() {
        _state.value = _state.value.copy(error = null, success = false)
    }
}
