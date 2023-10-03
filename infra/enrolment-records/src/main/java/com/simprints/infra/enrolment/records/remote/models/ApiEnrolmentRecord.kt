package com.simprints.infra.enrolment.records.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.takeIfTokenized
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.remote.models.ApiEnrolmentRecord.Companion.ATTENDANT_ID
import com.simprints.infra.enrolment.records.remote.models.ApiEnrolmentRecord.Companion.MODULE_ID
import com.simprints.infra.enrolment.records.remote.models.face.toApi
import com.simprints.infra.enrolment.records.remote.models.fingerprint.toApi

@Keep
internal data class ApiEnrolmentRecord(
    val subjectId: String,
    val moduleId: String,
    val attendantId: String,
    val tokenizedFields: List<String>,
    val biometricReferences: List<ApiBiometricReference>
) {
    companion object {
        const val MODULE_ID = "moduleId"
        const val ATTENDANT_ID = "attendantId"
    }
}

internal fun Subject.toEnrolmentRecord(encoder: EncodingUtils): ApiEnrolmentRecord {
    val tokenizedFieldModuleId: String? = moduleId.takeIfTokenized(MODULE_ID)
    val tokenizedFieldAttendantId: String? = attendantId.takeIfTokenized(ATTENDANT_ID)
    val tokenizedFields = listOfNotNull(tokenizedFieldModuleId, tokenizedFieldAttendantId)
    return ApiEnrolmentRecord(
        subjectId = subjectId,
        moduleId = moduleId.value,
        attendantId = attendantId.value,
        tokenizedFields = tokenizedFields,
        biometricReferences = buildBiometricReferences(fingerprintSamples, faceSamples, encoder)
    )
}

internal fun buildBiometricReferences(
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
