package com.r0adkll.gossip

import android.app.Application
import com.r0adkll.gossip.internal.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GossipApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GossipApp)
            androidLogger()
            modules(appModule)
        }


    }
}
