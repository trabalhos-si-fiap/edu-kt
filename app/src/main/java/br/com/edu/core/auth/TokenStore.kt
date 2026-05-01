package br.com.edu.core.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.authDataStore by preferencesDataStore(name = "auth")

class TokenStore(context: Context) {
    private val dataStore = context.applicationContext.authDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _accessToken: MutableStateFlow<String?>
    private val _refreshToken: MutableStateFlow<String?>

    init {
        val initial = runBlocking { dataStore.data.first() }
        _accessToken = MutableStateFlow(initial[KEY_ACCESS])
        _refreshToken = MutableStateFlow(initial[KEY_REFRESH])
    }

    val accessToken: StateFlow<String?> get() = _accessToken.asStateFlow()
    val refreshToken: StateFlow<String?> get() = _refreshToken.asStateFlow()

    /** True when an access token is present (used by the app to decide auth state). */
    val token: StateFlow<String?> get() = accessToken

    fun setPair(access: String, refresh: String) {
        _accessToken.value = access
        _refreshToken.value = refresh
        scope.launch {
            dataStore.edit {
                it[KEY_ACCESS] = access
                it[KEY_REFRESH] = refresh
            }
        }
    }

    fun updateAccess(access: String) {
        _accessToken.value = access
        scope.launch {
            dataStore.edit { it[KEY_ACCESS] = access }
        }
    }

    fun clear() {
        _accessToken.value = null
        _refreshToken.value = null
        scope.launch {
            dataStore.edit {
                it.remove(KEY_ACCESS)
                it.remove(KEY_REFRESH)
                it.remove(KEY_LEGACY_TOKEN)
            }
        }
    }

    fun currentAccess(): String? = _accessToken.value
    fun currentRefresh(): String? = _refreshToken.value

    private companion object {
        val KEY_ACCESS = stringPreferencesKey("access_token")
        val KEY_REFRESH = stringPreferencesKey("refresh_token")
        val KEY_LEGACY_TOKEN = stringPreferencesKey("token")
    }
}
