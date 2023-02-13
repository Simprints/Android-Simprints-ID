package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event

@Keep
data class ApiEvent(
    val id: String,
    val labels: ApiEventLabels,
    val payload: ApiEventPayload
)

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())
