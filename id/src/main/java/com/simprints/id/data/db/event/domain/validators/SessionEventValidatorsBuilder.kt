package com.simprints.id.data.db.event.domain.validators

interface SessionEventValidatorsBuilder {
    fun build(): Array<EventValidator>
}
