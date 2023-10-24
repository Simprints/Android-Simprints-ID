package com.simprints.fingerprint.controllers.core.eventData.model

import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

fun AlertError.fromFingerprintAlertToAlertTypeEvent(): CoreAlertScreenEventType = when (this) {
    AlertError.UNEXPECTED_ERROR -> CoreAlertScreenEventType.UNEXPECTED_ERROR
}
