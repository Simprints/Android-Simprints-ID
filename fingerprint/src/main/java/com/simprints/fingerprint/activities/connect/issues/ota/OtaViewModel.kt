package com.simprints.fingerprint.activities.connect.issues.ota

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.extentions.onError
import com.simprints.core.tools.extentions.onSuccess
import com.simprints.fingerprint.activities.connect.issues.otarecovery.OtaRecoveryFragmentRequest
import com.simprints.fingerprint.activities.connect.result.FetchOtaResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerFirmwareUpdateEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.*
import com.simprints.fingerprint.scanner.domain.ota.OtaStep
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.schedule

class OtaViewModel(
    private val scannerManager: ScannerManager,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val timeHelper: FingerprintTimeHelper,
    private val dispatcherProvider: DispatcherProvider,
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
        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                availableOtas.asFlow()
                    .flatMapConcat {
            it.toScannerObservable().onSuccess {
                                remainingOtas.remove(it)
        }}
                    .onSuccess {
                        progress.postValue(1f)
                        otaComplete.postEvent()
                    }
            .collect { otaStep ->
                Simber.d(otaStep.toString())
                currentStep = otaStep
                progress.postValue(
                    otaStep.totalProgress.mapToTotalProgress(
                        remainingOtas.size,
                        availableOtas.size
                    )
                )
            }
            } catch (ex: Throwable) {
                handleScannerError(ex, currentRetryAttempt)
        }
        }
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

    private suspend fun AvailableOta.toScannerObservable(): Flow<OtaStep> {
        val otaStartedTime = timeHelper.now()
        return when (this) {
            AvailableOta.CYPRESS -> scannerManager.scanner.performCypressOta(AvailableOta.CYPRESS)
            AvailableOta.STM -> scannerManager.scanner.performStmOta(AvailableOta.STM)
            AvailableOta.UN20 -> scannerManager.scanner.performUn20Ota(AvailableOta.UN20)
        }.onSuccess {
            saveOtaEventInSession(this@toScannerObservable, otaStartedTime)
        }.onError {
            saveOtaEventInSession(this@toScannerObservable, otaStartedTime, it)
            throw it
        }
    }

    private fun handleScannerError(e: Throwable, currentRetryAttempt: Int) {
        Simber.e(e)
        if (e is BackendMaintenanceException) {
            otaFailed.postEvent(
                FetchOtaResult(
                    isMaintenanceMode = true,
                    estimatedOutage = e.estimatedOutage
                )
            )
        } else if (currentRetryAttempt >= MAX_RETRY_ATTEMPTS) {
            otaFailed.postEvent(FetchOtaResult(isMaintenanceMode = false))
        } else {
            when (val strategy = currentStep?.recoveryStrategy) {
                HARD_RESET, SOFT_RESET ->
                    otaRecovery.postEvent(
                        OtaRecoveryFragmentRequest(
                            strategy,
                            remainingOtas,
                            currentRetryAttempt
                        )
                    )
                SOFT_RESET_AFTER_DELAY ->
                    timeHelper.newTimer().schedule(OtaRecoveryStrategy.DELAY_TIME_MS) {
                        otaRecovery.postEvent(
                            OtaRecoveryFragmentRequest(
                                SOFT_RESET,
                                remainingOtas,
                                currentRetryAttempt
                            )
                        )
                    }
                null -> otaFailed.postEvent(FetchOtaResult(isMaintenanceMode = false))
            }
        }
    }

    private fun saveOtaEventInSession(
        availableOta: AvailableOta,
        startTime: Long,
        e: Throwable? = null
    ) {
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
