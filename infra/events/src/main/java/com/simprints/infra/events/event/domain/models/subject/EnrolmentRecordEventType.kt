package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep

@Keep
enum class EnrolmentRecordEventType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove,
    EnrolmentRecordUpdate,
}
