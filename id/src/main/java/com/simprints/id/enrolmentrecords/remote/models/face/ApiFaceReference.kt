package com.simprints.id.enrolmentrecords.remote.models.face

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.concatTemplates
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.id.enrolmentrecords.remote.models.ApiBiometricReference
import java.util.*

@Keep
data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: ApiFaceTemplateFormat,
    val metadata: HashMap<String, String>? = null
) : ApiBiometricReference(ApiBiometricReferenceType.FaceReference)

fun List<FaceSample>.toApi(encoder: EncodingUtils): ApiFaceReference? =
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
