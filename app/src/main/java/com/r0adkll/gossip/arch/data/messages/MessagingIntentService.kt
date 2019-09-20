package com.r0adkll.gossip.arch.data.messages

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
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
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.domain.messages.Message
import com.r0adkll.gossip.arch.domain.messages.MessageRepository
import com.r0adkll.gossip.arch.ui.MainActivity
import com.r0adkll.gossip.fcm.GossipMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber


class MessagingIntentService : IntentService("messaging-intent-service"),
    CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val messageRepository by inject<MessageRepository>()
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

    override fun onHandleIntent(intent: Intent?) {
        Timber.d("onHandleIntent(): $intent")

        if (intent != null) {
            if (ACTION_REPLY == intent.action) {
                handleActionReply(getMessage(intent))
            }
        }
    }

    /** Handles action for replying to messages from the notification.  */
    private fun handleActionReply(replyCharSequence: CharSequence?) = launch {
        Timber.d("handleActionReply(): $replyCharSequence")
        if (replyCharSequence != null) {

            // Post message to firestore
            messageRepository.postMessage(Message.text(replyCharSequence.toString(), true))

            // Retrieves NotificationCompat.Builder used to create initial Notification
            val notificationCompatBuilder = recreateBuilderWithMessagingStyle()

            // Updates the Notification
            notificationManager.notify(
                GossipMessagingService.NOTIFICATION_ID,
                notificationCompatBuilder.build()
            )
        }
    }

    private suspend fun recreateBuilderWithMessagingStyle(): NotificationCompat.Builder {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val icon = firebaseUser?.photoUrl?.let { photoUri ->
            getIcon(photoUri)
        }

        val messages = messageRepository.getRecentMessages()
            .getOrDefault(emptyList())

        val replies = withContext(Dispatchers.IO) {
            messageRepository.getSmartReplies(messages)
        }.getOrDefault(emptyList()).toTypedArray()

        // Create the RemoteInput specifying this key.
        val replyLabel = getString(R.string.reply_label)
        val remoteInput: RemoteInput = RemoteInput.Builder(EXTRA_REPLY)
            .setLabel(replyLabel)
            .setChoices(replies)
            .build()

        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val replyActionPendingIntent: PendingIntent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(
                this,
                MessagingIntentService::class.java
            )
            intent.action = ACTION_REPLY
            replyActionPendingIntent = PendingIntent.getService(this, 0, intent, 0)
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
            NotificationCompat.MessagingStyle.Message(
                it.value,
                it.createdAt.toDate().time,
                Person.Builder()
                    .setName(it.user.name)
                    .setIcon(getIcon(it.user.avatarUrl.toUri()))
                    .build()
            )
        }

        val style = NotificationCompat.MessagingStyle(user)
            .setGroupConversation(true)
            .setConversationTitle("DevFest")

        notificationMessages.forEach {
            style.addMessage(it)
        }

        return NotificationCompat.Builder(this,
            getString(R.string.default_notification_channel_id))
            .setColor(color(R.color.colorPrimary))
            .setSmallIcon(R.drawable.ic_notification_message)
            .setStyle(style)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(replyAction)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    /*
     * Extracts CharSequence created from the RemoteInput associated with the Notification.
     */
    private fun getMessage(intent: Intent?): CharSequence? {
        val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent)
        return remoteInput.getCharSequence(EXTRA_REPLY)
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
        const val EXTRA_REPLY = "MessagingIntentService.Reply"
        const val ACTION_REPLY = "com.r0adkll.gossip.intent.ACTION_REPLY"
    }
}
