package com.simprints.infra.events.remote.models.subject.biometricref.face

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.face.FaceTemplateFormat
import com.simprints.infra.events.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.events.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.events.remote.models.subject.biometricref.ApiBiometricReferenceType.FaceReference
import java.util.*

@Keep
data class ApiFaceReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFaceTemplate>,
    val format: FaceTemplateFormat,
    val metadata: HashMap<String, String>? = null) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FaceReference
}
