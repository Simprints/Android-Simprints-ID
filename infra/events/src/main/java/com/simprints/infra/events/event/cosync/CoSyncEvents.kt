package com.simprints.infra.events.event.cosync

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.Event

@Keep
data class CoSyncEvents(
    val events: List<Event>,
)
