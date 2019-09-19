package com.r0adkll.gossip

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
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

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerCompat.createNotificationChannel(NotificationChannel(
                getString(R.string.default_notification_channel_id),
                "Gossip Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ))
        }
    }
}
