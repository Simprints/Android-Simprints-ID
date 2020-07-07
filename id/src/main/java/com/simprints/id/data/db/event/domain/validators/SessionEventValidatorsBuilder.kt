package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.validators.SessionEventValidator

interface SessionEventValidatorsBuilder {
    fun build(): Array<SessionEventValidator>
}
