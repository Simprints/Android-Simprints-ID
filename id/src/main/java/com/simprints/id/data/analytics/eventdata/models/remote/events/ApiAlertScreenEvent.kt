package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent

class ApiAlertScreenEvent(val relativeStartTime: Long,
                          val alert: String) : ApiEvent(ApiEventType.ALERT_SCREEN) {

    constructor(alertScreenEventDomain: AlertScreenEvent) :
        this(alertScreenEventDomain.relativeStartTime, alertScreenEventDomain.alert)
}
