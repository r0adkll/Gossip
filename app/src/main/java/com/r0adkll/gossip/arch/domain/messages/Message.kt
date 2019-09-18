package com.r0adkll.gossip.arch.domain.messages

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.r0adkll.gossip.arch.domain.user.User
import com.r0adkll.gossip.util.TimestampSerializer
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Message(
    @Exclude var id: String = "",
    val type: MessageType = MessageType.TEXT,
    val value: String = "",
    val wasSuggestion: Boolean = false,
    val user: User = User(),
    @Serializable(with = TimestampSerializer::class) val createdAt: Timestamp = Timestamp.now()
) : Parcelable {

    companion object {

        fun text(text: String, suggestion: Boolean = false): Message =
            Message(
                type = MessageType.TEXT,
                value = text,
                wasSuggestion = suggestion
            )

        fun gif(url: String): Message =
            Message(
                type = MessageType.GIF,
                value = url
            )
    }
}
