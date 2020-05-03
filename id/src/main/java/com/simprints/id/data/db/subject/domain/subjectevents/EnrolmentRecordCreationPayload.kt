package com.simprints.id.data.db.subject.domain.personevents

import com.simprints.id.data.db.subject.remote.models.personevents.ApiEnrolmentRecordCreationPayload

data class EnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<BiometricReference>
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_CREATION) {

    constructor(payload: ApiEnrolmentRecordCreationPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId,
            payload.attendantId, payload.biometricReferences.map { it.fromApiToDomain() })
}
