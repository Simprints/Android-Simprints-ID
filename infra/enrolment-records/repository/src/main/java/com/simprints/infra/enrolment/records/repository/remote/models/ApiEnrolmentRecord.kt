package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.remote.models.face.toFaceApi
import com.simprints.infra.enrolment.records.repository.remote.models.fingerprint.toFingerprintApi
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEnrolmentRecord(
    val subjectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>,
)

internal fun EnrolmentRecord.toEnrolmentRecord(encoder: EncodingUtils): ApiEnrolmentRecord = ApiEnrolmentRecord(
    subjectId,
    moduleId.value,
    attendantId.value,
    buildBiometricReferences(references, encoder),
)

internal fun buildBiometricReferences(
    references: List<BiometricReference>,
    encoder: EncodingUtils,
): List<ApiBiometricReference> = references.mapNotNull { reference ->
    when (reference.modality) {
        Modality.FINGERPRINT -> reference.toFingerprintApi(encoder)
        Modality.FACE -> reference.toFaceApi(encoder)
    }
}
