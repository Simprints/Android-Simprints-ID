package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class ConfirmationCalloutEvent(override val starTime: Long,
                               val integration: CalloutIntegrationInfo,
                               val selectedGuid: String,
                               val sessionId: String): Event(EventType.CALLOUT_CONFIRMATION)
