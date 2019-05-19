package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class IdentificationCalloutEvent(override val starTime: Long,
                                 val integration: CalloutIntegrationInfo,
                                 val projectId: String,
                                 val userId: String,
                                 val moduleId: String,
                                 val metadata: String?) : Event(EventType.CALLOUT_IDENTIFICATION)
