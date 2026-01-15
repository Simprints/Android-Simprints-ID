package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import com.simprints.infra.eventsync.event.remote.ApiExternalCredential
import com.simprints.infra.eventsync.event.remote.fromApiToDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_CREATION_KEY)
internal data class ApiEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    val biometricReferences: List<ApiBiometricReference>?,
    val externalCredentials: List<ApiExternalCredential>?,
    override val type: ApiEnrolmentRecordPayloadType = ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation,
) : ApiEnrolmentRecordEventPayload()

internal fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
    subjectId = subjectId,
    projectId = projectId,
    moduleId = moduleId.asTokenizableEncrypted(),
    attendantId = attendantId.asTokenizableEncrypted(),
    biometricReferences = biometricReferences?.map { it.fromApiToDomain() } ?: emptyList(),
    externalCredentials = externalCredentials?.map { it.fromApiToDomain(subjectId) } ?: emptyList(),
)
