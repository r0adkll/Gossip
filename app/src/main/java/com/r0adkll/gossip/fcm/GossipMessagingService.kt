package com.r0adkll.gossip.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.arch.domain.user.UserRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class GossipMessagingService : FirebaseMessagingService(), CoroutineScope by MainScope() {

    private val appPreferences by inject<AppPreferences>()
    private val userRepository by inject<UserRepository>()

    override fun onMessageReceived(message: RemoteMessage) {

    }

    override fun onNewToken(token: String) {
        appPreferences.pushToken = token
        appPreferences.pushTokenUploaded = false
        launch {
            userRepository.updatePushToken(token)
        }
    }
}
