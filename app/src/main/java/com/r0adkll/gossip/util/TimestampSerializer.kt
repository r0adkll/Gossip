package com.r0adkll.gossip.util

import com.google.firebase.Timestamp
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.decode
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter

@Serializer(forClass = Timestamp::class)
object TimestampSerializer : KSerializer<Timestamp> {

    @Suppress("naming")
    @Serializable
    class TimestampObject(
        @SerialName("_seconds") val seconds: Long,
        @SerialName("_nanoseconds") val nanoseconds: Int
    )

    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("FirebaseTimestamp")

    override fun serialize(encoder: Encoder, obj: Timestamp) {
        val dateTime = DateTimeFormatter.ISO_INSTANT
            .format(Instant.ofEpochSecond(obj.seconds, obj.nanoseconds.toLong()))
        encoder.encodeString(dateTime)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val dateTime = decoder.decode(TimestampObject.serializer())
        return Timestamp(dateTime.seconds, dateTime.nanoseconds)
    }
}
