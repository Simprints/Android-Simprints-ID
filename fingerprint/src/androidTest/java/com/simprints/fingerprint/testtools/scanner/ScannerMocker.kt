package com.simprints.fingerprint.testtools.scanner

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.commontesttools.FingerprintGeneratorUtils
import com.simprints.fingerprint.commontesttools.PeopleGeneratorUtils
import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscanner.ScannerCallback
import com.simprints.fingerprintscanner.enums.UN20_STATE
import com.simprints.fingerprintscannermock.MockScannerManager
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.wheneverThis
import org.mockito.ArgumentMatchers.*

fun createMockedScanner(also: Scanner.() -> Unit = {}): Scanner =
    setupMock {
        makeCallbackSucceeding { connect(anyOrNull()) }
        makeCallbackSucceeding { disconnect(anyOrNull()) }
        makeCallbackSucceeding { switchToFactoryResetBank(anyOrNull()) }
        makeCallbackSucceeding { switchToProductionBank(anyOrNull()) }
        makeCallbackSucceeding { sendFirmwareMetadata(anyString(), anyOrNull()) }
        makeCallbackSucceeding { sendFirmwareHex(anyString(), anyOrNull()) }
        makeCallbackSucceeding { getFirmwareInfo(anyOrNull()) }
        makeCallbackSucceeding { un20Wakeup(anyOrNull()) }
        makeCallbackSucceeding { un20Shutdown(anyOrNull()) }
        makeCallbackSucceeding { updateSensorInfo(anyOrNull()) }
        makeCallbackSucceeding { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
        wheneverThis { stopContinuousCapture() } thenReturn true
        wheneverThis { registerButtonListener(anyNotNull()) } thenReturn true
        wheneverThis { unregisterButtonListener(anyNotNull()) } thenReturn true
        makeCallbackSucceeding { forceCapture(anyInt(), anyOrNull()) }
        makeCallbackSucceeding { resetUI(anyNotNull()) }
        wheneverThis { setHardwareConfig(anyNotNull(), anyOrNull()) }
        wheneverThis { isConnected } thenReturn true
        wheneverThis { scannerId } thenReturn DEFAULT_SCANNER_ID
        wheneverThis { ucVersion } thenReturn DEFAULT_UC_VERSION
        wheneverThis { bankId } thenReturn DEFAULT_BANK_ID
        wheneverThis { unVersion } thenReturn DEFAULT_UN_VERSION
        wheneverThis { macAddress } thenReturn DEFAULT_MAC_ADDRESS
        wheneverThis { batteryLevel1 } thenReturn DEFAULT_BATTERY_LEVEL_1
        wheneverThis { batteryLevel2 } thenReturn DEFAULT_BATTERY_LEVEL_2
        wheneverThis { hardwareVersion } thenReturn DEFAULT_HARDWARE_VERSION
        wheneverThis { crashLogValid } thenReturn true
        wheneverThis { un20State } thenReturn UN20_STATE.READY
        wheneverThis { imageQuality } thenReturn DEFAULT_GOOD_IMAGE_QUALITY
        wheneverThis { template } thenReturn PeopleGeneratorUtils.getRandomFingerprint().templateBytes
        wheneverThis { connection_sendOtaPacket(anyInt(), anyInt(), anyString()) } thenReturn true
        wheneverThis { connection_sendOtaMeta(anyInt(), anyShort()) } thenReturn true
        wheneverThis { connection_setBank(anyChar(), anyChar(), anyChar()) } thenReturn true
        also(this)
    }

fun Scanner.makeCallbackSucceeding(method: (Scanner) -> Unit) {
    wheneverThis(method) then { onMock ->
        val callback = onMock.arguments.find { it is ScannerCallback } as ScannerCallback?
        callback?.onSuccess()
    }
}

fun Scanner.makeCallbackFailing(error: SCANNER_ERROR, method: (Scanner) -> Unit) {
    wheneverThis(method) then { onMock ->
        val callback = onMock.arguments.find { it is ScannerCallback } as ScannerCallback?
        callback?.onFailure(error)
    }
}

fun Scanner.queueFinger(fingerIdentifier: FingerIdentifier, qualityScore: Int) {
    makeScansSuccessful()
    wheneverThis { imageQuality } thenReturn qualityScore
    wheneverThis { template } thenReturn FingerprintGeneratorUtils.generateRandomFingerprint(fingerIdentifier, qualityScore.toByte()).templateBytes
}

fun Scanner.queueGoodFinger(fingerIdentifier: FingerIdentifier = FingerIdentifier.LEFT_THUMB) =
    queueFinger(fingerIdentifier, DEFAULT_GOOD_IMAGE_QUALITY)

fun Scanner.queueBadFinger(fingerIdentifier: FingerIdentifier = FingerIdentifier.LEFT_THUMB) =
    queueFinger(fingerIdentifier, DEFAULT_BAD_IMAGE_QUALITY)

fun Scanner.queueFingerNotDetected() {
    // SCANNER_ERROR.UN20_SDK_ERROR corresponds to no finger detected on sensor
    makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
    makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { forceCapture(anyInt(), anyOrNull()) }
}

fun Scanner.makeScansSuccessful() {
    makeCallbackSucceeding { startContinuousCapture(anyInt(), anyLong(), anyOrNull()) }
    makeCallbackSucceeding { forceCapture(anyInt(), anyOrNull()) }
}

const val DEFAULT_SCANNER_ID = "scannerId"
const val DEFAULT_UC_VERSION = 20.toShort()
const val DEFAULT_BANK_ID = 0.toByte()
const val DEFAULT_UN_VERSION = 0.toShort()
const val DEFAULT_MAC_ADDRESS = MockScannerManager.DEFAULT_MAC_ADDRESS
const val DEFAULT_BATTERY_LEVEL_1 = 80.toShort()
const val DEFAULT_BATTERY_LEVEL_2 = 80.toShort()
const val DEFAULT_HARDWARE_VERSION = 6.toByte()
const val DEFAULT_GOOD_IMAGE_QUALITY = 87
const val DEFAULT_BAD_IMAGE_QUALITY = 23
