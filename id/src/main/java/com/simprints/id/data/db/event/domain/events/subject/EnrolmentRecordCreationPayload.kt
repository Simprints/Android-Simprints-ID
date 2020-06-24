package com.simprints.id.data.db.event.domain.events.subject

import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationPayload

data class EnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<BiometricReference>
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_CREATION, 0, 0)
// startTime and relativeStartTime are not used for Pokodex events

/* For GDPR, we might have to remove biometric references for some creation events,
which would mean that we would get a response from the backend without biometric references,
if that happens, we would  not be converting that event payload to domain. */
fun ApiEnrolmentRecordCreationPayload.fromApiToDomainOrNullIfNoBiometricReferences() =
    biometricReferences?.let { biometricRefs ->
        EnrolmentRecordCreationPayload(
            subjectId,
            projectId,
            moduleId,
            attendantId,
            biometricRefs.map { it.fromApiToDomain() }
        )
    }
