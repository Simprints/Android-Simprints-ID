package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.commontesttools.generators.FingerprintGenerator
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.ScannerCallback
import com.simprints.fingerprintscanner.v1.enums.UN20_STATE
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1

fun createMockedScannerV1(also: ScannerV1.() -> Unit = {}): ScannerV1 =
    mockk {
        makeCallbackSucceeding { connect(any()) }
        makeCallbackSucceeding { disconnect(any()) }
        makeCallbackSucceeding { switchToFactoryResetBank(any()) }
        makeCallbackSucceeding { switchToProductionBank(any()) }
        makeCallbackSucceeding { sendFirmwareMetadata(any(), any()) }
        makeCallbackSucceeding { sendFirmwareHex(any(), any()) }
        makeCallbackSucceeding { getFirmwareInfo(any()) }
        makeCallbackSucceeding { un20Wakeup(any()) }
        makeCallbackSucceeding { un20Shutdown(any()) }
        makeCallbackSucceeding { updateSensorInfo(any()) }
        makeCallbackSucceeding { startContinuousCapture(any(), any(), any()) }
        every { stopContinuousCapture() } returns true
        every { registerButtonListener(any()) } returns true
        every { unregisterButtonListener(any()) } returns true
        makeCallbackSucceeding { forceCapture(any(), any()) }
        makeCallbackSucceeding { resetUI(any()) }
        every { setHardwareConfig(any(), any()) }
        every { isConnected } returns true
        every { scannerId } returns DEFAULT_SCANNER_ID
        every { ucVersion } returns DEFAULT_UC_VERSION
        every { bankId } returns DEFAULT_BANK_ID
        every { unVersion } returns DEFAULT_UN_VERSION
        every { macAddress } returns DEFAULT_MAC_ADDRESS
        every { batteryLevel1 } returns DEFAULT_BATTERY_LEVEL_1
        every { batteryLevel2 } returns DEFAULT_BATTERY_LEVEL_2
        every { hardwareVersion } returns DEFAULT_HARDWARE_VERSION
        every { crashLogValid } returns true
        every { un20State } returns UN20_STATE.READY
        every { imageQuality } returns DEFAULT_GOOD_IMAGE_QUALITY
        every { template } returns FingerprintGenerator.generateRandomFingerprint().templateBytes
        every { connection_sendOtaPacket(any(), any(), any()) } returns true
        every { connection_sendOtaMeta(any(), any()) } returns true
        every { connection_setBank(any(), any(), any()) } returns true
        also(this)
    }

fun ScannerV1.makeCallbackSucceeding(method: MockKMatcherScope.(ScannerV1) -> Unit) {
    every { this.method(this@makeCallbackSucceeding) } answers { call ->
        val callback = call.invocation.args.find { it is ScannerCallback } as ScannerCallback?
        callback?.onSuccess()
    }
}

fun ScannerV1.makeCallbackFailing(error: SCANNER_ERROR, method: MockKMatcherScope.(ScannerV1) -> Unit) {
    every { this.method(this@makeCallbackFailing) } answers { call ->
        val callback = call.invocation.args.find { it is ScannerCallback } as ScannerCallback?
        callback?.onFailure(error)
    }
}

fun ScannerV1.queueFinger(fingerIdentifier: FingerIdentifier, qualityScore: Int) {
    makeScansSuccessful()
    every { imageQuality } returns qualityScore
    every { template } returns FingerprintGenerator.generateRandomFingerprint(fingerIdentifier, qualityScore.toByte()).templateBytes
}

fun ScannerV1.queueGoodFinger(fingerIdentifier: FingerIdentifier = FingerIdentifier.LEFT_THUMB) =
    queueFinger(fingerIdentifier, DEFAULT_GOOD_IMAGE_QUALITY)

fun ScannerV1.queueBadFinger(fingerIdentifier: FingerIdentifier = FingerIdentifier.LEFT_THUMB) =
    queueFinger(fingerIdentifier, DEFAULT_BAD_IMAGE_QUALITY)

fun ScannerV1.queueFingerNotDetected() {
    // SCANNER_ERROR.UN20_SDK_ERROR corresponds to no finger detected on sensor
    makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { startContinuousCapture(any(), any(), any()) }
    makeCallbackFailing(SCANNER_ERROR.UN20_SDK_ERROR) { forceCapture(any(), any()) }
}

fun ScannerV1.makeScansSuccessful() {
    makeCallbackSucceeding { startContinuousCapture(any(), any(), any()) }
    makeCallbackSucceeding { forceCapture(any(), any()) }
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
