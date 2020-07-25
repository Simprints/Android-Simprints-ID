package com.simprints.id.data.db.event.domain.validators

class SessionEventValidatorsBuilderImpl : SessionEventValidatorsBuilder {
    override fun build(): Array<EventValidator> =
        arrayOf(GuidSelectionEventValidator())
}
