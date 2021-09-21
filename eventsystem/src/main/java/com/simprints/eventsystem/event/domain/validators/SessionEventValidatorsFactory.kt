package com.simprints.eventsystem.event.domain.validators

interface SessionEventValidatorsFactory {
    fun build(): Array<EventValidator>
}
