package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep

@Keep
enum class EnrolmentRecordEventType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove,
    EnrolmentRecordUpdate,
}
