package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.validators.GuidSelectionEventValidator
import com.simprints.id.data.db.event.domain.validators.SessionEventValidator
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsBuilder

class SessionEventValidatorsBuilderImpl : SessionEventValidatorsBuilder {
    override fun build(): Array<SessionEventValidator> =
        arrayOf(GuidSelectionEventValidator())
}
