package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class EnrolmentRecordUpdateEvent(
    override val id: String,
    val payload: EnrolmentRecordUpdatePayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordUpdate) {
    constructor(
        subjectId: String,
        biometricReferencesAdded: List<BiometricReference>,
        biometricReferencesRemoved: List<String>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordUpdatePayload(
            subjectId,
            biometricReferencesAdded,
            biometricReferencesRemoved,
        ),
    )

    @Keep
    data class EnrolmentRecordUpdatePayload(
        val subjectId: String,
        val biometricReferencesAdded: List<BiometricReference>,
        val biometricReferencesRemoved: List<String>,
    )
}
