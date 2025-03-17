package com.simprints.infra.enrolment.records.store.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.store.remote.models.ApiBiometricReference
import java.util.UUID

@Keep
internal data class ApiFingerprintReference(
    val id: String,
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference(ApiBiometricReferenceType.FingerprintReference)

internal fun List<FingerprintSample>.toApi(encoder: EncodingUtils): ApiFingerprintReference? = if (isNotEmpty()) {
    ApiFingerprintReference(
        UUID.randomUUID().toString(), // We will test with face no need to fix fingerprint
        map {
            ApiFingerprintTemplate(
                it.templateQualityScore,
                encoder.floatArrayToBase64(it.template),
                it.fingerIdentifier.toApi(),
            )
        },
        first().format,
    )
} else {
    null
}
