package com.example.cryptoexchange

import android.app.Application
import com.example.cryptoexchange.di.AppContainer

class CryptoApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        appContainer.syncScheduler.schedulePeriodicSync()
    }
}