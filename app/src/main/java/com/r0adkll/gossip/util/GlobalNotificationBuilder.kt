package com.r0adkll.gossip.util

import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat

object GlobalNotificationBuilder {

    @SuppressLint("StaticFieldLeak")
    private var globalNotificationCompatBuilder: NotificationCompat.Builder? = null

    fun setNotificationCompatBuilderInstance(builder: NotificationCompat.Builder?) {
        globalNotificationCompatBuilder = builder
    }

    val notificationCompatBuilderInstance: NotificationCompat.Builder?
        get() = globalNotificationCompatBuilder
}
