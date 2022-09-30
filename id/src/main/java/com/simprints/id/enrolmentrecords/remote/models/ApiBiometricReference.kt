package com.simprints.id.enrolmentrecords.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.enrolmentrecords.remote.models.ApiBiometricReference.Companion.FACE_REFERENCE_KEY
import com.simprints.id.enrolmentrecords.remote.models.ApiBiometricReference.Companion.FINGERPRINT_REFERENCE_KEY
import com.simprints.id.enrolmentrecords.remote.models.face.ApiFaceReference
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.ApiFingerprintReference

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = ApiFaceReference::class,
        name = FACE_REFERENCE_KEY
    ),
    JsonSubTypes.Type(value = ApiFingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY)
)
@Keep
abstract class ApiBiometricReference(
    val type: ApiBiometricReferenceType,
) {

    enum class ApiBiometricReferenceType {
        FingerprintReference,
        FaceReference
    }

    companion object {
        const val FACE_REFERENCE_KEY = "FaceReference"
        const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"
    }
}
