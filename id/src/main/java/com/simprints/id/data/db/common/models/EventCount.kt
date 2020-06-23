package com.simprints.id.data.db.common.models

import com.simprints.id.data.db.session.domain.models.events.EventPayloadType


class EventCount(val type: EventPayloadType, val count: Int)
