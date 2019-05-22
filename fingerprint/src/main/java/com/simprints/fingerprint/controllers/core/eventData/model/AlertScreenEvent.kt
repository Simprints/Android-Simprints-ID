package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent as CoreAlertScreenEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent.AlertScreenEventType as CoreAlertScreenEventType

@Keep
class AlertScreenEvent(starTime: Long,
                       val alertType: FingerprintAlert) : Event(EventType.ALERT_SCREEN, starTime)

fun AlertScreenEvent.fromDomainToCore() =
    CoreAlertScreenEvent(starTime, alertType.fromFingerprintAlertToAlertTypeEvent())

fun FingerprintAlert.fromFingerprintAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        FingerprintAlert.GUID_NOT_FOUND_ONLINE -> CoreAlertScreenEventType.GUID_NOT_FOUND_OFFLINE
        FingerprintAlert.GUID_NOT_FOUND_OFFLINE -> CoreAlertScreenEventType.GUID_NOT_FOUND_OFFLINE
        FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> CoreAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
        FingerprintAlert.BLUETOOTH_NOT_ENABLED -> CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED
        FingerprintAlert.NOT_PAIRED -> CoreAlertScreenEventType.NOT_PAIRED
        FingerprintAlert.MULTIPLE_PAIRED_SCANNERS -> CoreAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
        FingerprintAlert.DISCONNECTED -> CoreAlertScreenEventType.DISCONNECTED
        FingerprintAlert.LOW_BATTERY -> CoreAlertScreenEventType.LOW_BATTERY
        FingerprintAlert.UNEXPECTED_ERROR -> CoreAlertScreenEventType.UNEXPECTED_ERROR
    }
