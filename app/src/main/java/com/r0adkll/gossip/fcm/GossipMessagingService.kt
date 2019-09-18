package com.r0adkll.gossip.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.arch.domain.messages.Message
import com.r0adkll.gossip.arch.domain.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.koin.android.ext.android.inject
import timber.log.Timber

class GossipMessagingService : FirebaseMessagingService(), CoroutineScope by MainScope() {

    private val appPreferences by inject<AppPreferences>()
    private val userRepository by inject<UserRepository>()

    @ImplicitReflectionSerializer
    override fun onMessageReceived(message: RemoteMessage) {
        message.data["lastMessages"]?.let { lastMessages ->
            val messages = Json.nonstrict.parse(Message.serializer().list, lastMessages)
            Timber.d("Notification(lastMessages=$messages)")
        }
    }

    override fun onNewToken(token: String) {
        appPreferences.pushToken = token
        appPreferences.pushTokenUploaded = false
        launch {
            userRepository.updatePushToken(token)
        }
    }
}
