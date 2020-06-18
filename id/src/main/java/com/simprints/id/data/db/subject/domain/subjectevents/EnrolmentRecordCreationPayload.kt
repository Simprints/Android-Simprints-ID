package com.simprints.id.data.db.subject.domain.subjectevents

import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordCreationPayload

data class EnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<BiometricReference>
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_CREATION)

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
