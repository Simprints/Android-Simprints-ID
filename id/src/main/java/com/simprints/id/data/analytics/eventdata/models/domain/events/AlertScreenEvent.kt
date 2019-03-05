package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.ALERT_TYPE

class AlertScreenEvent(val relativeStartTime: Long,
                       val alertType: ALERT_TYPE) : Event(EventType.ALERT_SCREEN)
