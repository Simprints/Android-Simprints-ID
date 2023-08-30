package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException

/**
 * A common interface for both Vero 1 and Vero 2. Some features are only available on later versions
 * - care must be taken to not invoke functions on inappropriate hardware otherwise this could throw
 * an [UnavailableVero2FeatureException].
 */
interface ScannerWrapper {

    fun isImageTransferSupported(): Boolean

    suspend fun connect()
    suspend fun disconnect()

    /**
     * This method checks for available firmware updates
     *
     * @throws OtaAvailableException
     */
    suspend fun setScannerInfoAndCheckAvailableOta()

    suspend fun sensorWakeUp()
    suspend fun sensorShutDown()

    fun isLiveFeedbackAvailable(): Boolean
    /** @throws UnavailableVero2FeatureException - if UN20 API version is less then 1.1 or if using Vero 1 */
    suspend fun startLiveFeedback()
    /** @throws UnavailableVero2FeatureException - if UN20 API version is less then 1.1 or if using Vero 1 */
    suspend fun stopLiveFeedback()

    suspend fun setUiIdle()

    fun registerTriggerListener(triggerListener: ScannerTriggerListener)
    fun unregisterTriggerListener(triggerListener: ScannerTriggerListener)
    fun versionInformation(): ScannerVersion
    fun batteryInformation(): BatteryInfo

}
