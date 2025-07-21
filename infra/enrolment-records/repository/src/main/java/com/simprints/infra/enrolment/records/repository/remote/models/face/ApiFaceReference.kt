package com.simprints.infra.enrolment.records.repository.remote.models.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.repository.remote.models.ApiBiometricReference

@Keep
internal data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: String,
    // [MS-1076] The parent 'ApiBiometricReference' class should have its JsonSubTypes annotation updated to
    // @JsonSubTypes.Type([...], looseHandling = true) once we update to SDK => 25 and Jackson => 2.16.0.
    // Then, this annotation should be removed
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
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
