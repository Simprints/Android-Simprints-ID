package com.simprints.infra.enrolment.records.repository.remote.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ApiBiometricReference.ApiBiometricReferenceSerializer::class)
internal sealed class ApiBiometricReference {
    abstract val type: ApiBiometricReferenceType

    @Serializable
    enum class ApiBiometricReferenceType {
        FingerprintReference,
        FaceReference,
    }

    companion object {
        const val FACE_REFERENCE_KEY = "FaceReference"
        const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"
    }

    object ApiBiometricReferenceSerializer : KSerializer<ApiBiometricReference> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ApiBiometricReference", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ApiBiometricReference {
            error("Deserialization not supported")
        }

        override fun serialize(
            encoder: Encoder,
            value: ApiBiometricReference,
        ) {
            when (value) {
                is ApiFaceReference -> encoder.encodeSerializableValue(ApiFaceReference.serializer(), value)
                is ApiFingerprintReference -> encoder.encodeSerializableValue(ApiFingerprintReference.serializer(), value)
            }
        }
    }
}
