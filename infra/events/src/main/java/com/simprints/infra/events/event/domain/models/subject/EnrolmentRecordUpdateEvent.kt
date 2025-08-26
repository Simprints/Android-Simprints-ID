package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredential
import java.util.UUID

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class EnrolmentRecordUpdateEvent(
    override val id: String,
    val payload: EnrolmentRecordUpdatePayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordUpdate) {
    constructor(
        subjectId: String,
        biometricReferencesAdded: List<BiometricReference>,
        biometricReferencesRemoved: List<String>,
        externalCredentialAdded: ExternalCredential?,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordUpdatePayload(
            subjectId = subjectId,
            biometricReferencesAdded = biometricReferencesAdded,
            biometricReferencesRemoved = biometricReferencesRemoved,
            externalCredentialAdded = externalCredentialAdded,
        ),
    )

    @Keep
    data class EnrolmentRecordUpdatePayload(
        val subjectId: String,
        val biometricReferencesAdded: List<BiometricReference>,
        val biometricReferencesRemoved: List<String>,
        val externalCredentialAdded: ExternalCredential?,
    )
}
