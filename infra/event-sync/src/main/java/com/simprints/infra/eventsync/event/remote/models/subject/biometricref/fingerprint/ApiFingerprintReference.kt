package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FingerprintReference
import java.util.UUID

@Keep
internal data class ApiFingerprintReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    val metadata: HashMap<String, String>? = null,
) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FingerprintReference
}
