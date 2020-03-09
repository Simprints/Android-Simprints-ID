package com.simprints.id.data.db.session.domain.models

import com.simprints.id.data.db.session.domain.models.session.SessionEvents

interface SessionEventValidator {

    fun validate(session: SessionEvents)

}
