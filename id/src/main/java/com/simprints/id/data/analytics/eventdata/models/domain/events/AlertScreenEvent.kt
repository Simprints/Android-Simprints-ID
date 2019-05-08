package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.alert.Alert

@Keep
class AlertScreenEvent(val relativeStartTime: Long,
                       val alertType: String) : Event(EventType.ALERT_SCREEN) {

    constructor(relativeStartTime: Long, alert: Alert):
        this(relativeStartTime, alert.name)
}
