package com.simprints.id.data.db.common.models


class EventCount(val type: EventType, val count: Int)

enum class EventType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}
