package com.simprints.core.tools.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringNumberNullableValueSerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringNumberNullableValue", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Any?,
    ) {
        if (value == null) {
            encoder.encodeNull()
            return
        }

        when (value) {
            is String -> encoder.encodeString(value)

            is Number -> encoder.encodeDouble(value.toDouble())

            else -> throw SerializationException(
                "Unsupported nullable type: ${value::class}",
            )
        }
    }

    override fun deserialize(decoder: Decoder): Any? = try {
        val raw = decoder.decodeString()
        raw.toDoubleOrNull() ?: raw
    } catch (_: SerializationException) {
        null
    }
}
