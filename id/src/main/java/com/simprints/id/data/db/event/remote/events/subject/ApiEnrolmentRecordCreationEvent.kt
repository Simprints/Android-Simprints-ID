package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.fromApiToDomain
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent.ApiEnrolmentRecordCreationPayload

@Keep
class ApiEnrolmentRecordCreationEvent(domainEvent: EnrolmentRecordCreationEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiEnrolmentRecordCreationPayload(
        createdAt: Long,
        version: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<ApiBiometricReference>?
    ) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_CREATION, version, createdAt) {

        constructor(payload: EnrolmentRecordCreationPayload) :
            this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId,
                payload.attendantId, payload.biometricReferences.map { it.fromDomainToApi() })
    }
}

fun ApiEnrolmentRecordCreationPayload.fromApiToDomain() =
    EnrolmentRecordCreationPayload(
        createdAt ?: 0,
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
