package com.simprints.fingerprint.activities.connect.issues.ota

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerFirmwareUpdateEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.*
import com.simprints.fingerprint.scanner.domain.ota.OtaStep
import com.simprints.fingerprint.tools.livedata.postEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.concurrent.schedule

class OtaViewModel(
    private val scannerManager: ScannerManager,
    private val firmwareFileManager: FirmwareFileManager,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val crashReportManager: FingerprintCrashReportManager,
    private val timeHelper: FingerprintTimeHelper
) : ViewModel() {

    val progress = MutableLiveData(0f)
    val otaComplete = MutableLiveData<LiveDataEvent>()
    val otaRecovery = MutableLiveData<LiveDataEventWithContent<OtaRecoveryFragmentRequest>>()
    val otaFailed = MutableLiveData<LiveDataEvent>()

    private var currentStep: OtaStep? = null
    private val remainingOtas = mutableListOf<AvailableOta>()

    @SuppressLint("CheckResult")
    fun startOta(availableOtas: List<AvailableOta>, currentRetryAttempt: Int) {
        remainingOtas.addAll(availableOtas)
        Observable.concat(availableOtas.map {
                it.toScannerObservable().doOnComplete { remainingOtas.remove(it) }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onNext = { otaStep ->
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

    private fun AvailableOta.toScannerObservable(): Observable<out OtaStep> = Observable.defer {
        val otaStartedTime = timeHelper.now()
        when (this) {
            AvailableOta.CYPRESS -> scannerManager.scannerObservable { performCypressOta() }
            AvailableOta.STM -> scannerManager.scannerObservable { performStmOta() }
            AvailableOta.UN20 -> scannerManager.scannerObservable { performUn20Ota() }
        }.doOnComplete { saveOtaEventInSession(this, otaStartedTime) }
            .doOnError { saveOtaEventInSession(this, otaStartedTime, it) }
    }

    private fun handleScannerError(e: Throwable, currentRetryAttempt: Int) {
        Timber.e(e)
        crashReportManager.logExceptionOrSafeException(e)
        if (currentRetryAttempt >= MAX_RETRY_ATTEMPTS) {
            otaFailed.postEvent()
        } else {
            when (val strategy = currentStep?.recoveryStrategy) {
                HARD_RESET, SOFT_RESET ->
                    otaRecovery.postEvent(OtaRecoveryFragmentRequest(strategy, remainingOtas, currentRetryAttempt))
                SOFT_RESET_AFTER_DELAY ->
                    timeHelper.newTimer().schedule(OtaRecoveryStrategy.DELAY_TIME_MS) {
                        otaRecovery.postEvent(OtaRecoveryFragmentRequest(SOFT_RESET, remainingOtas, currentRetryAttempt))
                    }
                null -> otaFailed.postEvent()
            }
        }
    }

    private fun saveOtaEventInSession(availableOta: AvailableOta, startTime: Long, e: Throwable? = null) {
        val chipName = when (availableOta) {
            AvailableOta.CYPRESS -> "cypress"
            AvailableOta.STM -> "stm"
            AvailableOta.UN20 -> "un20"
        }

        val targetVersion = when (availableOta) {
            AvailableOta.CYPRESS -> firmwareFileManager.getAvailableScannerFirmwareVersions().cypress.toString()
            AvailableOta.STM -> firmwareFileManager.getAvailableScannerFirmwareVersions().stm.toString()
            AvailableOta.UN20 -> firmwareFileManager.getAvailableScannerFirmwareVersions().un20.toString()
        }

        val failureReason = e?.let { "${it::class.java.simpleName} : ${it.message}" }

        sessionEventsManager.addEventInBackground(ScannerFirmwareUpdateEvent(
            startTime,
            timeHelper.now(),
            chipName,
            targetVersion,
            failureReason
        ))
    }

    private fun Float.mapToTotalProgress(nRemainingOtas: Int, nTotalOtas: Int): Float {
        val baseProgress = (nTotalOtas.toFloat() - nRemainingOtas.toFloat()) / nTotalOtas.toFloat()
        return baseProgress + this / nTotalOtas.toFloat()
    }

    companion object {
        const val MAX_RETRY_ATTEMPTS = 1
    }
}
