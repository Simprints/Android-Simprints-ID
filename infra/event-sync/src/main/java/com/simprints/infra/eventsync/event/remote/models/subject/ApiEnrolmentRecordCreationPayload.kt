package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fromApiToDomain

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?,
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation)

internal fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
    subjectId,
    projectId,
    moduleId.asTokenizableEncrypted(),
    attendantId.asTokenizableEncrypted(),
    biometricReferences?.map { it.fromApiToDomain() } ?: emptyList(),
)
