package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.infra.events.event.domain.models.AlertScreenEvent as CoreAlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

fun AlertError.fromFingerprintAlertToAlertTypeEvent(): CoreAlertScreenEventType = when (this) {
    AlertError.BLUETOOTH_NOT_SUPPORTED -> CoreAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
    AlertError.BLUETOOTH_NOT_ENABLED -> CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED
    AlertError.NOT_PAIRED -> CoreAlertScreenEventType.NOT_PAIRED
    AlertError.MULTIPLE_PAIRED_SCANNERS -> CoreAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
    AlertError.DISCONNECTED -> CoreAlertScreenEventType.DISCONNECTED
    AlertError.LOW_BATTERY -> CoreAlertScreenEventType.LOW_BATTERY
    AlertError.UNEXPECTED_ERROR -> CoreAlertScreenEventType.UNEXPECTED_ERROR
}

/**
 * This class represents an event that occurs as a result of an alert shown for an issue connecting
 * to the vero scanner.
 *
 * @property scannerIssue  the connection issue that occurred
 */
@Keep
class AlertScreenEventWithScannerIssue(
    startTime: Long,
    val scannerIssue: ConnectScannerIssue,
) : Event(EventType.ALERT_SCREEN_WITH_SCANNER_ISSUE, startTime)

fun AlertScreenEventWithScannerIssue.fromDomainToCore() =
    CoreAlertScreenEvent(startTime, scannerIssue.fromConnectScannerIssueToAlertTypeEvent())

fun ConnectScannerIssue.fromConnectScannerIssueToAlertTypeEvent() =
    when (this) {
        ConnectScannerIssue.BluetoothOff -> CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED
        ConnectScannerIssue.NfcOff -> CoreAlertScreenEventType.NFC_NOT_ENABLED
        ConnectScannerIssue.NfcPair -> CoreAlertScreenEventType.NFC_PAIR
        ConnectScannerIssue.SerialEntryPair -> CoreAlertScreenEventType.SERIAL_ENTRY_PAIR
        ConnectScannerIssue.ScannerOff -> CoreAlertScreenEventType.DISCONNECTED
        is ConnectScannerIssue.Ota -> CoreAlertScreenEventType.OTA
        is ConnectScannerIssue.OtaRecovery -> CoreAlertScreenEventType.OTA_RECOVERY
        ConnectScannerIssue.OtaFailed -> CoreAlertScreenEventType.OTA_FAILED
    }
