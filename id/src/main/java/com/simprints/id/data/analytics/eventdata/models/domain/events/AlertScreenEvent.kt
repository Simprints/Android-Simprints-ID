package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.alert.Alert

class AlertScreenEvent(val relativeStartTime: Long,
                       val alert: Alert) : Event(EventType.ALERT_SCREEN)
