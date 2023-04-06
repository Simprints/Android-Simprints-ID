package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.Event

@Keep
internal data class ApiEvent(
    val id: String,
    val labels: ApiEventLabels,
    val payload: ApiEventPayload
)

internal fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())
