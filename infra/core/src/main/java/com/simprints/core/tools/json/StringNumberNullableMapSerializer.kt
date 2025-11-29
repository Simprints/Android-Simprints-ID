package com.simprints.core.tools.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringNumberNullableMapSerializer : KSerializer<Map<String, Any?>> {
    private val delegate = MapSerializer(
        String.serializer(),
        StringNumberNullableValueSerializer,
    )

    override val descriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Map<String, Any?>,
    ) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> = delegate.deserialize(decoder)
}
