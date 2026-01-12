package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Keep
@Serializable(with = ApiCallbackSerializer::class)
internal sealed class ApiCallback {
    abstract val type: ApiCallbackType
}

internal object ApiCallbackSerializer : KSerializer<ApiCallback> {
    override val descriptor = buildClassSerialDescriptor("ApiCallback")

    override fun serialize(
        encoder: Encoder,
        value: ApiCallback,
    ) {
        when (value) {
            is ApiConfirmationCallback -> encoder.encodeSerializableValue(ApiConfirmationCallback.serializer(), value)
            is ApiEnrolmentCallback -> encoder.encodeSerializableValue(ApiEnrolmentCallback.serializer(), value)
            is ApiErrorCallback -> encoder.encodeSerializableValue(ApiErrorCallback.serializer(), value)
            is ApiIdentificationCallback -> encoder.encodeSerializableValue(ApiIdentificationCallback.serializer(), value)
            is ApiRefusalCallback -> encoder.encodeSerializableValue(ApiRefusalCallback.serializer(), value)
            is ApiVerificationCallback -> encoder.encodeSerializableValue(ApiVerificationCallback.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): ApiCallback {
        error("Deserialization without discriminator is not supported")
    }
}
