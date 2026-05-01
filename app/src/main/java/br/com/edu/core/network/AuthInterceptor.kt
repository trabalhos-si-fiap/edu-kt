package br.com.edu.core.network

import br.com.edu.EduApplication
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = EduApplication.tokenStore.currentAccess()
        val request = if (access.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        }
        return chain.proceed(request)
    }
}
