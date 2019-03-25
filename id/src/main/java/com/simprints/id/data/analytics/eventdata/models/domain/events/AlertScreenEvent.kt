package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.alert.Alert

class AlertScreenEvent(val relativeStartTime: Long,
                       val alert: String) : Event(EventType.ALERT_SCREEN) {

    constructor(relativeStartTime: Long, alert: Alert):
        this(relativeStartTime, alert.name)
}
