package com.simprints.infra.enrolment.records.repository.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.remote.models.ApiBiometricReference

@Keep
internal data class ApiFingerprintReference(
    val id: String,
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference(ApiBiometricReferenceType.FingerprintReference)

internal fun List<FingerprintSample>.toApi(encoder: EncodingUtils): ApiFingerprintReference? = if (isNotEmpty()) {
    ApiFingerprintReference(
        first().referenceId,
        map {
            ApiFingerprintTemplate(
                it.templateQualityScore,
                encoder.byteArrayToBase64(it.template),
                it.fingerIdentifier.toApi(),
            )
        },
        first().format,
    )
} else {
    null
}
