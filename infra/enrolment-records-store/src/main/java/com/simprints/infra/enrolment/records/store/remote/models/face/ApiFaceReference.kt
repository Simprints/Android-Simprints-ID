package com.simprints.infra.enrolment.records.store.remote.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.concatTemplates
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.floatArrayToByteArray
import com.simprints.infra.enrolment.records.store.remote.models.ApiBiometricReference
import java.util.UUID

@Keep
internal data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference(ApiBiometricReferenceType.FaceReference)

internal fun List<FaceSample>.toApi(encoder: EncodingUtils): ApiFaceReference? = if (isNotEmpty()) {
    ApiFaceReference(
        UUID.nameUUIDFromBytes(floatArrayToByteArray(concatTemplates())).toString(),
        map {
            ApiFaceTemplate(encoder.floatArrayToBase64(it.template))
        },
        first().format,
    )
} else {
    null
}
