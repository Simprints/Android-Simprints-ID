package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class EventScopeType {
    SESSION,
    UP_SYNC,
    DOWN_SYNC,
    SAMPLE_UP_SYNC,
}
