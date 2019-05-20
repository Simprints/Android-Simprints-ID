package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.alert.Alert

@Keep
class AlertScreenEvent(starTime: Long,
                       val alertType: String) : Event(EventType.ALERT_SCREEN, starTime) {

    constructor(starTime: Long, alert: Alert):
        this(starTime, alert.name)
}
