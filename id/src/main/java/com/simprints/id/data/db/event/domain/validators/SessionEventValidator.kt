package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent

interface SessionEventValidator {

    fun validate(session: SessionCaptureEvent)

}
