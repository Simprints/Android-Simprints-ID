package com.simprints.id.data.db.session.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.Event
import com.simprints.id.data.db.session.domain.events.EventType

@Keep
class IdentificationCalloutEvent(starTime: Long,
                                 val projectId: String,
                                 val userId: String,
                                 val moduleId: String,
                                 val metadata: String?) : Event(EventType.CALLOUT_IDENTIFICATION, starTime)
