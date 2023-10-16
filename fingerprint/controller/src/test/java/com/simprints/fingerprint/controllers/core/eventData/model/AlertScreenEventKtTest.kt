package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue.*
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import org.junit.Test
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

class AlertScreenEventKtTest {

    @Test
    fun fromFingerprintAlertToAlertTypeEvent() = mapOf(
        AlertError.BLUETOOTH_NOT_SUPPORTED to CoreAlertScreenEventType.BLUETOOTH_NOT_SUPPORTED,
        AlertError.BLUETOOTH_NOT_ENABLED to CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED,
        AlertError.NOT_PAIRED to CoreAlertScreenEventType.NOT_PAIRED,
        AlertError.MULTIPLE_PAIRED_SCANNERS to CoreAlertScreenEventType.MULTIPLE_PAIRED_SCANNERS,
        AlertError.DISCONNECTED to CoreAlertScreenEventType.DISCONNECTED,
        AlertError.LOW_BATTERY to CoreAlertScreenEventType.LOW_BATTERY,
        AlertError.UNEXPECTED_ERROR to CoreAlertScreenEventType.UNEXPECTED_ERROR,
        AlertError.BLUETOOTH_NO_PERMISSION to CoreAlertScreenEventType.BLUETOOTH_NO_PERMISSION,
    ).forEach { (alert, event) ->
        assertThat(alert.fromFingerprintAlertToAlertTypeEvent()).isEqualTo(event)
    }

    @Test
    fun fromDomainToCore() = mapOf(
        BluetoothOff to CoreAlertScreenEventType.BLUETOOTH_NOT_ENABLED,
        NfcOff to CoreAlertScreenEventType.NFC_NOT_ENABLED,
        NfcPair to CoreAlertScreenEventType.NFC_PAIR,
        SerialEntryPair to CoreAlertScreenEventType.SERIAL_ENTRY_PAIR,
        ScannerOff to CoreAlertScreenEventType.DISCONNECTED,
        Ota(OtaFragmentRequest(emptyList())) to CoreAlertScreenEventType.OTA,
        OtaRecovery(
            OtaRecoveryFragmentRequest(OtaRecoveryStrategy.HARD_RESET, emptyList(), 0)
        ) to CoreAlertScreenEventType.OTA_RECOVERY,
        OtaFailed to CoreAlertScreenEventType.OTA_FAILED,
    ).forEach { (scannerIssue, event) ->
        val alertEvent = AlertScreenEventWithScannerIssue(0, scannerIssue).fromDomainToCore()
        assertThat(alertEvent.payload.alertType).isEqualTo(event)
    }
}
