package com.simprints.id.data.db.session.domain.models.events

import com.simprints.id.data.db.subject.domain.subjectevents.fromApiToDomain
import com.simprints.id.data.db.subject.domain.subjectevents.fromApiToDomainAndNullForCreationIfBiometricRefsAreNull
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEventPayload

abstract class EventPayload(val type: EventPayloadType)

fun ApiEventPayload.fromApiToDomainOrNullIfNoBiometricReferences() = when(this) {
    is ApiEnrolmentRecordCreationPayload -> this.fromApiToDomainOrNullIfNoBiometricReferences()
    is ApiEnrolmentRecordDeletionPayload -> this.fromApiToDomain()
    is ApiEnrolmentRecordMovePayload -> this.fromApiToDomainAndNullForCreationIfBiometricRefsAreNull()
    else -> throw IllegalStateException("Invalid payload type for events")
}
