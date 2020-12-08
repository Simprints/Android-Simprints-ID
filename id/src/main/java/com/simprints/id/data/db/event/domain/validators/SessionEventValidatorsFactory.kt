package com.simprints.id.data.db.event.domain.validators

interface SessionEventValidatorsFactory {
    fun build(): Array<EventValidator>
}
