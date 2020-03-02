package com.simprints.id.data.db.session.domain.models.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventType

@Keep
class VerificationCalloutEvent(starTime: Long,
                               val projectId: String,
                               val userId: String,
                               val moduleId: String,
                               val verifyGuid: String,
                               val metadata: String) : Event(EventType.CALLOUT_VERIFICATION, starTime)
