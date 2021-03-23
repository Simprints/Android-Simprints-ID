package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent

interface SessionDataCache {

    var currentSession: SessionCaptureEvent?

}
