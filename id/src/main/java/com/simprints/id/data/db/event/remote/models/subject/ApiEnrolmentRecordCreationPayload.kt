package com.simprints.id.data.db.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType

@Keep
data class ApiEnrolmentRecordCreationPayload(
    @JsonIgnore override val relativeStartTime: Long, //Not added on API yet
    override val version: Int,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?
) : ApiEventPayload(ApiEventPayloadType.EnrolmentRecordCreation, version, relativeStartTime) {

    constructor(payload: EnrolmentRecordCreationPayload) :
        this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId,
            payload.attendantId, payload.biometricReferences.map { it.fromDomainToApi() })
}


fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() =
    EnrolmentRecordCreationPayload(
        relativeStartTime ?: 0,
        version,
        subjectId,
        projectId,
        moduleId,
        attendantId,
        biometricReferences?.map { it.fromApiToDomain() } ?: emptyList()
    )

/* For GDPR, we might have to remove biometric references for some creation events,
which would mean that we would get a response from the backend without biometric references,
if that happens, we would  not be converting that event payload to domain. */
//fun ApiEnrolmentRecordCreationPayload.fromApiToDomainOrNullIfNoBiometricReferences() =
//    biometricReferences?.let { biometricRefs ->
//        EnrolmentRecordCreationPayload(
//            subjectId,
//            projectId,
//            moduleId,
//            attendantId,
//            biometricRefs.map { it.fromApiToDomain() }
//        )
//    }
