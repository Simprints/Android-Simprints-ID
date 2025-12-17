package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.remote.models.ApiBiometricReference

@Keep
@ExcludedFromGeneratedTestCoverageReports("API model")
internal data class ApiFingerprintReference(
    val id: String,
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    // [MS-1076] The parent 'ApiBiometricReference' class should have its JsonSubTypes annotation updated to
    // @JsonSubTypes.Type([...], looseHandling = true) once we update to SDK => 25 and Jackson => 2.16.0.
    // Then, this annotation should be removed
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference(ApiBiometricReferenceType.FingerprintReference)

internal fun BiometricReference.toFingerprintApi(encoder: EncodingUtils): ApiFingerprintReference? = if (templates.isNotEmpty()) {
    ApiFingerprintReference(
        referenceId,
        templates.map {
            ApiFingerprintTemplate(encoder.byteArrayToBase64(it.template), it.identifier.toApi())
        },
        format,
    )
} else {
    null
}
