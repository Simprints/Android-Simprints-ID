package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fromApiToDomain

@Keep
internal data class ApiEnrolmentRecordUpdatePayload(
    val subjectId: String,
    val biometricReferencesAdded: List<ApiBiometricReference>?,
    val biometricReferencesRemoved: List<String>?,
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordUpdate)

internal fun ApiEnrolmentRecordUpdatePayload.fromApiToDomain() = EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload(
    subjectId,
    biometricReferencesAdded?.map { it.fromApiToDomain() }.orEmpty(),
    biometricReferencesRemoved.orEmpty(),
)
