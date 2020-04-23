package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreationPayload

@Keep
data class ApiEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_CREATION) {

    constructor(payload: EnrolmentRecordCreationPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId,
            payload.attendantId, payload.biometricReferences.map { it.fromDomainToApi() })
}
