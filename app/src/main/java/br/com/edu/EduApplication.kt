package br.com.edu

import android.app.Application
import android.content.Context
import br.com.edu.core.auth.TokenStore

class EduApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        tokenStore = TokenStore(applicationContext)
    }

    companion object {
        lateinit var appContext: Context
            private set
        lateinit var tokenStore: TokenStore
            private set
    }
}
