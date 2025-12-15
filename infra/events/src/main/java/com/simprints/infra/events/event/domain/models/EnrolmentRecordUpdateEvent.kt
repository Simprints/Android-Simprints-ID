package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredential
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName("EnrolmentRecordUpdate")
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class EnrolmentRecordUpdateEvent(
    override val id: String,
    val payload: EnrolmentRecordUpdatePayload,
) : EnrolmentRecordEvent() {
    override val type: EnrolmentRecordEventType
        get() = EnrolmentRecordEventType.EnrolmentRecordUpdate

    constructor(
        subjectId: String,
        biometricReferencesAdded: List<BiometricReference>,
        biometricReferencesRemoved: List<String>,
        externalCredentialsAdded: List<ExternalCredential>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordUpdatePayload(
            subjectId = subjectId,
            biometricReferencesAdded = biometricReferencesAdded,
            biometricReferencesRemoved = biometricReferencesRemoved,
            externalCredentialsAdded = externalCredentialsAdded,
        ),
    )

    @Keep
    @Serializable
    data class EnrolmentRecordUpdatePayload(
        val subjectId: String,
        val biometricReferencesAdded: List<BiometricReference> = emptyList(),
        val biometricReferencesRemoved: List<String> = emptyList(),
        val externalCredentialsAdded: List<ExternalCredential> = emptyList(),
    )
}
