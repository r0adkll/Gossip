package com.r0adkll.gossip

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.r0adkll.gossip.internal.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class GossipApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(this)
        startKoin {
            androidContext(this@GossipApp)
            androidLogger()
            modules(appModule)
        }
    }
}
