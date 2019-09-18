package com.r0adkll.gossip.util

import com.google.firebase.Timestamp
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

@Serializer(forClass = Timestamp::class)
object TimestampSerializer : KSerializer<Timestamp>{

    @Serializable
    class TimestampObject (
        val _seconds: Long,
        val _nanoseconds: Int
    )

    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("FirebaseTimestamp")

    override fun serialize(encoder: Encoder, obj: Timestamp) {
        val dateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(obj.seconds, obj.nanoseconds.toLong()))
        encoder.encodeString(dateTime)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val dateTime = decoder.decode(TimestampObject.serializer())
        return Timestamp(dateTime._seconds, dateTime._nanoseconds)
    }
}
