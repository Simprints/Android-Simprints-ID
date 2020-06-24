package com.simprints.id.data.db.event.domain.validators

import com.simprints.id.data.db.event.domain.session.SessionEvents

interface SessionEventValidator {

    fun validate(session: SessionEvents)

}
