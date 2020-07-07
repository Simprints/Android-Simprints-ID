package com.simprints.id.data.db.common.models

import com.simprints.id.data.db.event.domain.events.EventPayloadType


class EventCount(val type: EventPayloadType, val count: Int)
