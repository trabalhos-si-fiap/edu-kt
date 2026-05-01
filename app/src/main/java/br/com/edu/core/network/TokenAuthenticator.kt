package br.com.edu.core.network

import br.com.edu.BuildConfig
import br.com.edu.EduApplication
import br.com.edu.features.auth.data.remote.RefreshRequest
import br.com.edu.features.auth.data.remote.RefreshResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * okhttp3.Authenticator that swaps an expired access token for a new pair on 401.
 *
 * Uses a dedicated Retrofit/OkHttp stack without AuthInterceptor or this authenticator
 * so the refresh call cannot recurse.
 */
class TokenAuthenticator : Authenticator {

    private val refreshApi: RefreshApi by lazy {
        val client = OkHttpClient.Builder().build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RefreshApi::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        if (responseCount(response) >= 2) return null

        val store = EduApplication.tokenStore
        val refresh = store.currentRefresh() ?: run {
            store.clear()
            return null
        }

        val newPair = synchronized(this) {
            // Re-check inside the lock — another thread might already have rotated.
            val currentRefresh = store.currentRefresh() ?: return@synchronized null
            if (currentRefresh != refresh) {
                // Another caller already rotated. Use the new access for this retry.
                store.currentAccess()?.let { return@synchronized RefreshResult(it, currentRefresh) }
            }
            try {
                val res = runCatchingBlocking { refreshApi.refresh(RefreshRequest(currentRefresh)) }
                if (!res.isSuccessful) {
                    store.clear()
                    null
                } else {
                    val body = res.body() ?: run {
                        store.clear()
                        return@synchronized null
                    }
                    store.setPair(body.access, body.refresh)
                    RefreshResult(body.access, body.refresh)
                }
            } catch (_: Exception) {
                store.clear()
                null
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newPair.access}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private data class RefreshResult(val access: String, val refresh: String)

    private interface RefreshApi {
        @POST("auth/refresh")
        fun refresh(@Body body: RefreshRequest): retrofit2.Call<RefreshResponse>
    }

    private fun <T> runCatchingBlocking(block: () -> retrofit2.Call<T>): retrofit2.Response<T> =
        block().execute()
}
