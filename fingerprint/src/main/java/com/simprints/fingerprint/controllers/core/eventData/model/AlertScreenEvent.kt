package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent as CoreAlertScreenEvent
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

@Keep
class AlertScreenEvent(startTime: Long,
                       val alertType: FingerprintAlert) : Event(EventType.ALERT_SCREEN, startTime)

fun AlertScreenEvent.fromDomainToCore() =
    CoreAlertScreenEvent(startTime, alertType.fromFingerprintAlertToAlertTypeEvent())

fun FingerprintAlert.fromFingerprintAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> CoreAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED
        FingerprintAlert.BLUETOOTH_NOT_ENABLED -> CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED
        FingerprintAlert.NOT_PAIRED -> CoreAlertScreenEventType.NOT_PAIRED
        FingerprintAlert.MULTIPLE_PAIRED_SCANNERS -> CoreAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS
        FingerprintAlert.DISCONNECTED -> CoreAlertScreenEventType.DISCONNECTED
        FingerprintAlert.LOW_BATTERY -> CoreAlertScreenEventType.LOW_BATTERY
        FingerprintAlert.UNEXPECTED_ERROR -> CoreAlertScreenEventType.UNEXPECTED_ERROR
    }

@Keep
class AlertScreenEventWithScannerIssue(startTime: Long,
                                       val scannerIssue: ConnectScannerIssue) : Event(EventType.ALERT_SCREEN_WITH_SCANNER_ISSUE, startTime)

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
