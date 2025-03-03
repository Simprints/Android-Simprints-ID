package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.remote.models.face.toApi
import com.simprints.infra.enrolment.records.repository.remote.models.fingerprint.toApi

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
    fingerprintSamples: List<FingerprintSample>,
    faceSamples: List<FaceSample>,
    encoder: EncodingUtils,
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
