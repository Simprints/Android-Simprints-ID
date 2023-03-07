package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.Event

@Keep
data class ApiEvent(
    val id: String,
    val labels: ApiEventLabels,
    val payload: ApiEventPayload
)

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())
