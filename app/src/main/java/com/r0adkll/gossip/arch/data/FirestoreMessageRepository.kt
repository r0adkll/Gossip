package com.r0adkll.gossip.arch.data

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import com.r0adkll.gossip.arch.domain.Message
import com.r0adkll.gossip.arch.domain.MessageRepository
import com.r0adkll.gossip.arch.domain.MessageType
import com.r0adkll.gossip.extensions.await
import com.r0adkll.gossip.extensions.liveData
import timber.log.Timber
import java.io.IOException


class FirestoreMessageRepository : MessageRepository {

    override fun observeMessages(): LiveData<List<Message>> {
        return getMessagesCollection().liveData { message, id ->
            message.id = id
        }
    }

    override suspend fun postMessage(message: Message): Result<String> {
        return try {
            val newDocument = getMessagesCollection().add(message).await()
            Result.success(newDocument.id)
        } catch (e: Exception) {
            Timber.e(e, "Unable to add message to Firestore")
            Result.failure(e)
        }
    }

    override suspend fun getSmartReplies(messages: List<Message>): Result<List<String>> {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
            val conversation = messages
                .filter { it.type == MessageType.TEXT }
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
                Result.failure(IOException("No viable suggestions"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Unable to fetch smart replies")
            Result.failure(e)
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
