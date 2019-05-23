package com.simprints.clientapi.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent as CoreAlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent.AlertScreenEventType as CoreAlertScreenEventType

@Keep
class AlertScreenEvent(starTime: Long,
                       val clientApiAlertType: ClientApiAlert) : Event(EventType.ALERT_SCREEN, starTime)

fun AlertScreenEvent.fromDomainToCore() =
    CoreAlertScreenEvent(starTime, clientApiAlertType.fromAlertToAlertTypeEvent())

fun ClientApiAlert.fromAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        ClientApiAlert.INVALID_CLIENT_REQUEST -> CoreAlertScreenEventType.INVALID_INTENT_ACTION
        ClientApiAlert.INVALID_METADATA -> CoreAlertScreenEventType.INVALID_METADATA
        ClientApiAlert.INVALID_MODULE_ID -> CoreAlertScreenEventType.INVALID_MODULE_ID
        ClientApiAlert.INVALID_PROJECT_ID -> CoreAlertScreenEventType.INVALID_PROJECT_ID
        ClientApiAlert.INVALID_SELECTED_ID -> CoreAlertScreenEventType.INVALID_SELECTED_ID
        ClientApiAlert.INVALID_SESSION_ID -> CoreAlertScreenEventType.INVALID_SESSION_ID
        ClientApiAlert.INVALID_USER_ID -> CoreAlertScreenEventType.INVALID_USER_ID
        ClientApiAlert.INVALID_VERIFY_ID -> CoreAlertScreenEventType.INVALID_VERIFY_ID
    }
