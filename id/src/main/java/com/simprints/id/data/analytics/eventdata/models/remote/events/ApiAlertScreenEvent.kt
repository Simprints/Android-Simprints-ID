package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent

@Keep
class ApiAlertScreenEvent(val relativeStartTime: Long,
                          val alert: String) : ApiEvent(ApiEventType.ALERT_SCREEN) {

    constructor(alertScreenEventDomain: AlertScreenEvent) :
        this(alertScreenEventDomain.relativeStartTime, alertScreenEventDomain.alert)
}
