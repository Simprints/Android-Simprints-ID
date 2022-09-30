package com.simprints.id.enrolmentrecords.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.enrolmentrecords.remote.models.face.toApi
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.toApi

@Keep
data class ApiEnrolmentRecord(
    val subjectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>
)

fun Subject.toEnrolmentRecord(encoder: EncodingUtils): ApiEnrolmentRecord =
    ApiEnrolmentRecord(
        subjectId,
        moduleId,
        attendantId,
        buildBiometricReferences(fingerprintSamples, faceSamples,encoder)
    )

fun buildBiometricReferences(
    fingerprintSamples: List<FingerprintSample>,
    faceSamples: List<FaceSample>,
    encoder: EncodingUtils
): List<ApiBiometricReference> {
    val biometricReferences = mutableListOf<ApiBiometricReference>()

    fingerprintSamples.toApi(encoder)?.let {
        biometricReferences.add(it)
    }

    faceSamples.toApi(encoder)?.let {
        biometricReferences.add(it)
    }

    return biometricReferences
}
