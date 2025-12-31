package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Keep
@Serializable(with = ApiCalloutSerializer::class)
internal sealed class ApiCallout {
    abstract val type: ApiCalloutType
}

internal object ApiCalloutSerializer : KSerializer<ApiCallout> {
    override val descriptor = buildClassSerialDescriptor("ApiCallout")

    override fun serialize(
        encoder: Encoder,
        value: ApiCallout,
    ) {
        when (value) {
            is ApiConfirmationCalloutV2 -> encoder.encodeSerializableValue(ApiConfirmationCalloutV2.serializer(), value)

            is ApiConfirmationCalloutV3 -> encoder.encodeSerializableValue(ApiConfirmationCalloutV3.serializer(), value)

            is ApiEnrolmentCalloutV2 -> encoder.encodeSerializableValue(ApiEnrolmentCalloutV2.serializer(), value)

            is ApiEnrolmentCalloutV3 -> encoder.encodeSerializableValue(ApiEnrolmentCalloutV3.serializer(), value)

            is ApiEnrolmentLastBiometricsCalloutV2 -> encoder.encodeSerializableValue(
                ApiEnrolmentLastBiometricsCalloutV2.serializer(),
                value,
            )

            is ApiEnrolmentLastBiometricsCalloutV3 -> encoder.encodeSerializableValue(
                ApiEnrolmentLastBiometricsCalloutV3.serializer(),
                value,
            )

            is ApiIdentificationCalloutV2 -> encoder.encodeSerializableValue(ApiIdentificationCalloutV2.serializer(), value)

            is ApiIdentificationCalloutV3 -> encoder.encodeSerializableValue(ApiIdentificationCalloutV3.serializer(), value)

            is ApiVerificationCalloutV2 -> encoder.encodeSerializableValue(ApiVerificationCalloutV2.serializer(), value)

            is ApiVerificationCalloutV3 -> encoder.encodeSerializableValue(ApiVerificationCalloutV3.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): ApiCallout {
        TODO("Not yet implemented")
    }
}
