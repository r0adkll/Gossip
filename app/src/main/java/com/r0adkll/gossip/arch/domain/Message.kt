package com.r0adkll.gossip.arch.domain

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message(
    @Exclude var id: String = "",
    val type: MessageType = MessageType.TEXT,
    val value: String = "",
    val user: User = User(),
    val createdAt: Timestamp = Timestamp.now()
) : Parcelable {

    companion object {

        fun text(text: String): Message = Message(type = MessageType.TEXT, value = text)
        fun gif(url: String): Message = Message(type = MessageType.GIF, value = url)
    }
}
