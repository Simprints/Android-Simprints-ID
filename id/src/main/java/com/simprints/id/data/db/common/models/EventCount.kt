package com.simprints.id.data.db.common.models

import com.simprints.id.data.db.event.domain.models.EventType


class EventCount(val type: EventType, val count: Int)
