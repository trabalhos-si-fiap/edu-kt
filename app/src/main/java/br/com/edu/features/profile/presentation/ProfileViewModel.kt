package br.com.edu.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.core.auth.TokenStore
import br.com.edu.features.profile.data.UserRepository
import br.com.edu.features.profile.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(
        val profile: UserProfile,
        val isEditing: Boolean = false,
        val saving: Boolean = false,
        val saveError: String? = null,
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            _state.value = runCatching { repository.getProfile() }
                .fold(
                    onSuccess = { ProfileUiState.Ready(it) },
                    onFailure = { ProfileUiState.Error(it.message ?: "Erro ao carregar perfil") },
                )
        }
    }

    fun startEdit() {
        _state.update { current ->
            if (current is ProfileUiState.Ready) current.copy(isEditing = true, saveError = null)
            else current
        }
    }

    fun cancelEdit() {
        _state.update { current ->
            if (current is ProfileUiState.Ready) current.copy(isEditing = false, saveError = null)
            else current
        }
    }

    fun save(name: String, phone: String, birthDate: String) {
        val current = _state.value as? ProfileUiState.Ready ?: return
        _state.value = current.copy(saving = true, saveError = null)
        viewModelScope.launch {
            runCatching {
                repository.updateProfile(
                    name = name.trim(),
                    phone = phone.trim(),
                    birthDate = birthDate.trim(),
                )
            }.fold(
                onSuccess = { profile ->
                    _state.value = ProfileUiState.Ready(profile, isEditing = false, saving = false)
                },
                onFailure = { err ->
                    _state.value = current.copy(
                        saving = false,
                        saveError = err.message ?: "Erro ao salvar alterações",
                    )
                },
            )
        }
    }

    fun logout(onDone: () -> Unit) {
        TokenStore.clear()
        onDone()
    }
}
