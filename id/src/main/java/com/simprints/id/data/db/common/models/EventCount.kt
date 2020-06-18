package com.simprints.id.data.db.common.models

import com.simprints.id.data.db.subject.domain.subjectevents.EventPayloadType


class EventCount(val type: EventPayloadType, val count: Int)
