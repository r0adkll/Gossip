package com.r0adkll.gossip.arch.data.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.arch.domain.user.UserRepository
import com.r0adkll.gossip.extensions.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class FirestoreUserRepository(
    val appPreferences: AppPreferences
) : UserRepository {

    override suspend fun updatePushToken(pushToken: String) = withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw IllegalStateException("No user logged in")

        val userDocument = getUserDocument(user.uid)

        userDocument.set(mapOf(
            "name" to user.displayName,
            "avatarUrl" to user.photoUrl?.toString(),
            "pushToken" to pushToken
        )).await()

        appPreferences.pushTokenUploaded = true
    }

    private fun getUserDocument(uid: String): DocumentReference {
        return Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(uid)
    }

    companion object {
        private const val COLLECTION_USERS = "users"
    }
}
