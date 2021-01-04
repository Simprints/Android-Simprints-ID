package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * A common interface for both Vero 1 and Vero 2. Some features are only available on later versions
 * - care must be taken to not invoke functions on inappropriate hardware otherwise this could throw
 * an [UnavailableVero2FeatureException].
 */
interface ScannerWrapper {

    fun connect(): Completable
    fun disconnect(): Completable

    fun setup(): Completable

    fun sensorWakeUp(): Completable
    fun sensorShutDown(): Completable

    fun isLiveFeedbackAvailable(): Boolean
    /** @throws UnavailableVero2FeatureException - if UN20 API version is less then 1.1 or if using Vero 1 */
    fun startLiveFeedback(): Completable
    /** @throws UnavailableVero2FeatureException - if UN20 API version is less then 1.1 or if using Vero 1 */
    fun stopLiveFeedback(): Completable

    fun captureFingerprint(captureFingerprintStrategy: CaptureFingerprintStrategy, timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse>
    /** @throws UnavailableVero2FeatureException - if using Vero 1 */
    fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy): Single<AcquireImageResponse>

    fun setUiIdle(): Completable

    fun registerTriggerListener(triggerListener: ScannerTriggerListener)
    fun unregisterTriggerListener(triggerListener: ScannerTriggerListener)
    fun versionInformation(): ScannerVersion
    fun batteryInformation(): BatteryInfo

    /** @throws UnavailableVero2FeatureException - if using Vero 1 */
    fun performCypressOta(): Observable<CypressOtaStep>
    /** @throws UnavailableVero2FeatureException - if using Vero 1 */
    fun performStmOta(): Observable<StmOtaStep>
    /** @throws UnavailableVero2FeatureException - if using Vero 1 */
    fun performUn20Ota(): Observable<Un20OtaStep>
}
