package com.simprints.eventsystem.event.domain.validators

internal interface SessionEventValidatorsFactory {
    fun build(): Array<EventValidator>
}
