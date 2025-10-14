package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fromApiToDomain
import com.simprints.infra.logging.Simber

@Keep
internal data class ApiEnrolmentRecordUpdatePayload(
    val subjectId: String,
    val biometricReferencesAdded: List<ApiBiometricReference>?,
    val biometricReferencesRemoved: List<String>?,
    val externalCredentialsAdded: List<ApiExternalCredential>?,
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordUpdate)

internal fun ApiEnrolmentRecordUpdatePayload.fromApiToDomain(): EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload {
    if (externalCredentialsAdded?.isNotEmpty() == true) {
        Simber.i(
            "ApiEnrolmentRecordUpdatePayload contains ${externalCredentialsAdded.size} credentials. Subject Id=[$subjectId], external credentials: [$externalCredentialsAdded]",
        )
    }
    return EnrolmentRecordUpdateEvent.EnrolmentRecordUpdatePayload(
        subjectId,
        biometricReferencesAdded?.map { it.fromApiToDomain() }.orEmpty(),
        biometricReferencesRemoved.orEmpty(),
        externalCredentialsAdded?.map { it.fromApiToDomain(subjectId) }.orEmpty(),
    )
}
