package com.simprints.id.data.db.common.models


class EventCount(val type: EventType, val count: Int)

enum class EventType {
    ENROLMENT_RECORD_CREATION,
    ENROLMENT_RECORD_DELETION,
    ENROLMENT_RECORD_MOVE
}
