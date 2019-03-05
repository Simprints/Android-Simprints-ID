package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.domain.ALERT_TYPE

class ApiAlertScreenEvent(val relativeStartTime: Long,
                          val alertType: ALERT_TYPE) : ApiEvent(ApiEventType.ALERT_SCREEN) {

    constructor(alertScreenEventDomain: AlertScreenEvent) :
        this(alertScreenEventDomain.relativeStartTime, alertScreenEventDomain.alertType)
}
