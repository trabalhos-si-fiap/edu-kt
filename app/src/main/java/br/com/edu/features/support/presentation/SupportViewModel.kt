package br.com.edu.features.support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edu.features.support.data.SupportRepository
import br.com.edu.features.support.domain.SupportMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SupportUiState {
    data object Loading : SupportUiState
    data class Ready(
        val messages: List<SupportMessage>,
        val sending: Boolean = false,
    ) : SupportUiState
    data class Error(val message: String) : SupportUiState
}

class SupportViewModel(
    private val repository: SupportRepository = SupportRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<SupportUiState>(SupportUiState.Loading)
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    private val mutex = Mutex()

    fun load() {
        _state.value = SupportUiState.Loading
        viewModelScope.launch {
            mutex.withLock {
                try {
                    _state.value = SupportUiState.Ready(repository.list())
                } catch (t: Throwable) {
                    _state.value = SupportUiState.Error(t.message ?: "Erro ao carregar mensagens")
                }
            }
        }
    }

    fun send(text: String) {
        val body = text.trim()
        if (body.isEmpty()) return
        val current = (_state.value as? SupportUiState.Ready) ?: return
        if (current.sending) return

        _state.value = current.copy(sending = true)
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val updated = repository.send(body)
                    val merged = mergeUnique(current.messages, updated)
                    _state.value = SupportUiState.Ready(merged, sending = false)
                } catch (t: Throwable) {
                    _state.value = SupportUiState.Ready(
                        messages = current.messages,
                        sending = false,
                    )
                }
            }
        }
    }

    private fun mergeUnique(
        existing: List<SupportMessage>,
        incoming: List<SupportMessage>,
    ): List<SupportMessage> {
        val byId = LinkedHashMap<Long, SupportMessage>()
        existing.forEach { byId[it.id] = it }
        incoming.forEach { byId[it.id] = it }
        return byId.values.sortedBy { it.createdAt }
    }

    companion object {
        @Volatile private var instance: SupportViewModel? = null
        fun get(): SupportViewModel = instance ?: synchronized(this) {
            instance ?: SupportViewModel().also { instance = it }
        }
    }
}
