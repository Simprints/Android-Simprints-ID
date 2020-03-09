package com.simprints.id.data.db.session.domain.models

class SessionEventValidatorsBuilderImpl : SessionEventValidatorsBuilder {
    override fun build(): Array<SessionEventValidator> =
        arrayOf(GuidSelectionEventValidator())
}
