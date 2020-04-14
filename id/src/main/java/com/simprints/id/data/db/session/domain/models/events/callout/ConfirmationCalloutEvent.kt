package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class ConfirmationCalloutEvent(starTime: Long,
                               val projectId: String,
                               val selectedGuid: String,
                               val sessionId: String): Event(EventType.CALLOUT_CONFIRMATION, starTime)
