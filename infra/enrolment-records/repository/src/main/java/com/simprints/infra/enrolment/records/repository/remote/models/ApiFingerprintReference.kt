package com.simprints.infra.enrolment.records.repository.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.tools.utils.EncodingUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("API model")
@Serializable
@SerialName(ApiBiometricReference.FINGERPRINT_REFERENCE_KEY)
internal data class ApiFingerprintReference(
    val id: String,
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
    override val type: ApiBiometricReferenceType = ApiBiometricReferenceType.FingerprintReference,
) : ApiBiometricReference()

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
