package com.simprints.id.data.db.subject.remote.models.subjectevents

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordCreationPayload

@Keep
data class ApiEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_CREATION) {

    constructor(payload: EnrolmentRecordCreationPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId,
            payload.attendantId, payload.biometricReferences?.map { it.fromDomainToApi() })
}
