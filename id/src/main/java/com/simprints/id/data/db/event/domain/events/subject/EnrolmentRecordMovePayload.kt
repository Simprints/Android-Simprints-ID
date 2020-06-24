package com.simprints.id.data.db.event.domain.events.subject

import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMovePayload

data class EnrolmentRecordMovePayload(
    val enrolmentRecordCreation: EnrolmentRecordCreationPayload?,
    val enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_MOVE, 0, 0)
// startTime and relativeStartTime are not used for Pokodex events

fun ApiEnrolmentRecordMovePayload.fromApiToDomainAndNullForCreationIfBiometricRefsAreNull() =
    EnrolmentRecordMovePayload(enrolmentRecordCreation.fromApiToDomainOrNullIfNoBiometricReferences(),
        enrolmentRecordDeletion.fromApiToDomain())

