package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.domain.models.subject.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.subject.biometricref.ApiBiometricReference

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation) {

}


fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() =
    EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
        subjectId,
        projectId,
        moduleId,
        attendantId,
        biometricReferences?.map { it.fromApiToDomain() } ?: emptyList()
    )
