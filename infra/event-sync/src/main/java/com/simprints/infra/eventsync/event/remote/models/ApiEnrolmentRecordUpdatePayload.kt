package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import com.simprints.infra.eventsync.event.remote.ApiExternalCredential
import com.simprints.infra.eventsync.event.remote.fromApiToDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_UPDATE_KEY)
internal data class ApiEnrolmentRecordUpdatePayload(
    val subjectId: String,
    val biometricReferencesAdded: List<ApiBiometricReference>?,
    val biometricReferencesRemoved: List<String>?,
    val externalCredentialsAdded: List<ApiExternalCredential>?,
    override val type: ApiEnrolmentRecordPayloadType = ApiEnrolmentRecordPayloadType.EnrolmentRecordUpdate,
) : ApiEnrolmentRecordEventPayload()

internal fun ApiEnrolmentRecordUpdatePayload.fromApiToDomain() = EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload(
    subjectId,
    biometricReferencesAdded?.map { it.fromApiToDomain() }.orEmpty(),
    biometricReferencesRemoved.orEmpty(),
    externalCredentialsAdded?.map { it.fromApiToDomain(subjectId) }.orEmpty(),
)
