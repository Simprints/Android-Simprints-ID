package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent as CoreAlertScreenEvent

@Keep
class AlertScreenEvent(override val starTime: Long,
                       val alertType: String) : Event(EventType.ALERT_SCREEN)

fun AlertScreenEvent.fromDomainToCore() =
    CoreAlertScreenEvent(starTime, alertType)
