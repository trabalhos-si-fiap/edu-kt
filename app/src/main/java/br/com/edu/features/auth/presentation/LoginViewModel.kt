package br.com.edu.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun submit(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginUiState(error = "Preencha e-mail e senha")
            return
        }
        _state.value = LoginUiState(loading = true)
        viewModelScope.launch {
            repository.login(email, password)
                .onSuccess { _state.value = LoginUiState(success = true) }
                .onFailure { _state.value = LoginUiState(error = it.message ?: "Erro ao entrar") }
        }
    }

    fun consumedError() {
        _state.value = _state.value.copy(error = null)
    }
}
