package com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FingerprintReference
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("API model")
internal data class ApiFingerprintReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFingerprintTemplate>,
    val format: String,
    // [MS-1076] The parent 'ApiBiometricReference' class should have its JsonSubTypes annotation updated to
    // @JsonSubTypes.Type([...], looseHandling = true) once we update to SDK => 25 and Jackson => 2.16.0.
    // Then, this annotation should be removed
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    val metadata: Map<String, String>? = null,
) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FingerprintReference
}
