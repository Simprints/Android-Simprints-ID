package com.simprints.infra.enrolment.records.repository.remote.models

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type") // equivalent to Jackson's 'property = "type"'
@Polymorphic
internal abstract class ApiBiometricReference(
    val type: ApiBiometricReferenceType,
) {
    @Serializable
    enum class ApiBiometricReferenceType {
        FingerprintReference,
        FaceReference,
    }

    companion object {
        const val FACE_REFERENCE_KEY = "FaceReference"
        const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"
    }
}
