package br.com.edu.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.core.auth.TokenStore
import br.com.edu.features.profile.data.AddressRepository
import br.com.edu.features.profile.data.UserRepository
import br.com.edu.features.profile.data.remote.AddressInDto
import br.com.edu.features.profile.data.remote.AddressPatchDto
import br.com.edu.features.profile.domain.Address
import br.com.edu.features.profile.domain.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Ready(
        val profile: UserProfile,
        val addresses: List<Address> = emptyList(),
        val isEditing: Boolean = false,
        val saving: Boolean = false,
        val saveError: String? = null,
        val addressBusy: Boolean = false,
        val addressError: String? = null,
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val addressRepository: AddressRepository = AddressRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            _state.value = runCatching {
                coroutineScope {
                    val profileDeferred = async { userRepository.getProfile() }
                    val addressesDeferred = async {
                        runCatching { addressRepository.list() }.getOrDefault(emptyList())
                    }
                    ProfileUiState.Ready(
                        profile = profileDeferred.await(),
                        addresses = addressesDeferred.await(),
                    )
                }
            }.getOrElse {
                ProfileUiState.Error(it.message ?: "Erro ao carregar perfil")
            }
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
                userRepository.updateProfile(
                    name = name.trim(),
                    phone = phone.trim(),
                    birthDate = birthDate.trim(),
                )
            }.fold(
                onSuccess = { profile ->
                    _state.value = current.copy(
                        profile = profile,
                        isEditing = false,
                        saving = false,
                    )
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

    fun createAddress(input: AddressInDto) = runAddressOp {
        addressRepository.create(input)
    }

    fun updateAddress(id: Int, patch: AddressPatchDto) = runAddressOp {
        addressRepository.update(id, patch)
    }

    fun deleteAddress(id: Int) = runAddressOp {
        addressRepository.delete(id)
    }

    fun setFavorite(id: Int) = updateAddress(id, AddressPatchDto(isFavorite = true))

    private fun runAddressOp(block: suspend () -> Unit) {
        val current = _state.value as? ProfileUiState.Ready ?: return
        _state.value = current.copy(addressBusy = true, addressError = null)
        viewModelScope.launch {
            runCatching {
                block()
                addressRepository.list()
            }.fold(
                onSuccess = { addresses ->
                    val latest = _state.value as? ProfileUiState.Ready ?: return@fold
                    _state.value = latest.copy(
                        addresses = addresses,
                        addressBusy = false,
                        addressError = null,
                    )
                },
                onFailure = { err ->
                    val latest = _state.value as? ProfileUiState.Ready ?: return@fold
                    _state.value = latest.copy(
                        addressBusy = false,
                        addressError = err.message ?: "Erro ao salvar endereço",
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
