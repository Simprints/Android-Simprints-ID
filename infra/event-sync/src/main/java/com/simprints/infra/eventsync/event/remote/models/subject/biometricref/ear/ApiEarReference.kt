package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ear

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import java.util.UUID

@Keep
internal data class ApiEarReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiEarTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = ApiBiometricReferenceType.EarReference
}
