package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.sample.Sample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.remote.models.face.toFaceApi
import com.simprints.infra.enrolment.records.repository.remote.models.fingerprint.toFingerprintApi

@Keep
internal data class ApiEnrolmentRecord(
    val subjectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>,
)

internal fun Subject.toEnrolmentRecord(encoder: EncodingUtils): ApiEnrolmentRecord = ApiEnrolmentRecord(
    subjectId,
    moduleId.value,
    attendantId.value,
    buildBiometricReferences(fingerprintSamples, faceSamples, encoder),
)

internal fun buildBiometricReferences(
    fingerprintSamples: List<Sample>,
    faceSamples: List<Sample>,
    encoder: EncodingUtils,
): List<ApiBiometricReference> {
    val biometricReferences = mutableListOf<ApiBiometricReference>()
    fingerprintSamples.toFingerprintApi(encoder)?.let { biometricReferences.add(it) }
    faceSamples.toFaceApi(encoder)?.let { biometricReferences.add(it) }
    return biometricReferences
}
