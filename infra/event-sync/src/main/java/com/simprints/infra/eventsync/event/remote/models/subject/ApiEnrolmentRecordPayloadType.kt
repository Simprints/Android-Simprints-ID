package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType

@Keep
internal enum class ApiEnrolmentRecordPayloadType {
    // key added: ENROLMENT_RECORD_CREATION_KEY
    EnrolmentRecordCreation,

    // key added: ENROLMENT_RECORD_DELETION_KEY
    EnrolmentRecordDeletion,

    // key added: ENROLMENT_RECORD_UPDATE_KEY
    EnrolmentRecordUpdate,

    // key added: ENROLMENT_RECORD_MOVE_KEY
    EnrolmentRecordMove,

    ;

    companion object {
        const val ENROLMENT_RECORD_CREATION_KEY = "EnrolmentRecordCreation"
        const val ENROLMENT_RECORD_DELETION_KEY = "EnrolmentRecordDeletion"
        const val ENROLMENT_RECORD_MOVE_KEY = "EnrolmentRecordMove"
        const val ENROLMENT_RECORD_UPDATE_KEY = "EnrolmentRecordUpdate"
    }
}

internal fun ApiEnrolmentRecordPayloadType.fromApiToDomain(): EnrolmentRecordEventType = when (this) {
    ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation -> EnrolmentRecordEventType.EnrolmentRecordCreation
    ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion -> EnrolmentRecordEventType.EnrolmentRecordDeletion
    ApiEnrolmentRecordPayloadType.EnrolmentRecordMove -> EnrolmentRecordEventType.EnrolmentRecordMove
    ApiEnrolmentRecordPayloadType.EnrolmentRecordUpdate -> EnrolmentRecordEventType.EnrolmentRecordUpdate
}
