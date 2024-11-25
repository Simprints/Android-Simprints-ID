package com.simprints.fingerprint.infra.scanner.wrapper

import android.annotation.SuppressLint
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2Feature
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.helpers.ConnectionHelper
import com.simprints.fingerprint.infra.scanner.helpers.ScannerInitialSetupHelper
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerExtendedInfoReaderHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.executeSafely
import com.simprints.fingerprint.infra.scanner.v2.tools.mapPotentialErrorFromScanner
import com.simprints.fingerprint.infra.scanner.v2.tools.wrapErrorFromScanner
import com.simprints.infra.config.store.models.FingerprintConfiguration
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

internal class ScannerWrapperV2(
    private val scannerV2: ScannerV2,
    private val scannerUiHelper: ScannerUiHelper,
    private val macAddress: String,
    private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
    private val connectionHelper: ConnectionHelper,
    private val ioDispatcher: CoroutineDispatcher,
) : ScannerWrapper {

    private var scannerVersion: ScannerVersion? = null
    private var batteryInfo: BatteryInfo? = null

    /**
     * This function returns the already set scanner version info, or returns a default of UNKNOWN
     * values if the version info hasn't been set.
     *
     * @see setScannerInfoAndCheckAvailableOta
     */
    override fun versionInformation(): ScannerVersion = scannerVersion ?: ScannerVersion(
        hardwareVersion = ScannerExtendedInfoReaderHelper.UNKNOWN_HARDWARE_VERSION,
        generation = ScannerGeneration.VERO_2,
        firmware = ScannerFirmwareVersions.UNKNOWN,
    )

    override fun batteryInformation(): BatteryInfo = batteryInfo ?: BatteryInfo.UNKNOWN

    override fun isImageTransferSupported(): Boolean = true

    override suspend fun connect() =
        connectionHelper.connectScanner(scannerV2, macAddress).mapPotentialErrorFromScanner()
            .collect()


    /**
     * This function runs check of available firmware updates, and in turn reads and sets the
     * scanner's firmware versions and battery information.
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     * @throws OtaFailedException
     */
    override suspend fun setScannerInfoAndCheckAvailableOta(fingerprintSdk: FingerprintConfiguration.BioSdk) =
        executeSafely {
            scannerInitialSetupHelper.setupScannerWithOtaCheck(
                fingerprintSdk,
                scannerV2,
                macAddress,
                { scannerVersion = it },
                { batteryInfo = it }
            )

        }

    override suspend fun disconnect() = executeSafely {
        connectionHelper.disconnectScanner(scannerV2)
    }

    override fun isConnected() = scannerV2.isConnected()

    /**
     * This function turns on the Un20 sensor (fingerprint sensor), by specifying what state it
     * expects the sensor to be in, represented as a boolean value (true | false -> on | off)
     *
     * @see ensureUn20State
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     */
    override suspend fun sensorWakeUp() = executeSafely {
        scannerV2.ensureUn20State(true)
    }


    /**
     * This function turns off the Un20 sensor (fingerprint sensor), by specifying what state it
     * expects the sensor to be in, represented as a boolean value (true | false -> on | off)
     *
     * @see ensureUn20State
     *
     * @throws ScannerDisconnectedException
     * @throws UnexpectedScannerException
     * @throws NotConnectedException
     */
    override suspend fun sensorShutDown() = executeSafely {
        scannerV2.ensureUn20State(false)
    }

    override fun isLiveFeedbackAvailable(): Boolean = true

    override suspend fun startLiveFeedback(): Unit = executeSafely {
        if (isLiveFeedbackAvailable()) {
            scannerV2.setScannerLedStateOn()
            // ignore exceptions from getImageQualityWhileSettingLEDState
            runCatching { withContext(ioDispatcher) { getImageQualityWhileSettingLEDState(this) } }
        } else {
            throw UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK)
        }
    }

    private suspend fun getImageQualityWhileSettingLEDState(scope: CoroutineScope) {
        while (scope.isActive) {
            scannerV2.getImageQualityPreview()?.let { quality ->
                scannerV2.setSmileLedState(
                    scannerUiHelper.deduceLedStateFromQualityForLiveFeedback(quality)
                )
            }
            // add a delay to prevent the scanner from being overwhelmed
            delay(LIVE_FEEDBACK_DELAY_MS)
        }
    }

    @SuppressLint("CheckResult")
    override suspend fun stopLiveFeedback(): Unit = executeSafely {
        if (isLiveFeedbackAvailable()) {
            scannerV2.setSmileLedState(scannerUiHelper.turnedOffState())
            scannerV2.setScannerLedStateDefault()
        } else {
            throw UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK)
        }
    }


    private suspend fun ScannerV2.ensureUn20State(desiredState: Boolean) = executeSafely {
        getUn20Status().let { actualState ->
            when {
                desiredState && !actualState -> turnUn20On()
                !desiredState && actualState -> turnUn20Off()
            }
        }
    }

    override suspend fun turnOffSmileLeds(): Unit = executeSafely {
        // No need to handle exceptions while updating the LED state
        runCatching { scannerV2.setSmileLedState(scannerUiHelper.turnedOffState()) }
    }

    private val triggerListenerToObserverMap =
        mutableMapOf<ScannerTriggerListener, Observer<Unit>>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener] = object : DisposableObserver<Unit>() {
            override fun onComplete() {}
            override fun onNext(t: Unit) {
                triggerListener.onTrigger()
            }

            override fun onError(e: Throwable) {
                throw wrapErrorFromScanner(e)
            }
        }.also { scannerV2.triggerButtonListeners.add(it) }
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToObserverMap[triggerListener]?.let {
            scannerV2.triggerButtonListeners.remove(it)
        }
    }

    override suspend fun turnOnFlashingWhiteSmileLeds(): Unit = executeSafely {
        // No need to handle exceptions while updating the LED state
        runCatching { scannerV2.setSmileLedState(scannerUiHelper.whiteFlashingLedState()) }
    }

    override suspend fun setUiGoodCapture(): Unit = executeSafely {
        // No need to handle exceptions while updating the LED state
        runCatching { scannerV2.setSmileLedState(scannerUiHelper.goodScanLedState()) }
    }

    override suspend fun setUiBadCapture(): Unit = executeSafely {
        // No need to handle exceptions while updating the LED state
        runCatching { scannerV2.setSmileLedState(scannerUiHelper.badScanLedState()) }
    }

    companion object {
        private const val LIVE_FEEDBACK_DELAY_MS = 200L
    }
}
