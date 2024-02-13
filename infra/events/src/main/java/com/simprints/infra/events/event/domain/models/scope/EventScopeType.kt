package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep

@Keep
enum class EventScopeType {

    SESSION,
    UPSYNC,
    DOWNSYNC,
    ;
}
