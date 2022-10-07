package com.simprints.infra.enrolment.records.remote.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.concatTemplates
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.remote.models.ApiBiometricReference
import java.util.*

@Keep
internal data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: ApiFaceTemplateFormat,
    val metadata: HashMap<String, String>? = null
) : ApiBiometricReference(ApiBiometricReferenceType.FaceReference)

internal fun List<FaceSample>.toApi(encoder: EncodingUtils): ApiFaceReference? =
    if (isNotEmpty()) {
        ApiFaceReference(
            UUID.nameUUIDFromBytes(concatTemplates()).toString(),
            map {
                ApiFaceTemplate(encoder.byteArrayToBase64(it.template))
            },
            first().format.toApi()
        )
    } else {
        null
    }
