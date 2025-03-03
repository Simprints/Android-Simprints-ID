package com.simprints.infra.enrolment.records.repository.remote.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.remote.models.ApiBiometricReference

@Keep
internal data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference(ApiBiometricReferenceType.FaceReference)

internal fun List<FaceSample>.toApi(encoder: EncodingUtils): ApiFaceReference? = if (isNotEmpty()) {
    ApiFaceReference(
        first().referenceId,
        map { ApiFaceTemplate(encoder.byteArrayToBase64(it.template)) },
        first().format,
    )
} else {
    null
}
