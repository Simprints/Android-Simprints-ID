package com.simprints.feature.clientapi.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.Event

@Keep
internal data class CoSyncEvents(
    val events: List<Event>,
)
