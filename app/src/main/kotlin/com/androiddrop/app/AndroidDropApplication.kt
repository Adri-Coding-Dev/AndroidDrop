package com.androiddrop.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.conscrypt.BuildConfig
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security

@HiltAndroidApp
class AndroidDropApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        initializeSecurity()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private fun initializeSecurity() {
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        } catch (e: Exception) {
            Timber.w(e, "Error al inicializar Conscrypt")
        }
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= android.util.Log.ERROR) {
                // Enviar a servicio de crash reporting (Firebase, Sentry, etc.)
            }
        }
    }
}
