package com.simprints.infra.events.domain.validators

internal interface SessionEventValidatorsFactory {
    fun build(): Array<EventValidator>
}
