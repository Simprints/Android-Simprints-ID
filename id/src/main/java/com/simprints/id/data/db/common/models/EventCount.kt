package com.simprints.id.data.db.common.models

import com.simprints.id.data.db.person.domain.personevents.EventPayloadType


class EventCount(val type: EventPayloadType, val count: Int)
