package com.r0adkll.gossip.arch.data.messages

import androidx.lifecycle.LiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import com.r0adkll.gossip.arch.domain.messages.Message
import com.r0adkll.gossip.arch.domain.messages.MessageRepository
import com.r0adkll.gossip.arch.domain.messages.MessageType
import com.r0adkll.gossip.arch.domain.user.User
import com.r0adkll.gossip.extensions.await
import com.r0adkll.gossip.extensions.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import kotlin.IllegalStateException

class FirestoreMessageRepository : MessageRepository {

    override fun observeMessages(): LiveData<List<Message>> {
        return getMessagesCollection().liveData { message, id ->
            message.id = id
        }
    }

    override suspend fun getRecentMessages(limit: Long): Result<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val messages = getMessagesCollection()
                .limit(limit)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects<Message>()
            Result.success(messages)
        } catch (e: FirebaseException) {
            Timber.e(e, "Failed to get recent messages")
            Result.failure<List<Message>>(e)
        }
    }

    override suspend fun postMessage(message: Message): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val messageWithUser = message.copy(
                    user = User(
                        user.uid,
                        user.displayName ?: "Random User",
                        user.photoUrl?.toString() ?: ""
                    )
                )
                val newDocument = getMessagesCollection().add(messageWithUser).await()
                Result.success(newDocument.id)
            } else {
                Result.failure(IllegalStateException("No user authenticated"))
            }
        } catch (e: FirebaseException) {
            Timber.e(e, "Unable to add message to Firestore")
            Result.failure<String>(e)
        }
    }

    override suspend fun getSmartReplies(messages: List<Message>): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
            val conversation = messages
                .filter { it.type == MessageType.TEXT }
                .sortedBy { it.createdAt }
                .map {
                    if (it.user.id == userId) {
                        FirebaseTextMessage.createForLocalUser(it.value, it.createdAt.toDate().time)
                    } else {
                        FirebaseTextMessage.createForRemoteUser(it.value, it.createdAt.toDate().time, it.user.id)
                    }
                }

            val result = smartReply.suggestReplies(conversation).await()
            if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS && result.suggestions.isNotEmpty()) {
                Result.success(result.suggestions.map { it.text })
            } else {
                Timber.w("SmartReplyResult: $result")
                Result.failure(IOException("No viable suggestions"))
            }
        } catch (e: FirebaseException) {
            Timber.e(e, "Unable to fetch smart replies")
            Result.failure<List<String>>(e)
        }
    }

    private fun getMessagesCollection(): CollectionReference {
        return Firebase.firestore
            .collection(COLLECTION_THREADS)
            .document(DOC_GENERAL)
            .collection(COLLECTION_MESSAGES)
    }

    companion object {
        private const val COLLECTION_THREADS = "threads"
        private const val DOC_GENERAL = "general"
        private const val COLLECTION_MESSAGES = "messages"
    }
}
