package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.commontesttools.generators.FingerprintGeneratorUtils
import com.simprints.fingerprint.commontesttools.generators.PeopleGeneratorUtils
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.Scanner
import com.simprints.fingerprintscanner.v1.ScannerCallback
import com.simprints.fingerprintscanner.v1.enums.UN20_STATE
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.whenThis
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
        whenThis { stopContinuousCapture() } thenReturn true
        whenThis { registerButtonListener(anyNotNull()) } thenReturn true
        whenThis { unregisterButtonListener(anyNotNull()) } thenReturn true
        makeCallbackSucceeding { forceCapture(anyInt(), anyOrNull()) }
        makeCallbackSucceeding { resetUI(anyNotNull()) }
        whenThis { setHardwareConfig(anyNotNull(), anyOrNull()) }
        whenThis { isConnected } thenReturn true
        whenThis { scannerId } thenReturn DEFAULT_SCANNER_ID
        whenThis { ucVersion } thenReturn DEFAULT_UC_VERSION
        whenThis { bankId } thenReturn DEFAULT_BANK_ID
        whenThis { unVersion } thenReturn DEFAULT_UN_VERSION
        whenThis { macAddress } thenReturn DEFAULT_MAC_ADDRESS
        whenThis { batteryLevel1 } thenReturn DEFAULT_BATTERY_LEVEL_1
        whenThis { batteryLevel2 } thenReturn DEFAULT_BATTERY_LEVEL_2
        whenThis { hardwareVersion } thenReturn DEFAULT_HARDWARE_VERSION
        whenThis { crashLogValid } thenReturn true
        whenThis { un20State } thenReturn UN20_STATE.READY
        whenThis { imageQuality } thenReturn DEFAULT_GOOD_IMAGE_QUALITY
        whenThis { template } thenReturn PeopleGeneratorUtils.getRandomFingerprint().templateBytes
        whenThis { connection_sendOtaPacket(anyInt(), anyInt(), anyString()) } thenReturn true
        whenThis { connection_sendOtaMeta(anyInt(), anyShort()) } thenReturn true
        whenThis { connection_setBank(anyChar(), anyChar(), anyChar()) } thenReturn true
        also(this)
    }

fun Scanner.makeCallbackSucceeding(method: (Scanner) -> Unit) {
    whenThis(method) then { onMock ->
        val callback = onMock.arguments.find { it is ScannerCallback } as ScannerCallback?
        callback?.onSuccess()
    }
}

fun Scanner.makeCallbackFailing(error: SCANNER_ERROR, method: (Scanner) -> Unit) {
    whenThis(method) then { onMock ->
        val callback = onMock.arguments.find { it is ScannerCallback } as ScannerCallback?
        callback?.onFailure(error)
    }
}

fun Scanner.queueFinger(fingerIdentifier: FingerIdentifier, qualityScore: Int) {
    makeScansSuccessful()
    whenThis { imageQuality } thenReturn qualityScore
    whenThis { template } thenReturn FingerprintGeneratorUtils.generateRandomFingerprint(fingerIdentifier, qualityScore.toByte()).templateBytes
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
const val DEFAULT_MAC_ADDRESS = "F0:AC:D7:CE:E3:B5"
const val DEFAULT_BATTERY_LEVEL_1 = 80.toShort()
const val DEFAULT_BATTERY_LEVEL_2 = 80.toShort()
const val DEFAULT_HARDWARE_VERSION = 6.toByte()
const val DEFAULT_GOOD_IMAGE_QUALITY = 87
const val DEFAULT_BAD_IMAGE_QUALITY = 23
