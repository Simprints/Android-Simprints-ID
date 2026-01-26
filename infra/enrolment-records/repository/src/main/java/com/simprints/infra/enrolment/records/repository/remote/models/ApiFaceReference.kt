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
@SerialName(ApiBiometricReference.FACE_REFERENCE_KEY)
internal data class ApiFaceReference(
    val id: String,
    val templates: List<ApiFaceTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
    override val type: ApiBiometricReferenceType = ApiBiometricReferenceType.FaceReference,
) : ApiBiometricReference()

internal fun BiometricReference.toFaceApi(encoder: EncodingUtils): ApiFaceReference? = if (templates.isNotEmpty()) {
    ApiFaceReference(
        referenceId,
        templates.map { ApiFaceTemplate(encoder.byteArrayToBase64(it.template)) },
        format,
    )
} else {
    null
}
