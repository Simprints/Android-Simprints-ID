package com.simprints.id.data.analytics.events.models

import com.simprints.id.domain.ALERT_TYPE

class AlertScreenEvent(val relativeStartTime: Long,
                       val alertType: ALERT_TYPE) : Event(EventType.ALERT_SCREEN)
