package com.simprints.feature.consent.screens.consent

import com.simprints.infra.events.event.domain.models.ConsentEvent

internal enum class ConsentTab {
    INDIVIDUAL,
    PARENTAL,
    ;

    fun asEventPayload() = when (this) {
        INDIVIDUAL -> ConsentEvent.ConsentPayload.Type.INDIVIDUAL
        PARENTAL -> ConsentEvent.ConsentPayload.Type.PARENTAL
    }
}
