package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType
import com.simprints.eventsystem.event.remote.models.subject.biometricref.ApiBiometricReference

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiEnrolmentRecordCreationPayload(
    override val startTime: Long = 0, //Not added on down-sync API yet
    override val version: Int,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?
) : ApiEventPayload(ApiEventPayloadType.EnrolmentRecordCreation, version, startTime) {

    constructor(payload: EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload) :
        this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId,
            payload.attendantId, payload.biometricReferences.map { it.fromDomainToApi() })
}


fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() =
    EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
        startTime,
        version,
        subjectId,
        projectId,
        moduleId,
        attendantId,
        biometricReferences?.map { it.fromApiToDomain() } ?: emptyList()
    )
