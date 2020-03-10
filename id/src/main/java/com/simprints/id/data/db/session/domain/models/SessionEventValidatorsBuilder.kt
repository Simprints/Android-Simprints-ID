package com.simprints.id.data.db.session.domain.models

interface SessionEventValidatorsBuilder {
    fun build(): Array<SessionEventValidator>
}
