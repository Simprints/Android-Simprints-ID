package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FaceReference
import java.util.UUID

@Keep
internal data class ApiFaceReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFaceTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FaceReference
}
