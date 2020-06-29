package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.events.session.SessionEvent

interface SessionEventValidator {

    fun validate(session: SessionEvent)

}
