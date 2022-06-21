package com.simprints.fingerprint.activities.connect.issues.ota

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.extentions.getEstimatedOutage
import com.simprints.core.tools.extentions.isBackendMaintenanceException
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest
import com.simprints.fingerprint.activities.connect.result.FetchOtaResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerFirmwareUpdateEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.HARD_RESET
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.SOFT_RESET
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY
import com.simprints.fingerprint.scanner.domain.ota.OtaStep
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.logging.Simber
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlin.concurrent.schedule

class OtaViewModel(
    private val scannerManager: ScannerManager,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val timeHelper: FingerprintTimeHelper,
    private val fingerprintPreferenceManager: FingerprintPreferencesManager
) : ViewModel() {

    val progress = MutableLiveData(0f)
    val otaComplete = MutableLiveData<LiveDataEvent>()
    val otaRecovery = MutableLiveData<LiveDataEventWithContent<OtaRecoveryFragmentRequest>>()
    val otaFailed = MutableLiveData<LiveDataEventWithContent<FetchOtaResult>>()

    private var currentStep: OtaStep? = null
    private val remainingOtas = mutableListOf<AvailableOta>()

    @SuppressLint("CheckResult")
    fun startOta(availableOtas: List<AvailableOta>, currentRetryAttempt: Int) {
        remainingOtas.addAll(availableOtas)
        Observable.concat(availableOtas.map {
                it.toScannerObservable().doOnComplete { remainingOtas.remove(it) }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = { otaStep ->
                    Simber.d(otaStep.toString())
                    currentStep = otaStep
                    progress.postValue(otaStep.totalProgress.mapToTotalProgress(remainingOtas.size, availableOtas.size))
                },
                onComplete = {
                    progress.postValue(1f)
                    otaComplete.postEvent()
                },
                onError = { handleScannerError(it, currentRetryAttempt) }
            )
    }
    private fun targetVersions(availableOta: AvailableOta): String {
        val scannerVersion = fingerprintPreferenceManager.lastScannerVersion
        val availableFirmwareVersions = fingerprintPreferenceManager.scannerHardwareRevisions
        return when (availableOta) {
            AvailableOta.CYPRESS -> availableFirmwareVersions[scannerVersion]?.cypress ?: ""
            AvailableOta.STM -> availableFirmwareVersions[scannerVersion]?.stm ?: ""
            AvailableOta.UN20 -> availableFirmwareVersions[scannerVersion]?.un20 ?: ""
        }
    }
    private fun AvailableOta.toScannerObservable(): Observable<out OtaStep> = Observable.defer {
        val otaStartedTime = timeHelper.now()
        when (this) {
            AvailableOta.CYPRESS -> scannerManager.scannerObservable {
                performCypressOta(targetVersions(AvailableOta.CYPRESS))
            }
            AvailableOta.STM -> scannerManager.scannerObservable {
                performStmOta(targetVersions(AvailableOta.STM))
            }
            AvailableOta.UN20 -> scannerManager.scannerObservable {
                performUn20Ota(targetVersions(AvailableOta.UN20))
            }
        }.doOnComplete { saveOtaEventInSession(this, otaStartedTime) }
            .doOnError { saveOtaEventInSession(this, otaStartedTime, it) }
    }

    private fun handleScannerError(e: Throwable, currentRetryAttempt: Int) {
        Simber.e(e)
        if (e.isBackendMaintenanceException()) {
            otaFailed.postEvent(FetchOtaResult(isMaintenanceMode = true, estimatedOutage = e.getEstimatedOutage()))
        } else if (currentRetryAttempt >= MAX_RETRY_ATTEMPTS) {
            otaFailed.postEvent(FetchOtaResult(isMaintenanceMode = false))
        } else {
            when (val strategy = currentStep?.recoveryStrategy) {
                HARD_RESET, SOFT_RESET ->
                    otaRecovery.postEvent(OtaRecoveryFragmentRequest(strategy, remainingOtas, currentRetryAttempt))
                SOFT_RESET_AFTER_DELAY ->
                    timeHelper.newTimer().schedule(OtaRecoveryStrategy.DELAY_TIME_MS) {
                        otaRecovery.postEvent(OtaRecoveryFragmentRequest(SOFT_RESET, remainingOtas, currentRetryAttempt))
                    }
                null -> otaFailed.postEvent(FetchOtaResult(isMaintenanceMode = false))
            }
        }
    }

    private fun saveOtaEventInSession(availableOta: AvailableOta, startTime: Long, e: Throwable? = null) {
        val chipName = when (availableOta) {
            AvailableOta.CYPRESS -> "cypress"
            AvailableOta.STM -> "stm"
            AvailableOta.UN20 -> "un20"
        }
        val failureReason = e?.let { "${it::class.java.simpleName} : ${it.message}" }

        sessionEventsManager.addEventInBackground(
            ScannerFirmwareUpdateEvent(
                startTime,
                timeHelper.now(),
                chipName,
                targetVersions(availableOta),
                failureReason
            )
        )
    }

    private fun Float.mapToTotalProgress(nRemainingOtas: Int, nTotalOtas: Int): Float {
        val baseProgress = (nTotalOtas.toFloat() - nRemainingOtas.toFloat()) / nTotalOtas.toFloat()
        return baseProgress + this / nTotalOtas.toFloat()
    }

    companion object {
        const val MAX_RETRY_ATTEMPTS = 1
    }
}
