package com.r0adkll.gossip.fcm

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.Coil
import coil.api.get
import coil.transform.CircleCropTransformation
import com.ftinc.kit.extensions.color
import com.ftinc.kit.extensions.dip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.data.messages.MessagingIntentService
import com.r0adkll.gossip.arch.domain.messages.Message
import com.r0adkll.gossip.arch.domain.messages.MessageRepository
import com.r0adkll.gossip.arch.domain.user.UserRepository
import com.r0adkll.gossip.arch.ui.MainActivity
import com.r0adkll.gossip.util.GlobalNotificationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.koin.android.ext.android.inject
import timber.log.Timber


class GossipMessagingService : FirebaseMessagingService(), CoroutineScope by MainScope() {

    private val appPreferences by inject<AppPreferences>()
    private val userRepository by inject<UserRepository>()
    private val messagingRepository by inject<MessageRepository>()
    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(this) }

    @ImplicitReflectionSerializer
    override fun onMessageReceived(message: RemoteMessage) {
        message.data["lastMessages"]?.let { lastMessages ->
            val messages = Json.nonstrict.parse(Message.serializer().list, lastMessages)
            Timber.d("Notification(lastMessages=${messages.size})")
            showNotification(messages)
        }
    }

    override fun onNewToken(token: String) {
        appPreferences.pushToken = token
        appPreferences.pushTokenUploaded = false
        launch {
            userRepository.updatePushToken(token)
        }
    }

    private fun showNotification(messages: List<Message>) = launch {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val icon = firebaseUser?.photoUrl?.let { photoUri ->
            getIcon(photoUri)
        }


        val replies = withContext(Dispatchers.IO) {
            messagingRepository.getSmartReplies(messages)
        }.getOrDefault(emptyList()).toTypedArray()

        // Create the RemoteInput specifying this key.
        val replyLabel = getString(R.string.reply_label)
        val remoteInput: RemoteInput = RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
            .setLabel(replyLabel)
            .setChoices(replies)
            .build()

        val mainPendingIntent = PendingIntent.getActivity(
            this@GossipMessagingService,
            0,
            Intent(this@GossipMessagingService, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val replyActionPendingIntent: PendingIntent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                this@GossipMessagingService,
                MessagingIntentService::class.java
            )
            intent.action = MessagingIntentService.ACTION_REPLY
            replyActionPendingIntent = PendingIntent.getService(this@GossipMessagingService, 0, intent, 0)
        } else {
            replyActionPendingIntent = mainPendingIntent
        }

        val replyAction: NotificationCompat.Action? =
            NotificationCompat.Action.Builder(
                R.drawable.ic_reply_white_18dp,
                replyLabel,
                replyActionPendingIntent
            )
                .addRemoteInput(remoteInput)
                // Informs system we aren't bringing up our own custom UI for a reply
                // action.
                .setShowsUserInterface(false)
                // Allows system to generate replies by context of conversation.
                .setAllowGeneratedReplies(true)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .build()

        val user = Person.Builder()
            .setName(firebaseUser?.displayName)
            .setIcon(icon)
            .build()

        val notificationMessages = messages.map {
            MessagingStyle.Message(
                it.value,
                it.createdAt.toDate().time,
                Person.Builder()
                    .setName(it.user.name)
                    .setIcon(getIcon(it.user.avatarUrl.toUri()))
                    .build()
            )
        }

        val style = MessagingStyle(user)
            .setGroupConversation(true)
            .setConversationTitle("DevFest")

        notificationMessages.forEach {
            style.addMessage(it)
        }

        val builder = NotificationCompat.Builder(this@GossipMessagingService,
            getString(R.string.default_notification_channel_id))
                .setColor(color(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_message)
                .setStyle(style)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .addAction(replyAction)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(builder)

        withContext(Dispatchers.Main) {
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private suspend fun getIcon(iconUri: Uri): IconCompat? {
        return withContext(Dispatchers.IO) {
            IconCompat.createWithBitmap(
                Coil.get(iconUri) {
                    transformations(CircleCropTransformation())
                    size(dip(40f), dip(40f))
                }.toBitmap()
            )
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1000
    }
}
