package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FaceReference
import java.util.UUID

@Keep
internal data class ApiFaceReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFaceTemplate>,
    val format: String,
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    val metadata: Map<String, String>? = null,
) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FaceReference
}
