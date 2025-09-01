package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modality
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
    buildBiometricReferences(samples, encoder),
)

internal fun buildBiometricReferences(
    samples: List<Sample>,
    encoder: EncodingUtils,
): List<ApiBiometricReference> = samples
    .groupBy { it.modality }
    .mapNotNull { (modality, modalitySamples) ->
        when (modality) {
            Modality.FINGERPRINT -> modalitySamples.toFingerprintApi(encoder)
            Modality.FACE -> modalitySamples.toFaceApi(encoder)
        }
    }
